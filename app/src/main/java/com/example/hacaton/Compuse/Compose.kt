package com.example.hacaton.Compuse

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hacaton.ui.theme.HacatonTheme
import com.example.hacaton.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch

@PreviewScreenSizes
@Composable
fun HacatonApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.READER) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(70.dp),
                containerColor = Color.Transparent, 
                contentColor = Color.White
            ) {
                AppDestinations.entries.forEach { item ->
                    val isSelected = currentDestination == item
                    
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .offset(y = (-2).dp)
                                    .size(48.dp)
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .shadow(
                                                    elevation = 10.dp,
                                                    shape = CircleShape,
                                                    ambientColor = Color(0xFF6200EE),
                                                    spotColor = Color(0xFF6200EE)
                                                )
                                                .background(Color(0xFF6200EE), CircleShape)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        },
                        label = { },
                        selected = isSelected,
                        onClick = { currentDestination = item },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.Transparent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Transparent,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            val showReader = currentDestination == AppDestinations.READER
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (showReader) 1f else 0f }
                    .zIndex(if (showReader) 1f else 0f)
            ) {
                ReaderScreen(
                    onNavigateToList = { currentDestination = AppDestinations.LIST }
                )
            }

            if (currentDestination == AppDestinations.LIST) {
                Box(modifier = Modifier.fillMaxSize().zIndex(2f).background(MaterialTheme.colorScheme.background)) {
                    ListScreen()
                }
            }
            if (currentDestination == AppDestinations.PROFILE) {
                Box(modifier = Modifier.fillMaxSize().zIndex(2f).background(MaterialTheme.colorScheme.background)) {
                    Greeting("Profile")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier = Modifier, 
    viewModel: ReaderViewModel = viewModel(),
    onNavigateToList: () -> Unit = {}
) {
    val fileContent by viewModel.fileContent.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var textContent by remember(fileContent) { mutableStateOf(TextFieldValue(fileContent ?: "")) }
    val hasSelection = textContent.selection.length > 0
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            viewModel.openFile(it)
            scope.launch { drawerState.close() }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Reader Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()
                
                NavigationDrawerItem(
                    label = { Text("Open File", style = MaterialTheme.typography.bodyLarge) },
                    selected = false,
                    icon = { Icon(Icons.Default.Description, null) },
                    onClick = { 
                         launcher.launch(arrayOf(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                            "text/plain",
                            "*/*"
                        ))
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Recent Files", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                
                LazyColumn {
                    items(recentFiles) { file ->
                         NavigationDrawerItem(
                            label = { Text(file.name) },
                            selected = false,
                            onClick = { 
                                viewModel.openFile(Uri.parse(file.uri))
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = Color.Transparent,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp) // Standard comfortable height

                            .padding(horizontal = 16.dp), // More padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                val start = textContent.selection.start
                                val end = textContent.selection.end
                                val actualStart = minOf(start, end)
                                val actualEnd = maxOf(start, end)

                                if (actualStart != actualEnd) {
                                    val selectedText = textContent.text.substring(actualStart, actualEnd)
                                    focusManager.clearFocus()
                                    viewModel.explainText(selectedText)
                                    textContent = textContent.copy(selection = TextRange.Zero)
                                    onNavigateToList()
                                }
                            },
                            enabled = hasSelection,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasSelection) Color(0xFF6200EE) else Color(0xFFE0E0E0),
                                disabledContainerColor = Color(0xFFE0E0E0),
                                contentColor = Color.White,
                                disabledContentColor = Color.Gray
                            ),
                            // Enhanced button style (shadow, shape)
                            modifier = Modifier
                                .shadow(4.dp, CircleShape)
                                .height(40.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp)
                        ) {
                            Text("Объяснить", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (fileContent == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No file currently open.", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            launcher.launch(arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "text/plain",
                                "*/*"
                            ))
                        }) {
                            Text("Open File")
                        }
                    }
                } else {
                     Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp) // Cleaner margins
                    ) {
                        BasicTextField(
                            value = textContent,
                            onValueChange = {
                                textContent = it
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            readOnly = true,
                            // Improved readability style
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListScreen(modifier: Modifier = Modifier, viewModel: ReaderViewModel = viewModel()) {
    val explanations by viewModel.explanations.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // More side padding
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Top padding
        Text(
            "Explanations",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (explanations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "List is empty. Select text in Reader to explain.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp), // More space between cards
                contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
            ) {
                items(explanations, key = { it.id }) { item ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(16.dp), // Softer corners
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Header Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Title
                                Text(
                                    text = item.title ?: "Определение...",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                // "Read Later" / Bookmark placeholder
                                IconButton(onClick = { /* TODO: Save to favorites */ }) {
                                    Icon(
                                        Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (item.isLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Загрузка объяснения...", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                // Explanation Text
                                Text(
                                    text = item.explanation ?: "No explanation available.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        color = Color(0xFFDEDEDE) // Softer black
                                    ),
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 4, // Limit to 4-5 lines
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (item.error != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                // Expand/Collapse Icon indicator
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    READER("Reader", Icons.Default.Description),
    LIST("List", Icons.Default.List),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello $name!",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HacatonTheme {
        ReaderScreen()
    }
}
