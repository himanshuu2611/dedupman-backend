package com.dedupman.dedupman.service;

import com.dedupman.dedupman.util.FileHashUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.nio.file.Files;

@Service
public class DuplicateFileRemover {

    // ✅ Detect duplicates (without deleting)
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

    // ✅ Delete duplicates (keep first file, delete rest)
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
}
