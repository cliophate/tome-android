package org.cliophate.tome.channel.audiobookshelf.common.converter

import org.cliophate.tome.channel.audiobookshelf.common.model.user.LoggedUserResponse
import org.cliophate.tome.lib.domain.UserAccount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginResponseConverter
  @Inject
  constructor() {
    fun apply(response: LoggedUserResponse): UserAccount =
      UserAccount(
        token = response.user.token,
        accessToken = response.user.accessToken,
        refreshToken = response.user.refreshToken,
        username = response.user.username,
        preferredLibraryId = response.userDefaultLibraryId,
      )
  }
