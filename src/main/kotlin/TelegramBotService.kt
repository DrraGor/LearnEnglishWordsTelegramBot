import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.*

class TelegramBotService(private val botToken: String) {
    companion object{
        const val HTTP_TELEGRAM = "https://api.telegram.org/bot"
        const val LEARN_WORDS_BUTTON_CLICKED = "learn_words_clicked"
        const val STATISTIC_BUTTON_CLICKED = "statistics_clicked"
        const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
    }

    fun sendMessage(chatId: String, text: String) {
        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )
        val urlNewMessage = "$HTTP_TELEGRAM$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlNewMessage)).build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$HTTP_TELEGRAM$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: String): String {
        val sendMessage = "$HTTP_TELEGRAM$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$LEARN_WORDS_BUTTON_CLICKED"
                            },
                            {
                                "text": "Стасистика",
                                "callback_data": "$STATISTIC_BUTTON_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendStartButton(chatId: String): String {
        val sendMessage = "$HTTP_TELEGRAM$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Начать обучение",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Старт",
                                "callback_data": "menu"
                            }                            
                        ]
                    ]
                }
            }
        """.trimIndent()
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendQuestion(botToken: String, chatId: String, question: Question): String {
        val sendMessage = "$HTTP_TELEGRAM$botToken/sendMessage"
        val sendNewQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${
            question.correctAnswer.questionWord.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "${
            question.variants[0].translate.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 0}"
                            },
                            {
                                "text": "${
            question.variants[1].translate.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 1}"
                            },
                            {
                                "text": "${
            question.variants[2].translate.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 2}"
                            },
                            {
                                "text": "${
            question.variants[3].translate.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }",
                                "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 3}"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendNewQuestionBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, botToken: String, chatId: String) {
        val question = trainer.getNewQuestion()
        if (question != null) {
            sendQuestion(botToken, chatId, question)
        } else sendMessage(chatId, "Вы выучили все слова в базе")
    }
}