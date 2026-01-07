package giftandgo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import giftandgo.config.FeatureFlags;
import giftandgo.model.Entry;
import giftandgo.model.OutcomeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Service class for processing txt entry files and converting them to JSON outcome files.
 * Handles file parsing, data validation and JSON transformation.
 */
@Service
public class FileProcessingService {

    @Autowired
    private FeatureFlags featureFlags;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingService.class);

    /**
     * Method to process an uploaded txt entry file.
     * Reads the file line by line, parses entries, and converts to JSON format.
     *
     * @param file the file containing pipe-delimited entry data
     * @return byte array containing the JSON representation of processed entries
     * @throws UncheckedIOException if file reading fails
     */
    public byte[] processEntryFile(MultipartFile file) throws IOException {
        logger.info("Processing file: {}", file.getOriginalFilename());
        logger.debug("Skip validation flag is set to {}", featureFlags.isSkipValidation());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read all non-blank lines from the file and parse them into Entry objects
            List<Entry> entries = reader.lines()
                                        .filter(line -> !line.isBlank())
                                        .map(this::parseLine)
                                        .toList();

            logger.info("Successfully parsed {} entries from file", entries.size());

            // Convert the list of entries to JSON format
            byte[] result = processToJson(entries);

            logger.info("Successfully processed file.");
            return result;
        }
    }

    /**
     * Parses a single line of pipe-delimited data into an Entry object.
     * Validates field count and data types if validation is enabled.
     *
     * @param line the pipe-delimited string to parse
     * @return Entry object containing the parsed data
     * @throws IllegalArgumentException if validation is enabled and the line format is invalid
     */
    private Entry parseLine(String line) {
        logger.debug("Parsing line: {}", line);

        // Split the line by pipe delimiter
        String[] fields = line.split("\\|");

        // Validate fields only if skip validation feature flag is false
        if (!featureFlags.isSkipValidation()){
            // Validate line has values for each field
            if (fields.length != 7) {
                logger.warn("Invalid field count. Expected 7, found {}: {}", fields.length, line);
                throw new IllegalArgumentException("Invalid line: " + line);
            }

            // Validate numeric fields
            try {
                Double.parseDouble(fields[5]);
                Double.parseDouble(fields[6]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format in fields: avgSpeed={}, topSpeed={}", fields[5], fields[6]);
                throw new IllegalArgumentException("Invalid number format in line: " + line, e);
            }
        } else {
            logger.debug("Validation skipped for line");
        }

        // Safe parsing with defaults when validation is skipped
        String uuid = fields.length > 0 ? fields[0] : "";
        String id = fields.length > 1 ? fields[1] : "";
        String name = fields.length > 2 ? fields[2] : "";
        String likes = fields.length > 3 ? fields[3] : "";
        String transport = fields.length > 4 ? fields[4] : "";
        double avgSpeed = fields.length > 5 ? parseDoubleOrDefault(fields[5]) : 0.0;
        double topSpeed = fields.length > 6 ? parseDoubleOrDefault(fields[6]) : 0.0;

        return new Entry(uuid, id, name, likes, transport, avgSpeed, topSpeed);
    }

    /**
     * Safely parses a string to a double, returning 0.0 if parsing fails.
     * Used when validation is skipped to handle potentially invalid numeric values.
     *
     * @param value the string to parse
     * @return the parsed double value, or 0.0 if parsing fails
     */
    private double parseDoubleOrDefault(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.debug("Failed to parse '{}' as double, using default value 0.0", value);
            return 0.0;
        }
    }

    /**
     * Transforms a list of Entry objects into JSON format.
     * Uses only the required fields (name, transport, topSpeed) for the outcome file.
     *
     * @param entries list of parsed Entry objects
     * @return byte array containing the JSON representation
     * @throws JsonProcessingException if JSON serialisation fails
     */
    private byte[] processToJson(List<Entry> entries) throws JsonProcessingException {
        logger.debug("Converting {} entries to JSON format", entries.size());

        // Map each Entry to an OutcomeEntry containing only the required fields
        List<OutcomeEntry> outcomeEntries = entries.stream().map(entry -> new OutcomeEntry(
                        entry.name(),
                        entry.transport(),
                        entry.topSpeed()
                ))
                .toList();

        // Serialise the list to JSON byte array
        return objectMapper.writeValueAsBytes(outcomeEntries);
    }

}
