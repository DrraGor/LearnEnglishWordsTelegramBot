fun main(args: Array<String>) {
    val botToken = args[0]
    val botService = TelegramBotService(botToken)
    var updateId = 0
    var chatId: String
    var text: String


    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        updateId = (parsing("\"update_id\":(\\d+)".toRegex(), updates) ?: continue).toInt() + 1

        chatId = parsing("\"chat\":\\{\"id\":(\\d+)".toRegex(), updates) ?: continue

        text = parsing("\"text\":\"(.+)\"".toRegex(), updates) ?: continue

        botService.sendMessage(chatId, text)
    }
}

fun parsing(regex: Regex, updates: String): String? {
    val matchResult: MatchResult? = regex.find(updates)
    val groups = matchResult?.groups
    return groups?.get(1)?.value
}