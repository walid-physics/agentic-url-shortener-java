package com.triviola.agentic.shortener;

public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String code) { super("Short URL not found: " + code); }
}
