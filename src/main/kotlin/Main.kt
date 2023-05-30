data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswerCount: Int,
)

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> "${index + 1} - ${word.translate}" }
        .joinToString(separator = "\n")
    return this.correctAnswer.questionWord + "\n" + variants + "\n0 - выйти в меню"
}

fun main() {

    val trainer = LearnWordsTrainer("words.txt", 3, 4)

    while (true) {

        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        when (readln().toIntOrNull()) {
            null -> println("Некорректный ввод, попробуйте ещё раз")

            1 -> {
                while (true) {
                    val question = trainer.getNewQuestion()
                    if (question == null) {
                        println("Вы выучили все слова")
                        break
                    } else {
                        println(question.asConsoleString())
                        val userAnswerInput = readln().toIntOrNull()
                        if (userAnswerInput == null) {
                            println("Некорректный ввод, попробуйте ещё раз")
                        }
                        if (userAnswerInput == 0) break
                        if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!")
                        } else {
                            println("Неправильно! ${question.correctAnswer.questionWord} - это ${question.correctAnswer.translate}")
                        }
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learned} из ${statistics.total} слов | ${statistics.percent}\n")
            }

            0 -> break
        }
    }
}









