package org.cliophate.tome.viewmodel

import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cliophate.tome.content.TomeMediaProvider
import org.cliophate.tome.lib.domain.Book
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.lib.domain.RecentBook
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import org.cliophate.tome.ui.screens.library.model.HomeSummary
import org.cliophate.tome.ui.screens.library.paging.LibraryDefaultPagingSource
import org.cliophate.tome.ui.screens.library.paging.LibrarySearchPagingSource
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel
  @Inject
  constructor(
    private val mediaChannel: TomeMediaProvider,
    private val preferences: TomeSharedPreferences,
  ) : ViewModel() {
    private val _recentBooks = MutableLiveData<List<RecentBook>>(emptyList())
    val recentBooks: LiveData<List<RecentBook>> = _recentBooks

    private val _recentBookUpdating = MutableLiveData(false)
    val recentBookUpdating: LiveData<Boolean> = _recentBookUpdating

    private val _searchRequested = MutableLiveData(false)
    val searchRequested: LiveData<Boolean> = _searchRequested

    private val _searchToken = MutableStateFlow(EMPTY_SEARCH)

    private val _seriesShelves = MutableLiveData<List<SeriesShelf>>(emptyList())
    val seriesShelves: LiveData<List<SeriesShelf>> = _seriesShelves

    private val _seriesLoading = MutableLiveData(false)
    val seriesLoading: LiveData<Boolean> = _seriesLoading

    private val _seriesLoadFailed = MutableLiveData(false)
    val seriesLoadFailed: LiveData<Boolean> = _seriesLoadFailed

    private val _homeSummary = MutableLiveData(HomeSummary(stats = null, recentlyAdded = emptyList(), unplayed = emptyList()))
    val homeSummary: LiveData<HomeSummary> = _homeSummary

    private val _homeLoading = MutableLiveData(false)
    val homeLoading: LiveData<Boolean> = _homeLoading

    private var defaultPagingSource: PagingSource<Int, Book>? = null
    private var searchPagingSource: PagingSource<Int, Book>? = null
    private var seriesLoadedLibraryId: String? = null
    private var homeLoadedLibraryId: String? = null

    private val _totalCount = MutableLiveData<Int>()
    val totalCount: LiveData<Int> = _totalCount

    private val pageConfig =
      PagingConfig(
        pageSize = PAGE_SIZE,
        initialLoadSize = PAGE_SIZE,
        prefetchDistance = PAGE_SIZE,
      )

    fun getPager(isSearchRequested: Boolean) =
      when (isSearchRequested) {
        true -> searchPager
        false -> libraryPager
      }

    private val searchPager: Flow<PagingData<Book>> =
      combine(
        _searchToken,
        searchRequested.asFlow(),
      ) { token, requested ->
        Pair(token, requested)
      }.flatMapLatest { (token, _) ->
        Pager(
          config = pageConfig,
          pagingSourceFactory = {
            val source =
              LibrarySearchPagingSource(
                preferences = preferences,
                mediaChannel = mediaChannel,
                searchToken = token,
                limit = PAGE_SEARCH_SIZE,
              ) { _totalCount.postValue(it) }

            searchPagingSource = source
            source
          },
        ).flow
      }.cachedIn(viewModelScope)

    private val libraryPager: Flow<PagingData<Book>> by lazy {
      Pager(
        config = pageConfig,
        pagingSourceFactory = {
          val source = LibraryDefaultPagingSource(preferences, mediaChannel) { _totalCount.postValue(it) }
          defaultPagingSource = source

          source
        },
      ).flow.cachedIn(viewModelScope)
    }

    fun requestSearch() {
      _searchRequested.postValue(true)
    }

    fun dismissSearch() {
      _searchRequested.postValue(false)
      _searchToken.value = EMPTY_SEARCH
    }

    fun updateSearch(token: String) {
      viewModelScope.launch { _searchToken.emit(token) }
    }

    fun fetchPreferredLibraryTitle(): String? =
      preferences
        .getPreferredLibrary()
        ?.title

    fun fetchPreferredLibraryType() =
      preferences
        .getPreferredLibrary()
        ?.type
        ?: LibraryType.UNKNOWN

    fun refreshRecentListening() {
      viewModelScope.launch {
        withContext(Dispatchers.IO) {
          fetchRecentListening()
        }
      }
    }

    fun refreshLibrary() {
      seriesLoadedLibraryId = null
      homeLoadedLibraryId = null

      viewModelScope.launch {
        withContext(Dispatchers.IO) {
          when (searchRequested.value) {
            true -> searchPagingSource?.invalidate()
            else -> defaultPagingSource?.invalidate()
          }
        }
      }
    }

    fun fetchHome(force: Boolean = false) {
      val preferredLibrary =
        preferences.getPreferredLibrary()?.id ?: run {
          _homeSummary.postValue(HomeSummary(stats = null, recentlyAdded = emptyList(), unplayed = emptyList()))
          _homeLoading.postValue(false)
          homeLoadedLibraryId = null
          return
        }

      if (!force && homeLoadedLibraryId == preferredLibrary) {
        return
      }

      _homeLoading.postValue(true)

      viewModelScope.launch {
        withContext(Dispatchers.IO) {
          mediaChannel
            .fetchHomeSummary(preferredLibrary)
            .fold(
              onSuccess = {
                _homeSummary.postValue(it)
                _homeLoading.postValue(false)
                homeLoadedLibraryId = preferredLibrary
              },
              onFailure = {
                _homeLoading.postValue(false)
              },
            )
        }
      }
    }

    fun fetchSeries(force: Boolean = false) {
      val preferredLibrary =
        preferences.getPreferredLibrary()?.id ?: run {
          _seriesShelves.postValue(emptyList())
          _seriesLoadFailed.postValue(false)
          _seriesLoading.postValue(false)
          seriesLoadedLibraryId = null
          return
        }

      if (!force && seriesLoadedLibraryId == preferredLibrary) {
        return
      }

      _seriesLoading.postValue(true)
      _seriesLoadFailed.postValue(false)

      viewModelScope.launch {
        withContext(Dispatchers.IO) {
          mediaChannel
            .fetchSeriesBooks(preferredLibrary)
            .fold(
              onSuccess = {
                _seriesShelves.postValue(buildSeriesShelves(it))
                _seriesLoadFailed.postValue(false)
                _seriesLoading.postValue(false)
                seriesLoadedLibraryId = preferredLibrary
              },
              onFailure = {
                _seriesShelves.postValue(emptyList())
                _seriesLoadFailed.postValue(true)
                _seriesLoading.postValue(false)
              },
            )
        }
      }
    }

    fun fetchRecentListening() {
      _recentBookUpdating.postValue(true)

      val preferredLibrary =
        preferences.getPreferredLibrary()?.id ?: run {
          _recentBookUpdating.postValue(false)
          return
        }

      viewModelScope.launch {
        mediaChannel
          .fetchRecentListenedBooks(preferredLibrary)
          .fold(
            onSuccess = {
              _recentBooks.postValue(it)
              _recentBookUpdating.postValue(false)
            },
            onFailure = {
              _recentBookUpdating.postValue(false)
            },
          )
      }
    }

    companion object {
      private const val EMPTY_SEARCH = ""
      private const val PAGE_SIZE = 20
      private const val PAGE_SEARCH_SIZE = 50
    }
  }

