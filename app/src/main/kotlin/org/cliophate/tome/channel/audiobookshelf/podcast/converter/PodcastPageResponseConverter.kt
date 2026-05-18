package org.cliophate.tome.channel.audiobookshelf.podcast.converter

import org.cliophate.tome.channel.audiobookshelf.podcast.model.PodcastItemsResponse
import org.cliophate.tome.lib.domain.Book
import org.cliophate.tome.lib.domain.PagedItems
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastPageResponseConverter
  @Inject
  constructor() {
    fun apply(response: PodcastItemsResponse): PagedItems<Book> =
      response
        .results
        .mapNotNull {
          val title = it.media.metadata.title ?: return@mapNotNull null

          Book(
            id = it.id,
            title = title,
            subtitle = null,
            series = null,
            author = it.media.metadata.author,
          )
        }.let {
          PagedItems(
            items = it,
            currentPage = response.page,
            totalItems = response.total,
          )
        }
  }
