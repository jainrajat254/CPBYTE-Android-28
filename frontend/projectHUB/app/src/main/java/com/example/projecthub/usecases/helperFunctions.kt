package com.example.projecthub.usecases

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.projecthub.navigation.routes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.projecthub.data.Bid
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun bottomNavigationBar(navController: NavHostController, currentRoute: String) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home_page",
            onClick = {
                if (currentRoute != "home_page") {
                    navController.navigate("home_page") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Assignments") },
            label = { Text("Assignments") },
            selected = currentRoute == "assignments",
            onClick = {
                if (currentRoute != "assignments") {
                    navController.navigate(routes.assignmentsScreen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }

            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Email, contentDescription = "Messages") },
            label = { Text("Messages") },
            selected = currentRoute == "messages",
            onClick = {
                //Navigate to messages screen

            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate(routes.profilePage.route) {

                    }
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(title: String, navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            IconButton(onClick = { /* Notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications"
                )
            }
            IconButton(onClick = { navController.navigate(routes.settingsScreen.route) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}

@Composable
fun bubbleBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
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
                .size(100.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.39f))
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
                .size(130.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (70).dp, y = (40).dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.38f))
        )
    }
}
@Composable
fun CreateAssignmentFAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier.offset(y = 35.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create Assignment",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

//fun formatTimestamp(timestamp: Timestamp): String {
//    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
//    return sdf.format(timestamp.toDate())
//}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
fun updateBidStatus(bidId: String, status: String, context: Context, onSuccess: () -> Unit = {}) {
    FirebaseFirestore.getInstance().collection("bids")
        .document(bidId)
        .update("status", status)
        .addOnSuccessListener {
            Toast.makeText(context, "Bid $status", Toast.LENGTH_SHORT).show()
            onSuccess() // Call the callback when successful
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

fun checkExistingBid(
    assignmentId: String,
    userId: String,
    onResult: (Boolean, Bid?) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("bids")
        .whereEqualTo("assignmentId", assignmentId)
        .whereEqualTo("bidderId", userId)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                onResult(false, null)
            } else {
                val existingBid = documents.documents[0].toObject(Bid::class.java)
                onResult(true, existingBid)
            }
        }
        .addOnFailureListener {
            onResult(false, null)
        }
}