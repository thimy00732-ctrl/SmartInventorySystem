package com.sis.android.data.remote

import com.google.gson.Gson
import com.sis.android.BuildConfig
import com.sis.android.data.model.RefreshRequest
import com.sis.android.data.session.SessionManager
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun create(sessionManager: SessionManager): ApiService {
        val converter = GsonConverterFactory.create(Gson())
        val base = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(converter)
            .build()

        val refreshApi = base.create(ApiService::class.java)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = sessionManager.get()?.accessToken
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) header("Authorization", "Bearer $token")
                }.build()
                chain.proceed(request)
            }
            .authenticator(SessionAuthenticator(sessionManager, refreshApi))
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(converter)
            .build()
            .create(ApiService::class.java)
    }

    private class SessionAuthenticator(
        private val sessions: SessionManager,
        private val refreshApi: ApiService
    ) : Authenticator {
        @Synchronized
        override fun authenticate(route: Route?, response: Response): Request? {
            if (responseCount(response) >= 2) return null
            val current = sessions.get() ?: return null
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (requestToken != current.accessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${current.accessToken}")
                    .build()
            }
            return try {
                val refreshed = refreshApi.refresh(RefreshRequest(current.refreshToken)).execute()
                if (!refreshed.isSuccessful) {
                    sessions.clear()
                    null
                } else {
                    val token = refreshed.body() ?: return null
                    sessions.save(
                        current.copy(
                            accessToken = token.accessToken,
                            refreshToken = token.refreshToken,
                            expiresIn = token.expiresIn,
                            user = token.user
                        )
                    )
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${token.accessToken}")
                        .build()
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }
}