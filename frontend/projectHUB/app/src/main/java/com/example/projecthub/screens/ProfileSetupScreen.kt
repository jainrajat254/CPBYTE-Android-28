package com.example.projecthub.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projecthub.R
import com.example.projecthub.ui.theme.SilverGray
import com.example.projecthub.viewModel.authViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavHostController,authViewModel: authViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var collegeName by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var collegeLocation by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }

    val skills = remember { mutableStateListOf<String>() }
    var profilePhoto by remember { mutableStateOf<Uri?>(null) }

    var selectedPhotoId by remember { mutableStateOf(R.drawable.profilephoto1) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    val maxBioWords = 50
    val maxSkills = 10

    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val isValid =
        name.isNotBlank() && collegeName.isNotBlank() && semester.isNotBlank() && collegeLocation.isNotBlank()

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.background
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-70).dp, y = (-100).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.40f))
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.39f))
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(4.dp),  // Add some padding for the border
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                            .background(SilverGray.copy(alpha = 0.5f))
                            .clickable { }, // Profile photo click handler
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhoto != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )
                        } else if (selectedPhotoId != 0) {

                            Image(
                                painter = painterResource(id = selectedPhotoId),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Add profile photo",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .clickable {
                                showPhotoDialog = true
                            },           //edit profile photo
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit profile photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                }
                if (showPhotoDialog) {
                    ProfilePhotoSelection(
                        showDialog = remember { mutableStateOf(showPhotoDialog) },
                        selectedPhotoId = selectedPhotoId,
                        onPhotoSelected = { photoId ->
                            selectedPhotoId = photoId
                            showPhotoDialog = false
                        }
                    )
                }

                Text(
                    text = "Add Photo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name*") },
                        leadingIcon = { Icon(Icons.Default.Person, "Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
//                        val wordCount by  remember { derivedStateOf {
//                            if (bio.isBlank()) 0 else bio.trim().split("\\s+".toRegex()).size
//                        }
//
//                        }
//                        val exceedsLimit by remember { derivedStateOf { wordCount > maxBioWords }}

                        val wordList = bio.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                        val wordCount = wordList.size
                        val exceedsLimit = wordCount > maxBioWords


                        OutlinedTextField(
                            value = bio,
                            onValueChange = { newText ->
                                val newWordList = newText.trim().split("\\s+".toRegex())
                                    .filter { it.isNotEmpty() }
                                if (newWordList.size <= maxBioWords || newText.length < bio.length) {
                                    bio =
                                        if (newWordList.size >= maxBioWords) newText.trimEnd() else newText
                                }
                            },
                            label = {
                                Row() {
                                    Text("Bio")
                                    Text(
                                        text = "$wordCount/$maxBioWords words",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (exceedsLimit) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                            .padding(start = 8.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (exceedsLimit) Color.Red  //fix
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                unfocusedBorderColor = if (exceedsLimit) Color.Red //fix
                                else Color.Transparent,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                            )
                        )


                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = collegeName,
                        onValueChange = { collegeName = it },
                        label = { Text("College Name*") },
                        leadingIcon = { Icon(Icons.Default.School, "College") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    var isExpanded by remember { mutableStateOf(false) }
                    val semesterOptions = listOf("1", "2", "3", "4", "5", "6", "7", "8")

                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = semester,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Current Semester*") },
                            leadingIcon = { Icon(Icons.Default.DateRange, "Semester") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                                .width(120.dp)
                                .align(Alignment.End)
// fix the alignment

                        ) {
                            semesterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        semester = option
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = collegeLocation,
                        onValueChange = { collegeLocation = it },
                        label = { Text("College Location*") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, "Location") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                Text(
                    text = "Skills",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(vertical = 8.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = skill,
                            onValueChange = { skill = it },
                            label = { Text("Add Skill") },
                            leadingIcon = { Icon(Icons.Default.Code, "Skills") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Button(
                            onClick = {
                                if (skill.isNotBlank() && skills.size < maxSkills) {
                                    skills.add(skill)
                                    skill = ""
                                } else if (skills.size >= maxSkills) {
                                    Toast.makeText(
                                        context,
                                        "Maximum 10 skills allowed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .padding(end = 8.dp, top = 8.dp),
                            enabled = skill.isNotBlank() && skills.size < maxSkills
                        ) {
                            Text("Add")
                        }
                    }
                }
                if (skills.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(skills) { currentSkill ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(currentSkill) },
                                icon = {

                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove skill",
                                        modifier = Modifier.size(16.dp)
                                    )

                                }
                            )
                        }
                    }
                }

                //Change it later
                Button(
                    onClick = {
                        val profile = hashMapOf(
                            "profilePhoto" to selectedPhotoId,
                            "name" to name,
                            "bio" to bio,
                            "collegeName" to collegeName,
                            "semester" to semester,
                            "collegeLocation" to collegeLocation,
                            "skills" to skills
                        )
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .set(profile)
                                .addOnSuccessListener {
                                    authViewModel.completeProfileSetup()
                                    Toast.makeText(context, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home_page")
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    enabled = isValid
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Save Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


        }


    }

}

//fun saveProfileToFireStore() {
//    val userProfile = hashMapOf(
//        "name" to name,
//        "bio" to bio,
//        "collegeName" to collegeName,
//        "semester" to semester,
//        "collegeLocation" to collegeLocation,
//        "skills" to skills.toList()
//    )
//
//    db.collection("users")
//        .document(name)
//        .set(userProfile)
//        .addOnSuccessListener {
//            Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
//            navController.navigate("home_page")
//        }
//        .addOnFailureListener{
//            Toast.makeText(context, "failed to save profile photo: ${it.message}!", Toast.LENGTH_SHORT).show()
//        }
//}
//






