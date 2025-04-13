    package com.example.projecthub.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.projecthub.data.Assignment
import com.example.projecthub.usecases.MainAppBar
import com.example.projecthub.usecases.bottomNavigationBar
import com.example.projecthub.usecases.bubbleBackground
import com.example.projecthub.viewModel.authViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun createAssignmentScreen(
    navController: NavController,
    authViewModel: authViewModel,
    assignmentId: String? = null) {


    val isEditMode = assignmentId != null
    var assignment by remember { mutableStateOf<Assignment?>(null) }
    var isLoading by remember { mutableStateOf(isEditMode) }

    LaunchedEffect(assignmentId) {
        if (isEditMode && assignmentId != null) {
            FirebaseFirestore.getInstance().collection("assignments")
                .document(assignmentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        assignment = document.toObject(Assignment::class.java)?.copy(id = document.id)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.background
    )

    var showDialog by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            MainAppBar(
                title = if (isEditMode) "Edit Assignment" else "Create Assignment",
                navController = navController as NavHostController
            )        },
        bottomBar = {
            bottomNavigationBar(navController = navController as NavHostController, currentRoute = "assignments")
        },

    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = gradientColors))
                .padding(paddingValues)
        ) {
            bubbleBackground()


            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                CreateAssignmentDialog(
                    showDialog = showDialog,
                    onDismiss = {
                        showDialog = false
                        navController.popBackStack()
                    },
                    authViewModel = authViewModel,
                    existingAssignment = assignment,
                    onAssignmentCreated = {
                        showDialog = false
                        Toast.makeText(
                            context,
                            if (isEditMode) "Assignment updated successfully!" else "Assignment created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    authViewModel: authViewModel,
    existingAssignment: Assignment? = null,
    onAssignmentCreated: () -> Unit
) {
    if (!showDialog) return

    val isEditMode = existingAssignment != null



    var title by remember { mutableStateOf(existingAssignment?.title ?: "") }
    var description by remember { mutableStateOf(existingAssignment?.description ?: "") }
    var subject by remember { mutableStateOf(existingAssignment?.subject ?: "") }
    var deadline by remember { mutableStateOf(existingAssignment?.deadline ?: "") }
    var budget by remember { mutableStateOf(existingAssignment?.budget?.toString() ?: "") }
    val showDatePicker = remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()

    val isFormValid = title.isNotBlank() && description.isNotBlank() &&
            subject.isNotBlank() && deadline.isNotBlank() && budget.isNotBlank()

    if (showDatePicker.value) {
        val currentMillis = System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },

            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        deadline = dateFormatter.format(Date(it))
                    }
                    showDatePicker.value = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(360.dp)
            .wrapContentHeight()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),

        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 8.dp,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Assignment Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title*") },
                        leadingIcon = { Icon(Icons.Default.Title, "Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            ,
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
                        .padding(bottom = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject*") },
                        leadingIcon = { Icon(Icons.Default.Book, "Subject") },
                        modifier = Modifier
                            .fillMaxWidth()
                            ,
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
                        .padding(bottom = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description*") },
                        leadingIcon = { Icon(Icons.Default.Description, "Description") },
                        modifier = Modifier
                            .fillMaxWidth()

                            .height(100.dp),
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
                        .padding(bottom = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { },
                        label = { Text("Deadline*") },
                        leadingIcon = { Icon(Icons.Default.DateRange, "Deadline") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker.value = true }) {
                                Icon(Icons.Default.CalendarToday, "Select date")
                            }
                        },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            ,
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
                        .padding(bottom = 8.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        singleLine = true,
                        value = budget,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                budget = it
                            }
                        },
                        label = { Text("Budget (in ₹)*") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, "Budget") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            ,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancel",
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = {
                            if (isFormValid) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
//                                val assignmentRef = db.collection("assignments").document()
                                if (isEditMode && existingAssignment != null) {
                                    // Update existing assignment
                                    val assignmentRef = db.collection("assignments").document(existingAssignment.id)
                                    val updates = hashMapOf<String, Any>(
                                        "title" to title,
                                        "description" to description,
                                        "subject" to subject,
                                        "deadline" to deadline,
                                        "budget" to (budget.toIntOrNull() ?: 0)
                                    )

                                    assignmentRef.update(updates)
                                        .addOnSuccessListener {
                                            onAssignmentCreated()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Failed to update: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }else{
                                    val assignmentRef = db.collection("assignments").document()
                                    val assignment = hashMapOf(
                                        "id" to assignmentRef.id,
                                        "title" to title,
                                        "description" to description,
                                        "subject" to subject,
                                        "deadline" to deadline,
                                        "budget" to (budget.toIntOrNull() ?: 0),
                                        "createdBy" to userId,
                                        "timestamp" to FieldValue.serverTimestamp()
                                    )

                                    assignmentRef.set(assignment)
                                        .addOnSuccessListener {
                                            onAssignmentCreated()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Failed: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                }

                            } else {
                                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                                colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            "Post",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

    )
}