package giftandgo.model;

/**
 * Represents an entry from the txt entry file.
 * Contains all fields from the pipe-delimited format.
 */
public record Entry(String uuid, String id, String name, String likes, String transport, double avgSpeed, double topSpeed) {}

