package org.cliophate.tome.channel.audiobookshelf.common.api

import org.cliophate.tome.channel.common.USER_AGENT
import org.cliophate.tome.lib.domain.connection.ServerRequestHeader
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestHeadersProvider
  @Inject
  constructor(
    private val preferences: TomeSharedPreferences,
  ) {
    fun fetchRequestHeaders(): List<ServerRequestHeader> {
      val usersHeaders = preferences.getCustomHeaders()

      val userAgent = ServerRequestHeader("User-Agent", USER_AGENT)
      return usersHeaders + userAgent
    }
  }
