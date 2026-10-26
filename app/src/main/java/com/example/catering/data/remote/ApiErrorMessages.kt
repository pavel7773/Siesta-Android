package com.example.catering.data.remote

import com.example.catering.domain.model.UserException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

internal enum class UserApiOperation { LOGIN, REGISTER, PROFILE }

/** Raw server bodies and exception messages must never become UI text. */
internal object ApiErrorMessages {
    const val UNAVAILABLE = "Сервис временно недоступен. Попробуйте позже"
    const val RATE_LIMIT = "Слишком много попыток. Подождите немного и попробуйте снова"
    const val FORBIDDEN = "Это действие недоступно для вашего аккаунта"

    val registration = setOf(
        "Имя должно содержать от 3 до 100 символов",
        "Введите корректный номер телефона",
        "Пароль должен содержать от 6 до 128 символов"
    )
    val profile = setOf(
        "Имя должно содержать от 3 до 100 символов",
        "Адрес не должен превышать 255 символов",
        "Нет данных для изменения"
    )
    val order = setOf(
        "Корзина должна содержать от 1 до 100 позиций",
        "Некорректное количество блюда",
        "Комментарий не должен превышать 500 символов",
        "Введите корректный номер телефона",
        "Имя должно содержать от 3 до 100 символов",
        "Укажите адрес доставки",
        "Адрес не должен превышать 300 символов",
        "Блюдо не найдено",
        "Промокод доступен от 1000 ₽",
        "Промокод доступен от 1500 ₽",
        "Промокод доступен от 3000 ₽",
        "Промокод больше не активен",
        "Неверный промокод"
    )
    val cancellation = setOf(
        "Заказ уже отменён",
        "Заказ уже нельзя отменить",
        "Статус заказа изменился. Повторите запрос",
        "Заказ нельзя отменить"
    )

    fun validation(error: HttpException, allowed: Set<String>, fallback: String): String {
        val message = try {
            error.response()?.errorBody()?.use { body ->
                // Read at most 1 KiB; accept only exact messages from the known API contract.
                val source = body.source()
                source.request(1025)
                if (source.buffer.size > 1024) null else source.readUtf8().trim()
            }
        } catch (_: IOException) {
            null
        }
        return message?.takeIf { it in allowed } ?: fallback
    }
}

internal suspend fun <T> userApiCall(operation: UserApiOperation, request: suspend () -> T): T {
    try {
        return request()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        if (e.code() == 401 && operation == UserApiOperation.PROFILE) {
            throw UserException.SessionExpired()
        }
        val message = when (e.code()) {
            400, 422 -> when (operation) {
                UserApiOperation.LOGIN -> "Проверьте номер телефона и пароль"
                UserApiOperation.REGISTER -> ApiErrorMessages.validation(
                    e, ApiErrorMessages.registration, "Проверьте имя, номер телефона и пароль"
                )
                UserApiOperation.PROFILE -> ApiErrorMessages.validation(
                    e, ApiErrorMessages.profile, "Проверьте данные профиля"
                )
            }
            401 -> if (operation == UserApiOperation.LOGIN) "Неверный телефон или пароль"
                else "Не удалось зарегистрироваться. Попробуйте позже"
            403 -> ApiErrorMessages.FORBIDDEN
            408, 504 -> "Сервер не ответил вовремя. Попробуйте ещё раз"
            409 -> if (operation == UserApiOperation.REGISTER)
                "Этот номер уже зарегистрирован. Войдите или восстановите пароль"
                else "Данные изменились. Обновите экран и попробуйте снова"
            429 -> ApiErrorMessages.RATE_LIMIT
            in 500..599 -> ApiErrorMessages.UNAVAILABLE
            else -> "Не удалось выполнить действие. Попробуйте позже"
        }
        throw UserException.RequestRejected(message)
    } catch (e: IOException) {
        throw UserException.Network()
    } catch (e: UserException) {
        throw e
    } catch (e: Exception) {
        throw UserException.RequestRejected("Не удалось выполнить действие. Попробуйте позже")
    }
}
