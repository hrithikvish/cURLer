package com.hrithikvish.curler.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

@SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
@Composable
fun WebViewBottomSheet(
    title: String,
    url: String,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(
        title = title,
        onDismiss = onDismiss,
        scrollable = false,
        contentPadding = PaddingValues(0.dp),
    ) {
        val isDarkTheme = isSystemInDarkTheme()
        val sheetBackground = MaterialTheme.colorScheme.surfaceContainerLow
        var isLoading by remember { mutableStateOf(true) }
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    @SuppressLint("MissingOnRenderProcessGone")
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, finishedUrl: String?) {
                                isLoading = false
                            }
                        }
                        settings.javaScriptEnabled = true

                        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, false)
                        }

                        @Suppress("DEPRECATION")
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
                        }

                        // Matches the sheet's own container color so there's no seam or flash
                        // before the page's CSS paints.
                        val backgroundArgb = sheetBackground.toArgb()
                        setBackgroundColor(backgroundArgb)
                        val backgroundHex = String.format("%06X", 0xFFFFFF and backgroundArgb)

                        val themedUrl = url.toUri().buildUpon()
                            .appendQueryParameter("theme", if (isDarkTheme) "dark" else "light")
                            .appendQueryParameter("bg", backgroundHex)
                            .build()

                        setOnTouchListener { view, _ ->
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                            false
                        }

                        loadUrl(themedUrl.toString())
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}
