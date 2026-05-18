package org.cliophate.tome.lib.domain

import java.io.Serializable

sealed interface TimerOption : Serializable

class DurationTimerOption(
  val duration: Int,
) : TimerOption

data object CurrentEpisodeTimerOption : TimerOption
