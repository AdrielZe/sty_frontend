package com.example.training_tracker.ui.screens.workout_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.ClickBlue
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
import com.example.training_tracker.ui.theme.Typography

@Composable
fun WorkoutScreen(workoutUiState: WorkoutUiState, onRepsChange: (String, String) -> Unit) {
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
                items(workoutUiState.workout?.exercises ?: emptyList()){ exercise ->
                    ExerciseLine(modifier = Modifier, exercise = exercise, onRepsChange = {newValue -> onRepsChange(exercise.id, newValue)})
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
fun ExerciseLine(modifier: Modifier = Modifier, exercise: Exercise, onRepsChange: (String) -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.paddingMedium),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
        ) {


        Text(
            modifier = Modifier
                .weight(0.7f),
            text = exercise.name
        )

        Text(
            modifier = Modifier
                .weight(0.3f),
            text = exercise.reps.toString()
        )

        Box(
            modifier = Modifier
                .weight(0.3f)
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
                value = exercise.repsDone,
                onValueChange = { newValue -> onRepsChange(newValue) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { innerTextField ->
                    if (exercise.repsDone.isEmpty()) {
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
    }
