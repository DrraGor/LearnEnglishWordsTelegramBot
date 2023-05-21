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

                    when (val answer = readLine()?.toIntOrNull()?.minus(1)) {

                        null -> println("Некорректный ввод, попробуйте ещё раз")

                        -1 -> break

                        else -> {

                            if (unexploredWords[answer].hashCode() == translatedWord.hashCode()) {
                                println("Вы ответили правильно")
                                val a = dictionary.indexOf(translatedWord)
                                dictionary[a].correctAnswer += 1


                                break
                            } else println("Ответ не верный")
                            break
                        }
                    }
                }
            }

            2 -> {
                val learnedWords = dictionary.filter { it.correctAnswer >= 3 }
                println("Выучено ${learnedWords.size} из ${dictionary.size} слов | ${learnedWords.size * (100 / dictionary.size)}%")
                println()

            }

            0 -> break

        }
    }
}

// Функция для вывода на печать набора ответов
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

// Функуия служит для "набивки" нового комплекта слов для изучения.
// Если невыученных слов меньше 4х добавляет в список уже выученные до комплекта.
// Если это не сделать то при одном оставшемся слове будет предложен только один правильный вариант ответа
// Невыученные слова всегда первые по индексу
fun makeNewRoundListening(unexploredWords: List<Word>, dictionary: List<Word>): List<Word> {
    if (unexploredWords.size >= 3) return unexploredWords.take(4)
    else {
        while (unexploredWords.size < 3) {
            val newWord = dictionary.random()
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
    override fun toString(): String {
        return "Word(text= '$text', translate= '$translate', correctAnswer= '$correctAnswer')"
    }
}
