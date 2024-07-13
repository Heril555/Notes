package com.heril.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Button
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.heril.notes.data.Note
import com.heril.notes.ui.theme.BlueSky
import com.heril.notes.ui.theme.BorderColor
import com.heril.notes.ui.theme.NightSky
import com.heril.notes.ui.theme.NotesTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode = remember { mutableStateOf(true) }
            NotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDarkMode.value) Color.Black else MaterialTheme.colors.background
                ) {
                    Navigation(
                        isDarkMode = isDarkMode.value,
                        onToggleDarkMode = { isDarkMode.value = it }
                    )
                }
            }
        }
    }
}


@Composable
fun Navigation(
    viewModel: NotesViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route
    ) {
        composable(Screen.HomeScreen.route) {
            HomeView(
                viewModel,
                navController,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }
        composable(Screen.AddScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                    nullable = false
                }
            )
        ) { entry ->
            val id = if (entry.arguments != null) entry.arguments!!.getLong("id") else 0L
            AddEditDetailView(id, viewModel, navController, isDarkMode, onToggleDarkMode)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeView(
    viewModel: NotesViewModel,
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBarView(
                "Notes", {}, isDarkMode,
                onToggleDarkMode
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddScreen.route + "/0L")
                },
                modifier = Modifier.padding(end = 26.dp, bottom = 54.dp),
                backgroundColor = colorResource(id = R.color.yellow),
                contentColor = colorResource(id = R.color.black)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        },
        backgroundColor = if (isDarkMode) Color.Black else MaterialTheme.colors.background
    ) {
        val notes = viewModel.getAllNotes.collectAsState(initial = listOf())
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            items(notes.value, key = { note -> note.id }) { note ->
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                            viewModel.deleteNote(note)
                        }
                        true
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        val color = when (dismissState.dismissDirection) {
                            DismissDirection.StartToEnd, DismissDirection.EndToStart -> Color.Red
                            else -> Color.Transparent
                        }
                        val alignment = when (dismissState.dismissDirection) {
                            DismissDirection.StartToEnd -> Alignment.CenterStart
                            DismissDirection.EndToStart -> Alignment.CenterEnd
                            else -> Alignment.Center
                        }
                        val icon = when (dismissState.dismissDirection) {
                            DismissDirection.StartToEnd, DismissDirection.EndToStart -> Icons.Default.Delete
                            else -> null
                        }
                        val scale =
                            animateFloatAsState(if (dismissState.dismissDirection != null) 1f else 0.8f)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Delete Icon",
                                    modifier = Modifier.scale(scale.value),
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                    dismissThresholds = { FractionalThreshold(0.25f) },
                    dismissContent = {
                        NoteItem(note = note, onClick = {
                            val id = note.id
                            navController.navigate(Screen.AddScreen.route + "/$id")
                        })
                    }
                )
            }
        }
    }
}


@Composable
fun NoteItem(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .clickable(onClick = onClick),
        elevation = 10.dp,
        backgroundColor = colorResource(id = R.color.grey)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = note.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = colorResource(id = R.color.yellow)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                fontSize = 18.sp,
                color = colorResource(id = R.color.yellow)
            )
        }
    }
}

