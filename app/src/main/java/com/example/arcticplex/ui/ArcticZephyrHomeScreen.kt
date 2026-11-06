package com.example.arcticplex.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.arcticplex.data.PlexMediaItem
import java.text.SimpleDateFormat
import java.util.*

val ArcticCyan = Color(0xFF00B4D8)
val ArcticDarkBg = Color(0xFF0B0F14)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ArcticZephyrHomeScreen(
    serverUrl: String,
    plexToken: String,
    selectedCategory: MenuCategory,
    mediaItems: List<PlexMediaItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelectCategory: (MenuCategory) -> Unit,
    onSaveSettings: (String, String) -> Unit,
    onPlayMedia: (PlexMediaItem) -> Unit
) {
    var focusedItem by remember(mediaItems) { mutableStateOf(mediaItems.firstOrNull()) }
    val currentTime = remember { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcticDarkBg)
    ) {
        Crossfade(
            targetState = focusedItem?.art,
            animationSpec = tween(durationMillis = 600),
            label = "BackdropCrossfade"
        ) { artPath ->
            if (!artPath.isNullOrBlank() && plexToken.isNotBlank()) {
                AsyncImage(
                    model = "$serverUrl$artPath?X-Plex-Token=$plexToken",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.80f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (selectedCategory != MenuCategory.SETTINGS) {
                    Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                        Text(
                            text = focusedItem?.title?.uppercase() ?: "ARCTIC PLEX",
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            focusedItem?.year?.let {
                                Text(
                                    text = it.toString(),
                                    color = ArcticCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            focusedItem?.contentRating?.let {
                                MetadataBadge(text = it)
                            }
                            val durationText = focusedItem?.getFormattedDuration() ?: ""
                            if (durationText.isNotEmpty()) {
                                Text(
                                    text = durationText,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }
                            focusedItem?.Media?.firstOrNull()?.videoResolution?.let {
                                MetadataBadge(text = "${it.uppercase()}P")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = focusedItem?.summary ?: "",
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFF6B6B),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "PLEX SERVER CONFIGURATION",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = currentTime,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.5.sp
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedCategory == MenuCategory.SETTINGS) {
                    ArcticSettingsPanel(
                        initialUrl = serverUrl,
                        initialToken = plexToken,
                        onSave = onSaveSettings
                    )
                } else if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ArcticCyan)
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(mediaItems, key = { it.ratingKey }) { item ->
                            PosterCard(
                                item = item,
                                serverUrl = serverUrl,
                                plexToken = plexToken,
                                onFocused = { focusedItem = item },
                                onClick = { onPlayMedia(item) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuCategory.entries.forEach { category ->
                        var isMenuFocused by remember { mutableStateOf(false) }
                        val isSelected = category == selectedCategory

                        Text(
                            text = category.label,
                            color = when {
                                isMenuFocused -> ArcticCyan
                                isSelected -> Color.White
                                else -> Color.White.copy(alpha = 0.45f)
                            },
                            fontSize = 17.sp,
                            fontWeight = if (isSelected || isMenuFocused) FontWeight.SemiBold else FontWeight.Light,
                            letterSpacing = 1.8.sp,
                            modifier = Modifier
                                .onFocusChanged { state ->
                                    isMenuFocused = state.isFocused
                                    if (state.isFocused && selectedCategory != category) {
                                        onSelectCategory(category)
                                    }
                                }
                                .focusable()
                                .clickable { onSelectCategory(category) }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PosterCard(
    item: PlexMediaItem,
    serverUrl: String,
    plexToken: String,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(148.dp)
            .aspectRatio(2f / 3f)
            .onFocusChanged { if (it.isFocused) onFocused() },
        scale = CardDefaults.scale(focusedScale = 1.08f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, ArcticCyan),
                shape = RoundedCornerShape(6.dp)
            )
        ),
        shape = CardDefaults.shape(RoundedCornerShape(6.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF161F29))
        ) {
            if (!item.thumb.isNullOrBlank() && plexToken.isNotBlank()) {
                AsyncImage(
                    model = "$serverUrl${item.thumb}?X-Plex-Token=$plexToken",
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            val offset = item.viewOffset ?: 0L
            val dur = item.duration ?: 0L
            if (offset > 0L && dur > 0L) {
                val progress = (offset.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(ArcticCyan)
                    )
                }
            }
        }
    }
}

@Composable
fun MetadataBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ArcticSettingsPanel(
    initialUrl: String,
    initialToken: String,
    onSave: (String, String) -> Unit
) {
    var urlInput by remember { mutableStateOf(initialUrl) }
    var tokenInput by remember { mutableStateOf(initialToken) }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF121820).copy(alpha = 0.9f))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter your local Plex Media Server URL (e.g. http://192.168.1.100:32400) and X-Plex-Token:",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Plex Server URL", color = ArcticCyan) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = ArcticCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tokenInput,
            onValueChange = { tokenInput = it },
            label = { Text("X-Plex-Token", color = ArcticCyan) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = ArcticCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSave(urlInput, tokenInput) },
            colors = ButtonDefaults.colors(
                containerColor = Color(0xFF1E293B),
                focusedContainerColor = ArcticCyan,
                contentColor = Color.White,
                focusedContentColor = Color.Black
            )
        ) {
            Text("SAVE & CONNECT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}
