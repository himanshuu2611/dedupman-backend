package com.dedupman.dedupman.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // ===== Existing endpoints =====
    // Keep your existing methods here (e.g., detect by folder path, delete duplicates)
    // Example:
    // @PostMapping("/detect-duplicates")
    // public ResponseEntity<Map<String, List<String>>> detectDuplicatesByFolder(...) { ... }

    // ===== New endpoint for file uploads =====
    @PostMapping("/detect-duplicates-upload")
    public ResponseEntity<Map<String, List<String>>> detectDuplicatesUpload(
            @RequestParam("files") List<MultipartFile> files) {

        Map<String, List<String>> duplicates = new HashMap<>();

        try {
            Map<String, List<String>> fileHashes = new HashMap<>();

            for (MultipartFile file : files) {
                byte[] content = file.getBytes();
                String hash = DigestUtils.sha256Hex(content);

                fileHashes.computeIfAbsent(hash, k -> new ArrayList<>()).add(file.getOriginalFilename());
            }

            // Keep only duplicate groups
            for (Map.Entry<String, List<String>> entry : fileHashes.entrySet()) {
                if (entry.getValue().size() > 1) {
                    duplicates.put(entry.getKey(), entry.getValue());
                }
            }

            return ResponseEntity.ok(duplicates);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
