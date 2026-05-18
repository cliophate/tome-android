package org.cliophate.tome.ui.screens.common

import android.content.Context
import org.cliophate.tome.R
import org.cliophate.tome.lib.domain.AllItemsDownloadOption
import org.cliophate.tome.lib.domain.CurrentItemDownloadOption
import org.cliophate.tome.lib.domain.DownloadOption
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.lib.domain.NumberItemDownloadOption
import org.cliophate.tome.lib.domain.RemainingItemsDownloadOption

fun DownloadOption?.makeText(
  context: Context,
  libraryType: LibraryType,
): String =
  when (this) {
    null -> {
      context.getString(R.string.downloads_menu_download_option_disable)
    }

    CurrentItemDownloadOption -> {
      when (libraryType) {
        LibraryType.LIBRARY -> context.getString(R.string.downloads_menu_download_option_current_chapter)
        LibraryType.PODCAST -> context.getString(R.string.downloads_menu_download_option_current_episode)
        LibraryType.UNKNOWN -> context.getString(R.string.downloads_menu_download_option_current_item)
      }
    }

    AllItemsDownloadOption -> {
      when (libraryType) {
        LibraryType.LIBRARY -> context.getString(R.string.downloads_menu_download_option_entire_book)
        LibraryType.PODCAST -> context.getString(R.string.downloads_menu_download_option_entire_podcast)
        LibraryType.UNKNOWN -> context.getString(R.string.downloads_menu_download_option_entire_item)
      }
    }

    RemainingItemsDownloadOption -> {
      when (libraryType) {
        LibraryType.LIBRARY -> context.getString(R.string.downloads_menu_download_option_remaining_chapters)
        LibraryType.PODCAST -> context.getString(R.string.downloads_menu_download_option_remaining_episodes)
        LibraryType.UNKNOWN -> context.getString(R.string.downloads_menu_download_option_remaining_items)
      }
    }

    is NumberItemDownloadOption -> {
      when (libraryType) {
        LibraryType.LIBRARY -> {
          context.getString(
            R.string.downloads_menu_download_option_next_chapters,
            itemsNumber,
          )
        }

        LibraryType.PODCAST -> {
          context.getString(
            R.string.downloads_menu_download_option_next_episodes,
            itemsNumber,
          )
        }

        LibraryType.UNKNOWN -> {
          context.getString(
            R.string.downloads_menu_download_option_next_items,
            itemsNumber,
          )
        }
      }
    }
  }
