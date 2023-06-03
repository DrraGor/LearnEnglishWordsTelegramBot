fun main(args: Array<String>) {

    val botService = TelegramBotService()
    val botToken = args[0]
    var updateId = 0
    var chatId: String
    var text: String


    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(botToken, updateId)
        println(updates)

        val idUpdateText = parsing("\"update_id\":(\\d+)".toRegex(), updates)
        if (idUpdateText != null) {
            updateId = idUpdateText.toInt() + 1
        } else continue

        val chatIdRegexResultText = parsing("\"chat\":\\{\"id\":(\\d+)".toRegex(), updates)
        if (chatIdRegexResultText != null) {
            chatId = chatIdRegexResultText
        } else continue

        val messageText = parsing("\"text\":\"(.+)\"".toRegex(), updates)
        if (messageText != null) {
            text = messageText
        } else continue

        botService.sendMessage(chatId, text, botToken)
    }
}

fun parsing(regex: Regex, updates: String): String? {
    val matchResult: MatchResult? = regex.find(updates)
    val groups = matchResult?.groups
    return groups?.get(1)?.value
}