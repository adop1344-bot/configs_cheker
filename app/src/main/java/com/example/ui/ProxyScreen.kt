package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ProxyItem
import com.example.model.ProxyProtocol
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentMint
import com.example.ui.theme.AccentRed
import com.example.ui.theme.PrimaryCyan
import com.example.ui.theme.PrimaryPurple
import com.example.utils.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyScreen(viewModel: ProxyViewModel) {
    val checkingState by viewModel.checkingState.collectAsState()
    val workingProxies by viewModel.workingProxies.collectAsState()
    val rawCount by viewModel.rawConfigsCount.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    val strings = viewModel.strings()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Checker, 1 = Settings

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val filteredProxies = remember(workingProxies, selectedProtocol, searchQuery) {
        workingProxies.filter { item ->
            val matchesProtocol = selectedProtocol == null || item.protocol == selectedProtocol
            val matchesSearch = searchQuery.isBlank() ||
                    item.remark.contains(searchQuery, ignoreCase = true) ||
                    item.server.contains(searchQuery, ignoreCase = true) ||
                    item.protocol.displayName.contains(searchQuery, ignoreCase = true)
            matchesProtocol && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryCyan, PrimaryPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.appSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        val isChecking = checkingState is CheckingState.Fetching || checkingState is CheckingState.Testing
                        IconButton(
                            onClick = {
                                if (isChecking) viewModel.cancelScan() else viewModel.startScan()
                            },
                            modifier = Modifier.testTag("scan_button")
                        ) {
                            Icon(
                                imageVector = if (isChecking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isChecking) strings.btnStop else strings.btnCheck,
                                tint = if (isChecking) AccentRed else PrimaryCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Speed, contentDescription = strings.tabMain) },
                    label = { Text(strings.tabMain, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PrimaryCyan,
                        indicatorColor = PrimaryCyan,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_main")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = strings.tabSettings) },
                    label = { Text(strings.tabSettings, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = PrimaryCyan,
                        indicatorColor = PrimaryCyan,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (selectedTab == 0) {
            // Main Checker Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Status & Progress Card
                StatusCard(
                    checkingState = checkingState,
                    totalFound = rawCount,
                    workingCount = workingProxies.size,
                    strings = strings,
                    onCancel = { viewModel.cancelScan() },
                    onReScan = { viewModel.startScan() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Buttons bar
                QuickActionsBar(
                    workingCount = workingProxies.size,
                    strings = strings,
                    onCopy5 = { viewModel.copyTopN(5) },
                    onCopy10 = { viewModel.copyTopN(10) },
                    onCopy20 = { viewModel.copyTopN(20) },
                    onCopyAll = { viewModel.copyAll() },
                    onSaveDownloads = { viewModel.saveToDownloads() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips & Search Bar
                FilterAndSearchSection(
                    selectedProtocol = selectedProtocol,
                    onProtocolSelected = { viewModel.setProtocolFilter(it) },
                    searchQuery = searchQuery,
                    onSearchChanged = { viewModel.setSearchQuery(it) },
                    strings = strings
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Proxy List
                if (filteredProxies.isEmpty()) {
                    EmptyStateView(checkingState = checkingState, strings = strings)
                } else {
                    Text(
                        text = "${strings.workingConfigsTitle} (${filteredProxies.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredProxies, key = { it.id }) { proxy ->
                            ProxyCardItem(
                                item = proxy,
                                onCopy = { viewModel.copySingleProxy(proxy) }
                            )
                        }
                    }
                }
            }
        } else {
            // Settings Screen Content
            Box(modifier = Modifier.padding(innerPadding)) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StatusCard(
    checkingState: CheckingState,
    totalFound: Int,
    workingCount: Int,
    strings: AppStrings.Strings,
    onCancel: () -> Unit,
    onReScan: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (checkingState) {
                is CheckingState.Fetching -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryCyan,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = checkingState.statusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Stop, contentDescription = strings.btnStop, tint = AccentRed)
                        }
                    }
                }

                is CheckingState.Testing -> {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = PrimaryCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${strings.statusTesting} (${checkingState.tested}/${checkingState.total})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${strings.working}: ${checkingState.workingCount}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentMint
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Stop, contentDescription = strings.btnStop, tint = AccentRed)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { checkingState.percentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryCyan,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                is CheckingState.Finished -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccentMint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.statusFinished,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${strings.tested}: $totalFound | ${strings.working}: $workingCount",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onReScan,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.btnRepeat,
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                is CheckingState.Error -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = checkingState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentRed,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onReScan,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(strings.btnRepeat, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                CheckingState.Idle -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.statusIdle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onReScan,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.btnCheck, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsBar(
    workingCount: Int,
    strings: AppStrings.Strings,
    onCopy5: () -> Unit,
    onCopy10: () -> Unit,
    onCopy20: () -> Unit,
    onCopyAll: () -> Unit,
    onSaveDownloads: () -> Unit
) {
    Column {
        Text(
            text = strings.quickActionsTitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ActionButton(
                    text = strings.copy5,
                    icon = Icons.Default.ContentCopy,
                    tag = "copy_5_button",
                    onClick = onCopy5,
                    enabled = workingCount > 0
                )
            }
            item {
                ActionButton(
                    text = strings.copy10,
                    icon = Icons.Default.ContentCopy,
                    tag = "copy_10_button",
                    onClick = onCopy10,
                    enabled = workingCount > 0
                )
            }
            item {
                ActionButton(
                    text = strings.copy20,
                    icon = Icons.Default.ContentCopy,
                    tag = "copy_20_button",
                    onClick = onCopy20,
                    enabled = workingCount > 0
                )
            }
            item {
                ActionButton(
                    text = "${strings.copyAll} ($workingCount)",
                    icon = Icons.Default.FlashOn,
                    tag = "copy_all_button",
                    onClick = onCopyAll,
                    isAccent = true,
                    enabled = workingCount > 0
                )
            }
            item {
                ActionButton(
                    text = strings.saveDownloads,
                    icon = Icons.Default.Download,
                    tag = "download_txt_button",
                    onClick = onSaveDownloads,
                    enabled = workingCount > 0
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit,
    isAccent: Boolean = false,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = if (isAccent) PrimaryCyan else MaterialTheme.colorScheme.surface,
        border = if (isAccent) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAccent) Color.Black else if (enabled) PrimaryCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isAccent) Color.Black else if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun FilterAndSearchSection(
    selectedProtocol: ProxyProtocol?,
    onProtocolSelected: (ProxyProtocol?) -> Unit,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    strings: AppStrings.Strings
) {
    Column {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = { Text(strings.searchPlaceholder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = PrimaryCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = selectedProtocol == null,
                    onClick = { onProtocolSelected(null) },
                    label = { Text(strings.filterAll, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            items(ProxyProtocol.values().filter { it != ProxyProtocol.OTHER }) { protocol ->
                FilterChip(
                    selected = selectedProtocol == protocol,
                    onClick = {
                        onProtocolSelected(if (selectedProtocol == protocol) null else protocol)
                    },
                    label = { Text(protocol.displayName, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun ProxyCardItem(
    item: ProxyItem,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { onCopy() }
            .testTag("proxy_item_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Protocol Tag
                ProtocolBadge(protocol = item.protocol)

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.remark,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.server}:${item.port}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Latency Pill
            LatencyPill(latencyMs = item.latencyMs)

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProtocolBadge(protocol: ProxyProtocol) {
    val (bg, textCol) = when (protocol) {
        ProxyProtocol.VLESS -> Pair(Color(0xFF1E3A8A), Color(0xFF93C5FD))
        ProxyProtocol.TROJAN -> Pair(Color(0xFF581C87), Color(0xFFE9D5FF))
        ProxyProtocol.VMESS -> Pair(Color(0xFF065F46), Color(0xFFA7F3D0))
        ProxyProtocol.SHADOWSOCKS -> Pair(Color(0xFF831843), Color(0xFFFBCFE8))
        ProxyProtocol.HYSTERIA2 -> Pair(Color(0xFF7C2D12), Color(0xFFFED7AA))
        ProxyProtocol.TUIC -> Pair(Color(0xFF134E4A), Color(0xFF99F6E4))
        ProxyProtocol.OTHER -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = protocol.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}

@Composable
fun LatencyPill(latencyMs: Long) {
    val (pillColor, textColor) = when {
        latencyMs < 150 -> Pair(Color(0xFF052E16), AccentMint)
        latencyMs < 350 -> Pair(Color(0xFF451A03), AccentAmber)
        else -> Pair(Color(0xFF450A0A), AccentRed)
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(pillColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "${latencyMs} ms",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun EmptyStateView(checkingState: CheckingState, strings: AppStrings.Strings) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        if (checkingState is CheckingState.Fetching || checkingState is CheckingState.Testing) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryCyan)
                Spacer(modifier = Modifier.height(12.dp))
                Text(strings.emptyStateScanning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.emptyStateNoConfigs,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
