package io.github.nwma_fywf.mineword.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.nwma_fywf.mineword.data.local.ExportData
import io.github.nwma_fywf.mineword.data.local.ExportExampleSentence
import io.github.nwma_fywf.mineword.data.local.ExportMeaning
import io.github.nwma_fywf.mineword.data.local.ExportWrongAnswer
import io.github.nwma_fywf.mineword.data.local.ExportWord
import io.github.nwma_fywf.mineword.data.local.WordDatabase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tag = "BackupWorker"
        Log.d(tag, "Backup work triggered")

        return try {
            val database = WordDatabase.getDatabase(context)
            val exportData = collectExportData(database)

            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val backupFile = File(backupDir, "mineword_backup_$timestamp.json")

            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(exportData)

            backupFile.writeText(jsonString)
            Log.d(tag, "Backup saved to: ${backupFile.absolutePath}")

            cleanupOldBackups(backupDir)

            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Backup failed: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun collectExportData(database: WordDatabase): ExportData {
        val wordDao = database.wordDao()
        val meaningDao = database.meaningDao()
        val exampleSentenceDao = database.exampleSentenceDao()
        val wrongAnswerDao = database.wrongAnswerDao()

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

    private fun cleanupOldBackups(backupDir: File) {
        val backupFiles = backupDir.listFiles { file ->
            file.name.startsWith("mineword_backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() } ?: return

        val maxBackups = MAX_BACKUP_FILES
        if (backupFiles.size > maxBackups) {
            backupFiles.drop(maxBackups).forEach { file ->
                file.delete()
                Log.d("BackupWorker", "Deleted old backup: ${file.name}")
            }
        }
    }

    companion object {
        const val WORK_NAME = "auto_backup_work"
        private const val BACKUP_DIR = "backups"
        private const val MAX_BACKUP_FILES = 10
    }
}
