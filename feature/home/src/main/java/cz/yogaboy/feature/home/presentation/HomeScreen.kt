package cz.yogaboy.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.yogaboy.core.design.LocalDimens
import cz.yogaboy.core.design.R as DR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    content: @Composable (Modifier) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = LocalDimens.current.medium,
                            vertical = LocalDimens.current.small,
                        )
                ) {
                    TopAppBar(
                        title = {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(DR.string.home_title),
                                    color = MaterialTheme.colorScheme.onTertiary,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = if (isSystemInDarkTheme())
                                MaterialTheme.colorScheme.onPrimary else Color.Black
                        )
                    )
                    Spacer(Modifier.height(LocalDimens.current.medium))

                    TopSearchBar(
                        value = state.query,
                        onValueChange = { onEvent(HomeEvent.QueryChanged(it)) },
                        onSearch = { if (state.query.isNotBlank()) onEvent(HomeEvent.Submit) },
                        onClear = { onEvent(HomeEvent.Clear) }
                    )
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (state.showPlaceholder) {
                    Spacer(Modifier.height(LocalDimens.current.medium))
                    ElevatedCard(
                        modifier = Modifier
                            .padding(horizontal = LocalDimens.current.default)
                            .fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(LocalDimens.current.default)) {
                            Text(
                                stringResource(DR.string.home_card_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(LocalDimens.current.small))
                            Text(
                                stringResource(DR.string.home_card_desc),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(LocalDimens.current.medium))
                } else {
                    content(Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun TopSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .weight(1f)
                .clip(RoundedCornerShape(LocalDimens.current.radiusLarge)),
            singleLine = true,
            shape = RoundedCornerShape(LocalDimens.current.radiusLarge),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (value.isNotBlank()) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onSearch()
                    }
                }
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = {
                        onClear()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = stringResource(DR.string.home_search_placeholder),
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.75f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                focusedTextColor = MaterialTheme.colorScheme.onTertiary,
                unfocusedTextColor = MaterialTheme.colorScheme.onTertiary
            )
        )
        Spacer(Modifier.width(LocalDimens.current.medium))
        Button(
            onClick = {
                if (value.isNotBlank()) {
                    focusManager.clearFocus(force = true)
                    onSearch()
                }
            },
            enabled = value.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                disabledContentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                stringResource(DR.string.home_search_button),
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.75f)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        state = HomeState(query = "", showPlaceholder = true),
        onEvent = {},
        content = {}
    )
}