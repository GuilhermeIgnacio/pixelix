package com.daniebeler.pfpixelix.ui.composables.edit_post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowLeft
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.daniebeler.pfpixelix.di.injectViewModel
import com.daniebeler.pfpixelix.domain.model.MediaAttachment
import com.daniebeler.pfpixelix.ui.composables.newpost.ImagesPager
import com.daniebeler.pfpixelix.ui.composables.newpost.NewPostViewModel
import com.daniebeler.pfpixelix.ui.composables.states.ErrorComposable
import com.daniebeler.pfpixelix.ui.composables.states.LoadingComposable
import com.daniebeler.pfpixelix.ui.composables.textfield_location.TextFieldLocationsComposable
import com.daniebeler.pfpixelix.ui.composables.textfield_mentions.TextFieldMentionsComposable
import com.daniebeler.pfpixelix.utils.KmpUri
import com.daniebeler.pfpixelix.utils.LocalKmpContext
import com.daniebeler.pfpixelix.utils.getPlatformUriObject
import com.daniebeler.pfpixelix.utils.toKmpUri
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pixelix.app.generated.resources.Res
import pixelix.app.generated.resources.add_outline
import pixelix.app.generated.resources.alt_text
import pixelix.app.generated.resources.cancel
import pixelix.app.generated.resources.caption
import pixelix.app.generated.resources.content_warning_or_spoiler_text
import pixelix.app.generated.resources.delete
import pixelix.app.generated.resources.edit_post
import pixelix.app.generated.resources.location
import pixelix.app.generated.resources.save
import pixelix.app.generated.resources.sensitive_nsfw_media
import pixelix.app.generated.resources.sure_update_post
import pixelix.app.generated.resources.trash_outline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostComposable(
    postId: String,
    navController: NavController,
    viewModel: EditPostViewModel = injectViewModel("editPostViewModel") { editPostViewModel }
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showSaveAlert by remember {
        mutableStateOf(false)
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val context = LocalKmpContext.current

    LaunchedEffect(Unit) {
        viewModel.loadData(postId)
    }

    Scaffold(contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = stringResource(Res.string.edit_post), fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                },
                actions = {
                    if (viewModel.editPostState.post != null) {
                        val mediaAttachmentsEditIds = viewModel.mediaAttachmentsEdit.map { it.id }
                        val mediaAttachmentsBeforeIds =
                            viewModel.mediaAttachmentsBefore.map { it.id }
                        if (viewModel.mediaDescriptionItems.any { it.changed } || viewModel.caption.text != viewModel.editPostState.post!!.content || viewModel.sensitive != viewModel.editPostState.post!!.sensitive || mediaAttachmentsBeforeIds != mediaAttachmentsEditIds || viewModel.editPostState.post!!.place != viewModel.location) {
                            if (viewModel.editPostState.isLoading) {
                                Button(
                                    onClick = { }, modifier = Modifier.width(120.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { showSaveAlert = true },
                                    modifier = Modifier.width(120.dp)
                                ) {
                                    Text(text = stringResource(Res.string.save))
                                }
                            }
                        } else {
                            Button(
                                onClick = { }, enabled = false, modifier = Modifier.width(120.dp)
                            ) {
                                Text(text = stringResource(Res.string.save))
                            }
                        }
                    }
                })
        }) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(state = rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImagesPagerEditPost(viewModel.mediaAttachmentsEdit,
                    viewModel.mediaDescriptionItems,
                   { mediaDescriptionIndex, altText ->

                       //val changed = it != (oldMediaAttachment!!.description ?: "")
                       viewModel.mediaDescriptionItems[mediaDescriptionIndex] =
                           viewModel.mediaDescriptionItems[mediaDescriptionIndex].copy(
                               description = altText, changed = true
                           )
                    },
                    { index -> viewModel.moveMediaAttachmentUp(index) },
                    { index -> viewModel.moveMediaAttachmentDown(index) },
                    { index -> viewModel.deleteMedia(index) },
                )
                /* viewModel.mediaAttachmentsEdit.forEachIndexed { index, mediaAttachment ->
                     Row(
                         Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                     ) {
                         Box(contentAlignment = Alignment.Center) {

                             if (mediaAttachment.type == "image") {
                                 AsyncImage(
                                     model = mediaAttachment.url,
                                     contentDescription = null,
                                     modifier = Modifier.width(100.dp)
                                 )
                             } else {
                                 //todo KMP video
                                 AsyncImage(
                                     model = mediaAttachment.url,
                                     contentDescription = "video thumbnail",
                                     modifier = Modifier.width(100.dp)
                                 )
                             }
                         }

                         Spacer(Modifier.width(10.dp))

                         val mediaDescriptionItem =
                             viewModel.mediaDescriptionItems.find { mediaDescriptionItem -> mediaDescriptionItem.imageId == mediaAttachment.id }
                                 ?: EditPostViewModel.MediaDescriptionItem(
                                     mediaAttachment.id, "", false
                                 )
                         val indexOfDescriptionItem =
                             viewModel.mediaDescriptionItems.indexOf(mediaDescriptionItem)
                         TextField(
                             value = mediaDescriptionItem.description,
                             onValueChange = {
                                 val oldMediaAttachment =
                                     viewModel.editPostState.post!!.mediaAttachments.find { it.id == mediaDescriptionItem.imageId }
                                 val changed = it != (oldMediaAttachment!!.description ?: "")
                                 viewModel.mediaDescriptionItems[indexOfDescriptionItem] =
                                     viewModel.mediaDescriptionItems[indexOfDescriptionItem].copy(
                                         description = it, changed = changed
                                     )
                             },
                             modifier = Modifier.weight(1f),
                             singleLine = false,
                             placeholder = { Text(stringResource(Res.string.content_warning_or_spoiler_text)) },
                             shape = RoundedCornerShape(16.dp),
                             colors = TextFieldDefaults.colors(
                                 unfocusedIndicatorColor = Color.Transparent,
                                 focusedIndicatorColor = Color.Transparent,
                             ),
                             keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                             keyboardActions = KeyboardActions(onDone = {
                                 keyboardController?.hide()
                                 focusManager.clearFocus()
                             })
                         )

                         if (viewModel.mediaAttachmentsEdit.size > 1) {
                             Column {
                                 IconButton(onClick = { viewModel.moveMediaAttachmentUp(index) }) {
                                     Icon(
                                         imageVector = Icons.Outlined.ArrowUpward,
                                         contentDescription = "move Imageupwards"
                                     )
                                 }
                                 IconButton(onClick = { viewModel.moveMediaAttachmentDown(index) }) {
                                     Icon(
                                         imageVector = Icons.Outlined.ArrowDownward,
                                         contentDescription = "move Imageupwards"
                                     )
                                 }
                             }
                             IconButton(onClick = {
                                 viewModel.deleteMediaDialog = mediaAttachment.id
                             }) {
                                 Icon(
                                     imageVector = Icons.Outlined.Delete,
                                     contentDescription = "delete Image",
                                     tint = MaterialTheme.colorScheme.error
                                 )
                             }
                         }
                     }
                 }*/
                if (!viewModel.editPostState.isLoading && viewModel.editPostState.post != null) {
                    TextFieldMentionsComposable(
                        submit = {},
                        text = viewModel.caption,
                        changeText = { text -> viewModel.caption = text },
                        labelStringId = Res.string.caption,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Default,
                        suggestionsBoxColor = MaterialTheme.colorScheme.surfaceContainer,
                        submitButton = null
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(Res.string.sensitive_nsfw_media))
                        Switch(checked = viewModel.sensitive,
                            onCheckedChange = { viewModel.sensitive = it })
                    }
                    if (viewModel.sensitive) {
                        TextField(
                            value = viewModel.sensitiveText,
                            singleLine = false,
                            onValueChange = { viewModel.sensitiveText = it },
                            placeholder = { Text(stringResource(Res.string.content_warning_or_spoiler_text)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            })
                        )
                    }
                    TextFieldLocationsComposable(
                        submit = {},
                        submitPlace = { viewModel._setLocation(it) },
                        initialValue = viewModel.editPostState.post!!.place,
                        labelStringId = Res.string.location,
                        modifier = Modifier.fillMaxWidth(),
                        imeAction = ImeAction.Default,
                        suggestionsBoxColor = MaterialTheme.colorScheme.surfaceContainer,
                        submitButton = null
                    )
                }

                LoadingComposable(isLoading = viewModel.editPostState.isLoading)
                ErrorComposable(message = viewModel.editPostState.error)
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
            }

            if (showSaveAlert) {
                AlertDialog(title = {
                    Text(text = stringResource(Res.string.sure_update_post))
                }, onDismissRequest = {
                    showSaveAlert = false
                }, dismissButton = {
                    TextButton(onClick = {
                        showSaveAlert = false
                    }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }, confirmButton = {
                    TextButton(onClick = {
                        showSaveAlert = false
                        viewModel.updatePost(postId, navController)
                    }) {
                        Text(stringResource(Res.string.save))
                    }
                })
            }
        }
    }

    if (viewModel.deleteMediaDialog != null) {
        AlertDialog(icon = {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
        }, title = {
            Text(text = "Remove Media")
        }, text = {
            Text(text = "Are you sure you want to delete this media")
        }, onDismissRequest = {
            viewModel.deleteMediaDialog = null
        }, confirmButton = {
            TextButton(onClick = {
                viewModel.deleteMedia(viewModel.deleteMediaDialog!!)
            }) {
                Text(stringResource(Res.string.delete))
            }
        }, dismissButton = {
            TextButton(onClick = {
                viewModel.deleteMediaDialog = null
            }) {
                Text(stringResource(Res.string.cancel))
            }
        })
    }
}


