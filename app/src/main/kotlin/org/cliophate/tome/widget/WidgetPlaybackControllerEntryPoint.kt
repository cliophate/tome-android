package org.cliophate.tome.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetPlaybackControllerEntryPoint {
  fun widgetPlaybackController(): WidgetPlaybackController
}
