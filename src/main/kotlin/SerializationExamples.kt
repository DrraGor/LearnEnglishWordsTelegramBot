
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json



fun main() {

    val json = Json{
        ignoreUnknownKeys = true
    }

    val responseString = """
        {
            "ok": true,
            "result": [
                {
                    "update_id": 238923628,
                    "message": {
                        "message_id": 494,
                        "from": {
                            "id": 5124647422,
                            "is_bot": false,
                            "first_name": "\u042e\u0440\u0438\u0439",
                            "last_name": "\u042f\u043a\u0443\u0448\u0435\u0432",
                            "language_code": "en"
                        },
                        "chat": {
                            "id": 5124647422,
                            "first_name": "\u042e\u0440\u0438\u0439",
                            "last_name": "\u042f\u043a\u0443\u0448\u0435\u0432",
                            "type": "private"
                        },
                        "date": 1686682898,
                        "text": "/start",
                        "entities": [
                            {
                                "offset": 0,
                                "length": 6,
                                "type": "bot_command"
                            }
                        ]
                    }
                }
            ]
        }
    """.trimIndent()

//val word = Json.encodeToString(
//    Word(
//        questionWord = "Hello",
//        translate = "Привет",
//        correctAnswerCount = 0,
//    )
//)
//    println(word)
//    val wordObject = Json.decodeFromString<Word>(
//        """{"questionWord":"Hello","translate":"Привет","correctAnswerCount":0}"""
//    )
//    println(wordObject)
    val response = json.decodeFromString<Response>(responseString)
    println(response)
}