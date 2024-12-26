package qu.lingosnacks.view.editor.components

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qu.lingosnacks.entity.Resource
import qu.lingosnacks.entity.ResourceTypeEnum
import qu.lingosnacks.utils.displayMessage
import qu.lingosnacks.view.components.Dropdown
import qu.lingosnacks.view.theme.CyanLS
import qu.lingosnacks.view.theme.LightGreyLS
import qu.lingosnacks.viewmodel.StorageViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ResourceEditor(
    resources: List<Resource>,
    onCancel: () -> Unit,
    onConfirm: (Resource) -> Unit,
    resource: Resource = Resource()
) {
    val resourceTypes = listOf(
        ResourceTypeEnum.Photo.toString(),
        ResourceTypeEnum.Video.toString(),
        ResourceTypeEnum.Website.toString()
    )

    val context = LocalContext.current
    var title by rememberSaveable { mutableStateOf(resource.title) }
    var url by rememberSaveable { mutableStateOf(resource.url) }
    var type by rememberSaveable { mutableStateOf(resource.type) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        selectedImageUri = it
    }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        val uri = bitmap?.let { saveBitmapAndGetUri(context, it) }
        selectedImageUri = uri
    }

    val videoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        selectedVideoUri = it
    }



    val scope = rememberCoroutineScope()

//    fun isFormReady() = title.isNotBlank() && (url.isNotBlank() || selectedImageUri != null || selectedVideoUri != null)

    val storageViewModel = viewModel<StorageViewModel>()

    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                storageViewModel.uploadImage(it)
                displayMessage(context, "Picture uploaded successfully", Toast.LENGTH_SHORT)
            }
        }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            displayMessage(context, "Select image Uri: $uri")
            println(">> Debug: $uri")
            uri?.let {
                storageViewModel.uploadImage(it)
            }
        }

    Dialog(
        { onCancel() },
        DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        val fM = LocalFocusManager.current
        val kB = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.7F))
                .imePadding(),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CyanLS)
            ) {
                Text(
                    if (resource.title == "") "Add Resource" else "Edit Resource",
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    CyanLS,
                    textAlign = TextAlign.Center
                )
                Divider(Modifier.fillMaxWidth(), 1.dp, Color.LightGray)
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    label = {
                        Text(text = "Resource Title")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        fM.moveFocus(FocusDirection.Next)
                        kB?.hide()
                    })
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                    },
                    label = {
                        Text(text = "Resource URL")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        fM.clearFocus()
                        kB?.hide()
                    })
                )

                Dropdown(
                    label = "Type",
                    options = resourceTypes,
                    selectedOption = type,
                    onSelectionChange = { type = it }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)
                ) {
                    Button(
                        onClick = {
//                            imageLauncher.launch("image/*")
                            imagePicker.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = CyanLS
                        ),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text("Pick Image", modifier = Modifier.padding(horizontal = 2.5.dp))
                    }

                    Button(
                        onClick = {
//                            cameraLauncher.launch(null)
                            takePicture.launch()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = CyanLS
                        ),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text("Take Picture", modifier = Modifier.padding(horizontal = 2.5.dp))


                    }

                    Button(
                        onClick = {
                            videoLauncher.launch("video/*")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = CyanLS
                        )
                    ) {
                        Text("Pick Video")
                    }

                    if (storageViewModel.imageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(storageViewModel.imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .border(2.dp, Color.Magenta)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp, bottom = 10.dp)
                ) {
                    Button(
                        onClick = { onCancel() },
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, contentColor = CyanLS
                        ),
                        border = BorderStroke(1.dp, CyanLS),
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text("Cancel", modifier = Modifier.padding(horizontal = 2.5.dp))
                    }

                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                if (resource.title == "") "New resource created" else "Resource has been edited",
                                Toast.LENGTH_SHORT
                            ).show()

                            val updatedResource = Resource(
                                url.trim(),
                                title.trim(),
                                type,
                                selectedImageUri,
                                selectedVideoUri
                            )

                            onConfirm(updatedResource)
                        },
//                        enabled =,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanLS)
                    ) {
                        Text(if (resource.title == "") "Add Resource" else "Save Changes")
                    }
                }
            }
        }
    }

    DisposableEffect(selectedImageUri) {
        if (selectedImageUri != null) {
            scope.launch {
                url = getImagePathFromUri(selectedImageUri!!, context)
            }
        }
        onDispose {

        }
    }
}

suspend fun getImagePathFromUri(uri: Uri, context: Context): String {
    return withContext(Dispatchers.IO) {
        var path = ""
        val contentResolver: ContentResolver = context.contentResolver

        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            path = if (columnIndex == -1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val newUri = MediaStore.setRequireOriginal(uri)
                    newUri?.path ?: ""
                } else {
                    ""
                }
            } else {
                it.getString(columnIndex)
            }
        }
        path
    }
}

private fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
    val imagesDir = File(context.cacheDir, "images")
    imagesDir.mkdirs()

    val file = File(imagesDir, "${System.currentTimeMillis()}.jpg")

    val stream: OutputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    stream.flush()
    stream.close()

    return FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )
}
