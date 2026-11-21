package com.example.read2

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ReaderScreen()
                }
            }
        }
    }
}

@Composable
fun ReaderScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    var text by remember { mutableStateOf("Нажмите кнопку, чтобы выбрать файл") }
    var isLoading by remember { mutableStateOf(false) }


    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            isLoading = true
            scope.launch {
                try {

                    val content = withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openInputStream(uri)
                        var textData = stream?.bufferedReader()?.use { it.readText() } ?: ""

                        if (uri.toString().endsWith(".fb2")) {
                            textData = textData.replace(Regex("<[^>]*>"), "")
                        }
                        textData
                    }


                    if (content.isBlank()) throw Exception("Файл пустой или не читается")

                    text = content // Выводим текст
                } catch (e: Exception) {
                    text = "Ошибка: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {


        Button(
            onClick = { launcher.launch(arrayOf("text/plain", "*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Открыть файл")
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {

            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text(text = text, modifier = Modifier.padding(bottom = 32.dp))
            }
        }
    }
}