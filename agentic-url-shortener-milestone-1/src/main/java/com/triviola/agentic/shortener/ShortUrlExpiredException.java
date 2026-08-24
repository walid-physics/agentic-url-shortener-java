package com.triviola.agentic.shortener;

public class ShortUrlExpiredException extends RuntimeException {
    public ShortUrlExpiredException(String code) { super("Short URL has expired: " + code); }
}