@Composable
fun TopAppBarView(
    title: String,
    onBackNavClicked: () -> Unit = {},
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {

    val navigationIcon: (@Composable () -> Unit)? = {
        if (!title.contains("Notes")) {
            IconButton(onClick = onBackNavClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        } else {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home Screen"
                )
            }
        }
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .heightIn(max = 32.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        },
        elevation = 3.dp,
        backgroundColor = colorResource(id = R.color.yellow),
        navigationIcon = navigationIcon,
        actions = {
            DarkModeToggle(
                isDarkMode = isDarkMode,
                onToggle = onToggleDarkMode,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    )
}

@Composable
fun DarkModeToggle(
    isDarkMode: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    val switchWidth = 72.dp
    val switchHeight = 30.dp
    val handleSize = 25.dp
    val handlePadding = 4.dp

    val valueToOffset = if (isDarkMode) 1f else 0f
    val offset = remember { Animatable(valueToOffset) }
    val scope = rememberCoroutineScope()

    DisposableEffect(isDarkMode) {
        if (offset.targetValue != valueToOffset) {
            scope.launch {
                offset.animateTo(valueToOffset, animationSpec = tween(1000))
            }
        }
        onDispose { }
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .width(switchWidth)
            .height(switchHeight)
            .clip(RoundedCornerShape(switchHeight))
            .background(lerp(BlueSky, NightSky, offset.value))
            .border(3.dp, BorderColor, RoundedCornerShape(switchHeight))
            .toggleable(
                value = isDarkMode,
                onValueChange = onToggle,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        val backgroundPainter = painterResource(R.drawable.background)
        Canvas(modifier = Modifier.fillMaxSize()) {
            with(backgroundPainter) {
                val scale = size.width / intrinsicSize.width
                val scaledHeight = intrinsicSize.height * scale
                translate(top = (size.height - scaledHeight) * (1f - offset.value)) {
                    draw(Size(size.width, scaledHeight))
                }
            }
        }

        Image(
            painter = painterResource(R.drawable.glow),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(switchWidth)
                .graphicsLayer {
                    scaleX = 1.2f
                    scaleY = scaleX
                    translationX = lerp(
                        -size.width * 0.5f + handlePadding.toPx() + handleSize.toPx() * 0.5f,
                        switchWidth.toPx() - size.width * 0.5f - handlePadding.toPx() - handleSize.toPx() * 0.5f,
                        offset.value
                    )
                }
        )

        Box(
            modifier = Modifier
                .padding(horizontal = handlePadding)
                .size(handleSize)
                .offset(x = (switchWidth - handleSize - handlePadding * 2f) * offset.value)
                .paint(painterResource(R.drawable.sun))
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(R.drawable.moon),
                contentDescription = null,
                modifier = Modifier
                    .size(handleSize)
                    .graphicsLayer {
                        translationX = size.width * (1f - offset.value)
                    }
            )
        }
    }
}

@Composable
fun AddEditDetailView(
    id: Long,
    viewModel: NotesViewModel,
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val snackMessage = remember {
        mutableStateOf("")
    }
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val keyboardController = LocalSoftwareKeyboardController.current
    if (id != 0L) {
        val note = viewModel.getNoteById(id).collectAsState(initial = Note(0L, "", ""))
        viewModel.noteTitleState = note.value.title
        viewModel.noteContentState = note.value.content
    } else {
        viewModel.noteTitleState = ""
        viewModel.noteContentState = ""
    }

    Scaffold(
        topBar = {
            TopAppBarView(
                if (id != 0L) stringResource(id = R.string.edit_note) else stringResource(id = R.string.add_note),
                { navController.navigateUp() },//popBackStack() also works
                isDarkMode,
                onToggleDarkMode
            )
        },
        backgroundColor = if (isDarkMode) Color.Black else MaterialTheme.colors.background,
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .wrapContentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            WishTextField(label = "Title",
                value = viewModel.noteTitleState,
                onValueChange = {
                    viewModel.onNoteTitleChange(it)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            WishTextField(label = "Content",
                value = viewModel.noteContentState,
                onValueChange = {
                    viewModel.onNoteContentChange(it)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (viewModel.noteTitleState.isNotEmpty() && viewModel.noteContentState.isNotEmpty()) {
                        if (id != 0L) {
                            //TODO Update
                            viewModel.updateNote(
                                Note(
                                    id = id,
                                    title = viewModel.noteTitleState.trim(),
                                    content = viewModel.noteContentState.trim()
                                )
                            )
                        } else {
                            //TODO Add
                            viewModel.addNote(
                                Note(
                                    title = viewModel.noteTitleState.trim(),
                                    content = viewModel.noteContentState.trim()
                                )
                            )
                        }
                        // Hide keyboard after successful save (optional)
                        keyboardController?.hide()
                        navController.navigateUp()
                    } else {
                        //Fields cannot be empty
                        snackMessage.value = "Fields cannot be empty"
                        scope.launch {
                            // Hide keyboard after successful save (optional)
                            keyboardController?.hide()
                            scaffoldState.snackbarHostState.showSnackbar(snackMessage.value)
                            navController.navigateUp()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.yellow))
            ) {
                Text(
                    text = if (id != 0L) "Update" else "Add",
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.black)
                )
            }
        }
    }
}

@Composable
fun WishTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.yellow),
                fontSize = 16.sp
            )
        },
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = colorResource(id = R.color.yellow),
            cursorColor = colorResource(id = R.color.yellow),
            focusedBorderColor = colorResource(id = R.color.yellow),
            unfocusedBorderColor = colorResource(id = R.color.yellow),
            backgroundColor = colorResource(id = R.color.grey),
            focusedLabelColor = colorResource(id = R.color.yellow),
            unfocusedLabelColor = colorResource(id = R.color.yellow)
        )
    )
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    val isDarkMode = remember { mutableStateOf(false) }
//    val viewModel: NotesViewModel = viewModel()
//    val navController: NavHostController = rememberNavController()
//    HomeView(
//        viewModel,
//        navController,
//        isDarkMode = isDarkMode.value,
//        onToggleDarkMode = { isDarkMode.value = it }
//    )
    TopAppBarView(title = "Notes", isDarkMode = true){}
    //WishTextField(label = "Anime", value = "Attack on Titan", onValueChange = {})
}