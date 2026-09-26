package com.momenta.model;

/** Phase 22 — A deterministic, derived productivity achievement. */
public class Achievement {
    private final String title;
    private final String description;
    private final boolean unlocked;

    public Achievement(String title, String description, boolean unlocked) {
        this.title = title;
        this.description = description;
        this.unlocked = unlocked;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }

    @Override
    public String toString() {
        return (unlocked ? "✓ " : "○ ") + title;
    }
}
