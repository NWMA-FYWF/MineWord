package io.github.nwma_fywf.mineword.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingScreen(
    viewModel: ThemeSettingViewModel,
    onNavigateBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val customPrimaryColor by viewModel.customPrimaryColor.collectAsState()
    val customSecondaryColor by viewModel.customSecondaryColor.collectAsState()
    val customTertiaryColor by viewModel.customTertiaryColor.collectAsState()
    var showColorPickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.theme_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ThemeSettingsSection(
                themeMode = themeMode,
                useDynamicColor = useDynamicColor,
                customPrimaryColor = customPrimaryColor,
                onThemeModeChange = viewModel::setThemeMode,
                onUseDynamicColorChange = viewModel::setUseDynamicColor,
                onThemeColorSelected = viewModel::setCustomThemeColor,
                onClearThemeColor = viewModel::clearCustomThemeColor,
                onCustomColorClick = { showColorPickerDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showColorPickerDialog) {
        CustomColorPickerDialog(
            onColorSelected = { primary, secondary, tertiary ->
                viewModel.setCustomThemeColor(primary, secondary, tertiary)
                showColorPickerDialog = false
            },
            onDismiss = { showColorPickerDialog = false }
        )
    }
}