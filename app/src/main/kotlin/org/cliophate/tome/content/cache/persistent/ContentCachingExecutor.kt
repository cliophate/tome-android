package org.cliophate.tome.content.cache.persistent

import kotlinx.coroutines.flow.Flow
import org.cliophate.tome.channel.common.MediaChannel
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.DownloadOption

class ContentCachingExecutor(
  private val item: DetailedItem,
  private val options: DownloadOption,
  private val position: Double,
  private val contentCachingManager: ContentCachingManager,
) {
  fun run(channel: MediaChannel): Flow<CacheState> =
    contentCachingManager
      .cacheMediaItem(
        mediaItem = item,
        option = options,
        channel = channel,
        currentTotalPosition = position,
      )
}
