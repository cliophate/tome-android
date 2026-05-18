package org.cliophate.tome.ui.screens.library

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.ImageLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.cliophate.tome.R
import org.cliophate.tome.common.LibraryOrderingConfiguration
import org.cliophate.tome.common.NetworkService
import org.cliophate.tome.common.withHaptic
import org.cliophate.tome.lib.domain.DetailedItem
import org.cliophate.tome.lib.domain.LibraryType
import org.cliophate.tome.lib.domain.RecentBook
import org.cliophate.tome.ui.components.withScrollbar
import org.cliophate.tome.ui.extensions.withMinimumTime
import org.cliophate.tome.ui.navigation.AppNavigationService
import org.cliophate.tome.ui.screens.common.RequestNotificationPermissions
import org.cliophate.tome.ui.screens.library.composables.BookComposable
import org.cliophate.tome.ui.screens.library.composables.DefaultActionComposable
import org.cliophate.tome.ui.screens.library.composables.HomeBookShelfComposable
import org.cliophate.tome.ui.screens.library.composables.HomeSectionHeaderComposable
import org.cliophate.tome.ui.screens.library.composables.HomeStatsComposable
import org.cliophate.tome.ui.screens.library.composables.LibraryBottomBarComposable
import org.cliophate.tome.ui.screens.library.composables.LibrarySearchActionComposable
import org.cliophate.tome.ui.screens.library.composables.LibrarySettingsComposable
import org.cliophate.tome.ui.screens.library.composables.LibrarySwitchComposable
import org.cliophate.tome.ui.screens.library.composables.LibraryTab
import org.cliophate.tome.ui.screens.library.composables.MiniPlayerComposable
import org.cliophate.tome.ui.screens.library.composables.RecentBooksComposable
import org.cliophate.tome.ui.screens.library.composables.SeriesDetailGridComposable
import org.cliophate.tome.ui.screens.library.composables.SeriesDetailHeaderComposable
import org.cliophate.tome.ui.screens.library.composables.SeriesOverviewSummaryComposable
import org.cliophate.tome.ui.screens.library.composables.SeriesShelfComposable
import org.cliophate.tome.ui.screens.library.composables.fallback.LibraryFallbackComposable
import org.cliophate.tome.ui.screens.library.composables.placeholder.LibraryPlaceholderComposable
import org.cliophate.tome.ui.screens.library.composables.placeholder.RecentBooksPlaceholderComposable
import org.cliophate.tome.ui.screens.library.model.HomeSummary
import org.cliophate.tome.viewmodel.CachingModelView
import org.cliophate.tome.viewmodel.LibraryViewModel
import org.cliophate.tome.viewmodel.PlayerViewModel
import org.cliophate.tome.viewmodel.SeriesShelf
import org.cliophate.tome.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LibraryScreen(
  navController: AppNavigationService,
  libraryViewModel: LibraryViewModel = hiltViewModel(),
  playerViewModel: PlayerViewModel = hiltViewModel(),
  settingsViewModel: SettingsViewModel = hiltViewModel(),
  cachingModelView: CachingModelView = hiltViewModel(),
  imageLoader: ImageLoader,
  networkService: NetworkService,
) {
  RequestNotificationPermissions()

  val view: View = LocalView.current
  val coroutineScope = rememberCoroutineScope()

  val activity = LocalActivity.current
  val recentBooks: List<RecentBook> by libraryViewModel.recentBooks.observeAsState(emptyList())
  val homeSummary: HomeSummary by libraryViewModel.homeSummary.observeAsState(
    HomeSummary(stats = null, recentlyAdded = emptyList(), unplayed = emptyList()),
  )
  val homeLoading by libraryViewModel.homeLoading.observeAsState(false)
  val seriesShelves: List<SeriesShelf> by libraryViewModel.seriesShelves.observeAsState(emptyList())
  val seriesLoading by libraryViewModel.seriesLoading.observeAsState(false)
  val seriesLoadFailed by libraryViewModel.seriesLoadFailed.observeAsState(false)

  var currentLibraryId by rememberSaveable { mutableStateOf("") }
  var localCacheUpdatedAt by rememberSaveable { mutableStateOf(0L) }
  var currentOrdering by rememberSaveable(stateSaver = LibraryOrderingConfiguration.saver) {
    mutableStateOf(LibraryOrderingConfiguration.default)
  }
  var pullRefreshing by remember { mutableStateOf(false) }
  val recentBookRefreshing by libraryViewModel.recentBookUpdating.observeAsState(false)
  val searchRequested by libraryViewModel.searchRequested.observeAsState(false)
  val preparingError by playerViewModel.preparingError.observeAsState(false)

  val preferredLibrary by settingsViewModel.preferredLibrary.observeAsState()
  val libraries by settingsViewModel.libraries.observeAsState(emptyList())
  val serverConnected by settingsViewModel.serverConnected.observeAsState(false)

  var selectedTab by rememberSaveable { mutableStateOf(LibraryTab.HOME) }
  var previousBrowseTab by rememberSaveable { mutableStateOf(LibraryTab.HOME) }
  var preferredLibraryExpanded by remember { mutableStateOf(false) }
  var preferencesExpanded by remember { mutableStateOf(false) }
  var selectedSeriesTitle by rememberSaveable { mutableStateOf<String?>(null) }

  val library = libraryViewModel.getPager(searchRequested).collectAsLazyPagingItems()
  val libraryCount by libraryViewModel.totalCount.observeAsState()

  val libraryListState = rememberLazyListState()

  BackHandler {
    when (searchRequested) {
      true -> {
        libraryViewModel.dismissSearch()
        selectedTab = previousBrowseTab
      }

      false -> {
        if (selectedTab == LibraryTab.SERIES && selectedSeriesTitle != null) {
          selectedSeriesTitle = null
          return@BackHandler
        }

        activity?.moveTaskToBack(true)
      }
    }
  }

  fun refreshContent(showPullRefreshing: Boolean) {
    coroutineScope.launch {
      if (settingsViewModel.hasCredentials().not()) {
        navController.showLogin()
        return@launch
      }

      if (showPullRefreshing) {
        pullRefreshing = true
      }

      val minimumTime =
        when (showPullRefreshing) {
          true -> 500L
          false -> 0L
        }

      withMinimumTime(minimumTime) {
        listOf(
          async { settingsViewModel.refreshConnectionInfo() },
          async { settingsViewModel.fetchLibraries() },
          async { libraryViewModel.refreshLibrary() },
          async { libraryViewModel.fetchRecentListening() },
        ).awaitAll()
      }

      if (!searchRequested && selectedTab == LibraryTab.SERIES) {
        libraryViewModel.fetchSeries(force = true)
      }

      if (!searchRequested && selectedTab == LibraryTab.HOME) {
        libraryViewModel.fetchHome(force = true)
      }

      pullRefreshing = false
    }
  }

  val isPlaceholderRequired by remember {
    derivedStateOf {
      if (searchRequested) {
        return@derivedStateOf false
      }

      if (selectedTab == LibraryTab.SERIES) {
        return@derivedStateOf false
      }

      if (selectedTab == LibraryTab.HOME) {
        return@derivedStateOf false
      }

      pullRefreshing || recentBookRefreshing || library.loadState.refresh is LoadState.Loading
    }
  }

  val isHomePlaceholderRequired by remember {
    derivedStateOf {
      if (selectedTab != LibraryTab.HOME || searchRequested) {
        return@derivedStateOf false
      }

      homeLoading || recentBookRefreshing
    }
  }

  LaunchedEffect(preparingError) {
    if (preparingError) {
      playerViewModel.clearPlayingBook()
    }
  }

  val pullRefreshState =
    rememberPullRefreshState(
      refreshing = pullRefreshing,
      onRefresh = {
        withHaptic(view) { refreshContent(showPullRefreshing = true) }
      },
    )

  val titleTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
  val titleHeightDp = with(LocalDensity.current) { titleTextStyle.lineHeight.toPx().toDp() }

  val playingBook by playerViewModel.book.observeAsState()
  val context = LocalContext.current

  val homeCurrentBooks by remember(playingBook, recentBooks) {
    derivedStateOf {
      buildList {
        playingBook?.let { current ->
          add(current.toRecentBook())
        }

        recentBooks.forEach { recent ->
          if (none { it.id == recent.id }) {
            add(recent)
          }
        }
      }
    }
  }

  fun showSearchTab() {
    previousBrowseTab = selectedTab.takeIf { it != LibraryTab.SEARCH } ?: previousBrowseTab
    preferencesExpanded = false
    selectedSeriesTitle = null
    selectedTab = LibraryTab.SEARCH
    libraryViewModel.requestSearch()
  }

  fun dismissSearchTab() {
    libraryViewModel.dismissSearch()
    selectedTab = previousBrowseTab
  }

  fun selectBrowseTab(tab: LibraryTab) {
    if (searchRequested) {
      libraryViewModel.dismissSearch()
    }

    preferencesExpanded = false
    if (tab != LibraryTab.SERIES) {
      selectedSeriesTitle = null
    }
    selectedTab = tab
    previousBrowseTab = tab
  }

  fun openSettingsTab() {
    if (searchRequested) {
      libraryViewModel.dismissSearch()
    }

    selectedSeriesTitle = null
    navController.showSettings()
  }

  val visibleTab =
    when {
      searchRequested -> LibraryTab.SEARCH
      else -> selectedTab
    }

  val seriesSelected = visibleTab == LibraryTab.SERIES
  val selectedSeries = selectedSeriesTitle?.let { title -> seriesShelves.firstOrNull { it.title == title } }
  val seriesDetailSelected = selectedSeries != null
  val homeSelected = visibleTab == LibraryTab.HOME
  val booksSelected = visibleTab == LibraryTab.BOOKS

  LaunchedEffect(visibleTab) {
    if (visibleTab == LibraryTab.HOME) {
      libraryViewModel.fetchHome()
    }

    if (visibleTab == LibraryTab.SERIES) {
      libraryViewModel.fetchSeries()
    }
  }

  fun isRecentVisible(): Boolean {
    val fetchAvailable = networkService.isNetworkAvailable() || cachingModelView.localCacheUsing()
    val hasContent = homeCurrentBooks.isEmpty().not()

    return searchRequested.not() && homeSelected && hasContent && fetchAvailable
  }

  val showScrollbar by remember {
    derivedStateOf {
      val scrolledDown = libraryListState.firstVisibleItemIndex > 0 || libraryListState.firstVisibleItemScrollOffset > 0
      libraryListState.isScrollInProgress && scrolledDown
    }
  }

  val scrollbarAlpha by animateFloatAsState(
    targetValue = if (showScrollbar) 1f else 0f,
    animationSpec = tween(durationMillis = 300),
  )

  LaunchedEffect(Unit) {
    val emptyContent = library.itemCount == 0
    val libraryChanged = currentLibraryId != settingsViewModel.fetchPreferredLibraryId()
    val orderingChanged = currentOrdering != settingsViewModel.fetchLibraryOrdering()

    val localCacheUsing = cachingModelView.localCacheUsing()
    val localCacheUpdated = cachingModelView.fetchLatestUpdate(currentLibraryId)?.let { it > localCacheUpdatedAt } ?: true

    if (emptyContent || libraryChanged || orderingChanged || (localCacheUsing && localCacheUpdated)) {
      libraryViewModel.refreshRecentListening()
      libraryViewModel.refreshLibrary()

      currentLibraryId = settingsViewModel.fetchPreferredLibraryId()
      currentOrdering = settingsViewModel.fetchLibraryOrdering()
      localCacheUpdatedAt = cachingModelView.fetchLatestUpdate(currentLibraryId) ?: 0L
    }

    playerViewModel.updatePlayingItem()
    settingsViewModel.refreshConnectionInfo()
    settingsViewModel.fetchLibraries()

    if (settingsViewModel.hasCredentials().not()) {
      navController.showLogin()
    }
  }

  fun provideLibraryTitle(): String {
    val type = libraryViewModel.fetchPreferredLibraryType()

    return when (type) {
      LibraryType.LIBRARY -> {
        libraryViewModel
          .fetchPreferredLibraryTitle()
          ?: context.getString(R.string.library_screen_library_title)
      }

      LibraryType.PODCAST -> {
        libraryViewModel
          .fetchPreferredLibraryTitle()
          ?: context.getString(R.string.library_screen_podcast_title)
      }

      LibraryType.UNKNOWN -> {
        ""
      }
    }
  }

  val navBarTitle by remember {
    derivedStateOf {
      if (seriesSelected) {
        return@derivedStateOf context.getString(R.string.library_tab_series)
      }

      if (homeSelected) {
        return@derivedStateOf context.getString(R.string.library_tab_home)
      }

      if (booksSelected) {
        return@derivedStateOf provideLibraryTitle()
      }

      val showRecent = isRecentVisible()
      val recentBlockVisible =
        libraryListState.layoutInfo.visibleItemsInfo
          .firstOrNull()
          ?.key == "recent_books"

      when {
        isPlaceholderRequired -> context.getString(R.string.library_screen_continue_listening_title)
        showRecent && recentBlockVisible -> context.getString(R.string.library_screen_continue_listening_title)
        else -> provideLibraryTitle()
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        actions = {
          if (!seriesDetailSelected) {
            AnimatedContent(
              targetState = searchRequested,
              label = "library_action_animation",
              transitionSpec = {
                fadeIn(animationSpec = keyframes { durationMillis = 150 }) togetherWith
                  fadeOut(animationSpec = keyframes { durationMillis = 150 })
              },
            ) { isSearchRequested ->
              when (isSearchRequested) {
                true -> {
                  LibrarySearchActionComposable(
                    onSearchDismissed = { dismissSearchTab() },
                    onSearchRequested = { libraryViewModel.updateSearch(it) },
                  )
                }

                false -> {
                  DefaultActionComposable(
                    isConnectedToServer = serverConnected,
                    onConnectionRequested = { navController.showConnectionSettings() },
                    onFiltersRequested = { preferencesExpanded = true },
                  )
                }
              }
            }
          }
        },
        title = {
          if (seriesDetailSelected) {
            Spacer(modifier = Modifier.fillMaxWidth())
          } else if (!searchRequested) {
            Row(
              modifier =
                when (navBarTitle) {
                  provideLibraryTitle() -> {
                    Modifier
                      .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                      ) { preferredLibraryExpanded = true }
                      .fillMaxWidth()
                  }

                  else -> {
                    Modifier.fillMaxWidth()
                  }
                },
            ) {
              Text(
                text = navBarTitle,
                style = titleTextStyle,
                maxLines = 1,
              )

              if (navBarTitle == provideLibraryTitle()) {
                LibrarySwitchComposable { preferredLibraryExpanded = true }
              }
            }
          }
        },
        navigationIcon = {
          if (seriesDetailSelected) {
            IconButton(onClick = { selectedSeriesTitle = null }) {
              Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
              )
            }
          }
        },
        modifier = Modifier.systemBarsPadding(),
      )
    },
    bottomBar = {
      Column {
        playingBook?.let {
          MiniPlayerComposable(
            navController = navController,
            book = it,
            imageLoader = imageLoader,
            playerViewModel = playerViewModel,
          )
        }

        Surface(shadowElevation = 4.dp) {
          LibraryBottomBarComposable(
            selectedTab = visibleTab,
            onTabSelected = { tab ->
              when (tab) {
                LibraryTab.HOME,
                LibraryTab.BOOKS,
                LibraryTab.SERIES,
                -> selectBrowseTab(tab)

                LibraryTab.SEARCH -> showSearchTab()

                LibraryTab.SETTINGS -> openSettingsTab()
              }
            },
          )
        }
      }
    },
    modifier =
      Modifier
        .systemBarsPadding()
        .fillMaxSize(),
    content = { innerPadding ->
      Box(
        modifier =
          Modifier
            .padding(innerPadding)
            .fillMaxSize(),
      ) {
        if (seriesSelected) {
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
          ) {
            LazyColumn(
              state = libraryListState,
              modifier =
                Modifier
                  .fillMaxSize()
                  .imePadding(),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
              when {
                seriesLoading -> {
                  item { LibraryPlaceholderComposable(itemCount = 10) }
                }

                seriesLoadFailed -> {
                  item {
                    LibraryFallbackComposable(
                      searchRequested = false,
                      contentCachingModelView = cachingModelView,
                      networkService = networkService,
                      libraryViewModel = libraryViewModel,
                    )
                  }
                }

                seriesShelves.isEmpty() -> {
                  item {
                    SeriesFallbackComposable(
                      modifier = Modifier.fillMaxWidth(),
                      isPodcastLibrary = libraryViewModel.fetchPreferredLibraryType() == LibraryType.PODCAST,
                    )
                  }
                }

                seriesDetailSelected -> {
                  val shelf = selectedSeries

                  item(key = "series_detail_header_${shelf.title}") {
                    SeriesDetailHeaderComposable(
                      shelf = shelf,
                      onStartListening = {
                        val firstBook = shelf.books.firstOrNull { !it.isFinished } ?: shelf.books.firstOrNull()

                        firstBook?.let {
                          navController.showPlayer(
                            bookId = it.id,
                            bookTitle = it.title,
                            bookSubtitle = it.subtitle,
                            startInstantly = true,
                          )
                        }
                      },
                    )
                  }

                  item(key = "series_detail_grid_${shelf.title}") {
                    SeriesDetailGridComposable(
                      shelf = shelf,
                      imageLoader = imageLoader,
                      onBookSelected = { book ->
                        navController.showPlayer(
                          bookId = book.id,
                          bookTitle = book.title,
                          bookSubtitle = book.subtitle,
                        )
                      },
                    )
                  }
                }

                else -> {
                  item(key = "series_summary") {
                    SeriesOverviewSummaryComposable(
                      seriesCount = seriesShelves.size,
                      totalBooks = seriesShelves.sumOf { it.books.size },
                    )
                  }

                  items(
                    items = seriesShelves,
                    key = { shelf -> "series_shelf_${shelf.title}" },
                  ) { shelf ->
                    SeriesShelfComposable(
                      shelf = shelf,
                      imageLoader = imageLoader,
                      onSelected = { selectedSeriesTitle = shelf.title },
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                  }
                }
              }
            }

            PullRefreshIndicator(
              refreshing = pullRefreshing || seriesLoading,
              state = pullRefreshState,
              contentColor = colorScheme.primary,
              backgroundColor = colorScheme.surfaceContainer,
              modifier = Modifier.align(Alignment.TopCenter),
            )
          }
        } else if (homeSelected) {
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
          ) {
            LazyColumn(
              state = libraryListState,
              modifier = Modifier.fillMaxSize().imePadding(),
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
              homeSummary.stats?.let { stats ->
                item(key = "home_stats") {
                  HomeStatsComposable(stats = stats)
                  Spacer(modifier = Modifier.height(24.dp))
                }
              }

              when {
                isHomePlaceholderRequired -> {
                  item(key = "home_currently_listening_title") {
                    HomeSectionHeaderComposable(
                      title = stringResource(R.string.home_section_currently_listening),
                    )
                  }

                  item(key = "home_currently_listening_placeholder") {
                    RecentBooksPlaceholderComposable(
                      libraryViewModel = libraryViewModel,
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                  }
                }

                isRecentVisible() -> {
                  item(key = "home_currently_listening_title") {
                    HomeSectionHeaderComposable(
                      title = stringResource(R.string.home_section_currently_listening),
                    )
                  }

                  item(key = "home_currently_listening") {
                    RecentBooksComposable(
                      navController = navController,
                      recentBooks = homeCurrentBooks,
                      imageLoader = imageLoader,
                      libraryViewModel = libraryViewModel,
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                  }
                }
              }

              if (homeSummary.recentlyAdded.isNotEmpty()) {
                item(key = "home_recently_added") {
                  HomeBookShelfComposable(
                    title = stringResource(R.string.home_section_recently_added),
                    books = homeSummary.recentlyAdded,
                    imageLoader = imageLoader,
                    navController = navController,
                  )

                  Spacer(modifier = Modifier.height(24.dp))
                }
              }

              if (homeSummary.unplayed.isNotEmpty()) {
                item(key = "home_unplayed") {
                  HomeBookShelfComposable(
                    title = stringResource(R.string.home_section_unplayed),
                    books = homeSummary.unplayed,
                    imageLoader = imageLoader,
                    navController = navController,
                  )
                }
              }

              if (
                !isHomePlaceholderRequired &&
                !isRecentVisible() &&
                homeSummary.recentlyAdded.isEmpty() &&
                homeSummary.unplayed.isEmpty()
              ) {
                item(key = "home_fallback") {
                  LibraryFallbackComposable(
                    searchRequested = false,
                    contentCachingModelView = cachingModelView,
                    networkService = networkService,
                    libraryViewModel = libraryViewModel,
                  )
                }
              }
            }

            PullRefreshIndicator(
              refreshing = pullRefreshing || homeLoading,
              state = pullRefreshState,
              contentColor = colorScheme.primary,
              backgroundColor = colorScheme.surfaceContainer,
              modifier = Modifier.align(Alignment.TopCenter),
            )
          }
        } else {
          Box(
            modifier =
              Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
          ) {
            LazyColumn(
              state = libraryListState,
              modifier =
                Modifier
                  .fillMaxSize()
                  .imePadding()
                  .withScrollbar(
                    state = libraryListState,
                    color = colorScheme.onBackground.copy(alpha = scrollbarAlpha),
                    totalItems = libraryCount,
                    ignoreItems = listOf("recent_books", "library_title"),
                  ),
              contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
              item(key = "recent_books") {
                val showRecent = isRecentVisible()

                when {
                  isPlaceholderRequired -> {
                    RecentBooksPlaceholderComposable(
                      libraryViewModel = libraryViewModel,
                    )
                  }

                  showRecent -> {
                    RecentBooksComposable(
                      navController = navController,
                      recentBooks = recentBooks,
                      imageLoader = imageLoader,
                      libraryViewModel = libraryViewModel,
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                  }
                }
              }

              item(key = "library_title") {
                if (!searchRequested && isRecentVisible()) {
                  AnimatedContent(
                    targetState = navBarTitle,
                    transitionSpec = {
                      fadeIn(
                        animationSpec =
                          tween(300),
                      ) togetherWith
                        fadeOut(
                          animationSpec =
                            tween(
                              300,
                            ),
                        )
                    },
                    label = "library_header_fade",
                  ) {
                    when {
                      it == provideLibraryTitle() -> {
                        Spacer(
                          modifier =
                            Modifier
                              .fillMaxWidth()
                              .height(titleHeightDp),
                        )
                      }

                      else -> {
                        if (isPlaceholderRequired.not()) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                              Modifier
                                .clickable(
                                  interactionSource = remember { MutableInteractionSource() },
                                  indication = null,
                                ) { preferredLibraryExpanded = true }
                                .fillMaxWidth(),
                          ) {
                            Text(
                              style = titleTextStyle,
                              text = provideLibraryTitle(),
                            )

                            LibrarySwitchComposable { preferredLibraryExpanded = true }
                          }
                        }
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))
              }

              when {
                isPlaceholderRequired -> {
                  item { LibraryPlaceholderComposable() }
                }

                library.itemCount == 0 -> {
                  item {
                    LibraryFallbackComposable(
                      searchRequested = searchRequested,
                      contentCachingModelView = cachingModelView,
                      networkService = networkService,
                      libraryViewModel = libraryViewModel,
                    )
                  }
                }

                else -> {
                  items(count = library.itemCount, key = { "library_item_$it" }) {
                    val book = library[it] ?: return@items

                    BookComposable(
                      book = book,
                      imageLoader = imageLoader,
                      navController = navController,
                    )
                  }
                }
              }
            }

            if (!searchRequested) {
              PullRefreshIndicator(
                refreshing = pullRefreshing,
                state = pullRefreshState,
                contentColor = colorScheme.primary,
                backgroundColor = colorScheme.surfaceContainer,
                modifier = Modifier.align(Alignment.TopCenter),
              )
            }
          }
        }
      }
    },
  )

  if (preferredLibraryExpanded) {
    PreferredLibrarySettingComposable(
      libraries = libraries,
      preferredLibrary = preferredLibrary,
      onDismissRequest = { preferredLibraryExpanded = false },
      onItemSelected = {
        settingsViewModel.preferLibrary(it)
        currentLibraryId = settingsViewModel.fetchPreferredLibraryId()
        selectedSeriesTitle = null
        refreshContent(false)

        playerViewModel.updatePlayingItem()
        preferredLibraryExpanded = false
      },
    )
  }

  if (preferencesExpanded) {
    LibrarySettingsComposable(
      onDismissRequest = { preferencesExpanded = false },
      onForceLocalToggled = {
        cachingModelView.toggleCacheForce()
        playerViewModel.book.value?.let { playerViewModel.preparePlayback(it.id) }
        refreshContent(showPullRefreshing = false)
      },
      onHideCompletedToggled = {
        settingsViewModel.toggleHideCompleted()
        playerViewModel.book.value?.let { playerViewModel.preparePlayback(it.id) }
        refreshContent(showPullRefreshing = false)
      },
    )
  }
}

@Composable
private fun SeriesFallbackComposable(
  isPodcastLibrary: Boolean,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.padding(horizontal = 24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text =
          stringResource(
            if (isPodcastLibrary) R.string.series_podcast_title else R.string.series_empty_title,
          ),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text =
          stringResource(
            if (isPodcastLibrary) R.string.series_podcast_description else R.string.series_empty_description,
          ),
        style = MaterialTheme.typography.bodyLarge,
        color = colorScheme.onBackground.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
      )
    }
  }
}

private fun DetailedItem.toRecentBook(): RecentBook {
  val totalDuration = chapters.sumOf { it.duration }
  val currentTime = progress?.currentTime ?: 0.0

  val listenedPercentage =
    when {
      totalDuration <= 0.0 -> null
      else -> ((currentTime / totalDuration) * 100).toInt().coerceIn(0, 100)
    }

  return RecentBook(
    id = id,
    title = title,
    subtitle = subtitle,
    author = author,
    listenedPercentage = listenedPercentage,
    listenedLastUpdate = progress?.lastUpdate,
  )
}
