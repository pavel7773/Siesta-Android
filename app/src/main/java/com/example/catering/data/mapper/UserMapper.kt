package com.example.catering.data.mapper

import com.example.catering.data.remote.dto.user.UserResponse
import com.example.catering.domain.model.User
import javax.inject.Inject

class UserMapper @Inject constructor() {

    fun toUser(userResponse: UserResponse): User {
        return User(
            id = userResponse.id,
            name = userResponse.name,
            phone = userResponse.phone,
            address = userResponse.address
        )
    }
}