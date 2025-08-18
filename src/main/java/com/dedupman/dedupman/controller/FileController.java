package com.dedupman.dedupman.controller;

import com.dedupman.dedupman.service.DuplicateFileRemover; // <-- THIS WAS MISSING
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*") // Allow all origins
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private DuplicateFileRemover duplicateFileRemover;

    /**
     * Detect duplicates from uploaded files
     */
    @PostMapping("/detect-duplicates")
    public ResponseEntity<?> detectDuplicates(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("❌ No files uploaded");
            }

            Map<String, List<String>> duplicates = duplicateFileRemover.findDuplicateFilesFromUploads(files);
            return ResponseEntity.ok(duplicates);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Error detecting duplicates: " + e.getMessage());
        }
    }

    /**
     * Delete duplicate uploaded files
     */
    @PostMapping("/delete-duplicates")
    public ResponseEntity<?> deleteDuplicates(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("❌ No files uploaded");
            }

            List<String> deletedFiles = duplicateFileRemover.deleteDuplicatesFromUploads(files);
            return ResponseEntity.ok(deletedFiles);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Error deleting duplicates: " + e.getMessage());
        }
    }
}
