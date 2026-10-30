package com.sis.android.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Nếu chạy trên Android Emulator, localhost của máy tính là 10.0.2.2
    // Nếu chạy thiết bị thật qua cáp/Wi-Fi, thay bằng IP LAN của máy tính (VD: 192.168.1.x:8080)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}