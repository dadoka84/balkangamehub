package com.example.balkangamehubapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.balkangamehubapp.network.RetrofitInstance
import kotlinx.coroutines.launch
import com.example.balkangamehubapp.model.authorName
import com.example.balkangamehubapp.ui.theme.BalkanGameHubAppTheme  // 🔥 OVO JE KLJUČNO!

class DetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // 🔥 PRAVI DARK EDGE-TO-EDGE (verzija za tvoj API)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0x00000000),
            navigationBarStyle = SystemBarStyle.dark(0x00000000)
        )

        super.onCreate(savedInstanceState)

        val postId = intent.getStringExtra("url")?.toIntOrNull()

        // 🔥 UMJESTO MaterialTheme → KORISTIMO TVOJU TEMU
        setContent {
            BalkanGameHubAppTheme {
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
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Greška: Nije pronađen ID posta.",
            color = MaterialTheme.colorScheme.onBackground
        )
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
    var authorName by remember { mutableStateOf("Balkan Game Hub Team") }

    // 📌 LOAD DATA
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val post = api.getPostById(postId)

                title = post.title.rendered
                imageUrl = post.embedded?.media?.firstOrNull()?.bestImageUrl
                postDate = post.date
                authorName = post.authorName

                val rawCats = post.embedded?.terms
                val catList = rawCats?.flatMap { it } ?: emptyList()
                if (catList.isNotEmpty()) {
                    categoryName = catList.first().name
                    categoryId = catList.first().id
                }

                val htmlContent = post.content?.rendered ?: ""
                val spanned = HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY)
                content = AnnotatedString(spanned.toString())

            } catch (_: Exception) { }
            finally { isLoading = false }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logobgh),
                        contentDescription = null,
                        modifier = Modifier
                            .height(100.dp)
                            .padding(top = 10.dp, bottom = 10.dp)
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
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            // ⭐ CATEGORY CHIP
            if (categoryName.isNotEmpty()) {
                AssistChip(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("open_category_id", categoryId)
                        context.startActivity(intent)
                    },
                    label = { Text(categoryName, color = MaterialTheme.colorScheme.onSecondary) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // ⭐ IMAGE
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
                Spacer(Modifier.height(20.dp))
            }

            // ⭐ AUTHOR + DATE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Autor",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))

                Text(
                    text = authorName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.width(12.dp))

                Icon(
                    painter = painterResource(id = R.drawable.ic_time),
                    contentDescription = "Vrijeme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))

                Text(
                    text = formatDateTime(postDate),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // ⭐ CONTENT
            Text(
                text = content,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
