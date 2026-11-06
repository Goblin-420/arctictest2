package com.example.arcticplex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arcticplex.data.PlexMediaItem
import com.example.arcticplex.ui.ArcticVideoPlayer
import com.example.arcticplex.ui.ArcticZephyrHomeScreen
import com.example.arcticplex.ui.PlexViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: PlexViewModel = viewModel()
            val serverUrl by viewModel.serverUrl.collectAsState()
            val plexToken by viewModel.plexToken.collectAsState()
            val selectedCategory by viewModel.selectedCategory.collectAsState()
            val mediaItems by viewModel.mediaItems.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val errorMessage by viewModel.errorMessage.collectAsState()

            var activePlaybackItem by remember { mutableStateOf<PlexMediaItem?>(null) }

            val currentPlaying = activePlaybackItem
            if (currentPlaying != null) {
                val partKey = currentPlaying.Media?.firstOrNull()?.Part?.firstOrNull()?.key
                val streamUrl = if (partKey != null) {
                    "$serverUrl$partKey?X-Plex-Token=$plexToken"
                } else {
                    "$serverUrl/video/:/transcode/universal/start.m3u8?path=/library/metadata/${currentPlaying.ratingKey}&mediaIndex=0&partIndex=0&protocol=hls&X-Plex-Token=$plexToken"
                }

                ArcticVideoPlayer(
                    mediaItem = currentPlaying,
                    streamUrl = streamUrl,
                    onProgressUpdate = { state, pos, dur ->
                        viewModel.reportProgress(currentPlaying, state, pos, dur)
                    },
                    onBack = { activePlaybackItem = null }
                )
            } else {
                ArcticZephyrHomeScreen(
                    serverUrl = serverUrl,
                    plexToken = plexToken,
                    selectedCategory = selectedCategory,
                    mediaItems = mediaItems,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onSaveSettings = { url, token -> viewModel.saveServerConfig(url, token) },
                    onPlayMedia = { item ->
                        if (item.Media != null) {
                            activePlaybackItem = item
                        }
                    }
                )
            }
        }
    }
}
