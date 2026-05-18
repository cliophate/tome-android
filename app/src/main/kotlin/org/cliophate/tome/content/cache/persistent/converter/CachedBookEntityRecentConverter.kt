package org.cliophate.tome.content.cache.persistent.converter

import org.cliophate.tome.content.cache.persistent.entity.BookEntity
import org.cliophate.tome.lib.domain.RecentBook
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedBookEntityRecentConverter
  @Inject
  constructor() {
    fun apply(
      entity: BookEntity,
      currentTime: Pair<Long, Double>?,
    ): RecentBook =
      RecentBook(
        id = entity.id,
        title = entity.title,
        subtitle = entity.subtitle,
        author = entity.author,
        listenedLastUpdate = currentTime?.first ?: 0,
        listenedPercentage =
          currentTime
            ?.second
            ?.let { it / entity.duration }
            ?.let { it * 100 }
            ?.toInt(),
      )
  }
