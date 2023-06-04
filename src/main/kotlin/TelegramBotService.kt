import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
const val HTTPTELEGRAM = "https://api.telegram.org/bot"
class TelegramBotService(botToken_: String) {
        val botToken = botToken_
    fun sendMessage(chatId: String, text: String) {
        val urlNewMessage = "$HTTPTELEGRAM$botToken/sendMessage?chat_id=$chatId&text=$text"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlNewMessage)).build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$HTTPTELEGRAM$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }
}