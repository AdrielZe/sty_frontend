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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.training_tracker.data.models.User
import com.example.training_tracker.ui.theme.CyanAccent
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
                        if (file.name.startsWith("profile_pic")) {
                            file.delete()
                        }
                    }
                    
                    // Gerar um nome unico com timestamp para forcar a recomposicao da imagem no Coil
                    val timestamp = System.currentTimeMillis()
                    val profilePicFile = File(context.filesDir, "profile_pic_$timestamp.jpg")
                    val outputStream = FileOutputStream(profilePicFile)

                    inputStream?.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    viewModel.updateProfilePicture(Uri.fromFile(profilePicFile).toString())
                } catch (e: Exception) {
                    Log.e("UserProfileScreen", "Erro ao salvar imagem cortada", e)
                }
            }
        } else {
            val error = result.error
            error?.printStackTrace()
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is UserProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))

            // Foto de Perfil
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(CyanAccent, Color.Transparent)),
                            CircleShape
                        )
                        .clickable { onEditPhotoClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.profilePicture.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.profilePicture)
                                .size(Size.ORIGINAL) // Força o Coil a não pré-escalar baseado num bounding box temporário
                                .build(),
                            contentDescription = stringResource(R.string.foto_de_perfil),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.gym)
                            // Removemos o placeholder para evitar que ele interfira no cálculo de proporção enquanto a imagem real carrega
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

                SmallFloatingActionButton(
                    onClick = onEditPhotoClick,
                    containerColor = CyanAccent,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = (-8).dp, y = (-8).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(48.dp))
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditNameClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar nome",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "@${user.name.lowercase().replace(" ", "")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Volume e Músculo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                    value = stringResource(
                        stats.mostTrainedMuscleGroup?.resId ?: R.string.nenhum_registro
                    ),
                    icon = Icons.Default.MyLocation
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Maior Peso
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(CyanAccent.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = CyanAccent)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.maior_carga),
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAccent
                        )
                        Text(
                            text = stats.heaviestExerciseName
                                ?: stringResource(R.string.nenhum_registro),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", stats.heaviestWeight)} kg",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
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
        title = { Text("Editar Nome") },
        text = {
            TextField(
                value = name,
                onValueChange =  { newValue ->
                    // 1. Validação de limite de 20 caracteres
                    if (newValue.length <= 20) {

                        // 2. Permite apenas letras e espaços
                        if (newValue.all { it.isLetter() || it.isWhitespace() }) {

                            // 3. Evita começar com espaço ou ter espaços duplos
                            if (newValue.startsWith(" ") || newValue.contains("  ")) return@TextField

                            // 4. Validação de máximo de 2 espaços
                            val spaceCount = newValue.count { it == ' ' }

                            if (spaceCount <= 2) {
                                // 5. Formatação: Primeira letra de cada palavra em Maiúscula
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
                label = { Text("Nome de usuário") },
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
                Text("Salvar", color = CyanAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
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
        modifier = modifier.defaultMinSize(minHeight = 110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
