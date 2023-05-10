package additional

import java.io.File

fun main() {

    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.readLines().forEach { println(it) }

}

