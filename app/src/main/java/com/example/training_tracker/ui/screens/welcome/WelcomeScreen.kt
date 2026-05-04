package com.example.training_tracker.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.R
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient

@Composable
fun WelcomeScreen(
    onSaveComplete: () -> Unit,
    viewModel: WelcomeViewModel = viewModel(factory = WelcomeViewModel.Factory)
) {
    var name by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.bem_vindo_ao),
                style = MaterialTheme.typography.labelLarge,
                color = CyanAccent,
                letterSpacing = 2.sp
            )
            Text(
                text = stringResource(R.string.sty),
                style = TextStyle(
                    brush = CyanGradient,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.para_comecar_como_podemos_te_chamar),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { newValue ->
                    if (newValue.all { it.isLetter() || it.isWhitespace() }) {
                        name = newValue
                    }
                },
                label = { Text(stringResource(R.string.seu_nome)) },
                placeholder = { Text(stringResource(R.string.ex_jose)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    focusedLabelColor = CyanAccent,
                    cursorColor = CyanAccent
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.saveUser(name, onSaveComplete)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanAccent,
                    disabledContainerColor = CyanAccent.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = stringResource(R.string.comecar_jornada),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
