package com.denis.dnschat.network
import com.denis.dnschat.interfaces.AIAPIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkUtils {
    private const val BASE_URL = "https://api.openai.com/v1/"

    fun createApiService(): AIAPIService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AIAPIService::class.java)
    }
}
