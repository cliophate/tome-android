package org.cliophate.tome.channel.common

import android.content.Context
import com.squareup.moshi.Moshi
import org.cliophate.tome.lib.domain.connection.ServerRequestHeader
import org.cliophate.tome.lib.domain.fixUriScheme
import org.cliophate.tome.persistence.preferences.TomeSharedPreferences
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ApiClient(
  host: String,
  requestHeaders: List<ServerRequestHeader>?,
  preferences: TomeSharedPreferences,
  context: Context,
) {
  private val httpClient = createOkHttpClient(requestHeaders, preferences = preferences, context = context)

  val retrofit: Retrofit? =
    runCatching {
      Retrofit
        .Builder()
        .baseUrl(host.fixUriScheme())
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
    }.getOrNull()

  companion object {
    private val moshi: Moshi =
      Moshi
        .Builder()
        .build()
  }
}
