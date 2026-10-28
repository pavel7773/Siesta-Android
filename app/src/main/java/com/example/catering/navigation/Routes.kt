package com.example.catering.navigation

import kotlinx.serialization.Serializable

sealed interface Route // Маршруты навигации

@Serializable
object Home : Route

@Serializable
object Cart : Route

@Serializable
data class DishDetail(val dishId: Int): Route

@Serializable
object Menu : Route

@Serializable
object Checkout : Route

@Serializable
object OrderSuccess: Route

@Serializable
object OrdersHistory: Route

@Serializable
object Support: Route

@Serializable
object Contacts: Route

@Serializable
object Register: Route

@Serializable
object Login: Route

@Serializable
object Profile: Route

@Serializable
object Favorites: Route

@Serializable
object RecentlyViewed: Route

@Serializable
data class PhoneVerification(
    val phone: String,
    val codeSent: Boolean
) : Route