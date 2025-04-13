package com.example.projecthub.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ManageHistory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.projecthub.R
import com.example.projecthub.data.Assignment
import com.example.projecthub.data.Bid
import com.example.projecthub.data.UserProfileCache
import com.example.projecthub.usecases.CreateAssignmentFAB
import com.example.projecthub.usecases.MainAppBar
import com.example.projecthub.usecases.bottomNavigationBar
import com.example.projecthub.usecases.checkExistingBid
import com.example.projecthub.usecases.formatTimestamp
import com.example.projecthub.usecases.updateBidStatus
import com.example.projecthub.viewModel.authViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun assignmentsScreen(navController: NavHostController,
                      authViewModel: authViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("My Assignments", "All Assignments")
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val assignmentsState = remember { mutableStateListOf<Assignment>() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    var assignmentToEdit by remember { mutableStateOf<Assignment?>(null) }




    LaunchedEffect(Unit) {
        isLoading = true
        Firebase.firestore.collection("assignments")
            .get()
            .addOnSuccessListener { result ->
                assignmentsState.clear()
                val assignments = result.documents.mapNotNull { doc ->
                    val assignment = doc.toObject(Assignment::class.java)
                    assignment?.copy(id = doc.id)
                }
                assignmentsState.addAll(assignments)
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch assignments", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }


    Scaffold(
        topBar = {
            MainAppBar(title = "Assignments", navController = navController)
        },
        bottomBar = {
            bottomNavigationBar(navController = navController, currentRoute = "assignments_page")
        },


        floatingActionButton = {
            CreateAssignmentFAB(onClick = { showDialog = true })
        },
        floatingActionButtonPosition = FabPosition.Center,

        ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab
            ) {
                tabs.forEachIndexed{ index, title ->
                    Tab(
                        text = { Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        }
                    )
                }

            }
            when (selectedTab) {
                0 -> {
                    val myAssignments = if (currentUserId != null) {
                        assignmentsState.filter {
                            it.createdBy == currentUserId
                        }
                    }else emptyList()
                    AvailableAssignmentsList(myAssignments, isLoading,navController,

                        onEditAssignment = { assignment ->
                            assignmentToEdit = assignment
                            showDialog = true
                        }
                    )
                }
                1 -> AvailableAssignmentsList(assignments = assignmentsState,navController = navController,
                    onEditAssignment = { assignment ->
                        assignmentToEdit = assignment
                        showDialog = true
                    })
            }

        }
    }
    if (showDialog) {
        CreateAssignmentDialog(
            showDialog = showDialog,
            onDismiss = {
                showDialog = false
                assignmentToEdit = null  // Clear the assignment being edited
            },
            authViewModel = authViewModel,
            existingAssignment = assignmentToEdit,
            onAssignmentCreated = {
                showDialog = false
                assignmentToEdit = null  // Clear the assignment being edited
                Toast.makeText(
                    context,
                    if (assignmentToEdit != null) "Assignment updated successfully!" else "Assignment created successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

}

@Composable
fun AvailableAssignmentsList(assignments: List<Assignment>,isLoading: Boolean = false,navController: NavHostController,onEditAssignment: (Assignment) -> Unit = {}) {
    if(isLoading){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }else if (assignments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No assignments available")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(assignments) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    navController = navController,
                    onEditAssignment = onEditAssignment
                )
            }
        }
    }
}

//@Composable
//fun PostedAssignmentsList(assignments: List<Assignment>){
//    if(assignments.isEmpty()){
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("You haven't posted any assignments yet")
//        }
//    }else{
//        LazyColumn (
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//
//        ){
//            items(assignments){ assignment ->
//                AssignmentCard(assignment)
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentCard(assignment: Assignment, navController: NavHostController,onEditAssignment: (Assignment) -> Unit = {}) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isCreatedByCurrentUser = assignment.createdBy == currentUserId
    var showBidDialog by remember { mutableStateOf(false) }
    var showBidsListDialog by remember { mutableStateOf(false) }

    var posterName by remember {
        mutableStateOf(UserProfileCache.getUserName(assignment.createdBy))
    }
    var posterPhotoId by remember {
        mutableStateOf(UserProfileCache.getProfilePhotoId(assignment.createdBy))
    }

    var hasExistingBid by remember { mutableStateOf(false) }
    var existingBidData by remember { mutableStateOf<Bid?>(null) }


    LaunchedEffect(assignment.createdBy) {
        if (posterName == "Unknown User") {
            FirebaseFirestore.getInstance().collection("users")
                .document(assignment.createdBy)
                .get()
                .addOnSuccessListener { document ->
                    posterName = document.getString("name") ?: "Unknown User"
                    document.getLong("profilePhotoId")?.toInt()?.let {
                        posterPhotoId = it
                    }
                }
        }
    }
    LaunchedEffect(currentUserId, assignment.id) {
        currentUserId?.let { userId ->
            checkExistingBid(assignment.id, userId) { hasBid, existingBid ->
                hasExistingBid = hasBid
                existingBidData = existingBid
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = posterPhotoId),
                    contentDescription = "Poster profile photo",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate("user_profile/${assignment.createdBy}")
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = posterName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        navController.navigate("user_profile/${assignment.createdBy}")
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

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

            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )

            // Assignment details
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

                Text(
                    text = "Posted: ${formatTimestamp(assignment.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(Icons.Default.Timer, "Deadline: ${assignment.deadline}")
                InfoChip(Icons.Default.CurrencyRupee, "₹${assignment.budget}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isCreatedByCurrentUser) {
                    OutlinedButton(
                        onClick = { showBidsListDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = "View Bids",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Bids")
                        }
                    }

                    OutlinedButton(
                        onClick = { onEditAssignment(assignment) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Assignment",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage")
                        }
                    }
                } else {
                    Button(
                        onClick = { showBidDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (hasExistingBid) Icons.Default.Edit else Icons.Default.MonetizationOn,
                                contentDescription = if (hasExistingBid) "Edit Bid" else "Place Bid",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasExistingBid) "Edit Bid" else "Place Bid")
                        }
                    }
                }
            }
        }
    }
    if (showBidDialog) {
        PlaceBidDialog(
            assignmentId = assignment.id,
            budget = assignment.budget,
            existingBid = if (hasExistingBid) existingBidData else null,
            onDismiss = { showBidDialog = false },
            onBidPlaced = {
                showBidDialog = false
                Toast.makeText(
                    context,
                    if (hasExistingBid) "Bid updated successfully!" else "Bid placed successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (showBidsListDialog) {
        BidsListDialog(
            assignmentId = assignment.id,
            navController = navController,
            onDismiss = { showBidsListDialog = false }
        )
    }
}

@Composable
fun BidsListDialog(assignmentId: String,navController: NavHostController, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var bids by remember { mutableStateOf<List<Bid>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }

    fun loadBids() {
        isLoading = true
        FirebaseFirestore.getInstance().collection("bids")
            .whereEqualTo("assignmentId", assignmentId)
            .get()
            .addOnSuccessListener { documents ->
                bids = documents.toObjects(Bid::class.java)
                isLoading = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading bids: ${e.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }
    LaunchedEffect(assignmentId, refreshTrigger) {
        loadBids()
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface.copy(0.90f),
        onDismissRequest = onDismiss,
        title = { Text("Bids for Assignment") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    bids.isEmpty() -> {
                        Text(
                            text = "No bids have been placed on this assignment yet.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn {
                            items(bids) { bid ->
                                BidItem(bid = bid, navController= navController,onStatusChanged = {
                                    refreshTrigger++
                                })
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BidItem(bid: Bid, navController: NavHostController, onStatusChanged: () -> Unit = {}) {
    val context = LocalContext.current
    var profilePhotoResId by remember { mutableStateOf(R.drawable.profilephoto1) }


    LaunchedEffect(bid.bidderId) {
        FirebaseFirestore.getInstance().collection("users")
            .document(bid.bidderId)
            .get()
            .addOnSuccessListener { document ->
                document.getLong("profilePhotoId")?.toInt()?.let {
                    profilePhotoResId = it
                }
            }
    }


    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                verticalAlignment = Alignment.CenterVertically,

            ) {
                Image(
                    painter = painterResource(id = profilePhotoResId),
                    contentDescription = "Bidder profile photo",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate("user_profile/${bid.bidderId}")
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    modifier = Modifier.clickable { navController.navigate("user_profile/${bid.bidderId}")},
                    text = bid.bidderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

                Surface(
                    color = when(bid.status) {
                        "accepted" -> MaterialTheme.colorScheme.primaryContainer
                        "rejected" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = bid.status.capitalize(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Bid Amount: ₹${bid.bidAmount}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Submitted: ${formatTimestamp(bid.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (bid.status == "pending") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            updateBidStatus(bid.id, "rejected",context){
                                onStatusChanged()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Reject")
                    }

                    Button(
                        onClick = {
                            updateBidStatus(bid.id, "accepted", context) {
                                onStatusChanged()
                            }
                        }
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        modifier = Modifier.padding(end = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}