package com.kite.libai.hello;

import java.time.Instant;
import java.util.Objects;

/**
 * Response payload returned by the hello endpoints.
 *
 * <p>Plain JavaBean rather than a record, since records require Java 16+.
 */
public class HelloResponse {

    private String message;

    private Instant timestamp;

    /** Required by Jackson for deserialization. */
    public HelloResponse() {
    }

    public HelloResponse(String message, Instant timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HelloResponse)) {
            return false;
        }
        HelloResponse other = (HelloResponse) obj;
        return Objects.equals(message, other.message) && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, timestamp);
    }

    @Override
    public String toString() {
        return "HelloResponse{message='" + message + "', timestamp=" + timestamp + '}';
    }
}
