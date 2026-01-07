package giftandgo.controller;

import giftandgo.config.FeatureFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import giftandgo.service.FileProcessingService;

import java.io.IOException;
import java.util.Objects;

/**
 * REST controller for handling file upload and processing operations.
 * Accepts txt entry files via HTTP POST and returns processed JSON outcome files.
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileProcessingService fileProcessingService;

    @Autowired
    private FeatureFlags featureFlags;

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    /**
     * Endpoint to upload and process an entry file.
     * Accepts a file upload, validates it (if validation is enabled),
     * processes the content, and returns a JSON outcome file.
     *
     * @param file the uploaded file containing entry data
     * @return ResponseEntity containing the processed JSON data as a downloadable attachment,
     *         or an error status if validation fails
     */
    @PostMapping("/upload")
    public ResponseEntity<byte[]> processEntryFile(@RequestParam("file") MultipartFile file) throws IOException {
        // HTTP-level validation when the feature flag is enabled
        // Checks basic file requirements are met before processing
        // For production, keeping file empty/null checks should be considered even when the flag is enabled
        if (!featureFlags.isSkipValidation()) {
            // Check if the uploaded file is empty
            if (file.isEmpty()) {
                logger.warn("Rejected empty file upload");
                return ResponseEntity.badRequest().build();
            }

            // Validate that the file has a .txt extension
            if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".txt")) {
                logger.warn("Rejected file with unsupported type: {}", file.getOriginalFilename());
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }
        } else {
            logger.info("Validation skipped for file");
        }

        // Process file and return JSON
        byte[] jsonBytes = fileProcessingService.processEntryFile(file);

        // Configure HTTP headers for the response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Set response as a downloadable json attachment
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename("OutcomeFile.json")
                        .build()
        );

        logger.info("Returning outcome JSON file.");
        return new ResponseEntity<>(jsonBytes, headers, HttpStatus.OK);
    }

}