data class SeriesShelf(
  val title: String,
  val author: String?,
  val description: String?,
  val summary: String,
  val books: List<SeriesBook>,
)

data class SeriesBook(
  val id: String,
  val title: String,
  val subtitle: String?,
  val author: String?,
  val sequenceLabel: String?,
  val isFinished: Boolean,
)

private data class SeriesShelfEntry(
  val seriesName: String,
  val sequenceNumber: Double?,
  val sequenceText: String?,
  val description: String?,
  val year: Int?,
  val book: SeriesBook,
)

private fun buildSeriesShelves(items: List<DetailedItem>): List<SeriesShelf> =
  items
    .flatMap { item ->
      item.series.map { series ->
        val sequence = series.serialNumber?.trim()?.takeIf(String::isNotBlank)

        SeriesShelfEntry(
          seriesName = series.name.trim(),
          sequenceNumber = parseSeriesSequence(sequence),
          sequenceText = sequence,
          description = cleanSeriesDescription(item.abstract),
          year = item.year?.toIntOrNull(),
          book =
            SeriesBook(
              id = item.id,
              title = item.title,
              subtitle = item.subtitle,
              author = item.author,
              sequenceLabel = sequence?.let { "#$it" },
              isFinished = item.progress?.isFinished == true,
            ),
        )
      }
    }.filter { it.seriesName.isNotBlank() }
    .groupBy { it.seriesName }
    .map { (seriesName, entries) ->
      val sortedEntries =
        entries
          .distinctBy { it.book.id }
          .sortedWith(
            compareBy<SeriesShelfEntry>(
              { it.sequenceNumber == null && it.sequenceText == null },
              { it.sequenceNumber ?: Double.MAX_VALUE },
              { it.sequenceText?.lowercase() ?: "" },
              { it.book.title.lowercase() },
            ),
          )

      val yearLabel = provideYearLabel(sortedEntries.mapNotNull { it.year })

      SeriesShelf(
        title = seriesName,
        author = providePrimaryAuthor(sortedEntries),
        description = sortedEntries.mapNotNull { it.description }.firstOrNull(),
        summary = buildSeriesSummary(sortedEntries.size, yearLabel),
        books =
          sortedEntries.map { it.book },
      )
    }.sortedBy { it.title.lowercase() }

private fun parseSeriesSequence(sequence: String?): Double? =
  sequence
    ?.replace(',', '.')
    ?.toDoubleOrNull()

private fun providePrimaryAuthor(entries: List<SeriesShelfEntry>): String? =
  entries
    .mapNotNull { entry ->
      entry.book.author
        ?.trim()
        ?.takeIf(String::isNotBlank)
    }.groupingBy { it }
    .eachCount()
    .entries
    .sortedWith(
      compareByDescending<Map.Entry<String, Int>> { it.value }
        .thenBy { it.key.lowercase() },
    ).firstOrNull()
    ?.key

private fun provideYearLabel(years: List<Int>): String? {
  if (years.isEmpty()) {
    return null
  }

  val minYear = years.minOrNull() ?: return null
  val maxYear = years.maxOrNull() ?: return null

  return when (minYear == maxYear) {
    true -> minYear.toString()
    false -> "$minYear-$maxYear"
  }
}

private fun buildSeriesSummary(
  bookCount: Int,
  yearLabel: String?,
): String =
  buildString {
    append(bookCount)
    append(if (bookCount == 1) " book" else " books")
    yearLabel?.let {
      append(" • ")
      append(it)
    }
  }

private fun cleanSeriesDescription(description: String?): String? =
  description
    ?.trim()
    ?.takeIf(String::isNotBlank)
    ?.let {
      HtmlCompat
        .fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString()
        .replace(Regex("\\s+"), " ")
        .trim()
        .takeIf(String::isNotBlank)
    }
