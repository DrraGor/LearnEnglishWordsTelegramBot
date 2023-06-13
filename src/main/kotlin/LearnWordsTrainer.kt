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

class LearnWordsTrainer(
    _fileName: String,
    _limitCorrectAnswers: Int,
    _numberVariantsAnswers: Int,
) {
    private var wordsFile = File(_fileName)
    var question: Question? = null
    private var dictionary = loadDictionary()
    private var limitCorrectAnswers = _limitCorrectAnswers
    private var numberVariantsAnswers = _numberVariantsAnswers

    fun getStatistics(): Statistics {
        val learnedWords = dictionary.filter { it.correctAnswerCount >= limitCorrectAnswers }.size
        val total = dictionary.size
        val percent = learnedWords * 100 / total
        return Statistics(learnedWords, total, percent)
    }

    fun getNewQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswerCount < limitCorrectAnswers }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.take(numberVariantsAnswers).shuffled()
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
        wordsFile.writeText("")
        for (word in words) {
            wordsFile.appendText("${word.questionWord}|${word.translate}|${word.correctAnswerCount}\n")
        }
    }
}

