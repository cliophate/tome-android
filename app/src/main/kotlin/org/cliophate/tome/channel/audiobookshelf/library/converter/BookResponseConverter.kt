package org.cliophate.tome.channel.audiobookshelf.library.converter

import org.cliophate.tome.channel.audiobookshelf.common.model.MediaProgressResponse
import org.cliophate.tome.channel.audiobookshelf.library.model.BookResponse
import org.cliophate.tome.channel.audiobookshelf.library.model.LibraryAuthorResponse
import org.cliophate.tome.lib.domain.BookFile
import org.cliophate.tome.lib.domain.BookSeries
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.MediaProgress
import org.cliophate.tome.lib.domain.PlayingChapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookResponseConverter
  @Inject
  constructor() {
    fun apply(
      item: BookResponse,
      progressResponse: MediaProgressResponse? = null,
    ): DetailedItem {
      val maybeChapters =
        item
          .media
          .chapters
          ?.takeIf { it.isNotEmpty() }
          ?.map {
            PlayingChapter(
              start = it.start,
              end = it.end,
              title = it.title,
              available = true,
              id = it.id,
              duration = it.end - it.start,
              podcastEpisodeState = null,
            )
          }

      val filesAsChapters: () -> List<PlayingChapter> = {
        item
          .media
          .audioFiles
          ?.sortedBy { it.index }
          ?.fold(0.0 to mutableListOf<PlayingChapter>()) { (accDuration, chapters), file ->
            chapters.add(
              PlayingChapter(
                available = true,
                start = accDuration,
                end = accDuration + file.duration,
                title = file.metaTags?.tagTitle ?: file.metadata.filename.removeSuffix(file.metadata.ext),
                duration = file.duration,
                id = file.ino,
                podcastEpisodeState = null,
              ),
            )
            accDuration + file.duration to chapters
          }?.second
          ?: emptyList()
      }

      return DetailedItem(
        id = item.id,
        title = item.media.metadata.title,
        subtitle = item.media.metadata.subtitle,
        author =
          item.media.metadata.authors
            ?.joinToString(", ", transform = LibraryAuthorResponse::name),
        narrator =
          item.media.metadata.narrators
            ?.joinToString(separator = ", "),
        files =
          item
            .media
            .audioFiles
            ?.sortedBy { it.index }
            ?.map {
              BookFile(
                id = it.ino,
                name =
                  it.metaTags
                    ?.tagTitle
                    ?: (it.metadata.filename.removeSuffix(it.metadata.ext)),
                duration = it.duration,
                mimeType = it.mimeType,
                size = it.metadata.size,
              )
            }
            ?: emptyList(),
        chapters = maybeChapters ?: filesAsChapters(),
        libraryId = item.libraryId,
        localProvided = false,
        year = item.media.metadata.publishedYear,
        abstract = item.media.metadata.description,
        publisher = item.media.metadata.publisher,
        series =
          item
            .media
            .metadata
            .series
            ?.map {
              BookSeries(
                name = it.name,
                serialNumber = it.sequence,
              )
            } ?: emptyList(),
        createdAt = item.addedAt,
        updatedAt = item.ctimeMs,
        progress =
          progressResponse
            ?.let {
              MediaProgress(
                currentTime = it.currentTime,
                isFinished = it.isFinished,
                lastUpdate = it.lastUpdate,
              )
            },
      )
    }
  }
