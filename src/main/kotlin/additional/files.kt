package additional

import java.io.File

fun main() {
    var outputWasSelected = false

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    val dictionary = mutableListOf<Word>()

    val lines: List<String> = wordsFile.readLines()

    for (i in lines) {
        val line = i.split("|")
        dictionary.add(Word(line[0].trim(), line[1].trim(), line[2].trim().toIntOrNull() ?: 0))
    }

    while (!outputWasSelected) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        when (readln().toIntOrNull()) {
            null -> println("Некорректный ввод, попробуйте ещё раз")
            1 -> println("Выбран вариант 1")
            2 -> {
                val learnedWords = dictionary.filter { it.correctAnswer >= 3 }
                println("Выучено ${learnedWords.size} из ${dictionary.size} слов | ${learnedWords.size * (100 / dictionary.size)}%")
                println()
            }

            0 -> outputWasSelected = true
        }
    }
}


data class Word(
    val text: String,
    val translate: String,
    val correctAnswer: Int,
) {
    override fun toString(): String {
        return "Word(text= '$text', translate= '$translate', correctAnswer= '$correctAnswer')"
    }
}
