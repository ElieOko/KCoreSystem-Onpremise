package com.schoolstats.presentation.screens.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.presentation.viewmodel.UsersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UsersScreen(viewModel: UsersViewModel = koinViewModel()) {
    val users by viewModel.users.collectAsState()
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Gestion des utilisateurs", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { user ->
                Surface(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(user.fullName, fontWeight = FontWeight.SemiBold)
                        Text(user.email, style = MaterialTheme.typography.bodySmall)
                        Text("${user.role.name} — ${if (user.isActive) "Actif" else "Inactif"}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
