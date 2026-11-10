package com.sis.android.di

import android.content.Context
import com.sis.android.data.remote.RetrofitClient
import com.sis.android.data.repository.AuthRepository
import com.sis.android.data.repository.InventoryRepository
import com.sis.android.data.repository.StaffManagementRepository
import com.sis.android.data.session.SessionManager

/**
 * AppModule: cung cấp các dependency dùng chung toàn app.
 * Dùng pattern singleton đơn giản (không dùng Hilt/Koin để tránh phức tạp).
 */
object AppModule {

    @Volatile
    private var sessionManager: SessionManager? = null

    @Volatile
    private var authRepository: AuthRepository? = null

    @Volatile
    private var staffRepository: StaffManagementRepository? = null

    @Volatile
    private var inventoryRepository: InventoryRepository? = null

    fun provideSessionManager(context: Context): SessionManager {
        return sessionManager ?: synchronized(this) {
            sessionManager ?: SessionManager(context.applicationContext).also {
                sessionManager = it
            }
        }
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: AuthRepository(
                sessionManager = provideSessionManager(context),
                apiService = RetrofitClient.apiService
            ).also { authRepository = it }
        }
    }

    fun provideStaffRepository(): StaffManagementRepository {
        return staffRepository ?: synchronized(this) {
            staffRepository ?: StaffManagementRepository(
                apiService = RetrofitClient.apiService
            ).also { staffRepository = it }
        }
    }

    fun provideInventoryRepository(): InventoryRepository {
        return inventoryRepository ?: synchronized(this) {
            inventoryRepository ?: InventoryRepository(
                apiService = RetrofitClient.apiService
            ).also { inventoryRepository = it }
        }
    }
}
