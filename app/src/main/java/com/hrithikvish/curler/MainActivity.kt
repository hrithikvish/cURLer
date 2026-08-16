package com.hrithikvish.curler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hrithikvish.curler.data.update.UpdateManager
import com.hrithikvish.curler.ui.navigation.CurlerNavHost
import com.hrithikvish.curler.ui.theme.CurlerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var updateManager: UpdateManager

    // Any result (accepted, dismissed, or failed) is handled the same way:
    // re-derive true state from a fresh live query rather than hand-mapping
    // RESULT_OK/RESULT_CANCELED/RESULT_IN_APP_UPDATE_FAILED — cheaper and
    // more trustworthy than reconstructing state locally.
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        lifecycleScope.launch { updateManager.checkForUpdate() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateManager.attachLauncher(updateLauncher)
        // Covers first resume and every later resume — catches a flexible
        // download that finished (or a launch that failed) while the app or
        // process was backgrounded.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                updateManager.checkForUpdate()
            }
        }
        enableEdgeToEdge()
        setContent {
            CurlerTheme {
                CurlerNavHost()
            }
        }
    }

    override fun onDestroy() {
        updateManager.detachLauncher()
        super.onDestroy()
    }
}
