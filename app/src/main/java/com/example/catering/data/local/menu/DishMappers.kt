package com.example.catering.data.local.menu

import com.example.catering.domain.model.Dish

fun dishToDishEntity(dish: Dish): DishEntity {
    return DishEntity(
        id = dish.id,
        name = dish.name,
        price = dish.price,
        info = dish.info,
        imageUrl = dish.imageUrl,
        weight = dish.weight,
        calories = dish.calories,
        proteins = dish.proteins,
        fats = dish.fats,
        carbs = dish.carbs,
        category = dish.category
    )
}

fun dishEntityToDish(dishEntity: DishEntity): Dish {
    return Dish(
        id = dishEntity.id,
        name = dishEntity.name,
        price = dishEntity.price,
        info = dishEntity.info,
        imageUrl = dishEntity.imageUrl,
        weight = dishEntity.weight,
        calories = dishEntity.calories,
        proteins = dishEntity.proteins,
        fats = dishEntity.fats,
        carbs = dishEntity.carbs,
        category = dishEntity.category
    )
}

fun dishesToDishEntities(dishes: List<Dish>): List<DishEntity> {
    return dishes.map { dish ->
        dishToDishEntity(dish)
    }
}

fun dishEntitiesToDishes(dishEntities: List<DishEntity>): List<Dish> {
    return dishEntities.map { dishEntity ->
        dishEntityToDish(dishEntity)
    }
}