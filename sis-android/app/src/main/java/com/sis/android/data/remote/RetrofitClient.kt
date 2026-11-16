package com.sis.android.data.remote

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * Base URL của backend Ktor.
     * - Máy ảo Android Emulator: 10.0.2.2 trỏ đến localhost của máy host
     * - Thiết bị thật: thay bằng IP của máy chủ trên cùng mạng LAN (VD: "http://192.168.1.x:8080/")
     */
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Lưu context để có thể lấy token động
    private var appContext: Context? = null

    /**
     * Khởi tạo RetrofitClient với context
     * Gọi hàm này trong Application.onCreate() hoặc MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Auth Interceptor - Tự động thêm JWT token vào header
     * Token được lấy ĐỘNG từ SharedPreferences mỗi khi gọi API
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()
        
        // Các endpoint không cần token
        val publicEndpoints = listOf(
            "/api/auth/login",
            "/api/auth/logout", 
            "/health"
        )
        
        // Kiểm tra xem endpoint có cần token không
        val isPublicEndpoint = publicEndpoints.any { requestUrl.contains(it) }
        
        if (isPublicEndpoint) {
            // Public endpoint, không cần token
            android.util.Log.d("RetrofitAuth", "🔓 Public endpoint: $requestUrl")
            chain.proceed(originalRequest)
        } else {
            // Protected endpoint, lấy token ĐỘNG từ SharedPreferences
            val token = appContext?.getSharedPreferences("sis_session", Context.MODE_PRIVATE)
                ?.getString("token", null)
            
            if (token != null) {
                android.util.Log.d("RetrofitAuth", "🔐 Protected endpoint với token: $requestUrl")
                android.util.Log.d("RetrofitAuth", "Token: ${token.take(20)}...")
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                // Không có token
                android.util.Log.e("RetrofitAuth", "❌ Không có token cho protected endpoint: $requestUrl")
                chain.proceed(originalRequest)
            }
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)      // Thêm token vào header
        .addInterceptor(loggingInterceptor)   // Log request/response
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = instance.create(ApiService::class.java)
}

// Alias for backward compatibility
object RetrofitInstance {
    val api: ApiService = RetrofitClient.apiService
}
