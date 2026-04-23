package com.neetquest.neetquestsaver.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SavedQuestions : Screen("saved_questions")
    object Categories : Screen("categories")
    object Settings : Screen("settings")
    object QuestionDetail : Screen("question_detail/{questionId}") {
        fun createRoute(id: Long) = "question_detail/$id"
    }
    object CropEditor : Screen("crop_editor")
    object SaveQuestion : Screen("save_question")
    object AddManual : Screen("add_manual")
}
