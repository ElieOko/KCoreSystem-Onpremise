package com.schoolstats.desktop.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schoolstats.presentation.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    fun submit() {
        if (!uiState.isLoading) viewModel.login(email.trim(), password)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                    submit()
                    true
                } else {
                    false
                }
            },
    ) {
        val wide = maxWidth >= 980.dp
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                BrandPanel(Modifier.weight(1.05f).fillMaxHeight())
                FormPanel(
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    onSubmit = ::submit,
                    onDemoLogin = {
                        email = "demo@local"
                        password = "demo"
                        viewModel.login("demo@local", "demo")
                    },
                    modifier = Modifier.weight(0.95f).fillMaxHeight(),
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                BrandPanel(Modifier.fillMaxWidth().height(220.dp), compact = true)
                FormPanel(
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    onSubmit = ::submit,
                    onDemoLogin = {
                        email = "demo@local"
                        password = "demo"
                        viewModel.login("demo@local", "demo")
                    },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun BrandPanel(modifier: Modifier = Modifier, compact: Boolean = false) {
    val navy = Color(0xFF0B2A4A)
    val teal = Color(0xFF0F766E)
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(navy, Color(0xFF123A63), teal.copy(alpha = 0.92f))),
        ),
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 36.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 24.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 28.dp else 48.dp),
            verticalArrangement = if (compact) Arrangement.Center else Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text("KCoreSystem", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Statistiques scolaires", color = Color.White.copy(0.75f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (!compact) {
                    Text(
                        "Collecte, validation et fichier central\nde la sous-division.",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 36.sp,
                    )
                    Text(
                        "Les écoles renseignent les effectifs. La sous-division agrège, contrôle et pilote.",
                        color = Color.White.copy(0.78f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            if (!compact) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    BrandPoint(Icons.Default.BarChart, "Effectifs, âges, enseignants, administratif et ouvriers")
                    BrandPoint(Icons.Default.Verified, "Validation des déclarations par la sous-division")
                    BrandPoint(Icons.Default.Hub, "Fichier central agrégé, graphiques et exports")
                    Spacer(Modifier.height(8.dp))
                    Text("Année scolaire 2025-2026", color = Color.White.copy(0.55f), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun BrandPoint(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF5EEAD4), modifier = Modifier.size(18.dp))
        }
        Text(text, color = Color.White.copy(0.92f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FormPanel(
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    error: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onSubmit: () -> Unit,
    onDemoLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = 36.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Connexion", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Accédez à l’espace de saisie ou au fichier central de votre sous-division.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Adresse e-mail") },
                    placeholder = { Text("ex. ecole@sous-division.cd") },
                    leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Mot de passe") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = onTogglePassword) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Masquer le mot de passe" else "Afficher le mot de passe",
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    shape = RoundedCornerShape(12.dp),
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = onSubmit,
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3D68)),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Se connecter", fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(Modifier.weight(1f))
                    Text("  ou  ", color = MaterialTheme.colorScheme.onSurface.copy(0.45f), style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(Modifier.weight(1f))
                }
                OutlinedButton(
                    onClick = onDemoLogin,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Ouvrir la démo sous-division")
                }
                Text(
                    "Démo : demo@local  ·  mot de passe demo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                )
            }
        }
    }
}
