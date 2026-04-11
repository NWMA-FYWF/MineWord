package io.github.nwma_fywf.mineword.ui.navigation

sealed class Screen(val route: String) {
    data object WordList : Screen("word_list")
    data object AddWord : Screen("add_word")
    data object WordDetail : Screen("word_detail/{wordId}") {
        fun createRoute(wordId: Long) = "word_detail/$wordId"
    }
    data object EditWord : Screen("edit_word/{wordId}") {
        fun createRoute(wordId: Long) = "edit_word/$wordId"
    }
    data object QuizMode : Screen("quiz_mode")
    data object QuizPlay : Screen("quiz_play/{mode}/{count}/{tag}") {
        fun createRoute(mode: String, count: Int, tag: String?) = "quiz_play/$mode/$count/${tag ?: "all"}"
    }
    data object Review : Screen("review")
    data object WrongAnswer : Screen("wrong_answer")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
}
