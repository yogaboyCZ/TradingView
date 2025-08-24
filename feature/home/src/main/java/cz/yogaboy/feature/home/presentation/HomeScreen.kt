package cz.yogaboy.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    showPlaceholder: Boolean,
    content: @Composable (Modifier) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF2E7DFF), Color(0xFF6A5CF6))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    CenterAlignedTopAppBar(
                        title = { Text("Hledat ISIN, ETF atd.") },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    TopSearchBar(
                        value = query,
                        onValueChange = onQueryChange,
                        onSearch = { if (query.isNotBlank()) onSearch() },
                        onClear = { onQueryChange("") }
                    )
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (showPlaceholder) {
                    Spacer(Modifier.height(12.dp))
                    ElevatedCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Domovská obrazovka", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text("Výsledky vyhledávání a graf zobrazíme po zadání produktu.")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                content(Modifier.fillMaxSize())
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
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = onClear) { Icon(Icons.Filled.Close, null) }
                }
            },
            placeholder = { Text("aapl, nvda, msft…") },
            singleLine = true,
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.15f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.75f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.75f),
                focusedLeadingIconColor = Color.White,
                unfocusedLeadingIconColor = Color.White,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White
            )
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = {
                if (value.isNotBlank()) {
                    focusManager.clearFocus(force = true)
                    onSearch()
                }
            },
            enabled = value.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.4f),
                disabledContentColor = Color.Black.copy(alpha = 0.6f)
            )
        ) { Text("Hledat") }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        query = "",
        onQueryChange = {},
        showPlaceholder = true,
        onSearch = {},
    )
}
