package com.example.hacaton.Compuse

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hacaton.ui.theme.HacatonTheme
import com.example.hacaton.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch
import kotlin.math.max

@PreviewScreenSizes
@Composable
fun HacatonApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.READER) }
    var previousDestination by remember { mutableStateOf(currentDestination) }


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
                                    .offset(y = (0).dp)
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
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = {
                val screenOrder = listOf(
                    AppDestinations.READER,
                    AppDestinations.LIST,
                    AppDestinations.PROFILE
                )

                val currentIndex = screenOrder.indexOf(targetState)
                val previousIndex = screenOrder.indexOf(initialState)
                val isForward = currentIndex > previousIndex

                val slideIn = slideInHorizontally(
                    initialOffsetX = { fullWidth ->
                        if (isForward) fullWidth / 3 else -fullWidth / 3
                    }
                ) + fadeIn()

                val slideOut = slideOutHorizontally(
                    targetOffsetX = { fullWidth ->
                        if (isForward) -fullWidth else fullWidth
                    }
                ) + fadeOut()

                slideIn togetherWith slideOut
            },
            label = "DirectionalTransition"
        ) { destination ->

            // Обновляем previousDestination
            previousDestination = destination

            Box(modifier = Modifier.padding(innerPadding)) {
                when (destination) {
                    AppDestinations.READER ->
                        ReaderScreen(onNavigateToList = { currentDestination = AppDestinations.LIST })

                    AppDestinations.LIST ->
                        ListScreen()

                    AppDestinations.PROFILE ->
                        Greeting("Profile")
                }
            }
        }
    }
}

// Custom Scrollbar Modifier

fun Modifier.simpleVerticalScrollbar(
    state: ScrollState,
    width: Dp = 10.dp
): Modifier = composed {
    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration),
        label = "ScrollbarAlpha"
    )

    drawWithContent {
        drawContent()

        val needDraw = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if needed
        if (needDraw && state.maxValue > 0) {
            val visibleHeight = this.size.height - state.maxValue
            val scrollbarHeight = max(10f, (this.size.height * (this.size.height / (state.maxValue + this.size.height))))
            val scrollbarOffsetY = (state.value.toFloat() / state.maxValue) * (this.size.height - scrollbarHeight)

            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.5f * alpha), // Use alpha for fade out
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                cornerRadius = CornerRadius(width.toPx() / 2)
            )
        }
    }
}

// Draggable Scrollbar Composable
@Composable
fun DraggableScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    thumbColor: Color = Color(0xFF6200EE),
    thumbWidth: Dp = 10.dp
) {
    if (scrollState.maxValue > 0) {
        BoxWithConstraints(modifier = modifier.fillMaxHeight().width(24.dp)) {
            val heightPx = constraints.maxHeight.toFloat()
            val totalContentHeight = heightPx + scrollState.maxValue
            val thumbHeightPx = max(50f, (heightPx * heightPx / totalContentHeight))
            
            // Calculate thumb offset
            val thumbOffsetPx = if (scrollState.maxValue == 0) 0f else {
                (scrollState.value.toFloat() / scrollState.maxValue) * (heightPx - thumbHeightPx)
            }

            val coroutineScope = rememberCoroutineScope()
            
            // Draggable thumb
            Box(
                modifier = Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(0, thumbOffsetPx.toInt()) }
                    .width(thumbWidth)
                    .height(with(LocalDensity.current) { thumbHeightPx.toDp() })
                    .align(Alignment.TopCenter)
                    .background(thumbColor, CircleShape)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            val newOffset = thumbOffsetPx + delta
                            val maxOffset = heightPx - thumbHeightPx
                            val fraction = (newOffset / maxOffset).coerceIn(0f, 1f)
                            val newScrollValue = (fraction * scrollState.maxValue).toInt()
                            coroutineScope.launch {
                                scrollState.scrollTo(newScrollValue)
                            }
                        }
                    )
            )
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
                            },
                             modifier = Modifier
                                 .padding(vertical = 7.dp, horizontal = 15.dp)
                                 .fillMaxWidth()
                                 .background(color = Color(0xFF3E3E41), shape = RoundedCornerShape(16.dp))
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

                            .height(56.dp) 
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                containerColor = if (hasSelection) Color(0xFF7B5CFF) else Color(0xFFE0E0E0),
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                                contentColor = Color.White,
                                disabledContentColor = Color.Gray
                            ),
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
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
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
                        .padding(vertical = 16.dp)
                         .padding(start = 16.dp, end = 20.dp)
                    ) {
                        BasicTextField(
                            value = textContent,
                            onValueChange = {
                                textContent = it
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)

                                ,
                            readOnly = true,
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 30.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        
                        // Add Draggable Scrollbar on the right
                        DraggableScrollbar(
                            scrollState = scrollState,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = 20.dp)
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
            .padding(horizontal = 16.dp) 
    ) {
        Spacer(modifier = Modifier.height(16.dp)) 
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
                verticalArrangement = Arrangement.spacedBy(12.dp), 
                contentPadding = PaddingValues(bottom = 100.dp) 
            ) {
                items(explanations, key = { it.id }) { item ->
                    var isExpanded by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(16.dp), 
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface, 
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.title ?: "Определение...",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.togglePin(item.id) }) {
                                    Icon(
                                        imageVector = if (item.isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = if (item.isPinned) Color.Yellow else Color.Gray
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteExplanation(item.id)  }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
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
                                Text(
                                    text = item.explanation ?: "No explanation available.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        color = Color(0xFFE0E0E0) // Lighter text for dark theme readability
                                    ),
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 4, 
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
