package com.assignment.loganalyser.app.model;

import java.util.Arrays;

/**
 * 
 * @author tukaram
 *
 */

public enum State {
    STARTED("STARTED"),
    FINISHED("FINISHED");

    private final String value;

    State(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static State fromValue(String text) {
        return Arrays.stream(values())
                .filter(v -> v.getValue().equals(text))
                .findFirst()
                .orElse(null);
    }
}
