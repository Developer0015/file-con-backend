package com.converter.fileconverter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"*"})
public class FileController {

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convertFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format
    ) {

        try {

            String originalName = file.getOriginalFilename();

            if (originalName == null) {
                return ResponseEntity.badRequest().build();
            }

            /* ---------- create temp file safely ---------- */

            File tempFile = File.createTempFile("upload-", "-" + originalName);
            file.transferTo(tempFile);

            /* ---------- libreoffice command ---------- */

            ProcessBuilder pb = new ProcessBuilder(
                    "soffice",
                    "--headless",
                    "--convert-to",
                    format.toLowerCase(),
                    tempFile.getAbsolutePath(),
                    "--outdir",
                    tempFile.getParent()
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            /* ---------- read libreoffice logs ---------- */

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("LibreOffice exit code: " + exitCode);

            /* ---------- detect converted file ---------- */

            File dir = new File(tempFile.getParent());

            File[] convertedFiles = dir.listFiles((d, name) ->
                    name.endsWith("." + format.toLowerCase())
            );

            if (convertedFiles == null || convertedFiles.length == 0) {
                System.out.println("Conversion failed: output file not created");
                return ResponseEntity.status(500).build();
            }

            File converted = convertedFiles[0];

            /* ---------- read converted file ---------- */

            byte[] output;

            try (FileInputStream fis = new FileInputStream(converted)) {
                output = fis.readAllBytes();
            }

            /* ---------- cleanup ---------- */

            tempFile.delete();
            converted.delete();

            /* ---------- return converted file ---------- */

            return ResponseEntity.ok()
                    .header(
                            "Content-Disposition",
                            "attachment; filename=converted." + format.toLowerCase()
                    )
                    .body(output);

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(500).build();

        }
    }
}