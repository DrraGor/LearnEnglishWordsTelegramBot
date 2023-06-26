import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import kotlin.collections.HashMap

class TelegramBotService(private val botToken: String) {
    companion object {
        private const val LEARN_WORDS_BUTTON_CLICKED = "learn_words_clicked"
        private const val STATISTIC_BUTTON_CLICKED = "statistics_clicked"
        private const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
        private const val HTTP_TELEGRAM = "https://api.telegram.org/bot"
        private const val RESET_CLICKED = "reset_clicked"
    }

    private fun sendMenu(json: Json, chatId: Long, botToken: String): String {
        val sendMessage = "$HTTP_TELEGRAM$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARN_WORDS_BUTTON_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTIC_BUTTON_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "Сбросить прогресс", callbackData = RESET_CLICKED),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendMessage(json: Json, botToken: String, chatId: Long, message: String): String {
        val sendMessage = "$HTTP_TELEGRAM$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun checkNextQuestionAndSend(json: Json, trainer: LearnWordsTrainer, botToken: String, chatId: Long) {

        val question = trainer.getNewQuestion()
        if (question != null) {
            sendQuestion(json, botToken, chatId, question)
        } else sendMessage(json, botToken, chatId, "Вы выучили все слова в базе")
    }

    private fun makeKeyboard(question: Question): List<List<InlineKeyboard>> {

        val horizontalKeyboard =
            question.variants.mapIndexed { index, word ->
                InlineKeyboard(
                    text = word.translate.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    },
                    callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                )
            }
        val verticalKeyboard = mutableListOf<List<InlineKeyboard>>()

        for (i in horizontalKeyboard) {
            val row = mutableListOf(i)
            verticalKeyboard.add(row)
        }
        verticalKeyboard.add(
            listOf(
                InlineKeyboard(text = "Перейти в меню", callbackData = "/menu")
            )
        )
        return verticalKeyboard
    }

    private fun sendQuestion(json: Json, botToken: String, chatId: Long, question: Question): String {
        val urlGetUpdates = "$HTTP_TELEGRAM$botToken/sendMessage"
        val keyboardLayout = question.variants
            .mapIndexed { index: Int, word: Word -> "{\"text\": \"${word.translate}\", \"callback_data\": \"$CALLBACK_DATA_ANSWER_PREFIX$index\"" }
            .joinToString(separator = ",")
        println(keyboardLayout)

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.questionWord.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            },
            replyMarkup = ReplyMarkup(makeKeyboard(question))

        )
        val requestBodyString = json.encodeToString(requestBody)
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$HTTP_TELEGRAM$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun handleUpdate(update: Update, json: Json, trainers: HashMap<Long, LearnWordsTrainer>) {

        val message = update.message?.text
        val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

        if (data == LEARN_WORDS_BUTTON_CLICKED)
            checkNextQuestionAndSend(
                json,
                trainer,
                botToken,
                chatId
            )

        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val answerId = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(answerId)) {
                sendMessage(json, botToken, chatId, "Правильно")
            } else {
                sendMessage(
                    json,
                    botToken,
                    chatId,
                    "Не правильно: ${trainer.question?.correctAnswer?.questionWord} - ${trainer.question?.correctAnswer?.translate}",
                )
            }
            checkNextQuestionAndSend(json, trainer, botToken, chatId)
        }
        if (message?.lowercase() == "/menu") {
            sendMenu(json, chatId, botToken)
        }
        if (message?.lowercase() == "/start") {
            sendMenu(json, chatId, botToken)
        }

        if (data?.lowercase() == STATISTIC_BUTTON_CLICKED) {

            sendMessage(
                json,
                botToken,
                chatId,
                "Выучено ${trainer.getStatistics().learned} из ${trainer.getStatistics().total} слов | ${trainer.getStatistics().percent}%",
            )
        }
        if (data == RESET_CLICKED) {
            trainer.resetProgress()
            sendMessage(json, botToken, chatId, "Прогресс сброшен")
        }
        if (data == "/menu") {
            sendMenu(json, chatId, botToken)
        }
    }
}