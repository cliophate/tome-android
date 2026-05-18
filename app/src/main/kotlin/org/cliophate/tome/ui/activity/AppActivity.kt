package org.cliophate.tome.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import org.cliophate.tome.common.NetworkService
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import org.cliophate.tome.ui.navigation.AppLaunchAction
import org.cliophate.tome.ui.navigation.AppNavHost
import org.cliophate.tome.ui.navigation.AppNavigationService
import org.cliophate.tome.ui.navigation.CONTINUE_PLAYBACK
import org.cliophate.tome.ui.navigation.SHOW_DOWNLOADS
import org.cliophate.tome.ui.theme.TomeTheme
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity : ComponentActivity() {
  @Inject
  lateinit var preferences: TomeSharedPreferences

  @Inject
  lateinit var imageLoader: ImageLoader

  @Inject
  lateinit var networkService: NetworkService

  private lateinit var appNavigationService: AppNavigationService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val colorScheme by preferences
        .colorSchemeFlow
        .collectAsState(initial = preferences.getColorScheme())

      val materialYou by preferences
        .materialYouFlow
        .collectAsState(initial = preferences.getMaterialYouColors())

      TomeTheme(colorScheme, materialYou) {
        val navController = rememberNavController()
        appNavigationService = AppNavigationService(navController)

        AppNavHost(
          navController = navController,
          navigationService = appNavigationService,
          preferences = preferences,
          imageLoader = imageLoader,
          networkService = networkService,
          appLaunchAction = getLaunchAction(intent),
        )
      }
    }
  }

  private fun getLaunchAction(intent: Intent?) =
    when (intent?.action) {
      CONTINUE_PLAYBACK -> AppLaunchAction.CONTINUE_PLAYBACK
      SHOW_DOWNLOADS -> AppLaunchAction.MANAGE_DOWNLOADS
      else -> AppLaunchAction.DEFAULT
    }
}
