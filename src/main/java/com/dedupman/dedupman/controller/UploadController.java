package com.dedupman.dedupman.controller;

import com.dedupman.dedupman.model.FileResponse;
import com.dedupman.dedupman.service.DeduplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/apps")
@CrossOrigin(origins = "*") // Allow all origins (change in production)
public class UploadController {

    @Autowired
    private DeduplicationService deduplicationService;

    /**
     * Upload a single file for duplicate processing.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String result = deduplicationService.processFile(file);
        return ResponseEntity.ok(result);
    }

    /**
     * Scan a folder for duplicate files.
     */
    @PostMapping("/scan")
    public ResponseEntity<?> scanFolder(@RequestParam("path") String folderPath) {
        List<FileResponse> result = deduplicationService.processFolder(folderPath);
        return ResponseEntity.ok(result);
    }
}
