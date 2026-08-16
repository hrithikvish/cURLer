package com.hrithikvish.curler.data.model

// Domain model for one About-screen changelog entry. Sourced from Firebase
// Remote Config (see AboutConfigRepository) — plain/framework-free so it
// stays JVM-testable like the rest of data/model.
data class ChangelogEntry(
    val version: String,
    val dateLabel: String,
    val notes: List<String>,
    val isLatest: Boolean = false,
)
