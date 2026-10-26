package com.example.catering.data.remote.menu

import com.example.catering.domain.model.Dish
import javax.inject.Inject


class MenuRemoteDataSource @Inject constructor(private val cateringApi: MenuApi) {

    suspend fun getMenu(): List<Dish> {
        return cateringApi.getMenu()
    }
}