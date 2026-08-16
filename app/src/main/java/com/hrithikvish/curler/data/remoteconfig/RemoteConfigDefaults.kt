package com.hrithikvish.curler.data.remoteconfig

// In-app fallback values, used until the first successful fetch activates
// (or if a fetch ever fails/is throttled). Kept in the same raw JSON shape as
// the Firebase console value so RemoteConfigRepository.getString() behaves
// identically regardless of source. Deliberately empty/blank rather than
// fabricated sample copy — a blank devImgUrl means the UI falls back to the
// initial-letter avatar, and an empty changeLogs means the changelog section
// is hidden, until real values are fetched.
object RemoteConfigDefaults {
    val values: Map<String, Any> = mapOf(
        RemoteConfigKeys.ABOUT_SCREEN to """
            {
              "devImgUrl": "",
              "changeLogs": []
            }
        """.trimIndent(),
    )
}
