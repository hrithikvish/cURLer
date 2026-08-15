package com.hrithikvish.curler.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Wireframe radii mapped to the closest M3 Shapes() slots.
val CurlerShapes = Shapes(
    small = RoundedCornerShape(14.dp),  // --r-sm
    medium = RoundedCornerShape(20.dp), // --r-md
    large = RoundedCornerShape(32.dp),  // --r-lg
)

// M3's global Shapes() has no pill slot — applied directly via each pill
// component's `shape =` param (buttons, chips, segmented-control segments).
val PillShape = RoundedCornerShape(percent = 50)
