package com.artemissoftware.apollopictures

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.net.URI
import java.text.DecimalFormat

@Composable
fun ImageSelectScreen() {
    val context = LocalContext.current

    val coroutine = rememberCoroutineScope()

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var selectedImageUris by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            coroutine.launch {
                uri?.let {


                    val string = uri.toString()
                    val uristring: Uri = Uri.parse(string)




                    val bytes = context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes()
                    } ?: byteArrayOf()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // TODO: Test different compression settings
                    val compressed = outputStream.toByteArray()

                    context.contentResolver.openInputStream(it)?.let { inputStream ->

                        val size: Int = inputStream.available() ?: 0
                        inputStream.close()

                        if (size < 1048576) {
                            selectedImageUri = uristring
                        }
                    }
                }
            }
        },
    )
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris },
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                Button(onClick = {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }) {
                    Text(text = "Pick one photo")
                }
                Button(onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }) {
                    Text(text = "Pick multiple photo")
                }
            }
        }

        item {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
            )
        }

        items(selectedImageUris) { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

fun readableFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}
