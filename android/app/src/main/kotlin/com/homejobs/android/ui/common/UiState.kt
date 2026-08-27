package com.homejobs.android.ui.common

/** Generic loading/empty/error/success wrapper so every screen handles the same states the same way. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}
