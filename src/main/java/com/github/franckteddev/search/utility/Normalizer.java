package com.github.franckteddev.search.utility;

public final class Normalizer {
    private Normalizer(){
    }

    public static String normalize(String text) {
        return text.trim().toLowerCase();
    }
}
