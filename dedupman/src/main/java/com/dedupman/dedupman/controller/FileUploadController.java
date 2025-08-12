package com.dedupman.dedupman.controller;

import com.dedupman.dedupman.service.DuplicateCheckerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*") // Allow requests from any origin
public class FileUploadController {

    @Autowired
    private DuplicateCheckerService duplicateCheckerService;

    /**
     * Detect duplicates by folder path (upload-specific endpoint).
     */
    @PostMapping("/detect-duplicates")
    public ResponseEntity<?> detectDuplicatesByFolder(@RequestParam("folderPath") String folderPath) {
        try {
            Map<String, List<String>> duplicates = duplicateCheckerService.findDuplicatesInFolder(folderPath);
            return ResponseEntity.ok(duplicates);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("❌ Error reading folder: " + e.getMessage());
        }
    }

    /**
     * Delete duplicates in folder (upload-specific endpoint).
     */
    @PostMapping("/delete-duplicates")
    public ResponseEntity<?> deleteDuplicates(@RequestParam("folderPath") String folderPath) {
        try {
            Map<String, List<String>> duplicates = duplicateCheckerService.findDuplicatesInFolder(folderPath);
            List<String> deletedFiles = new ArrayList<>();
            List<String> failedFiles = new ArrayList<>();

            for (List<String> files : duplicates.values()) {
                // Keep the first file, delete the rest
                for (int i = 1; i < files.size(); i++) {
                    File file = new File(files.get(i));

                    System.out.println("Attempting to delete: " + file.getAbsolutePath());

                    try {
                        if (Files.deleteIfExists(file.toPath())) {
                            deletedFiles.add(file.getAbsolutePath());
                            System.out.println("✅ Deleted: " + file.getAbsolutePath());
                        } else {
                            failedFiles.add(file.getAbsolutePath());
                            System.err.println("⚠ Could not delete (file not found): " + file.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        failedFiles.add(file.getAbsolutePath());
                        System.err.println("❌ Could not delete: " + file.getAbsolutePath() + " - " + e.getMessage());
                    }
                }
            }

            return ResponseEntity.ok(Map.of(
                    "deleted", deletedFiles,
                    "failed", failedFiles
            ));

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("❌ Error deleting duplicates: " + e.getMessage());
        }
    }
}
