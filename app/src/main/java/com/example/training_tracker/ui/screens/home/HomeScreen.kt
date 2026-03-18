package com.example.training_tracker.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.ClickBlue
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
import com.example.training_tracker.ui.theme.Typography

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, onClickWorkoutCard: (String?) -> Unit, homeUiState: HomeUiState) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                HomeTopBar(
                    user = homeUiState.user,
                    date = homeUiState.currentDate,
                    expanded = expanded,
                    onMenuExpand = { expanded = true },
                    onMenuDismiss = { expanded = false },
                    onSettingsClick = {},
                    onLogoutClick = {}
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }) { innerPadding ->

        Column(modifier = modifier.padding(innerPadding)
            .padding(Dimens.paddingMedium),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Treino de hoje:",
                style = Typography.labelLarge
            )

            TodayWorkoutCard(
                workout = homeUiState.todayWorkout,
                onClick = { onClickWorkoutCard(homeUiState.todayWorkout?.id)}
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    user: User?,
    date: String?,
    expanded: Boolean = false,
    onMenuExpand: () -> Unit,
    onMenuDismiss: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Olá, ${user?.name}",
                    color = CyanAccent,
                    style = Typography.titleMedium
                )

                Text(
                    text = date.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        },
        actions = {
            Box {
                IconButton(onClick = onMenuExpand) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = CyanAccent,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = onMenuDismiss
                ) {
                    DropdownMenuItem(
                        text = { Text("Configurações") },
                        onClick = {
                            onSettingsClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sair") },
                        onClick = {
                            onLogoutClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ExitToApp, contentDescription = null)
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun TodayWorkoutCard(modifier: Modifier = Modifier, workout: Workout?, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingMedium, horizontal = Dimens.paddingSmall)
            .border(
                width = 2.dp,
                color = CyanAccent,
                shape = RoundedCornerShape(Dimens.cornerRadius)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ClickBlue)
            ) {
                onClick()
            }
            .padding(20.dp),
        color = Color.Transparent,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = workout?.name.toString(),
                style = Typography.bodyLarge
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TodayWorkoutCardPreview() {
    TodayWorkoutCard(
        workout = Workout(
            id = "1",
            name = "Treino de Peito",
            exercises = listOf(),
        ),
        onClick = {}
    )
}