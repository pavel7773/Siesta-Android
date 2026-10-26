package com.example.catering.domain.model

sealed class UserException(message: String) : Exception(message) {

    // Only application-owned, user-readable messages from the data layer.
    class RequestRejected(message: String) : UserException(message)

    class SessionExpired :
        UserException("Сессия истекла. Войдите снова")

    class Network :
        UserException("Не удалось подключиться к серверу")

    class Unknown :
        UserException("Не удалось проверить сессию")
}
