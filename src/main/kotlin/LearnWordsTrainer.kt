import java.io.File

data class Statistics(
    val learned: Int,
    val total: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainer {

    private var question: Question? = null
    val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val learnedWords = dictionary.filter { it.correctAnswerCount >= 3 }.size
        val total = dictionary.size
        val percent = learnedWords * 100 / total
        return Statistics(learnedWords, total, percent)
    }

    fun getNewQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswerCount < 3 }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.take(4).shuffled()
        val correctAnswer = questionWords.random()

        question = Question(
            variants = questionWords,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswerIndex) {
                it.correctAnswer.correctAnswerCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        val wordsFile = File("words.txt")
        val lines: List<String> = wordsFile.readLines()
        for (i in lines) {
            val line = i.split("|")
            dictionary.add(Word(line[0].trim(), line[1].trim(), line[2].trim().toIntOrNull() ?: 0))
        }
        return dictionary
    }

    /**
    Function for saving the response result
     */
    private fun saveDictionary(words: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.writeText("")
        for (word in words) {
            wordsFile.appendText("${word.questionWord}|${word.translate}|${word.correctAnswerCount}\n")
        }
    }


}

