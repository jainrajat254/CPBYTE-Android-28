    package com.example.projecthub.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projecthub.data.Assignment
import com.example.projecthub.data.UserProfileCache
import com.example.projecthub.navigation.routes
import com.example.projecthub.usecases.CreateAssignmentFAB
import com.example.projecthub.usecases.MainAppBar
import com.example.projecthub.usecases.bottomNavigationBar
import com.example.projecthub.usecases.bubbleBackground
import com.example.projecthub.usecases.getCurrentDate
import com.example.projecthub.usecases.getGreeting
import com.example.projecthub.viewModel.AuthState
import com.example.projecthub.viewModel.authViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.components.Lazy
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun homePage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: authViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()
    var showAssignmentDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isLoading by remember { mutableStateOf(true) }

    //Implement Stats
    var postedAssignments by remember { mutableStateOf(0) }
    var activeAssignments by remember { mutableStateOf(0) }
    var completedAssignments by remember { mutableStateOf(0) }

    var myAssignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }
    var availableAssignments by remember { mutableStateOf<List<Assignment>>(emptyList()) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val currentDate = Calendar.getInstance().time

    LaunchedEffect(Unit) {
        UserProfileCache.preloadUserProfiles()
    }

    LaunchedEffect(userId){
        isLoading = true
        if(userId != null){
            var completedQueries = 0

            db.collection("assignments")
                .whereEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener{documents ->
                    val allUserAssignments = documents.toObjects(Assignment::class.java)
                    postedAssignments = allUserAssignments.size

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    myAssignments = allUserAssignments.filter { assignment ->
                        try {
                            val deadlineDate = dateFormat.parse(assignment.deadline)
                            deadlineDate != null && deadlineDate.after(currentDate)
                        } catch (e: Exception) {
                            true
                        }
                    }
                    activeAssignments = myAssignments.size
                    completedAssignments = allUserAssignments.size - activeAssignments
                    completedQueries++
                    if (completedQueries >= 2) isLoading = false
                }
                .addOnFailureListener {
                    completedQueries++
                    if (completedQueries >= 2) isLoading = false
                }
            // add the condition for assignents user has bidded on later
            db.collection("assignments")
                .whereNotEqualTo("createdBy", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val allAvailableAssignments = documents.toObjects(Assignment::class.java)

                    // Filter assignments with deadline not passed
                    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    availableAssignments = allAvailableAssignments.filter { assignment ->
                        try {
                            val deadlineDate = dateFormat.parse(assignment.deadline)
                            deadlineDate != null && deadlineDate.after(currentDate)
                        } catch (e: Exception) {
                            true // Include if date parsing fails
                        }
                    }

                    completedQueries++
                    if (completedQueries >= 2) isLoading = false
                }
                .addOnFailureListener {
                    completedQueries++
                    if (completedQueries >= 2) isLoading = false
                }

        }else{
            isLoading = false
        }
    }


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login_page") {
                popUpTo("home_page") { inclusive = true }
            }
            else -> Unit
        }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = getGreeting(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getCurrentDate(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*Notifications  */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = {navController.navigate(routes.settingsScreen.route)}) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            bottomNavigationBar(navController = navController, currentRoute = "home_page")
        },
        floatingActionButton = {
            CreateAssignmentFAB(onClick = { showAssignmentDialog = true })
        },
        floatingActionButtonPosition = FabPosition.Center



    ) { paddingValues ->
        if (showAssignmentDialog) {
            CreateAssignmentDialog(
                showDialog = showAssignmentDialog,
                onDismiss = { showAssignmentDialog = false },
                authViewModel = authViewModel,
                onAssignmentCreated = {
                    showAssignmentDialog = false
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            bubbleBackground()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item {
                    StatsCard(
                        posted = postedAssignments,
                        active = activeAssignments,
                        completed = completedAssignments
                    )
                }
                item {
                    MyAssignmentsSection(
                        navController = navController,
                        assignments = myAssignments,
                        isLoading = isLoading
                    )
                }
                item {
                    AvailableAssignmentsSection(
                        navController = navController,
                        assignments = availableAssignments,
                        isLoading = isLoading
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

            }
        }
    }
}
@Composable
private fun StatsCard(posted: Int, active: Int, completed: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(0.98f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Your Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.Create,
                    label = "Posted",
                    value = posted.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Default.Assignment,
                    label = "Working On",
                    value = active.toString(),
                    color = Color(0xFFFFA000)
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Completed",
                    value = completed.toString(),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MyAssignmentsSection(
    navController: NavHostController,
    assignments: List<Assignment>,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Assignments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { navController.navigate(routes.assignmentsScreen.route) }) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                assignments.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active assignments")
                    }
                }
                else -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(assignments) { assignment ->
                            Assignment_Card(assignment = assignment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailableAssignmentsSection(
    navController: NavHostController,
    assignments: List<Assignment>,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Assignments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { navController.navigate(routes.assignmentsScreen.route) }) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                assignments.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No available assignments")
                    }
                }
                else -> {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(assignments) { assignment ->
                            Assignment_Card(assignment = assignment)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Assignment_Card(assignment: Assignment) {
    Card(
        modifier = Modifier.width(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {  }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(Icons.Default.Timer, "Deadline: ${assignment.deadline}")
                InfoChip(Icons.Default.CurrencyRupee, "${assignment.budget}")
            }
        }
    }
}
//
//@Composable
//private fun InfoChip(icon: ImageVector, text: String) {
//    Surface(
//        modifier = Modifier.padding(end = 4.dp),
//        color = MaterialTheme.colorScheme.surfaceVariant,
//        shape = MaterialTheme.shapes.small
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                icon,
//                contentDescription = null,
//                modifier = Modifier.size(16.dp),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Spacer(modifier = Modifier.width(4.dp))
//            Text(
//                text = text,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}