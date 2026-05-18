package org.cliophate.tome.playback

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cliophate.tome.common.RunningComponent

@Module
@InstallIn(SingletonComponent::class)
interface PlaybackEnhancerModule {
  @Binds
  @IntoSet
  fun bindPlaybackEnhancerService(service: PlaybackEnhancerService): RunningComponent
}
