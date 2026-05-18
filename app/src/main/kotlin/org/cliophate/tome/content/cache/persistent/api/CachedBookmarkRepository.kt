package org.cliophate.tome.content.cache.persistent.api

import org.cliophate.tome.content.cache.persistent.converter.CachedBookmarkEntityConverter
import org.cliophate.tome.content.cache.persistent.dao.CachedBookmarkDao
import org.cliophate.tome.content.cache.persistent.entity.CachedBookmarkEntity
import org.cliophate.tome.lib.domain.Bookmark
import org.cliophate.tome.lib.domain.asBookmarkSyncState
import org.cliophate.tome.lib.domain.asInteger
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedBookmarkRepository
  @Inject
  constructor(
    private val dao: CachedBookmarkDao,
    private val converter: CachedBookmarkEntityConverter,
  ) {
    suspend fun fetchBookmarks(libraryItemId: String): List<Bookmark> =
      dao
        .fetchByLibraryItemId(libraryItemId)
        .map { converter.apply(entity = it, syncState = it.syncState.asBookmarkSyncState()) }

    suspend fun upsertBookmark(bookmark: Bookmark) {
      dao.upsert(
        CachedBookmarkEntity(
          id = UUID.randomUUID().toString(),
          title = bookmark.title,
          libraryItemId = bookmark.libraryItemId,
          createdAt = bookmark.createdAt,
          totalPosition = bookmark.totalPosition.toLong(),
          syncState = bookmark.syncState.asInteger(),
        ),
      )
    }

    suspend fun deleteBookmark(
      libraryItemId: String,
      totalPosition: Double,
    ): Boolean =
      dao.deleteByLibraryItemIdAndTotalPosition(
        libraryItemId = libraryItemId,
        totalPosition = totalPosition.toLong(),
      ) > 0
  }
