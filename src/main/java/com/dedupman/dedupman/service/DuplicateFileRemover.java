package com.dedupman.dedupman.service;

import com.dedupman.dedupman.util.FileHashUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

@Service
public class DuplicateFileRemover {

    // ================= Folder Path Methods =================
    public Map<String, List<String>> findDuplicateFiles(String folderPath) {
        Map<String, List<String>> duplicates = new HashMap<>();
        Map<String, String> seenFiles = new HashMap<>();
        File folder = new File(folderPath);

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        scanForDuplicates(folder, seenFiles, duplicates);
        return duplicates;
    }

    private void scanForDuplicates(File folder, Map<String, String> seenFiles, Map<String, List<String>> duplicates) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanForDuplicates(file, seenFiles, duplicates);
            } else {
                try {
                    String hash = FileHashUtil.getFileHash(file);

                    if (seenFiles.containsKey(hash)) {
                        duplicates.computeIfAbsent(hash, k -> new ArrayList<>()).add(file.getAbsolutePath());

                        if (!duplicates.get(hash).contains(seenFiles.get(hash))) {
                            duplicates.get(hash).add(seenFiles.get(hash));
                        }
                    } else {
                        seenFiles.put(hash, file.getAbsolutePath());
                    }

                } catch (Exception e) {
                    System.err.println("⚠ Error processing file: " + file.getAbsolutePath() + " - " + e.getMessage());
                }
            }
        }
    }

    public List<String> deleteDuplicatesInFolder(String folderPath) {
        List<String> deletedFiles = new ArrayList<>();
        Map<String, File> seenFiles = new HashMap<>();
        File folder = new File(folderPath);

        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        deleteRecursively(folder, seenFiles, deletedFiles);
        return deletedFiles;
    }

    private void deleteRecursively(File folder, Map<String, File> seenFiles, List<String> deletedFiles) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                deleteRecursively(file, seenFiles, deletedFiles);
            } else {
                try {
                    String hash = FileHashUtil.getFileHash(file);

                    if (seenFiles.containsKey(hash)) {
                        try {
                            Files.delete(file.toPath());
                            deletedFiles.add(file.getCanonicalPath());
                            System.out.println("✅ Deleted: " + file.getCanonicalPath());
                        } catch (Exception e) {
                            System.err.println("❌ Could not delete: " + file.getCanonicalPath() + " - " + e.getMessage());
                        }
                    } else {
                        seenFiles.put(hash, file);
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Error processing file: " + file.getAbsolutePath() + " - " + e.getMessage());
                }
            }
        }
    }

    // ================= Uploaded File Methods =================
    public Map<String, List<String>> findDuplicateFilesFromUploads(MultipartFile[] files) throws Exception {
        Map<String, List<String>> hashMap = new HashMap<>();

        for (MultipartFile file : files) {
            byte[] fileBytes;
            try (InputStream is = file.getInputStream()) {
                fileBytes = is.readAllBytes();
            }

            String hash = getMD5Hash(fileBytes);
            hashMap.computeIfAbsent(hash, k -> new ArrayList<>()).add(file.getOriginalFilename());
        }

        // Only keep duplicates
        Map<String, List<String>> duplicates = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : hashMap.entrySet()) {
            if (entry.getValue().size() > 1) duplicates.put(entry.getKey(), entry.getValue());
        }

        return duplicates;
    }

    public List<String> deleteDuplicatesFromUploads(MultipartFile[] files) throws Exception {
        Map<String, MultipartFile> seenFiles = new HashMap<>();
        List<String> deletedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            byte[] fileBytes;
            try (InputStream is = file.getInputStream()) {
                fileBytes = is.readAllBytes();
            }

            String hash = getMD5Hash(fileBytes);

            if (seenFiles.containsKey(hash)) {
                deletedFiles.add(file.getOriginalFilename()); // mark as duplicate
            } else {
                seenFiles.put(hash, file);
            }
        }

        return deletedFiles;
    }

    private String getMD5Hash(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
