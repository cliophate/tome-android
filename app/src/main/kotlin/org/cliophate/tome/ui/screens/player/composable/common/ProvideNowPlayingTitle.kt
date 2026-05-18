package org.cliophate.tome.ui.screens.player.composable.common

import android.content.Context
import org.cliophate.tome.R
import org.cliophate.tome.lib.domain.LibraryType

fun provideNowPlayingTitle(
  libraryType: LibraryType,
  context: Context,
) = when (libraryType) {
  LibraryType.LIBRARY -> context.getString(R.string.player_screen_library_playing_title)
  LibraryType.PODCAST -> context.getString(R.string.player_screen_podcast_playing_title)
  LibraryType.UNKNOWN -> context.getString(R.string.player_screen_items_playing_title)
}
