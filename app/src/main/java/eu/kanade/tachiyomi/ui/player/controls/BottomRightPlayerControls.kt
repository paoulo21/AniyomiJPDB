/*
 * Copyright 2024 Abdallah Mehiz
 * https://github.com/abdallahmehiz/mpvKt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.kanade.tachiyomi.ui.player.controls

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.tachiyomi.ui.player.controls.components.ControlsButton
import eu.kanade.tachiyomi.ui.player.controls.components.FilledControlsButton
import eu.kanade.tachiyomi.ui.player.execute
import eu.kanade.tachiyomi.ui.player.executeLongPress
import eu.kanade.tachiyomi.ui.player.parseJPDBResponse
import eu.kanade.tachiyomi.ui.player.sendRequest
import `is`.xyz.mpv.MPVLib
import kotlinx.coroutines.launch
import tachiyomi.domain.custombuttons.model.CustomButton

@Composable
fun BottomRightPlayerControls(
    customButton: CustomButton?,
    customButtonTitle: String,
    skipIntroButton: String?,
    onPressSkipIntroButton: () -> Unit,
    isPipAvailable: Boolean,
    onAspectClick: () -> Unit,
    onPipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    var overlayVisible by remember { mutableStateOf(false) }
    var overlayText by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        Row(Modifier.align(Alignment.BottomEnd)) {
            if (skipIntroButton != null) {
                FilledControlsButton(
                    text = skipIntroButton,
                    onClick = onPressSkipIntroButton,
                    onLongClick = {},
                )
            } else if (customButton != null) {
                FilledControlsButton(
                    text = customButtonTitle,
                    onClick = customButton::execute,
                    onLongClick = customButton::executeLongPress,
                )
            }

            ControlsButton(
                text = "Sub",
                onClick = {
                    val currentSubtitle = MPVLib.getPropertyString("sub-text")
                    if (!currentSubtitle.isNullOrBlank()) {
                        Toast.makeText(context, currentSubtitle, Toast.LENGTH_SHORT).show()

                        sendRequest(currentSubtitle) { result ->
                            val parsedResult = if (result.isNotBlank()) parseJPDBResponse(result) else "No data from API"
                            overlayText = parsedResult
                            overlayVisible = true
                            scope.launch {
                                val delay = if (parsedResult.startsWith("ERROR:")) 5000L else 3000L
                                kotlinx.coroutines.delay(delay)
                                overlayVisible = false
                            }
                        }

                    } else {
                        Toast.makeText(context, "No subtitle on screen", Toast.LENGTH_SHORT).show()
                    }
                },
            )

            if (isPipAvailable) {
                ControlsButton(
                    Icons.Default.PictureInPictureAlt,
                    onClick = onPipClick,
                )
            }

            ControlsButton(
                Icons.Default.AspectRatio,
                onClick = onAspectClick,
            )
        }

        if (overlayVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 12.dp,
                    tonalElevation = 12.dp,
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                        .widthIn(max = 500.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = overlayText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }
    }
}
