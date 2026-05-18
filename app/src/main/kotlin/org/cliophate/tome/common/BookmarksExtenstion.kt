package org.cliophate.tome.common

import org.cliophate.tome.ui.extensions.formatTime

fun buildBookmarkTitle(
  currentChapterTitle: String,
  currentChapterPosition: Double,
): String = "$currentChapterTitle - ${currentChapterPosition.toInt().formatTime()}"
