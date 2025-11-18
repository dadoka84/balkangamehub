package com.example.balkangamehubapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.balkangamehubapp.network.RetrofitInstance
import kotlinx.coroutines.launch

class DetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val postId = intent.getStringExtra("url")?.toIntOrNull()

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                if (postId != null)
                    DetailsScreen(postId, onBack = { finish() })
                else
                    ErrorScreen()
            }
        }
    }
}

@Composable
fun ErrorScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Greška: Nije pronađen ID posta.", color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    postId: Int,
    onBack: () -> Unit
) {
    val api = RetrofitInstance.api
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var content by remember { mutableStateOf(AnnotatedString("")) }
    var postDate by remember { mutableStateOf<String?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Int?>(null) }

    // LOAD DATA ----------------------------------------------------
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val post = api.getPostById(postId)

                title = post.title.rendered
                imageUrl = post.embedded?.media?.firstOrNull()?.bestImageUrl
                postDate = post.date

                // ✔ category
                val rawCats = post.embedded?.terms
                val catList = rawCats?.flatMap { it } ?: emptyList()
                if (catList.isNotEmpty()) {
                    categoryName = catList.first().name
                    categoryId = catList.first().id
                }

                // ✔ HTML → text
                val htmlContent = post.content?.rendered ?: ""
                val spanned = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
                content = AnnotatedString(spanned.toString())

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Nazad",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logobgh),
                        contentDescription = null,
                        modifier = Modifier.height(56.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0A1A2F)
                )
            )
        }
    ) { innerPadding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(Color.Black)
                .padding(16.dp)
        ) {

            // TITLE
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // CATEGORY CHIP (same style as Home)
            if (categoryName.isNotEmpty()) {
                AssistChip(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("open_category_id", categoryId)
                        context.startActivity(intent)
                    },
                    label = { Text(categoryName) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF1E88E5),
                        labelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // IMAGE
            imageUrl?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // DATE
            Text(
                text = "Objavljeno: ${formatDateTime(postDate)}",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CONTENT (text-only, YouTube links included)
            Text(
                text = content,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
