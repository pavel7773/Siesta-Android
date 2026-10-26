package com.example.catering.domain.model

sealed class OrderException(message: String) : Exception(message) {

    class RequestRejected(message: String) : OrderException(message)

    class SessionExpired :
        OrderException("Сессия истекла. Войдите снова")

    class NotFound :
        OrderException("Заказ не найден")

    class Conflict(message: String) :
        OrderException(message)

    class Network :
        OrderException("Проверьте подключение к интернету")

    class Unknown :
        OrderException("Произошла ошибка")
}
