package com.denis.dnschat.interfaces

import com.denis.dnschat.model.RequestBody
import com.denis.dnschat.model.GeneratedAnswer
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface AIAPIService {
    @Headers("Content-Type: application/json")
    @POST("completions")
    suspend fun getPrompt(
        @Header("Authorization") apiKey: String,
        @Body request: RequestBody
    ): GeneratedAnswer
}
