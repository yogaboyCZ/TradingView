package cz.yogaboy.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.theme.tradingColors
import cz.yogaboy.core.design.R as DR

@Composable
internal fun TopSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    collapseProgress: Float,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val mergeProgress = ((collapseProgress - 0.08f) / 0.27f).coerceIn(0f, 1f)
        val buttonCollapse = ((collapseProgress - 0.68f) / 0.32f).coerceIn(0f, 1f)
        val buttonWidth = 104.dp * (1f - buttonCollapse)
        val gapWidth = LocalDimens.current.medium * (1f - mergeProgress)
        val searchWidth = 56.dp +
            (maxWidth - 56.dp - buttonWidth - gapWidth) * (1f - collapseProgress)
        val searchShape = RoundedCornerShape(28.dp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(searchWidth)
                    .height(56.dp)
                    .clip(searchShape)
                    .background(MaterialTheme.tradingColors.searchContainer)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.tradingColors.onBackdrop.copy(alpha = 0.48f),
                                MaterialTheme.tradingColors.onBackdrop.copy(alpha = 0.10f),
                            ),
                        ),
                        shape = searchShape,
                    ),
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .graphicsLayer {
                            alpha = 1f - ((collapseProgress - 0.82f) / 0.18f)
                                .coerceIn(0f, 1f)
                        },
                    singleLine = true,
                    shape = searchShape,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (value.isNotBlank()) {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSearch()
                            }
                        },
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.tradingColors.onBackdrop,
                        )
                    },
                    trailingIcon = {
                        if (value.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onClear()
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(DR.string.action_clear_search),
                                    tint = MaterialTheme.tradingColors.onBackdrop,
                                )
                            }
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(DR.string.home_search_placeholder),
                            color = MaterialTheme.tradingColors.onBackdrop.copy(alpha = 0.72f),
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.tradingColors.onBackdrop,
                        focusedTextColor = MaterialTheme.tradingColors.onBackdrop,
                        unfocusedTextColor = MaterialTheme.tradingColors.onBackdrop,
                    ),
                )

                if (collapseProgress > 0.90f) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = stringResource(DR.string.action_expand_search),
                        tint = MaterialTheme.tradingColors.onBackdrop,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                if (collapseProgress > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onExpand()
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                    )
                }
            }

            Spacer(Modifier.width(gapWidth))
            Button(
                onClick = {
                    if (value.isNotBlank()) {
                        focusManager.clearFocus(force = true)
                        onSearch()
                    }
                },
                enabled = value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.tradingColors.searchButton,
                    contentColor = MaterialTheme.tradingColors.onBackdrop,
                    disabledContainerColor = MaterialTheme.tradingColors.searchDisabledContainer,
                    disabledContentColor = MaterialTheme.tradingColors.onBackdrop.copy(alpha = 0.72f),
                ),
                modifier = Modifier
                    .width(buttonWidth)
                    .height(56.dp)
                    .graphicsLayer {
                        alpha = 1f - mergeProgress
                        translationX = -size.width * 0.82f * mergeProgress
                    },
                shape = RoundedCornerShape(28.dp),
            ) {
                if (buttonWidth > 1.dp) {
                    Text(
                        text = stringResource(DR.string.home_search_button),
                        color = MaterialTheme.tradingColors.onBackdrop,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
