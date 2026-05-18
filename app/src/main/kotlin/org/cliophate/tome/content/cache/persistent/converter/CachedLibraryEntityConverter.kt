package org.cliophate.tome.content.cache.persistent.converter

import org.cliophate.tome.content.cache.persistent.entity.CachedLibraryEntity
import org.cliophate.tome.lib.domain.Library
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedLibraryEntityConverter
  @Inject
  constructor() {
    fun apply(entity: CachedLibraryEntity): Library =
      Library(
        id = entity.id,
        title = entity.title,
        type = entity.type,
      )
  }
