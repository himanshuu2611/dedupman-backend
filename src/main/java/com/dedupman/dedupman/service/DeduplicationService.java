package com.dedupman.dedupman.service;

import com.dedupman.dedupman.model.FileResponse;
import com.dedupman.dedupman.util.HashUtil;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class DeduplicationService {

    private final Tika tika = new Tika();
    private final Set<String> fileHashes = new HashSet<>();

    /**
     * Process a single uploaded file and check for duplication.
     */
    public String processFile(MultipartFile file) {
        try {
            String content = tika.parseToString(file.getInputStream());
            String hash = generateHash(content);

            if (fileHashes.contains(hash)) {
                return "Duplicate file detected.";
            }

            fileHashes.add(hash);
            return "File uploaded successfully (Unique).";
        } catch (IOException | TikaException e) {
            return "Failed to process file: " + e.getMessage();
        }
    }

    /**
     * Scan a folder for duplicate files.
     */
    public List<FileResponse> processFolder(String folderPath) {
        List<FileResponse> results = new ArrayList<>();
        fileHashes.clear(); // reset for fresh scan

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            throw new RuntimeException("Invalid folder path: " + folderPath);
        }

        scanFolderRecursive(folder, results);
        return results;
    }

    /**
     * Recursively scan folder for duplicates.
     */
    private void scanFolderRecursive(File folder, List<FileResponse> results) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile()) {
                processFileForScan(file, results);
            } else if (file.isDirectory()) {
                scanFolderRecursive(file, results);
            }
        }
    }

    /**
     * Process a single file during folder scan.
     */
    private void processFileForScan(File file, List<FileResponse> results) {
        try {
            String content = tika.parseToString(file);
            String hash = HashUtil.sha256(content);

            if (fileHashes.contains(hash)) {
                results.add(new FileResponse(file.getAbsolutePath(), "Duplicate"));
            } else {
                fileHashes.add(hash);
                results.add(new FileResponse(file.getAbsolutePath(), "Unique"));
            }
        } catch (IOException | TikaException e) {
            results.add(new FileResponse(file.getAbsolutePath(), "Error: " + e.getMessage()));
        }
    }

    /**
     * Generate SHA-256 hash for given text.
     */
    private String generateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not available", e);
        }
    }
}
