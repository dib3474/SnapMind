package com.example.snapmind.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object PermissionDenied : AppError
    data object UnsupportedImageType : AppError
    data object FileNotFound : AppError
    data object NetworkUnavailable : AppError
    data object RemoteFeatureDisabled : AppError
    data class Unknown(val message: String) : AppError
}
