package io.github.nwma_fywf.mineword.data.repository

import io.github.nwma_fywf.mineword.data.local.DailyStats
import io.github.nwma_fywf.mineword.data.local.DailyStatsDao
import io.github.nwma_fywf.mineword.data.local.ExportData
import io.github.nwma_fywf.mineword.data.local.ExportExampleSentence
import io.github.nwma_fywf.mineword.data.local.ExportMeaning
import io.github.nwma_fywf.mineword.data.local.ExportWord
import io.github.nwma_fywf.mineword.data.local.ExportWrongAnswer
import io.github.nwma_fywf.mineword.data.local.ExampleSentence
import io.github.nwma_fywf.mineword.data.local.ExampleSentenceDao
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.MeaningDao
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.local.WordDao
import io.github.nwma_fywf.mineword.data.local.WrongAnswer
import io.github.nwma_fywf.mineword.data.local.WrongAnswerDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

enum class SortBy {
    CREATED_TIME,
    ALPHABETIC,
    REVIEW_TIME,
    MASTERY
}

data class ImportResult(
    val totalCount: Int,
    val successCount: Int,
    val skipCount: Int,
    val replacedCount: Int,
    val mergedCount: Int,
    val duplicateWords: List<String>
)

enum class DuplicateStrategy {
    REPLACE,
    SKIP,
    MERGE
}

