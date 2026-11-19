package com.toeicspace.android.feature.vocabulary.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.toeicspace.android.feature.vocabulary.presentation.flashcard.FlashcardRoute
import com.toeicspace.android.feature.vocabulary.presentation.set.VocabularySetRoute
import kotlinx.serialization.Serializable

/** Full-screen word list of one set, above the shell in the root graph. */
@Serializable
data class VocabularySetDestination(
    val setId: String,
) {
    companion object {
        /** The argument key, the property name above, for reading it from a SavedStateHandle. */
        const val ARG_SET_ID = "setId"
    }
}

/** Flashcards of one set, opened on the card at [startIndex]. */
@Serializable
data class FlashcardDestination(
    val setId: String,
    val startIndex: Int = 0,
) {
    companion object {
        const val ARG_SET_ID = "setId"
        const val ARG_START_INDEX = "startIndex"
    }
}

private val SingleTop = navOptions { launchSingleTop = true }

fun NavController.navigateToVocabularySet(setId: String) =
    navigate(VocabularySetDestination(setId), SingleTop)

fun NavController.navigateToFlashcards(
    setId: String,
    startIndex: Int = 0,
) = navigate(FlashcardDestination(setId, startIndex), SingleTop)

fun NavGraphBuilder.vocabularyScreens(
    onBack: () -> Unit,
    onStudySet: (setId: String, startIndex: Int) -> Unit,
    onOpenSet: (setId: String) -> Unit,
) {
    composable<VocabularySetDestination> {
        VocabularySetRoute(onBackClick = onBack, onStudyClick = onStudySet, onOpenSet = onOpenSet)
    }
    composable<FlashcardDestination> { FlashcardRoute(onBackClick = onBack) }
}
