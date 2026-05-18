package org.cliophate.tome.content.cache.persistent.converter

import com.squareup.moshi.Types
import org.cliophate.tome.common.moshi
import org.cliophate.tome.content.cache.persistent.entity.BookSeriesDto
import org.cliophate.tome.content.cache.persistent.entity.CachedBookEntity
import org.cliophate.tome.lib.domain.BookFile
import org.cliophate.tome.lib.domain.BookSeries
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.PlayingChapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedBookEntityDetailedConverter
  @Inject
  constructor(
    private val mediaProgressEntityConverter: MediaProgressEntityConverter,
  ) {
    fun apply(entity: CachedBookEntity): DetailedItem =
      DetailedItem(
        id = entity.detailedBook.id,
        title = entity.detailedBook.title,
        subtitle = entity.detailedBook.subtitle,
        author = entity.detailedBook.author,
        narrator = entity.detailedBook.narrator,
        libraryId = entity.detailedBook.libraryId,
        localProvided = true,
        files =
          entity.files.map { fileEntity ->
            BookFile(
              id = fileEntity.bookFileId,
              name = fileEntity.name,
              size = fileEntity.size,
              duration = fileEntity.duration,
              mimeType = fileEntity.mimeType,
            )
          },
        chapters =
          entity.chapters.map { chapterEntity ->
            PlayingChapter(
              duration = chapterEntity.duration,
              start = chapterEntity.start,
              end = chapterEntity.end,
              title = chapterEntity.title,
              available = chapterEntity.isCached,
              id = chapterEntity.bookChapterId,
              podcastEpisodeState = null, // currently state is not available for local mode
            )
          },
        abstract = entity.detailedBook.abstract,
        publisher = entity.detailedBook.publisher,
        year = entity.detailedBook.year,
        createdAt = entity.detailedBook.createdAt,
        updatedAt = entity.detailedBook.updatedAt,
        series =
          entity
            .detailedBook
            .seriesJson
            ?.let {
              val type = Types.newParameterizedType(List::class.java, BookSeriesDto::class.java)
              val adapter = moshi.adapter<List<BookSeriesDto>>(type)
              adapter.fromJson(it)
            }?.map {
              BookSeries(
                name = it.title,
                serialNumber = it.sequence,
              )
            } ?: emptyList(),
        progress = entity.progress?.let { mediaProgressEntityConverter.apply(it) },
      )
  }
