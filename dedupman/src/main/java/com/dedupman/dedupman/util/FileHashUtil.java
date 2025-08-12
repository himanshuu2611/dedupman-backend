package com.dedupman.dedupman.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileHashUtil {

    public static String getFileHash(File file) throws Exception {
        // Use SHA-256 for better collision resistance (can change to MD5 if needed)
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Use try-with-resources to auto-close stream
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192]; // larger buffer for better performance
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        // Convert hash bytes to hex string
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
