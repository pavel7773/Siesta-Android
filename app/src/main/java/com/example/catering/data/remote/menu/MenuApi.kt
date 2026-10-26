package com.example.catering.data.remote.menu

import com.example.catering.domain.model.Dish
import retrofit2.http.GET

// Это наш список поручений для Retrofit
interface MenuApi { // Сюда мы вставляем "хвостик" нашей вечной ссылки (БЕЗ начального домена)
    @GET("menu")
    suspend fun getMenu(): List<Dish>
}