package org.cliophate.tome.content.cache.persistent.api

import org.cliophate.tome.content.cache.persistent.converter.CachedLibraryEntityConverter
import org.cliophate.tome.content.cache.persistent.dao.CachedLibraryDao
import org.cliophate.tome.lib.domain.Library
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedLibraryRepository
  @Inject
  constructor(
    private val dao: CachedLibraryDao,
    private val converter: CachedLibraryEntityConverter,
  ) {
    suspend fun cacheLibraries(libraries: List<Library>) = dao.updateLibraries(libraries)

    suspend fun fetchLibraries() =
      dao
        .fetchLibraries()
        .map { converter.apply(it) }
  }
