package com.example.balkangamehubapp

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.room.Room
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.balkangamehubapp.data.AppDatabase
import com.example.balkangamehubapp.model.CachedPost
import com.example.balkangamehubapp.model.Category
import com.example.balkangamehubapp.network.RetrofitInstance
import com.example.balkangamehubapp.repository.PostRepository
import com.example.balkangamehubapp.ui.theme.BalkanGameHubAppTheme
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import androidx.compose.foundation.background


// 📌 Ovdje stoji globalna funkcija formatDateTime
fun formatDateTime(input: String?): String {
    if (input.isNullOrEmpty()) return "N/A"
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")
        try {
            val zoned = org.threeten.bp.ZonedDateTime.parse(input.trim())
            zoned.toLocalDateTime().format(formatter)
        } catch (e: Exception) {
            val local = LocalDateTime.parse(input.trim())
            local.format(formatter)
        }
    } catch (e: Exception) {
        input
    }
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // 🟢 Tamna navigacija i status bar
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0x00000000),
            navigationBarStyle = SystemBarStyle.dark(0x00000000)
        )

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        // 🔥 Splash animacija
        splashScreen.setOnExitAnimationListener { splashView ->
            val icon = splashView.iconView
            val scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0.7f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0.7f, 1.0f)
            val alpha = ObjectAnimator.ofFloat(icon, "alpha", 1f, 0f)
            scaleX.duration = 350; scaleY.duration = 350
            alpha.startDelay = 150; alpha.duration = 300
            AnimatorSet().apply {
                playTogether(scaleX, scaleY, alpha)
                start()
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) { splashView.remove() }
                })
            }
        }

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bgh_database").build()
        val api = RetrofitInstance.api
        val repo = PostRepository(api, db.postDao())

        setContent {

            BalkanGameHubAppTheme {

                val topBarColor = Color(0xFF0A1A2F) // isti kao DetailsActivity

                var selectedTab by remember { mutableStateOf(0) }
                var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

                var posts by remember { mutableStateOf<List<CachedPost>>(emptyList()) }
                var topCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
                var allCategories by remember { mutableStateOf<List<Category>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                var searchQuery by remember { mutableStateOf("") }
                var filteredPosts by remember { mutableStateOf<List<CachedPost>>(emptyList()) }

                val scope = rememberCoroutineScope()

                // 📌 Load kategorija i postova
                LaunchedEffect(selectedCategoryId, selectedTab) {
                    isLoading = true
                    try {
                        if (allCategories.isEmpty()) allCategories = api.getCategories(perPage = 20).sortedByDescending { it.count }
                        if (topCategories.isEmpty()) topCategories = allCategories.take(5)
                        if (selectedTab == 0) {
                            val catId = selectedCategoryId
                            if (catId == null) {
                                posts = db.postDao().getAll()
                                scope.launch { repo.getPosts(preload = true).collect { posts = it } }
                            } else posts = repo.getPostsByCategory(catId)
                        }
                    } catch (_: Exception) { }
                    isLoading = false
                }

                // 📌 Search filter
                LaunchedEffect(searchQuery, posts) {
                    filteredPosts = if (searchQuery.isBlank()) posts else posts.filter {
                        it.title.contains(searchQuery, true) ||
                                it.content.contains(searchQuery, true)
                    }
                }

                // 🎨 Animacija naslova
                var animatedState by remember { mutableStateOf(false) }
                val animatedColor by animateColorAsState(
                    targetValue = if (animatedState) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground,
                    animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
                    label = "colorShift"
                )
                LaunchedEffect(Unit) { animatedState = true }

                Scaffold(
                    topBar = {
                        Column {
                            CenterAlignedTopAppBar(
                                title = {
                                    Image(painter = painterResource(id = R.drawable.logobgh), contentDescription = null,
                                        modifier = Modifier.height(95.dp).padding(top = 10.dp, bottom = 6.dp))
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = topBarColor)
                            )

                            // 🔍 Search bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Pretraži...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onBackground) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Filled.Clear, null, tint = MaterialTheme.colorScheme.onBackground)
                                        }
                                    }
                                },
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 4.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedContainerColor = Color(0xFF1A2F4A),
                                    unfocusedContainerColor = Color(0xFF1A2F4A),
                                    cursorColor = MaterialTheme.colorScheme.onBackground
                                ),
                                singleLine = true
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar(containerColor = topBarColor) {
                            NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Home") })
                            NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Filled.List, null) }, label = { Text("Kategorije") })
                            NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Filled.Settings, null) }, label = { Text("Postavke") })
                        }
                    }
                ) { innerPadding ->
                    Column(Modifier.padding(innerPadding).fillMaxSize()) {

                        // 📰 HOME
                        if (selectedTab == 0) {
                            if (searchQuery.isBlank()) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    item {
                                        AssistChip(
                                            onClick = { selectedCategoryId = null },
                                            label = { Text("Sve") },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (selectedCategoryId == null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                                labelColor = MaterialTheme.colorScheme.onSecondary
                                            )
                                        )
                                    }
                                    items(topCategories) { cat ->
                                        AssistChip(
                                            onClick = { selectedCategoryId = cat.id },
                                            label = { Text(cat.name) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (selectedCategoryId == cat.id) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                                labelColor = MaterialTheme.colorScheme.onSecondary
                                            )
                                        )
                                    }
                                }
                            }
                            if (isLoading) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else NewsList(filteredPosts)
                        }

                        // 📚 KATEGORIJE
                        if (selectedTab == 1) {
                            LazyColumn(Modifier.fillMaxSize().padding(4.dp)) {
                                items(allCategories) { cat ->
                                    Text(
                                        "${cat.name} (${cat.count})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0A1A2F))   // 🔥 ista top bar boja
                                            .clickable {
                                                selectedCategoryId = cat.id
                                                selectedTab = 0
                                            }
                                            .padding(8.dp)
                                    )

                                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }
                        }

                        // ⚙️ POSTAVKE
                        if (selectedTab == 2) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                    Spacer(Modifier.height(40.dp))
                                    Text("BalkanGameHub", color = animatedColor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))
                                    Text(
                                        "Trudimo se biti vaša prva destinacija za gaming i esport u BiH.\n\n" +
                                                "Verzija: 1.0\n\n" +
                                                "Developed by NeraEtva",
                                        color = MaterialTheme.colorScheme.onBackground
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

@Composable
fun NewsList(posts: List<CachedPost>) {
    val context = LocalContext.current

    LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
        items(posts) { post ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
                    val intent = Intent(context, DetailsActivity::class.java)
                    intent.putExtra("url", post.id.toString())
                    context.startActivity(intent)
                }
            ) {
                Column(Modifier.padding(12.dp)) {
                    post.imageUrl?.let { imageUrl ->
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(imageUrl).crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(post.title, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(painterResource(id = R.drawable.ic_person), contentDescription = "Autor", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(post.authorName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Filled.AccessTime, contentDescription = "Vrijeme objave", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(formatDateTime(post.date), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
