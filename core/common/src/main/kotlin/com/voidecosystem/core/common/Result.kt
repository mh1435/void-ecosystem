package com.voidecosystem.core.common

/**
 * Shared outcome wrapper used across every module's data layer so
 * ViewModels throughout the ecosystem handle success/error uniformly.
 */
sealed interface VoidResult<out T> {
    data class Success<T>(val data: T) : VoidResult<T>
    data class Failure(val throwable: Throwable) : VoidResult<Nothing>
    data object Loading : VoidResult<Nothing>
}

inline fun <T> VoidResult<T>.onSuccess(action: (T) -> Unit): VoidResult<T> {
    if (this is VoidResult.Success) action(data)
    return this
}

inline fun <T> VoidResult<T>.onFailure(action: (Throwable) -> Unit): VoidResult<T> {
    if (this is VoidResult.Failure) action(throwable)
    return this
}
