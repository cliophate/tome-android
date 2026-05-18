package org.cliophate.tome.ui.screens.library.model

import org.cliophate.tome.lib.domain.Book

data class HomeSummary(
  val stats: ListeningStats?,
  val recentlyAdded: List<Book>,
  val unplayed: List<Book>,
)

data class ListeningStats(
  val todaySeconds: Int,
  val streakDays: Int,
)
