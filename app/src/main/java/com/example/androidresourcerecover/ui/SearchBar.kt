package com.example.androidresourcerecover.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidresourcerecover.R

/**
 * Search bar with history and filters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchHistory: List<String> = emptyList(),
    onClearHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isActive by remember { mutableStateOf(false) }

    androidx.compose.material3.SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = {
            onSearch(it)
            isActive = false
        },
        active = isActive,
        onActiveChange = { isActive = it },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        }
    ) {
        if (searchHistory.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.search_history),
                            style = MaterialTheme.typography.labelLarge
                        )
                        TextButton(onClick = onClearHistory) {
                            Text(stringResource(R.string.clear_search_history))
                        }
                    }
                }

                items(searchHistory) { historyQuery ->
                    ListItem(
                        headlineContent = { Text(historyQuery) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.clickable {
                            onQueryChange(historyQuery)
                            onSearch(historyQuery)
                            isActive = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Filter chips for sort and filter options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    onSortClick: () -> Unit,
    onDateFilterClick: () -> Unit,
    onSizeFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = false,
            onClick = onSortClick,
            label = { Text(stringResource(R.string.sort)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        FilterChip(
            selected = false,
            onClick = onDateFilterClick,
            label = { Text(stringResource(R.string.date_filter)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        FilterChip(
            selected = false,
            onClick = onSizeFilterClick,
            label = { Text(stringResource(R.string.size_filter)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}
