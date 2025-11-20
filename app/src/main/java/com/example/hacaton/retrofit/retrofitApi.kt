package com.example.hacaton.retrofit

import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Data models for Request and Response
data class ExplanationRequest(val text: String)

data class ExplanationResponse(
    val title: String,
    val summary: String
)

// Retrofit Interface
interface ExplanationApi {
    @POST("explain") // Placeholder endpoint
    suspend fun explainText(@Body request: ExplanationRequest): ExplanationResponse
}

// Retrofit Client
object RetrofitClient {
    // IMPORTANT: Replace with your real server URL
    private const val BASE_URL = "https://7c0980da5b76.ngrok-free.app/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS) // Время на соединение с сервером
        .readTimeout(120, TimeUnit.SECONDS)    // Время ожидания ответа (важно для AI/тяжелых задач)
        .writeTimeout(120, TimeUnit.SECONDS)   // Время на отправку запроса
        .protocols(listOf(Protocol.HTTP_1_1))
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                // Этот заголовок говорит ngrok пропустить страницу-предупреждение
                .header("ngrok-skip-browser-warning", "true")
                .method(original.method(), original.body())
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: ExplanationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExplanationApi::class.java)
    }
}