class WordRepository(
    private val wordDao: WordDao,
    private val meaningDao: MeaningDao,
    private val exampleSentenceDao: ExampleSentenceDao,
    private val wrongAnswerDao: WrongAnswerDao,
    private val dailyStatsDao: DailyStatsDao
) {
    fun getAllWords(): Flow<List<Word>> = wordDao.getAllWords()

    fun getAllWordsSorted(sortBy: SortBy): Flow<List<Word>> = when (sortBy) {
        SortBy.CREATED_TIME -> wordDao.getAllWords()
        SortBy.ALPHABETIC -> wordDao.getAllWordsSortedByName()
        SortBy.REVIEW_TIME -> wordDao.getAllWordsSortedByReviewTime()
        SortBy.MASTERY -> wordDao.getAllWordsSortedByMastery()
    }

    suspend fun getWordById(id: Long): Word? = wordDao.getWordById(id)

    fun searchWords(query: String): Flow<List<Word>> = wordDao.searchWords(query)

    suspend fun insertWord(word: Word): Long = wordDao.insertWord(word)

    suspend fun updateWord(word: Word) = wordDao.updateWord(word)

    suspend fun deleteWord(word: Word) = wordDao.deleteWord(word)

    fun getAllTagsRaw(): Flow<List<String>> = wordDao.getAllTagsRaw()

    fun getWordsByTag(tag: String): Flow<List<Word>> = wordDao.getWordsByTag(tag)

    suspend fun getWordsByTagOnce(tag: String): List<Word> = wordDao.getWordsByTagOnce(tag)

    suspend fun getWordsByTwoTagsOnce(tag1: String, tag2: String): List<Word> = wordDao.getWordsByTwoTagsOnce(tag1, tag2)

    suspend fun getWordsByTwoTagsCount(tag1: String, tag2: String): Int = wordDao.getWordsByTwoTagsOnce(tag1, tag2).size

    fun getWordsByTwoTagsCountFlow(tag1: String, tag2: String): Flow<Int> = flow {
        emit(getWordsByTwoTagsCount(tag1, tag2))
    }

    suspend fun getWordByWord(word: String): Word? = wordDao.getWordByWord(word)

    suspend fun getAllWordsList(): List<Word> = wordDao.getAllWordsOnce()

    suspend fun getWordCount(): Int = wordDao.getWordCount()

    fun getWordCountFlow(): Flow<Int> = wordDao.getWordCountFlow()

    fun getMeaningsByWordId(wordId: Long): Flow<List<Meaning>> =
        meaningDao.getMeaningsByWordId(wordId)

    suspend fun getMeaningCount(wordId: Long): Int =
        meaningDao.getMeaningCount(wordId)

    suspend fun saveMeanings(wordId: Long, meanings: List<Meaning>) {
        meaningDao.deleteByWordId(wordId)
        if (meanings.isNotEmpty()) {
            meaningDao.insertAll(meanings.map { it.copy(wordId = wordId) })
        }
    }

    fun getExampleSentencesByWordId(wordId: Long): Flow<List<ExampleSentence>> =
        exampleSentenceDao.getSentencesByWordId(wordId)

    suspend fun saveExampleSentences(wordId: Long, sentences: List<ExampleSentence>) {
        exampleSentenceDao.deleteByWordId(wordId)
        if (sentences.isNotEmpty()) {
            exampleSentenceDao.insertAll(sentences.map { it.copy(wordId = wordId) })
        }
    }

    suspend fun deleteAllWords() {
        val words = wordDao.getAllWordsOnce()
        words.forEach { word ->
            meaningDao.deleteByWordId(word.id)
            exampleSentenceDao.deleteByWordId(word.id)
        }
        wordDao.deleteAllWords()
    }

    suspend fun exportAllData(): ExportData {
        val words = wordDao.getAllWordsOnce()
        val exportWords = words.map { word ->
            val meanings = meaningDao.getMeaningsByWordIdOnce(word.id)
            val sentences = exampleSentenceDao.getSentencesByWordIdOnce(word.id)
            ExportWord(
                word = word.word,
                phoneticUK = word.phoneticUK,
                phoneticUS = word.phoneticUS,
                audioUrl = word.audioUrl,
                tags = word.tags,
                synonyms = word.synonyms,
                antonyms = word.antonyms,
                phrases = word.phrases,
                wordForms = word.wordForms,
                personalNotes = word.personalNotes,
                meanings = meanings.map { m ->
                    ExportMeaning(
                        partOfSpeech = m.partOfSpeech,
                        definition = m.definition,
                        order = m.order
                    )
                },
                exampleSentences = sentences.map { s ->
                    ExportExampleSentence(
                        sentence = s.sentence,
                        translation = s.translation,
                        order = s.order
                    )
                }
            )
        }
        val wrongAnswers = wrongAnswerDao.getAllWrongAnswers().first().map { wa ->
            val word = wordDao.getWordById(wa.wordId)
            ExportWrongAnswer(
                word = word?.word ?: "",
                quizMode = wa.quizMode,
                userAnswer = wa.userAnswer,
                correctAnswer = wa.correctAnswer,
                timestamp = wa.timestamp
            )
        }
        return ExportData(words = exportWords, wrongAnswers = wrongAnswers)
    }

    suspend fun importWords(
        exportWords: List<ExportWord>,
        duplicateStrategy: DuplicateStrategy
    ): ImportResult {
        var successCount = 0
        var skipCount = 0
        var replacedCount = 0
        var mergedCount = 0
        val duplicateWords = mutableListOf<String>()

        for (exportWord in exportWords) {
            val existingWord = wordDao.getWordByWord(exportWord.word)

            if (existingWord != null) {
                duplicateWords.add(exportWord.word)
                when (duplicateStrategy) {
                    DuplicateStrategy.SKIP -> {
                        skipCount++
                    }
                    DuplicateStrategy.REPLACE -> {
                        val newWord = existingWord.copy(
                            phoneticUK = exportWord.phoneticUK,
                            phoneticUS = exportWord.phoneticUS,
                            audioUrl = exportWord.audioUrl,
                            tags = exportWord.tags,
                            synonyms = exportWord.synonyms,
                            antonyms = exportWord.antonyms,
                            phrases = exportWord.phrases,
                            wordForms = exportWord.wordForms,
                            personalNotes = exportWord.personalNotes
                        )
                        wordDao.updateWord(newWord)
                        saveMeanings(newWord.id, exportWord.meanings.map { m ->
                            Meaning(wordId = newWord.id, partOfSpeech = m.partOfSpeech, definition = m.definition, order = m.order)
                        })
                        saveExampleSentences(newWord.id, exportWord.exampleSentences.map { s ->
                            ExampleSentence(wordId = newWord.id, sentence = s.sentence, translation = s.translation, order = s.order)
                        })
                        replacedCount++
                    }
                    DuplicateStrategy.MERGE -> {
                        val existingMeanings = meaningDao.getMeaningsByWordIdOnce(existingWord.id).toMutableList()
                        val existingSentences = exampleSentenceDao.getSentencesByWordIdOnce(existingWord.id).toMutableList()

                        for (m in exportWord.meanings) {
                            if (existingMeanings.none { it.definition == m.definition }) {
                                existingMeanings.add(Meaning(wordId = existingWord.id, partOfSpeech = m.partOfSpeech, definition = m.definition, order = existingMeanings.size))
                            }
                        }

                        for (s in exportWord.exampleSentences) {
                            if (existingSentences.none { it.sentence == s.sentence }) {
                                existingSentences.add(ExampleSentence(wordId = existingWord.id, sentence = s.sentence, translation = s.translation, order = existingSentences.size))
                            }
                        }

                        val updatedTags = if (exportWord.tags.isNotEmpty()) {
                            val existingTags = existingWord.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                            exportWord.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { existingTags.add(it) }
                            existingTags.joinToString(",")
                        } else {
                            existingWord.tags
                        }

                        val newWord = existingWord.copy(
                            tags = updatedTags,
                            synonyms = if (exportWord.synonyms.isNotEmpty()) exportWord.synonyms else existingWord.synonyms,
                            antonyms = if (exportWord.antonyms.isNotEmpty()) exportWord.antonyms else existingWord.antonyms,
                            phrases = if (exportWord.phrases.isNotEmpty()) exportWord.phrases else existingWord.phrases,
                            wordForms = if (exportWord.wordForms.isNotEmpty()) exportWord.wordForms else existingWord.wordForms,
                            personalNotes = if (exportWord.personalNotes.isNotEmpty()) exportWord.personalNotes else existingWord.personalNotes
                        )
                        wordDao.updateWord(newWord)
                        saveMeanings(newWord.id, existingMeanings)
                        saveExampleSentences(newWord.id, existingSentences)
                        mergedCount++
                    }
                }
            } else {
                val newWord = Word(
                    word = exportWord.word,
                    phoneticUK = exportWord.phoneticUK,
                    phoneticUS = exportWord.phoneticUS,
                    audioUrl = exportWord.audioUrl,
                    tags = exportWord.tags,
                    synonyms = exportWord.synonyms,
                    antonyms = exportWord.antonyms,
                    phrases = exportWord.phrases,
                    wordForms = exportWord.wordForms,
                    personalNotes = exportWord.personalNotes,
                )
                val wordId = wordDao.insertWord(newWord)
                saveMeanings(wordId, exportWord.meanings.map { m ->
                    Meaning(wordId = wordId, partOfSpeech = m.partOfSpeech, definition = m.definition, order = m.order)
                })
                saveExampleSentences(wordId, exportWord.exampleSentences.map { s ->
                    ExampleSentence(wordId = wordId, sentence = s.sentence, translation = s.translation, order = s.order)
                })
                successCount++
            }
        }

        return ImportResult(
            totalCount = exportWords.size,
            successCount = successCount,
            skipCount = skipCount,
            replacedCount = replacedCount,
            mergedCount = mergedCount,
            duplicateWords = duplicateWords
        )
    }

    fun getAllWrongAnswers(): Flow<List<WrongAnswer>> = wrongAnswerDao.getAllWrongAnswers()

    fun getWrongAnswersByWordId(wordId: Long): Flow<List<WrongAnswer>> =
        wrongAnswerDao.getWrongAnswersByWordId(wordId)

    suspend fun insertWrongAnswer(wrongAnswer: WrongAnswer): Long =
        wrongAnswerDao.insertWrongAnswer(wrongAnswer)

    suspend fun deleteWrongAnswerById(id: Long) =
        wrongAnswerDao.deleteWrongAnswerById(id)

    suspend fun deleteWrongAnswersByWordId(wordId: Long) =
        wrongAnswerDao.deleteWrongAnswersByWordId(wordId)

    suspend fun clearAllWrongAnswers() =
        wrongAnswerDao.clearAllWrongAnswers()

    fun getWrongAnswerCount(): Flow<Int> = wrongAnswerDao.getWrongAnswerCount()

    companion object {
        private val REVIEW_INTERVALS = listOf(
            1 * 24 * 60 * 60 * 1000L,
            3 * 24 * 60 * 60 * 1000L,
            7 * 24 * 60 * 60 * 1000L,
            15 * 24 * 60 * 60 * 1000L,
            30 * 24 * 60 * 60 * 1000L
        )
    }

    suspend fun recordReview(wordId: Long, stage: Int) {
        val word = wordDao.getWordById(wordId) ?: return
        val nextStage = (stage + 1).coerceAtMost(REVIEW_INTERVALS.size - 1)
        val interval = REVIEW_INTERVALS[nextStage]
        val now = System.currentTimeMillis()
        val updatedWord = word.copy(
            learningStage = nextStage,
            lastReviewTime = now,
            nextReviewTime = now + interval
        )
        wordDao.updateWord(updatedWord)
    }

    suspend fun initReview(wordId: Long) {
        val word = wordDao.getWordById(wordId) ?: return
        if (word.learningStage == 0 && word.nextReviewTime == 0L) {
            val now = System.currentTimeMillis()
            val interval = REVIEW_INTERVALS[0]
            val updatedWord = word.copy(
                learningStage = 0,
                lastReviewTime = now,
                nextReviewTime = now + interval
            )
            wordDao.updateWord(updatedWord)
        }
    }

    suspend fun getWordsDueForReview(): List<Word> {
        return wordDao.getWordsDueForReview(System.currentTimeMillis())
    }

    suspend fun getDueReviewCount(): Int {
        return wordDao.getDueReviewCount(System.currentTimeMillis())
    }

    suspend fun getWrongAnswerWordIds(): List<Long> {
        return wrongAnswerDao.getWrongAnswerWordIds()
    }

    fun getDueReviewCountFlow(): kotlinx.coroutines.flow.Flow<Int> = kotlinx.coroutines.flow.flow {
        emit(getDueReviewCount())
    }

    fun getTotalNewWordsFlow(): Flow<Int> = dailyStatsDao.getTotalNewWordsFlow()

    fun getTotalReviewedWordsFlow(): Flow<Int> = dailyStatsDao.getTotalReviewedWordsFlow()

    fun getTotalCorrectFlow(): Flow<Int> = dailyStatsDao.getTotalCorrectFlow()

    fun getTotalWrongFlow(): Flow<Int> = dailyStatsDao.getTotalWrongFlow()

    fun getRecentStats(limit: Int): Flow<List<DailyStats>> = dailyStatsDao.getRecentStats(limit)

    fun getDailyStatsDao(): DailyStatsDao = dailyStatsDao

    suspend fun getTotalNewWords(): Int = runCatching { dailyStatsDao.getTotalNewWords() }.getOrDefault(0)

    suspend fun getTotalReviewedWords(): Int = runCatching { dailyStatsDao.getTotalReviewedWords() }.getOrDefault(0)

    suspend fun getTotalCorrect(): Int = runCatching { dailyStatsDao.getTotalCorrect() }.getOrDefault(0)

    suspend fun getTotalWrong(): Int = runCatching { dailyStatsDao.getTotalWrong() }.getOrDefault(0)

    suspend fun recordNewWord() {
        val today = getTodayTimestamp()
        val existing = dailyStatsDao.getStatsForDate(today)
        if (existing != null) {
            dailyStatsDao.upsert(existing.copy(newWordsCount = existing.newWordsCount + 1))
        } else {
            dailyStatsDao.upsert(DailyStats(date = today, newWordsCount = 1))
        }
    }

    suspend fun recordReviewStats(wordId: Long, isCorrect: Boolean) {
        val today = getTodayTimestamp()
        val existing = dailyStatsDao.getStatsForDate(today)
        val stats = existing ?: DailyStats(date = today)
        dailyStatsDao.upsert(
            stats.copy(
                reviewedWordsCount = stats.reviewedWordsCount + 1,
                correctCount = if (isCorrect) stats.correctCount + 1 else stats.correctCount,
                wrongCount = if (!isCorrect) stats.wrongCount + 1 else stats.wrongCount
            )
        )
    }

    private fun getTodayTimestamp(): Long {
        val now = System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(now) * TimeUnit.DAYS.toMillis(1)
    }

    suspend fun importWrongAnswers(exportWrongAnswers: List<ExportWrongAnswer>) {
        for (exportWA in exportWrongAnswers) {
            val word = wordDao.getWordByWord(exportWA.word)
            if (word != null) {
                wrongAnswerDao.insertWrongAnswer(
                    WrongAnswer(
                        wordId = word.id,
                        quizMode = exportWA.quizMode,
                        userAnswer = exportWA.userAnswer,
                        correctAnswer = exportWA.correctAnswer,
                        timestamp = exportWA.timestamp
                    )
                )
            }
        }
    }
}
