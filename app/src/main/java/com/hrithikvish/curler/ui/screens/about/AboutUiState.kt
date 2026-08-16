package com.hrithikvish.curler.ui.screens.about

import android.graphics.Bitmap
import com.hrithikvish.curler.BuildConfig
import com.hrithikvish.curler.data.model.ChangelogEntry

data class AboutUiState(
    val versionName: String = BuildConfig.VERSION_NAME,
    val versionCode: Int = BuildConfig.VERSION_CODE,
    val devImageUrl: String = "",
    val devImageBitmap: Bitmap? = null,
    val changelogEntries: List<ChangelogEntry> = emptyList(),
)
