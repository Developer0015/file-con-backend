package com.converter.fileconverter.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileController {

    @PostMapping("/convert")
    public ResponseEntity<?> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format
    ) {

        try {

            String originalName = file.getOriginalFilename();

            if (originalName == null || !originalName.contains(".")) {
                return ResponseEntity.badRequest().body("Invalid file");
            }

            String baseName = originalName.substring(0, originalName.lastIndexOf("."));

            /* ---------- create temp file ---------- */

            File tempFile = File.createTempFile("upload-", "-" + originalName);
            file.transferTo(tempFile);

            /* ---------- run LibreOffice ---------- */

            ProcessBuilder pb = new ProcessBuilder(
                    "libreoffice",   // 🔥 FIXED (important)
                    "--headless",
                    "--convert-to",
                    format.toLowerCase(),
                    "--outdir",
                    tempFile.getParent(),
                    tempFile.getAbsolutePath()
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);

            /* ---------- exact output file ---------- */

            File converted = new File(
                    tempFile.getParent(),
                    baseName + "." + format.toLowerCase()
            );

            if (!converted.exists()) {
                return ResponseEntity.status(500).body("Conversion failed");
            }

            /* ---------- read file ---------- */

            byte[] output;

            try (FileInputStream fis = new FileInputStream(converted)) {
                output = fis.readAllBytes();
            }

            /* ---------- cleanup ---------- */

            tempFile.delete();
            converted.delete();

            /* ---------- return ---------- */

            return ResponseEntity.ok()
                    .header(
                            "Content-Disposition",
                            "attachment; filename=converted." + format.toLowerCase()
                    )
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(output);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Server error");
        }
    }
}