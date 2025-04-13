package com.example.projecthub.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projecthub.R
import com.example.projecthub.data.UserProfile
import com.example.projecthub.usecases.MainAppBar
import com.example.projecthub.usecases.bottomNavigationBar
import com.example.projecthub.usecases.bubbleBackground
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun userProfileScreen(navController: NavHostController, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        isLoading = true
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val collegeName = document.getString("collegeName") ?: ""
                    val semester = document.getString("semester") ?: ""
                    val collegeLocation = document.getString("collegeLocation") ?: ""
                    val skills = document.get("skills") as? List<String> ?: emptyList()
                    val profilePhotoId = document.getLong("profilePhotoId")?.toInt()
                        ?: R.drawable.profilephoto1

                    userProfile = UserProfile(
                        name,
                        bio,
                        collegeName,
                        semester,
                        collegeLocation,
                        skills,
                        profilePhotoId
                    )
                } else {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
                isLoading = false
                navController.popBackStack()
            }
    }

    Scaffold(
        topBar = { MainAppBar(title = "User Profile", navController = navController) },
        bottomBar = { bottomNavigationBar(navController = navController, currentRoute = "") }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            bubbleBackground()

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (userProfile != null) {
                userProfileContent(userProfile = userProfile!!)
            }
        }
    }
}

@Composable
fun userProfileContent(userProfile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(name = userProfile.name, photoId = userProfile.profilePhotoId)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                InfoSection(
                    title = "About",
                    icon = Icons.Default.Person,
                    content = userProfile.bio
                )

                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                InfoSection(
                    title = "Education",
                    icon = Icons.Default.School,
                    content = null
                )

                ProfileDetailRow(
                    label = "College",
                    value = userProfile.collegeName,
                    icon = Icons.Default.AccountBalance
                )

                ProfileDetailRow(
                    label = "Semester",
                    value = userProfile.semester,
                    icon = Icons.Default.DateRange
                )

                ProfileDetailRow(
                    label = "Location",
                    value = userProfile.collegeLocation,
                    icon = Icons.Default.LocationOn
                )

                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                InfoSection(
                    title = "Skills",
                    icon = Icons.Default.Code,
                    content = null
                )

                SkillsGrid(skills = userProfile.skills)
            }
        }
    }
}
