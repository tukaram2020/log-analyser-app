package com.assignment.loganalyser.app.model;

import java.util.Arrays;

/**
 * 
 * @author tukaram
 *
 */

public enum EventType {
    APPLICATION_LOG("APPLICATION_LOG");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventType fromValue(String text) {
        return Arrays.stream(values())
                .filter(v -> v.getValue().equals(text))
                .findFirst()
                .orElse(null);
    }
}
