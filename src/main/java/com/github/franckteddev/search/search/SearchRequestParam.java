package com.github.franckteddev.search.search;

import java.util.List;

public record SearchRequestParam(List<String>all, List<String>any, List<String>none) {
    public SearchRequestParam {
        all = List.copyOf(all);
        any = List.copyOf(any);
        none = List.copyOf(none);
    }
}