@Composable
fun ImagesPagerEditPost(
    images: List<MediaAttachment>,
    mediaDescriptionItems: List<EditPostViewModel.MediaDescriptionItem>,
    updateAltText: (index: Int, altText: String) -> Unit,
    moveImageUp: (index: Int) -> Unit,
    moveImageDown: (index: Int) -> Unit,
    deleteMedia: (mediaId: String) -> Unit,
) {
    val pagerState = rememberPagerState { images.size }
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 10.dp,
        verticalAlignment = Alignment.Top
    ) { page ->
        val image = images[page]
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (page == 0) {
                    Box(Modifier.width(48.dp)) {}
                } else {
                    IconButton(onClick = {
                        moveImageUp(page)
                        scope.launch {
                            pagerState.animateScrollToPage(page = page - 1)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowLeft,
                            contentDescription = "move Image upwards"
                        )
                    }
                }
                IconButton(onClick = {
                    deleteMedia(image.id)
                }) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.trash_outline),
                        contentDescription = "delete Image",
                        tint = MaterialTheme.colorScheme.error
                    )
                }


                if (page == images.size - 1) {
                    Box(Modifier.width(48.dp)) {}
                } else {
                    IconButton(onClick = {
                        moveImageDown(page)
                        scope.launch {
                            pagerState.animateScrollToPage(page = page + 1)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                            contentDescription = "move Image downwards"
                        )
                    }
                }
            }
            Card(Modifier.fillMaxWidth().aspectRatio(1f)) {

                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {

                    val type = image.type

                    if (image.url != null) {
                        if (type.take(5) == "video") {
                            //todo KMP video
                            AsyncImage(
                                model = image.url.toKmpUri().getPlatformUriObject(),
                                contentDescription = "video thumbnail",
                                modifier = Modifier.width(100.dp)
                            )
                        } else {
                            AsyncImage(
                                model = image.url.toKmpUri().getPlatformUriObject(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Inside
                            )
                        }
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.wrapContentSize(Alignment.Center)
                        )
                    }
                }
            }
            val mediaDescriptionItem =
                mediaDescriptionItems.find { mediaDescriptionItem -> mediaDescriptionItem.imageId == image.id }
                    ?: EditPostViewModel.MediaDescriptionItem(
                        image.id, "", false
                    )
            val indexOfDescriptionItem =
                mediaDescriptionItems.indexOf(mediaDescriptionItem)
            TextField(
                value = mediaDescriptionItem.description,
                onValueChange = { updateAltText(indexOfDescriptionItem, it) },
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                label = { Text(stringResource(Res.string.alt_text)) },
            )
        }
    }
}