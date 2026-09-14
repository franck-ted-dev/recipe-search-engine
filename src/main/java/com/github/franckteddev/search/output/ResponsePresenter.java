package com.github.franckteddev.search.output;

import java.util.List;

public interface ResponsePresenter {
    void output(List<String> results);
    void output(String message);
}
