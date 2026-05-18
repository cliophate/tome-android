package org.cliophate.tome.content.cache.persistent

import androidx.annotation.Keep
import org.cliophate.tome.lib.domain.CacheStatus

@Keep
data class CacheState(
  val status: CacheStatus,
  val progress: Double = 0.0,
)
