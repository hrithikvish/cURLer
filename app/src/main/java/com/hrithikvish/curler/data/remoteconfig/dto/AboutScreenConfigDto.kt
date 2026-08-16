package com.hrithikvish.curler.data.remoteconfig.dto

import kotlinx.serialization.Serializable

// Mirrors the "about screen" Remote Config JSON exactly (field names/casing
// match the console value) so decoding needs no @SerialName mapping. Keep
// this DTO wire-shaped; map it to app-domain types in AboutConfigRepository.
@Serializable
data class AboutScreenConfigDto(
    val devImgUrl: String = "",
    val changeLogs: List<ChangelogEntryDto> = emptyList(),
)

@Serializable
data class ChangelogEntryDto(
    val v: String = "",
    val date: String = "",
    val logs: List<String> = emptyList(),
)
