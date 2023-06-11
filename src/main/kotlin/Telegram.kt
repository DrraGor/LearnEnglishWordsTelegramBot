import java.util.*

fun main(args: Array<String>) {
    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    var updateId = 0
    var chatId: String
    val trainer = LearnWordsTrainer("words.txt", 3, 4)


    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)


        updateId = (parsing("\"update_id\":(\\d+)".toRegex(), updates) ?: continue).toInt() + 1

        chatId = parsing("\"chat\":\\{\"id\":(\\d+)".toRegex(), updates) ?: continue


        when {
            (parsing("\"text\":\"(.+?)\"".toRegex(), updates)
                ?: continue).lowercase(Locale.getDefault()) == "/menu" -> botService.sendMenu(chatId)

            (parsing("\"text\":\"(.+?)\"".toRegex(), updates)
                ?: continue).lowercase(Locale.getDefault()) == "/start" -> botService.sendStartButton(chatId)

            (parsing("\"data\":\"(.+)\"".toRegex(), updates)
                ?: continue).lowercase(Locale.getDefault()) == "statistics_clicked" -> botService.sendMessage(
                chatId,
                "Выучено ${trainer.getStatistics().learned} из ${trainer.getStatistics().total} слов | ${trainer.getStatistics().percent}%"
            )

            (parsing("\"data\":\"(.+)\"".toRegex(), updates)
                ?: continue).lowercase(Locale.getDefault()) == "learn_words_clicked" -> {
                botService.checkNextQuestionAndSend(trainer, botToken, chatId)
            }

            trainer.checkAnswer(
                parsing("\"data\":\"answer_(.)\"".toRegex(), updates)?.toInt()
                    ?: continue
            ) -> {
                botService.sendMessage(chatId, "Правильно")
                botService.checkNextQuestionAndSend(trainer, botToken, chatId)
            }

            else -> {
                botService.sendMessage(
                    chatId,
                    "Не правильно: ${trainer.question?.correctAnswer?.questionWord} - ${trainer.question?.correctAnswer?.translate}"
                )
                botService.checkNextQuestionAndSend(trainer, botToken, chatId)
            }
        }
    }
}

fun parsing(regex: Regex, updates: String): String? {
    val matchResult: MatchResult? = regex.find(updates)
    val groups = matchResult?.groups
    return groups?.get(1)?.value
}


