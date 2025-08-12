package com.dedupman.dedupman.controller;

import com.dedupman.dedupman.service.DuplicateFileRemover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*") // Allow all origins (use specific origins in production)
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private DuplicateFileRemover duplicateFileRemover;

    /**
     * Detect duplicates without deleting
     */
    @PostMapping("/detect-duplicates")
    public ResponseEntity<?> detectDuplicates(@RequestParam String folderPath) {
        try {
            Map<String, List<String>> duplicates = duplicateFileRemover.findDuplicateFiles(folderPath);
            return ResponseEntity.ok(duplicates);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ Invalid folder path: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error detecting duplicates: " + e.getMessage());
        }
    }

    /**
     * Delete duplicates
     */
    @PostMapping("/delete-duplicates")
    public ResponseEntity<?> deleteDuplicates(@RequestParam String folderPath) {
        try {
            List<String> deletedFiles = duplicateFileRemover.deleteDuplicatesInFolder(folderPath);
            return ResponseEntity.ok(deletedFiles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("❌ Invalid folder path: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error deleting duplicates: " + e.getMessage());
        }
    }
}
