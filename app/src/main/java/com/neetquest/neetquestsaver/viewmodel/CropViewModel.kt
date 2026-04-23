package com.neetquest.neetquestsaver.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CropViewModel @Inject constructor() : ViewModel() {

    private val _croppedBitmap = MutableStateFlow<Bitmap?>(null)
    val croppedBitmap: StateFlow<Bitmap?> = _croppedBitmap.asStateFlow()

    fun setCroppedBitmap(bitmap: Bitmap) {
        _croppedBitmap.value = bitmap
    }

    fun clearCroppedBitmap() {
        _croppedBitmap.value = null
    }

    override fun onCleared() {
        _croppedBitmap.value?.recycle()
        super.onCleared()
    }
}
