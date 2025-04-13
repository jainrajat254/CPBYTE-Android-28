package com.example.projecthub.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projecthub.usecases.MainAppBar
import com.example.projecthub.usecases.bottomNavigationBar
import com.example.projecthub.usecases.bubbleBackground
import com.example.projecthub.viewModel.ThemeViewModel
import com.example.projecthub.viewModel.authViewModel

@Composable
fun settingsScreen(
    navController: NavHostController,
    authViewModel: authViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
){
    var showSignOutDialog by remember { mutableStateOf(false) }
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()


    val selectedTheme = themeMode

    Scaffold(
        topBar = {
            MainAppBar(title = "Settings",navController = navController)
        },
        bottomBar = {
            bottomNavigationBar(navController = navController, currentRoute = "settings")
        }
    ){paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ){
            bubbleBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ){
                SettingsSection(
                    title = "My Account",
                    icon = Icons.Default.Person,
                    items = listOf(
                        SettingItem(
                            title = "Profile Information",
                            icon = Icons.Default.AccountCircle,
                            onClick = { navController.navigate("profile_page") }
                        ),
                        SettingItem(
                            title = "Change Password",
                            icon = Icons.Default.Lock,
                            onClick = { navController.navigate("change_password_page") },
                        )
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))


                SettingsSection(
                    title = "Appearance",
                    icon = Icons.Default.Palette,
                    items = listOf(
                        SettingItem(
                            title = "Theme",
                            icon = Icons.Default.BrightnessMedium,
                            onClick = { /* Theme dialog will be handled separately */ },
                            trailingContent = {
                                ThemeSelector(
                                    selectedTheme = selectedTheme,
                                    onThemeSelected = { theme ->
                                        themeViewModel.setThemeMode(theme)

                                    }
                                )
                            }
                        )

                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                SettingsSection(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    items = listOf(
                        SettingItem(
                            title = "Push Notifications",
                            icon = Icons.Default.NotificationsActive,
                            onClick = { /* Navigate to notification settings */ }
                        ),
                        SettingItem(
                            title = "Email Notifications",
                            icon = Icons.Default.Email,
                            onClick = { /* Navigate to email settings */ }
                        )
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))


                SettingsSection(
                    title = "Privacy & Security",
                    icon = Icons.Default.Security,
                    items = listOf(
                        SettingItem(
                            title = "Privacy Policy",
                            icon = Icons.Default.PrivacyTip,
                            onClick = { /* Navigate to privacy policy */ }
                        ),
                        SettingItem(
                            title = "Terms of Service",
                            icon = Icons.Default.Assignment,
                            onClick = { /* Navigate to terms of service */ }
                        )
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))



                Spacer(modifier = Modifier.height(16.dp))

                SettingsSection(
                    title = "Account Actions",
                    icon = Icons.Default.ExitToApp,
                    items = listOf(
                        SettingItem(
                            title = "Sign Out",
                            icon = Icons.Default.Logout,
                            iconTint = MaterialTheme.colorScheme.error,
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = { showSignOutDialog = true }
                        )
                    )
                )


            }

        }

        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                containerColor = MaterialTheme.colorScheme.surface.copy(0.95f),
                title = { Text("Sign Out") },
                text = { Text("Are you sure you want to sign out?") },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.signout()
                            navController.navigate("login_page") {
                                popUpTo("home_page") { inclusive = true }
                            }
                            showSignOutDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sign Out")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showSignOutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }


    }
}

data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val subtitle: String? = null,
    val iconTint: Color = Color.Unspecified,
    val titleColor: Color = Color.Unspecified,
    val onClick: () -> Unit,
    val trailingContent: @Composable (() -> Unit)? = null
)

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    items: List<SettingItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            items.forEach { item ->
                SettingItemRow(item)

                if (item != items.last()) {
                    Divider(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .padding(start = 40.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingItemRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = item.onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = if (item.iconTint != Color.Unspecified) item.iconTint else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.titleColor != Color.Unspecified) item.titleColor else MaterialTheme.colorScheme.onSurface
            )

            if (item.subtitle != null) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item.trailingContent?.invoke() ?: Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
@Composable
fun ThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Add "Default" to the themes list
    val themes = listOf("Light", "Dark", "Default")

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(selectedTheme)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(180.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            themes.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(theme) },
                    onClick = {
                        onThemeSelected(theme)
                        expanded = false
                    },
                    trailingIcon = {
                        if (theme == selectedTheme) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}