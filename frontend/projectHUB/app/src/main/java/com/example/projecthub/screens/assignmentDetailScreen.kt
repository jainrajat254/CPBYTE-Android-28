package com.example.projecthub.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projecthub.R
import com.example.projecthub.data.Assignment
import com.example.projecthub.data.Bid
import com.example.projecthub.usecases.MainAppBar
import com.example.projecthub.usecases.bottomNavigationBar
import com.example.projecthub.usecases.formatTimestamp
import com.example.projecthub.usecases.updateBidStatus
import com.example.projecthub.viewModel.authViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun assignmentDetailScreen(
    navController: NavHostController,
    assignmentId: String,
    authViewModel: authViewModel
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var assignment by remember { mutableStateOf<Assignment?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showBidDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(assignmentId) {
        isLoading = true
        if (assignmentId.isNotBlank()) {
            FirebaseFirestore.getInstance().collection("assignments")
                .document(assignmentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val loadedAssignment = document.toObject(Assignment::class.java)
                        assignment = loadedAssignment?.copy(id = document.id)
                    } else {
                        Toast.makeText(context, "Assignment not found", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading assignment: ${e.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    navController.popBackStack()
                }
        } else {
            Toast.makeText(context, "Invalid assignment ID", Toast.LENGTH_SHORT).show()
            isLoading = false
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { MainAppBar(title = "Assignment Details", navController = navController) },
        bottomBar = { bottomNavigationBar(navController = navController, currentRoute = "assignment_details") }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            assignment?.let { assignmentData ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AssignmentDetailCard(assignmentData)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentUserId != assignmentData.createdBy) {
                        Button(
                            onClick = { showBidDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Place Bid")
                        }
                    }
                }

                if (showBidDialog) {
                    PlaceBidDialog(
                        assignmentId = assignmentId,
                        onDismiss = { showBidDialog = false },
                        onBidPlaced = {
                            showBidDialog = false
                            Toast.makeText(context, "Bid placed successfully!", Toast.LENGTH_SHORT).show()
                        },
                        budget = assignmentData.budget
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceBidDialog(
    assignmentId: String,
    onDismiss: () -> Unit,
    onBidPlaced: () -> Unit,
    budget : Int,
    existingBid: Bid? = null
) {
    val context = LocalContext.current
    var bidAmount by remember { mutableStateOf(existingBid?.bidAmount?.toString() ?: "") }
    var isSubmitting by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    val showDatePicker = remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var enterDate by remember { mutableStateOf("") }


    if (showDatePicker.value) {
        val currentMillis = System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentMillis)


        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },

            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        enterDate = dateFormatter.format(Date(it))
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

    val db = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: ""
                }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(existingBid != null) "Edit Your Bid" else "Place a Bid") },
        text = {
            Column {
                Text(
                    text = "Enter your bid amount:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = bidAmount,
                    onValueChange = { bidAmount = it },
                    label = { Text("Bid Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = enterDate,
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
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bidAmount.isNotBlank() && currentUser != null && enterDate.isNotBlank()) {
                        val amount = bidAmount.toIntOrNull()
                        if (amount != null && amount > 0 && amount <= budget) {
                            isSubmitting = true

                            if (
                                existingBid != null
                            ) {
                                db.collection("bids").document(existingBid.id)
                                    .update("bidAmount", amount)
                                    .addOnSuccessListener {
                                        isSubmitting = false
                                        onBidPlaced()
                                    }
                                    .addOnFailureListener { e ->
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            "Error updating bid: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                val newBid = Bid(
                                    id = db.collection("bids").document().id,
                                    assignmentId = assignmentId,
                                    bidderId = currentUser.uid,
                                    bidderName = userName,
                                    bidAmount = amount,
                                    enterCompletionDate = enterDate,
                                    timestamp = Timestamp.now()
                                )


                                db.collection("bids").document(newBid.id)
                                    .set(newBid)
                                    .addOnSuccessListener {
                                        isSubmitting = false
                                        onBidPlaced()
                                    }
                                    .addOnFailureListener { e ->
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            "Error placing bid: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }else{
                            Toast.makeText(
                                context,
                                "Please enter a valid bid amount (greater than ₹0 and not more than ₹$budget)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                enabled = !isSubmitting && bidAmount.isNotBlank() && enterDate.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (existingBid != null) "Update Bid" else "Submit Bid")                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AssignmentDetailCard(assignment: Assignment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Posted: ${formatTimestamp(assignment.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(Icons.Default.Timer, "Deadline: ${assignment.deadline}")
                InfoChip(Icons.Default.CurrencyRupee, "₹${assignment.budget}")
            }
        }
    }
}