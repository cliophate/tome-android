package org.cliophate.tome.content.cache.persistent.converter

import org.cliophate.tome.content.cache.persistent.entity.CachedBookmarkEntity
import org.cliophate.tome.lib.domain.Bookmark
import org.cliophate.tome.lib.domain.BookmarkSyncState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedBookmarkEntityConverter
  @Inject
  constructor() {
    fun apply(
      entity: CachedBookmarkEntity,
      syncState: BookmarkSyncState,
    ): Bookmark =
      Bookmark(
        libraryItemId = entity.libraryItemId,
        title = entity.title,
        totalPosition = entity.totalPosition.toDouble(),
        createdAt = entity.createdAt,
        syncState = syncState,
      )
  }
