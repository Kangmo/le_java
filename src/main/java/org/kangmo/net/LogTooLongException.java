package org.kangmo.net;

/**
 * Thrown when a log + timestamps etc. is longer than {@link AsyncLogger#LOG_LENGTH_LIMIT} chars.
 */
public class LogTooLongException extends RuntimeException {
}
