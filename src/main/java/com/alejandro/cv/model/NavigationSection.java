package com.alejandro.cv.model;

import java.util.List;

public record NavigationSection(List<Item> items) {
    public record Item(String targetId, String label, String iconClass) {
    }
}
