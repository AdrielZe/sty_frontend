package com.example.training_tracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.training_tracker.ui.screens.home.HomeUiState
import com.example.training_tracker.ui.screens.home.HomeViewModel
import com.example.training_tracker.ui.screens.welcome.WelcomeScreen
import com.example.training_tracker.ui.components.StyLogo
import com.example.training_tracker.ui.components.StyLogoLayout
import androidx.compose.runtime.CompositionLocalProvider
import com.example.training_tracker.ui.theme.AccentThemes
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.LocalAccentTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GymTrackerApp(
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val uiState by homeViewModel.uiState.collectAsState()

    val accentTheme = when (val s = uiState) {
        is HomeUiState.Success -> AccentThemes.fromName(s.user?.accentThemeName)
        else -> AccentThemes.Cyan
    }

    CompositionLocalProvider(LocalAccentTheme provides accentTheme) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                AppLoadingScreen()
            }
            is HomeUiState.Success -> {
                val user = (uiState as HomeUiState.Success).user
                if (user == null) {
                    WelcomeScreen(onSaveComplete = {
                        // O Flow de uiState deve atualizar automaticamente
                    })
                } else {
                    GymTrackerNavHost()
                }
            }
            is HomeUiState.Error -> {
                AppErrorScreen(
                    message = (uiState as HomeUiState.Error).message ?: stringResource(R.string.erro_ao_carregar_dados),
                    onRetry = { /* O StateFlow do Room deve tentar se reconectar automaticamente */ }
                )
            }
        }
    }
    } // CompositionLocalProvider
}

@Composable
fun AppLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StyLogo(
                titleSize = 48.sp,
                layout = StyLogoLayout.VERTICAL
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = AppTheme.accent.light,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun AppErrorScreen(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.ops_algo_deu_errado),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent.light),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = stringResource(R.string.tentar_novamente), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
