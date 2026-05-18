package org.cliophate.tome.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cliophate.tome.content.cache.persistent.CacheState
import org.cliophate.tome.content.cache.persistent.ContentCachingManager
import org.cliophate.tome.content.cache.persistent.ContentCachingProgress
import org.cliophate.tome.content.cache.persistent.ContentCachingService
import org.cliophate.tome.content.cache.persistent.LocalCacheRepository
import org.cliophate.tome.content.cache.temporary.CachedCoverProvider
import org.cliophate.tome.lib.domain.CacheStatus
import org.cliophate.tome.lib.domain.ContentCachingTask
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.DownloadOption
import org.cliophate.tome.lib.domain.PlayingChapter
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import org.cliophate.tome.ui.screens.settings.advanced.cache.CachedItemsPageSource
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class CachingModelView
  @Inject
  constructor(
    @ApplicationContext private val context: Context,
    private val localCacheRepository: LocalCacheRepository,
    private val contentCachingProgress: ContentCachingProgress,
    private val contentCachingManager: ContentCachingManager,
    private val preferences: TomeSharedPreferences,
    private val cachedCoverProvider: CachedCoverProvider,
  ) : ViewModel() {
    private val _totalCount = MutableLiveData<Int>()
    val totalCount: LiveData<Int> = _totalCount

    val forceCache = preferences.forceCacheFlow

    private val _bookCachingProgress = mutableMapOf<String, MutableStateFlow<CacheState>>()

    private val pageConfig =
      PagingConfig(
        pageSize = PAGE_SIZE,
        initialLoadSize = PAGE_SIZE,
        prefetchDistance = PAGE_SIZE,
      )

    private var pageSource: PagingSource<Int, DetailedItem>? = null
    val libraryPager: Flow<PagingData<DetailedItem>> by lazy {
      Pager(
        config = pageConfig,
        pagingSourceFactory = {
          val source = CachedItemsPageSource(localCacheRepository) { _totalCount.postValue(it) }

          pageSource = source
          source
        },
      ).flow.cachedIn(viewModelScope)
    }

    init {
      viewModelScope.launch {
        contentCachingProgress.statusFlow.collect { (item, progress) ->
          val flow =
            _bookCachingProgress.getOrPut(item.id) {
              MutableStateFlow(progress)
            }
          flow.value = progress
        }
      }
    }

    suspend fun clearShortTermCache() {
      withContext(Dispatchers.IO) {
        cachedCoverProvider.clearCache()
      }
    }

    fun cache(
      mediaItem: DetailedItem,
      currentPosition: Double,
      option: DownloadOption,
    ) {
      val task =
        ContentCachingTask(
          item = mediaItem,
          options = option,
          currentPosition = currentPosition,
        )

      val intent =
        Intent(context, ContentCachingService::class.java).apply {
          action = ContentCachingService.CACHE_ITEM_ACTION
          putExtra(ContentCachingService.CACHING_TASK_EXTRA, task as Serializable)
        }

      context.startForegroundService(intent)
    }

    fun getProgress(bookId: String) =
      _bookCachingProgress
        .getOrPut(bookId) { MutableStateFlow(CacheState(CacheStatus.Idle)) }

    suspend fun dropCache(bookId: String) {
      contentCachingManager.dropCache(bookId)
    }

    fun stopCaching(item: DetailedItem) {
      val intent =
        Intent(context, ContentCachingService::class.java).apply {
          action = ContentCachingService.STOP_CACHING_ACTION
          putExtra(ContentCachingService.CACHING_PLAYING_ITEM, item as Serializable)
        }

      context.startForegroundService(intent)
    }

    suspend fun dropCache(
      item: DetailedItem,
      chapter: PlayingChapter,
    ) {
      contentCachingManager.dropCache(item, chapter)
    }

    fun toggleCacheForce() {
      when (localCacheUsing()) {
        true -> preferences.disableForceCache()
        false -> preferences.enableForceCache()
      }
    }

    fun localCacheUsing() = preferences.isForceCache()

    fun provideCacheState(bookId: String): LiveData<Boolean> = contentCachingManager.hasMetadataCached(bookId)

    fun provideCacheState(
      bookId: String,
      chapterId: String,
    ): LiveData<Boolean> = contentCachingManager.hasMetadataCached(bookId, chapterId)

    fun fetchCachedItems() {
      viewModelScope.launch {
        withContext(Dispatchers.IO) {
          pageSource?.invalidate()
        }
      }
    }

    suspend fun fetchLatestUpdate(libraryId: String) = localCacheRepository.fetchLatestUpdate(libraryId)

    companion object {
      private const val PAGE_SIZE = 20
    }
  }
