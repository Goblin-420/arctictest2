package com.example.arcticplex.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.arcticplex.data.PlexApiService
import com.example.arcticplex.data.PlexDirectory
import com.example.arcticplex.data.PlexMediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class MenuCategory(val label: String) {
    ON_DECK("CONTINUE WATCHING"),
    RECENTLY_ADDED("RECENTLY ADDED"),
    MOVIES("MOVIES"),
    TV_SHOWS("TV SHOWS"),
    SETTINGS("SETTINGS")
}

class PlexViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("arctic_plex_prefs", Context.MODE_PRIVATE)

    private val _serverUrl = MutableStateFlow(prefs.getString("server_url", "http://192.168.1.100:32400") ?: "")
    val serverUrl: StateFlow<String> = _serverUrl

    private val _plexToken = MutableStateFlow(prefs.getString("plex_token", "") ?: "")
    val plexToken: StateFlow<String> = _plexToken

    private val _selectedCategory = MutableStateFlow(MenuCategory.RECENTLY_ADDED)
    val selectedCategory: StateFlow<MenuCategory> = _selectedCategory

    private val _mediaItems = MutableStateFlow<List<PlexMediaItem>>(emptyList())
    val mediaItems: StateFlow<List<PlexMediaItem>> = _mediaItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var cachedDirectories: List<PlexDirectory> = emptyList()

    init {
        if (_plexToken.value.isNotBlank() && _serverUrl.value.isNotBlank()) {
            loadCategory(MenuCategory.RECENTLY_ADDED)
        } else {
            loadDemoData()
        }
    }

    fun saveServerConfig(url: String, token: String) {
        val cleanUrl = url.trim().removeSuffix("/")
        val cleanToken = token.trim()
        prefs.edit()
            .putString("server_url", cleanUrl)
            .putString("plex_token", cleanToken)
            .apply()
        _serverUrl.value = cleanUrl
        _plexToken.value = cleanToken
        cachedDirectories = emptyList()
        loadCategory(MenuCategory.RECENTLY_ADDED)
    }

    fun selectCategory(category: MenuCategory) {
        _selectedCategory.value = category
        if (category != MenuCategory.SETTINGS) {
            loadCategory(category)
        }
    }

    private fun loadCategory(category: MenuCategory) {
        val url = _serverUrl.value
        val token = _plexToken.value
        if (url.isBlank() || token.isBlank()) {
            loadDemoData()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val api = PlexApiService.create(url)
                if (cachedDirectories.isEmpty()) {
                    val sectionsResp = api.getSections(token)
                    cachedDirectories = sectionsResp.MediaContainer?.Directory ?: emptyList()
                }

                val response = when (category) {
                    MenuCategory.ON_DECK -> api.getOnDeck(token)
                    MenuCategory.RECENTLY_ADDED -> api.getRecentlyAdded(token)
                    MenuCategory.MOVIES -> {
                        val movieDir = cachedDirectories.firstOrNull { it.type == "movie" }
                        if (movieDir != null) api.getSectionItems(movieDir.key, token) else api.getRecentlyAdded(token)
                    }
                    MenuCategory.TV_SHOWS -> {
                        val showDir = cachedDirectories.firstOrNull { it.type == "show" }
                        if (showDir != null) api.getSectionItems(showDir.key, token) else api.getRecentlyAdded(token)
                    }
                    MenuCategory.SETTINGS -> null
                }

                _mediaItems.value = response?.MediaContainer?.Metadata ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Connection Error: ${e.localizedMessage ?: "Check Plex IP & Token in Settings"}"
                loadDemoData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reportProgress(item: PlexMediaItem, state: String, positionMs: Long, durationMs: Long) {
        val url = _serverUrl.value
        val token = _plexToken.value
        if (url.isBlank() || token.isBlank()) return
        viewModelScope.launch {
            try {
                val api = PlexApiService.create(url)
                api.updateTimeline(
                    ratingKey = item.ratingKey,
                    key = "/library/metadata/${item.ratingKey}",
                    state = state,
                    currentTimeMs = positionMs,
                    durationMs = durationMs,
                    token = token
                )
            } catch (_: Exception) {}
        }
    }

    private fun loadDemoData() {
        _mediaItems.value = listOf(
            PlexMediaItem(
                ratingKey = "demo1",
                title = "Configure Your Plex Server",
                year = 2026,
                summary = "Welcome to ArcticPlex (Arctic Zephyr: Reloaded edition). Navigate down to SETTINGS on the bottom blade menu to enter your local Plex Media Server IP address and X-Plex-Token.",
                rating = 9.5,
                contentRating = "4K HDR",
                duration = 7200000L,
                viewOffset = 0L,
                thumb = null,
                art = null,
                type = "movie",
                Media = null
            )
        )
    }
}
