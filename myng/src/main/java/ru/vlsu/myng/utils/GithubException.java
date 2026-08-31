package ru.vlsu.myng.utils;

public class GithubException extends RuntimeException {

    private final String field;

    public GithubException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
