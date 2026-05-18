package org.cliophate.tome.channel.audiobookshelf.common.converter

import org.cliophate.tome.channel.audiobookshelf.common.model.bookmark.BookmarksItemResponse
import org.cliophate.tome.lib.domain.Bookmark
import org.cliophate.tome.lib.domain.BookmarkSyncState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkItemResponseConverter {
  @Inject
  constructor()

  fun apply(
    item: BookmarksItemResponse,
    syncState: BookmarkSyncState,
  ): Bookmark =
    Bookmark(
      libraryItemId = item.libraryItemId,
      title = item.title,
      totalPosition = item.time,
      createdAt = item.createdAt,
      syncState = syncState,
    )
}
