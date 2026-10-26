package com.example.catering.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DishSearchTest {
    private fun dish(name: String, info: String = "", category: String = "") = Dish(
        id = 1, name = name, price = 500, info = info, imageUrl = "", weight = 300,
        calories = 400, proteins = 20.0, fats = 15.0, carbs = 40.0, category = category
    )

    @Test fun `Russian search ignores case whitespace and yo variation`() {
        assertTrue(dish("Запечённая рыба").matchesSearch(" ЗАПЕЧЕННАЯ   рыба "))
    }

    @Test fun `Latin query finds Russian names and common spelling`() {
        assertTrue(dish("Паста с курицей").matchesSearch("pasta kuritsey"))
        assertTrue(dish("Пицца Маргарита").matchesSearch("pizza"))
        assertTrue(dish("Цезарь с курицей").matchesSearch("caesar"))
    }

    @Test fun `English names remain searchable and Cyrillic transliteration works`() {
        assertTrue(dish("Pasta Carbonara").matchesSearch("PASTA carbon"))
        assertTrue(dish("Pasta Carbonara").matchesSearch("паста"))
    }

    @Test fun `search combines name description and category but requires all terms`() {
        val dish = dish("Цезарь", "С курицей и сыром", "Салаты")
        assertTrue(dish.matchesSearch("salaty курицей"))
        assertFalse(dish.matchesSearch("курицей лососем"))
        assertFalse(dish.matchesSearch("pizza"))
    }

    @Test fun `empty search returns every dish and punctuation separates terms`() {
        assertTrue(dish("Паста").matchesSearch("  "))
        assertTrue(dish("Паста с сыром").matchesSearch("pasta,syrom"))
    }
}
