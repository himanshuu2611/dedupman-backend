package com.dedupman.dedupman.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;

public class FileTextExtractor {

    public static String extractText(File file) throws IOException {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".pdf")) {
            return extractFromPDF(file);
        } else if (name.endsWith(".docx")) {
            return extractFromDOCX(file);
        } else if (name.endsWith(".doc")) {
            return extractFromDOC(file); // Added old Word support
        } else if (name.endsWith(".txt")) {
            return extractFromTXT(file);
        } else {
            return ""; // skip unsupported files
        }
    }

    private static String extractFromPDF(File file) throws IOException {
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true); // Preserve reading order
            return normalizeText(stripper.getText(doc));
        }
    }

    private static String extractFromDOCX(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append(" ");
            }
            return normalizeText(sb.toString());
        }
    }

    private static String extractFromDOC(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(fis)) {

            WordExtractor extractor = new WordExtractor(doc);
            return normalizeText(extractor.getText());
        }
    }

    private static String extractFromTXT(File file) throws IOException {
        String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return normalizeText(text);
    }

    /**
     * Normalizes text to make cross-format duplicate detection reliable.
     */
    private static String normalizeText(String text) {
        if (text == null) return "";

        // Lowercase
        text = text.toLowerCase();

        // Remove accents
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{M}", "");

        // Remove punctuation and symbols (keep only letters/numbers)
        text = text.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ");

        // Remove invisible characters
        text = text.replaceAll("\\u00A0", " ")
                .replaceAll("\\u200B", "")
                .replaceAll("\\u200C", "")
                .replaceAll("\\u200D", "");

        // Collapse multiple spaces
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }
}
