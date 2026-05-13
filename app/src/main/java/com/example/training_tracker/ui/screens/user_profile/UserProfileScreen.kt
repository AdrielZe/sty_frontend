package com.example.training_tracker.ui.screens.user_profile

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.training_tracker.data.models.User
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanDark
import com.example.training_tracker.R
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@Suppress("DEPRECATION")
@Composable
fun UserProfileScreen(
    onBackClick: () -> Unit,
    viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showEditNameDialog by remember { mutableStateOf(false) }

    val imageCropperLauncher = rememberLauncherForActivityResult(
        contract = com.canhub.cropper.CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    context.filesDir.listFiles()?.forEach { file ->
                        if (file.name.startsWith("profile_pic")) file.delete()
                    }
                    val timestamp = System.currentTimeMillis()
                    val profilePicFile = File(context.filesDir, "profile_pic_$timestamp.jpg")
                    val outputStream = FileOutputStream(profilePicFile)
                    inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
                    viewModel.updateProfilePicture(Uri.fromFile(profilePicFile).toString())
                } catch (e: Exception) {
                    Log.e("UserProfileScreen", "Erro ao salvar imagem cortada", e)
                }
            }
        } else {
            result.error?.printStackTrace()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(it, takeFlags)
                val cropOptions = com.canhub.cropper.CropImageContractOptions(
                    it,
                    com.canhub.cropper.CropImageOptions(
                        aspectRatioX = 1,
                        aspectRatioY = 1,
                        fixAspectRatio = true,
                        guidelines = com.canhub.cropper.CropImageView.Guidelines.ON,
                        imageSourceIncludeCamera = false
                    )
                )
                imageCropperLauncher.launch(cropOptions)
            } catch (e: Exception) {
                Log.e("PhotoPicker", "Erro ao obter permissão persistente", e)
            }
        }
    }

    if (showEditNameDialog && uiState is UserProfileUiState.Success) {
        val user = (uiState as UserProfileUiState.Success).user
        EditNameDialog(
            initialName = user.name,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateUserName(newName)
                showEditNameDialog = false
            }
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is UserProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CyanAccent
                    )
                }
                is UserProfileUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UserProfileUiState.Success -> {
                    ProfileContent(
                        user = state.user,
                        stats = state.stats,
                        onEditPhotoClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onEditNameClick = { showEditNameDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    stats: UserStats,
    onEditPhotoClick: () -> Unit,
    onEditNameClick: () -> Unit
) {
    val headerGradient = Brush.verticalGradient(
        colors = listOf(CyanDark.copy(alpha = 0.85f), CyanAccent.copy(alpha = 0.4f), Color.Transparent)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Hero header with gradient + avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(headerGradient)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarWithEdit(user = user, onEditPhotoClick = onEditPhotoClick)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = onEditNameClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar_nome),
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "@${user.name.lowercase().replace(" ", "")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Section label
            SectionLabel(
                text = stringResource(R.string.estat_sticas),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.treinos_min),
                    value = stats.totalWorkouts.toString(),
                    icon = Icons.Default.FitnessCenter
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.series),
                    value = stats.totalSets.toString(),
                    icon = Icons.Default.Reorder
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.vol_max),
                    value = "${String.format(Locale.US, "%.0f", stats.maxVolume)} kg",
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.foco),
                    value = stringResource(stats.mostTrainedMuscleGroup?.resId ?: R.string.nenhum_registro),
                    icon = Icons.Default.MyLocation
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionLabel(
                text = stringResource(R.string.maior_carga),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HeaviestLiftCard(
                stats = stats,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AvatarWithEdit(user: User, onEditPhotoClick: () -> Unit) {
    val avatarSize = 112.dp
    val badgeSize = 32.dp

    Box(
        modifier = Modifier.size(avatarSize + badgeSize / 2),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .align(Alignment.TopStart)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(listOf(CyanAccent, CyanDark)),
                    shape = CircleShape
                )
                .clickable { onEditPhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!user.profilePicture.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profilePicture)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = stringResource(R.string.foto_de_perfil),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.gym)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.gym),
                    contentDescription = stringResource(R.string.foto_de_perfil_padr_o),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Box(
            modifier = Modifier
                .size(badgeSize)
                .shadow(4.dp, CircleShape)
                .background(CyanAccent, CircleShape)
                .clickable { onEditPhotoClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.8.sp,
        modifier = modifier
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeaviestLiftCard(stats: UserStats, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(listOf(CyanAccent.copy(alpha = 0.3f), CyanDark.copy(alpha = 0.15f))),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stats.heaviestExerciseName ?: stringResource(R.string.nenhum_registro),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", stats.heaviestWeight)} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EditNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editar_nome)) },
        text = {
            TextField(
                value = name,
                onValueChange = { newValue ->
                    if (newValue.length <= 20) {
                        if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                            if (newValue.startsWith(" ") || newValue.contains("  ")) return@TextField
                            val spaceCount = newValue.count { it == ' ' }
                            if (spaceCount <= 2) {
                                val formattedName = newValue.split(" ").joinToString(" ") { word ->
                                    word.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault())
                                        else it.toString()
                                    }
                                }
                                name = formattedName
                            }
                        }
                    }
                },
                label = { Text(stringResource(R.string.nome_de_usu_rio)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = CyanAccent,
                    unfocusedIndicatorColor = Color.Gray,
                    cursorColor = CyanAccent
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.salvar), color = CyanAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}
