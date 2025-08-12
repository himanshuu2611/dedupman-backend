package com.dedupman.dedupman.service;

import com.dedupman.dedupman.util.FileTextExtractor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

@Service
public class DuplicateCheckerService {

    /**
     * Find duplicates in a folder by content hash.
     */
    public Map<String, List<String>> findDuplicatesInFolder(String folderPath) throws IOException {
        Map<String, List<String>> duplicates = new HashMap<>();
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("Invalid folder path: " + folderPath);
        }

        scanFolderRecursive(folder, duplicates);

        // Keep only hashes with more than 1 file (duplicates)
        duplicates.entrySet().removeIf(entry -> entry.getValue().size() < 2);

        return duplicates;
    }

    /**
     * Recursively scan the folder for duplicates.
     */
    private void scanFolderRecursive(File folder, Map<String, List<String>> duplicates) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile()) {
                try {
                    String text = FileTextExtractor.extractText(file);
                    if (text.isEmpty()) continue; // skip unsupported/empty files

                    String hash = hashText(text);
                    duplicates.computeIfAbsent(hash, k -> new ArrayList<>())
                            .add(file.getAbsolutePath());

                } catch (Exception e) {
                    System.err.println("Skipping file: " + file.getAbsolutePath() + " (" + e.getMessage() + ")");
                }
            } else if (file.isDirectory()) {
                scanFolderRecursive(file, duplicates);
            }
        }
    }

    /**
     * Generate SHA-256 hash of given text.
     */
    private String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(text.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing text", e);
        }
    }
}
