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

    while (true) {

        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        var translatedWord: Word

        when (readln().toIntOrNull()) {
            null -> println("Некорректный ввод, попробуйте ещё раз")

            1 -> while (true) {

                var unexploredWords = dictionary.filter { it.correctAnswer < 3 }

                if (unexploredWords.isEmpty()) println("Вы выучили все слова")
                else {
                    unexploredWords = makeNewRoundListening(unexploredWords, dictionary)

                    translatedWord = unexploredWords[0]
                    unexploredWords = unexploredWords.shuffled()
                    println("Выберите перевод слова: ${translatedWord.text}")
                    printVariantsWords(unexploredWords)

                    when (val answer = readLine()?.toIntOrNull()) {

                        null -> println("Некорректный ввод, попробуйте ещё раз")

                        0 -> break

                        else -> {

                            if (unexploredWords[answer - 1].hashCode() == translatedWord.hashCode()) {
                                println("Вы ответили правильно")
                                val correctAnswerIndex = dictionary.indexOf(translatedWord)
                                dictionary[correctAnswerIndex].correctAnswer += 1
                                saveDictionary(dictionary)
                                continue
                            } else println("Ответ не верный")
                            continue
                        }
                    }
                }
            }

            2 -> {
                val learnedWords = dictionary.filter { it.correctAnswer >= 3 }
                println("Выучено ${learnedWords.size} из ${dictionary.size} слов | ${learnedWords.size * (100 / dictionary.size)}%\n")
            }

            0 -> break

        }
    }
}

/**
Function for saving the response result
 */
fun saveDictionary(dictionary: List<Word>) {
    val file = File("words.txt")
    file.delete()
    // dictionary.forEach { file.writeText(it.toStr()) }  //Почему у меня не работает запись через writeText я разобраться не смог.
    dictionary.forEach { file.appendText(it.wordToString()) }
}

/**
Function for printing a set of responses
 */
fun printVariantsWords(words: List<Word>) {
    println(
        """
                                Варианты:
                                 1 - ${words[0].translate}
                                 2 - ${words[1].translate}
                                 3 - ${words[2].translate}
                                 4 - ${words[3].translate}
                                 0 - Выход
                                 """.trimIndent()
    )
}

/**
The function is used to "stuff" a new set of words to study.
If the unlearned words are less than 4x, it adds to the list those already learned before the set.
If this is not done, then with one remaining word, only one correct answer will be offered
Unlearned words are always first in the index
 */
fun makeNewRoundListening(unexploredWords: List<Word>, dictionary: List<Word>): List<Word> {
    val exploredWords = dictionary.filter { it.correctAnswer >= 3 }
    if (unexploredWords.size >= 3) return unexploredWords.take(4)
    else {
        while (unexploredWords.size < 3) {
            val newWord = exploredWords.random()
            if (!unexploredWords.contains(newWord)) unexploredWords.plus(newWord)
        }
    }

    return unexploredWords.take(4)
}

data class Word(
    val text: String,
    val translate: String,
    var correctAnswer: Int,
) {


    /**
    Function for printing a set of responses
     */
    fun wordToString(): String {
        return "$text | $translate | $correctAnswer\n"
    }
}
