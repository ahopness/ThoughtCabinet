package dev.lucasangelo.thoughtcabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class ThoughtsViewModel(
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    enum class FeedMode {
        STANDARD,
        BOOKMARKS_ONLY,
        ARCHIVED_ONLY,
        BLOCKED_PERSONAS_ONLY
    }
    private var _feedMode = MutableStateFlow(FeedMode.STANDARD)
    val feedMode = _feedMode.asStateFlow()
    fun onFeedModeChanged(to: FeedMode) {
        _isLoading.value = true
        _feedMode.value = to
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val feed = feedMode
        .debounce(300L)
        .flatMapLatest { mode ->
            _isLoading.value = true
            val flow = when (mode) {
                FeedMode.STANDARD ->
                    repository.getAllValidPosts()
                FeedMode.BOOKMARKS_ONLY ->
                    repository.getAllBookmarkedPosts()
                FeedMode.ARCHIVED_ONLY ->
                    repository.getAllArchivedPosts()
                FeedMode.BLOCKED_PERSONAS_ONLY ->
                    repository.getAllPostsFromBlockedPersonas()
            }
            flow.onEach { _isLoading.value = false }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}