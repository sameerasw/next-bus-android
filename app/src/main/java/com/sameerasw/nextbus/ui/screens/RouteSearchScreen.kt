package com.sameerasw.nextbus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val popularRoutes = listOf(
    "Colombo → Kandy",
    "Colombo → Galle",
    "Colombo → Jaffna",
    "Kandy → Colombo",
    "Galle → Colombo",
    "Colombo → Matara",
    "Kandy → Galle",
    "Colombo → Anuradhapura",
    "Colombo → Nuwara Eliya",
    "Colombo → Negombo",
    "Colombo → Batticaloa",
    "Colombo → Trincomalee",
    "Colombo → Kurunegala",
    "Colombo → Ratnapura",
    "Colombo → Matara",
    "Kandy → Nuwara Eliya"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchScreen(
    onSelectRoute: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRoutes = if (searchQuery.isEmpty()) {
        popularRoutes
    } else {
        popularRoutes.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Route",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search routes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredRoutes) { route ->
                    ListItem(
                        headlineContent = { Text(route) },
                        modifier = Modifier.clickable {
                            onSelectRoute(route)
                            onBack()
                        }
                    )
                }
            }
        }
    }
}

