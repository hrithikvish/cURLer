package com.hrithikvish.curler.data.remoteconfig

import com.hrithikvish.curler.data.model.ChangelogEntry
import com.hrithikvish.curler.data.remoteconfig.dto.AboutScreenConfigDto
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class AboutRemoteConfig(
    val devImageUrl: String,
    val changelogEntries: List<ChangelogEntry>,
)

private val json = Json { ignoreUnknownKeys = true }
private val remoteDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
private val displayDateFormat = DateTimeFormatter.ofPattern("MMM d, yyyy")

// Maps the raw "about screen" Remote Config JSON (AboutScreenConfigDto) to
// app-domain types. AboutViewModel depends on this, not on
// RemoteConfigRepository/FirebaseRemoteConfig directly, so a future second
// Remote Config-backed feature adds its own sibling repository instead of
// growing this one.
@Singleton
class AboutConfigRepository @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository,
) {

    suspend fun refresh() {
        remoteConfigRepository.refresh()
    }

    // Reads whatever is currently activated (last successful fetch, or the
    // in-app default) — safe to call before refresh() ever completes.
    fun getAboutConfig(): AboutRemoteConfig? {
        val raw = remoteConfigRepository.getString(RemoteConfigKeys.ABOUT_SCREEN)
        val dto = runCatching { json.decodeFromString(AboutScreenConfigDto.serializer(), raw) }
            .getOrNull() ?: return null

        return AboutRemoteConfig(
            devImageUrl = dto.devImgUrl,
            changelogEntries = dto.changeLogs.mapIndexed { index, entry ->
                ChangelogEntry(
                    version = "v${entry.v}",
                    dateLabel = formatDate(entry.date),
                    notes = entry.logs,
                    isLatest = index == 0,
                )
            },
        )
    }

    private fun formatDate(raw: String): String =
        runCatching { LocalDate.parse(raw, remoteDateFormat).format(displayDateFormat) }.getOrDefault(raw)
}
