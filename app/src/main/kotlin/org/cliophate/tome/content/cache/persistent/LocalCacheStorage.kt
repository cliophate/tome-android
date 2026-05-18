package org.cliophate.tome.content.cache.persistent

import androidx.room.Database
import androidx.room.RoomDatabase
import org.cliophate.tome.content.cache.persistent.dao.CachedBookDao
import org.cliophate.tome.content.cache.persistent.dao.CachedBookmarkDao
import org.cliophate.tome.content.cache.persistent.dao.CachedLibraryDao
import org.cliophate.tome.content.cache.persistent.entity.BookChapterEntity
import org.cliophate.tome.content.cache.persistent.entity.BookEntity
import org.cliophate.tome.content.cache.persistent.entity.BookFileEntity
import org.cliophate.tome.content.cache.persistent.entity.CachedBookmarkEntity
import org.cliophate.tome.content.cache.persistent.entity.CachedLibraryEntity
import org.cliophate.tome.content.cache.persistent.entity.MediaProgressEntity

@Database(
  entities = [
    BookEntity::class,
    BookFileEntity::class,
    BookChapterEntity::class,
    MediaProgressEntity::class,
    CachedLibraryEntity::class,
    CachedBookmarkEntity::class,
  ],
  version = 18,
  exportSchema = true,
)
abstract class LocalCacheStorage : RoomDatabase() {
  abstract fun cachedBookDao(): CachedBookDao

  abstract fun cachedBookmarkDao(): CachedBookmarkDao

  abstract fun cachedLibraryDao(): CachedLibraryDao
}
