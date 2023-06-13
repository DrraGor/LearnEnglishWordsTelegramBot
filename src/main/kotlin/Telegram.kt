import java.util.*
fun main(args: Array<String>) {
    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    var updateId = 0
    var chatId: String
    val trainer = LearnWordsTrainer("words.txt", 3, 4)

    val idRegex = "\"update_id\":(\\d+)".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val textRegex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex = "\"data\":\"(.+)\"".toRegex()
    val answerRegex = "\"data\":\"answer_(.)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        updateId = (parsing(idRegex, updates) ?: continue).toInt() + 1
        chatId = parsing(chatIdRegex, updates) ?: continue

        when {
            (parsing(textRegex, updates) ?: continue).lowercase(Locale.getDefault()) == "/menu" -> botService.sendMenu(
                chatId
            )

            (parsing(dataRegex, updates) ?: continue).lowercase(Locale.getDefault()) == "menu" -> botService.sendMenu(
            chatId
            )


            (parsing(textRegex, updates)
                ?: continue).lowercase(Locale.getDefault()) == "/start" -> botService.sendStartButton(chatId)

            (parsing(dataRegex, updates)
                ?: continue).lowercase(Locale.getDefault()) == "statistics_clicked" -> botService.sendMessage(
                chatId,
                "Выучено ${trainer.getStatistics().learned} из ${trainer.getStatistics().total} слов | ${trainer.getStatistics().percent}%"
            )

            (parsing(dataRegex, updates)
                ?: continue).lowercase(Locale.getDefault()) == "learn_words_clicked" -> {
                botService.checkNextQuestionAndSend(trainer, botToken, chatId)
            }

            trainer.checkAnswer(
                parsing(answerRegex, updates)?.toInt()
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


