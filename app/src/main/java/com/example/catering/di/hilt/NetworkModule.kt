package com.example.catering.di.hilt

import com.example.catering.data.remote.interceptor.AuthInterceptor
import com.example.catering.data.remote.menu.MenuApi
import com.example.catering.data.remote.order.OrderApi
import com.example.catering.data.remote.user.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BACKEND_BASE_URL = "https://cateringbackend-production-13ea.up.railway.app/"

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }
    @Provides
    @Singleton
    @BackendRetrofit
    fun provideBackendRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    @Provides
    @Singleton
    fun provideMenuApi(
        @BackendRetrofit retrofit: Retrofit
    ): MenuApi {
        return retrofit.create(MenuApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(@BackendRetrofit retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOrderApi(
        @BackendRetrofit retrofit: Retrofit
    ): OrderApi {
        return retrofit.create(OrderApi::class.java)
    }
}