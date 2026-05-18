package org.cliophate.tome.channel.audiobookshelf.podcast.converter

import org.cliophate.tome.channel.audiobookshelf.podcast.model.PodcastItem
import org.cliophate.tome.lib.domain.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastSearchItemsConverter
  @Inject
  constructor() {
    fun apply(response: List<PodcastItem>): List<Book> {
      return response
        .mapNotNull {
          val title = it.media.metadata.title ?: return@mapNotNull null

          Book(
            id = it.id,
            title = title,
            subtitle = null,
            series = null,
            author = it.media.metadata.author,
          )
        }
    }
  }
