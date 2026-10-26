package com.example.catering.domain.model

import java.util.Locale

/** Local search accepts either alphabet; this is transliteration, not translation. */
fun Dish.matchesSearch(query: String): Boolean {
    val terms = searchWords(query)
    if (terms.isEmpty()) return true
    val words = searchWords("$name $info $category")
    return terms.all { term -> words.any { word -> word.contains(term) } }
}

private val wordSeparators = Regex("[^\\p{L}\\p{N}]+")
private val russianLetters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя"
private val latinLetters = listOf(
    "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m",
    "n", "o", "p", "r", "s", "t", "u", "f", "kh", "ts", "ch", "sh", "shch",
    "", "y", "", "e", "yu", "ya"
)

private fun searchWords(text: String): List<String> =
    text.lowercase(Locale.ROOT).split(wordSeparators).filter { it.isNotEmpty() }.map { word ->
        val transliterated = buildString {
            word.forEach { letter ->
                val index = russianLetters.indexOf(letter)
                if (index >= 0) append(latinLetters[index]) else append(letter)
            }
        }
        // Common spelling of the same dish, rather than an arbitrary English translation.
        when (transliterated) {
            "pizza" -> "pitstsa"
            "caesar" -> "tsezar"
            else -> transliterated
        }
    }.filter { it.isNotEmpty() }
