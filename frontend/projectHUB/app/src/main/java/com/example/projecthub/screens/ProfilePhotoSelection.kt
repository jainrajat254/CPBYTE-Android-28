package com.example.projecthub.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.projecthub.R

@Composable
fun ProfilePhotoSelection(
    showDialog: MutableState<Boolean>,
    selectedPhotoId: Int,
    onPhotoSelected: (Int) -> Unit
) {
    val profilePhotos = listOf(
        R.drawable.profilephoto1,
        R.drawable.profilephoto2,
        R.drawable.profilephoto3,
        R.drawable.profilephoto4,
        R.drawable.profilephoto5,
        R.drawable.profilephoto6,
        R.drawable.profilephoto7,
        R.drawable.profilephoto8,
        R.drawable.profilephoto9
    )

    if (showDialog.value) {
        PhotoSelectionDialog(
            photos = profilePhotos,
            selectedPhotoId = selectedPhotoId,
            onPhotoSelected = { photo ->
                onPhotoSelected(photo)
                showDialog.value = false
            },
            onDismiss = { showDialog.value = false }
        )
    }
}

@Composable
fun PhotoSelectionDialog(
    photos: List<Int>,
    selectedPhotoId: Int,
    onPhotoSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Select a Profile Photo")
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(photos.size) { index ->
                    val photo = photos[index]
                    Image(
                        painter = painterResource(id = photo),
                        contentDescription = "Profile Option ${index + 1}",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (photo == selectedPhotoId)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable {
                                onPhotoSelected(photo)
                            }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}