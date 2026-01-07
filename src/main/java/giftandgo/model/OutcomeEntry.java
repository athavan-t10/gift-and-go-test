package giftandgo.model;

/**
 * Represents the outcome data structure for the processed file.
 * Only contains the required fields for the output JSON.
 */
public record OutcomeEntry(String name, String transport, double topSpeed){}

