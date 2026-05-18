package org.cliophate.tome.channel.audiobookshelf.common.converter

import org.cliophate.tome.channel.audiobookshelf.library.model.LibraryItemsResponse
import org.cliophate.tome.lib.domain.Book
import org.cliophate.tome.lib.domain.PagedItems
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryPageResponseConverter
  @Inject
  constructor() {
    fun apply(response: LibraryItemsResponse): PagedItems<Book> =
      response
        .results
        .mapNotNull {
          val title = it.media.metadata.title ?: return@mapNotNull null

          Book(
            id = it.id,
            title = title,
            series = it.media.metadata.seriesName,
            subtitle = it.media.metadata.subtitle,
            author = it.media.metadata.authorName,
          )
        }.let {
          PagedItems(
            items = it,
            currentPage = response.page,
            totalItems = response.total,
          )
        }
  }
