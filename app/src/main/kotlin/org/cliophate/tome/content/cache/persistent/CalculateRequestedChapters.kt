package org.cliophate.tome.content.cache.persistent

import org.cliophate.tome.lib.domain.AllItemsDownloadOption
import org.cliophate.tome.lib.domain.CurrentItemDownloadOption
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.DownloadOption
import org.cliophate.tome.lib.domain.NumberItemDownloadOption
import org.cliophate.tome.lib.domain.PlayingChapter
import org.cliophate.tome.lib.domain.RemainingItemsDownloadOption
import org.cliophate.tome.playback.service.calculateChapterIndex

fun calculateRequestedChapters(
  book: DetailedItem,
  option: DownloadOption,
  currentTotalPosition: Double,
): List<PlayingChapter> {
  val chapterIndex = calculateChapterIndex(book, currentTotalPosition)

  return when (option) {
    AllItemsDownloadOption -> {
      book.chapters
    }

    CurrentItemDownloadOption -> {
      listOfNotNull(book.chapters.getOrNull(chapterIndex))
    }

    is NumberItemDownloadOption -> {
      book.chapters.subList(
        chapterIndex.coerceAtLeast(0),
        (chapterIndex + option.itemsNumber).coerceIn(chapterIndex..book.chapters.size),
      )
    }

    RemainingItemsDownloadOption -> {
      book.chapters.subList(
        chapterIndex.coerceIn(0, book.chapters.size),
        book.chapters.size,
      )
    }
  }
}
