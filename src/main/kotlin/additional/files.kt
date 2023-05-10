package additional

import java.io.File

fun main() {

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()

    val dictionary = mutableListOf<Word>()

    val lines: List<String> = wordsFile.readLines()

    for (i in lines) {
        val line = i.split("|")
        dictionary.add(Word(line[0].trim(), line[1].trim(), line[2].trim().toIntOrNull() ?: 0))
    }

    dictionary.forEach { println(it) }
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
