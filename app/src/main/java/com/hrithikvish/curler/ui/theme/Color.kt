package com.hrithikvish.curler.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------
// RAW PALETTE — a single 13-stop grayscale ramp, nothing else.
//    Everything below is built from these values only.
// ---------------------------------------------------------------------

val Black100 = Color(0xFF000000)
val Black95 = Color(0xFF0D0D0D)
val Black90 = Color(0xFF1A1A1A)
val Black80 = Color(0xFF2E2E2E)
val Black70 = Color(0xFF454545)
val Black60 = Color(0xFF5C5C5C)
val Black50 = Color(0xFF737373)
val Black40 = Color(0xFF8A8A8A)
val Black30 = Color(0xFFA6A6A6)
val Black20 = Color(0xFFC2C2C2)
val Black10 = Color(0xFFDCDCDC)
val Black05 = Color(0xFFEDEDED)
val Black02 = Color(0xFFF6F6F6)
val White = Color(0xFFFFFFFF)

// Reserved signal colors — the ONLY non-grayscale values in the app.
// Kept minimal and used only where color is load-bearing for meaning
// (network failure, destructive delete), not for decoration or branding.
val SignalError = Color(0xFFD32F2F) // failed request / 4xx-5xx / delete
val SignalSuccess = Color(0xFF2E7D32) // 2xx response, optional — see note below
