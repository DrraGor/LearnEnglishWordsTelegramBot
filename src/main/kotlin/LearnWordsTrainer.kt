import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val questionWord: String,
    val translate: String,
    var correctAnswerCount: Int,
)

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

    private val fileName: String = "words.txt",
    private var limitCorrectAnswers: Int = 3,
    private var numberVariantsAnswers: Int = 4,
) {

    var question: Question? = null
    private var dictionary = loadDictionary()

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
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val wordsFile = File(fileName)
            if(!wordsFile.exists()){
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()
            val lines: List<String> = File(fileName).readLines()
            for (i in lines) {
                val line = i.split("|")
                dictionary.add(Word(line[0].trim(), line[1].trim(), line[2].trim().toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Не корректный файл")
        }
    }

    /**
    Function for saving the response result
     */
    private fun saveDictionary() {
        val wordsFile = File(fileName)
            wordsFile.writeText("")
        for (word in dictionary) {
            wordsFile.appendText("${word.questionWord}|${word.translate}|${word.correctAnswerCount}\n")
        }
    }

    fun resetProgress(){
        dictionary.forEach { it.correctAnswerCount = 0 }
        saveDictionary()
    }

}

