package com.example.training_tracker.ui.screens.workout_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
import com.example.training_tracker.ui.theme.Typography

@Composable
fun WorkoutScreen(
    workoutUiState: WorkoutUiState,
    onWeightChange: (String, String) -> Unit,
    onRepsChange: (String, String) -> Unit)
{
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            WorkoutNameCard(workout = workoutUiState.workout)

            LazyColumn {
                items(workoutUiState.workout?.exercises ?: emptyList()) { exercise ->
                    ExerciseCard(
                        modifier = Modifier,
                        exercise = exercise,
                        onRepsChange = { newValue -> onRepsChange(exercise.id, newValue) },
                        onWeightChange = { newValue -> onWeightChange(exercise.id, newValue)}
                    )
                }
            }


        }
    }

}

@Composable
fun WorkoutNameCard(modifier: Modifier = Modifier, workout: Workout?) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingExtraSmall)
            .border(
                width = 2.dp,
                color = CyanAccent,
                shape = RoundedCornerShape(Dimens.cornerRadius)
            )
            .padding(20.dp),
        color = Color.Transparent,
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = workout?.name.toString(),
                style = Typography.bodyLarge
            )
        }
    }
}

@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit
) {
    Card(
        modifier = modifier
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = exercise.name,
                style = Typography.titleLarge
            )

            (Spacer(modifier = Modifier.height(12.dp)))

            Row (modifier = Modifier.fillMaxWidth()){
                Text(
                    modifier = Modifier
                        .weight(0.1f),
                    text = "Série",
                    textAlign = TextAlign.Center,
                    style = Typography.bodyMedium
                )

                Text(
                    modifier = Modifier
                        .weight(0.2f),
                    text = "Peso (Kg)",
                    textAlign = TextAlign.Center,
                    style = Typography.bodyMedium
                )

                Text(
                    modifier = Modifier
                        .weight(0.2f),
                    text = "Repetições",
                    textAlign = TextAlign.Center,
                    style = Typography.bodyMedium
                )

                Spacer(
                    modifier = Modifier
                        .weight(0.1f)

                )
            }

                SetLine(
                    modifier = Modifier
                        .fillMaxWidth(),
                    exercise = exercise,
                    onRepsChange = { onRepsChange(it) },
                    onWeightChange = { onWeightChange(it)}
                )
        }
    }
}

@Composable
fun SetLine(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit
) {
    Card(
        modifier = modifier
            .padding(vertical = Dimens.paddingMedium)
            .height(65.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = Dimens.paddingExtraSmall, horizontal = Dimens.paddingExtraSmall)
                .fillMaxSize()
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(0.1f)
                    .height(36.dp)
                    .wrapContentSize(Alignment.Center),
                text = exercise.sets.toString(),
                textAlign = TextAlign.Center,
                style = Typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            InputTextBox(
                modifier = Modifier
                    .weight(0.15f)
                    .height(50.dp),
                inputName = "Weight",
                inputValue = exercise.weight,
                onRepsChange = { onWeightChange(it) }
            )

            InputTextBox(
                modifier = Modifier
                    .weight(0.15f)
                    .height(50.dp),
                inputName = "Reps",
                inputValue = exercise.reps,
                onRepsChange = { onRepsChange(it) }
            )

            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .height(36.dp)
                    .clip(CircleShape)
                    .clickable { /* ação */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Check",
                    tint = CyanAccent,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun InputTextBox(
    modifier: Modifier = Modifier,
    inputName: String,
    inputValue: String,
    onRepsChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = CyanAccent.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = inputValue,
            onValueChange = { onRepsChange(it) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                if (inputValue.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun AddSetButton()