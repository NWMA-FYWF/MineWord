package io.github.nwma_fywf.mineword.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToThemeSetting: () -> Unit,
    onNavigateToFontSetting: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToLearningSetting: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsEntryCard(
            icon = Icons.Default.Palette,
            title = "主题设置",
            subtitle = "主题模式、动态颜色、主题色",
            onClick = onNavigateToThemeSetting
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsEntryCard(
            icon = Icons.Default.FontDownload,
            title = "字体设置",
            subtitle = "字体样式、自定义字体",
            onClick = onNavigateToFontSetting
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsEntryCard(
            icon = Icons.Default.DataUsage,
            title = "数据管理",
            subtitle = "导出、导入、清空数据",
            onClick = onNavigateToDataManagement
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsEntryCard(
            icon = Icons.Default.School,
            title = "学习设置",
            subtitle = "复习提醒、提醒时间",
            onClick = onNavigateToLearningSetting
        )
    }
}

@Composable
private fun SettingsEntryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}