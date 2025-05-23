package com.daniebeler.pfpixelix.ui.composables.edit_post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.daniebeler.pfpixelix.domain.service.utils.Resource
import com.daniebeler.pfpixelix.domain.model.MediaAttachment
import com.daniebeler.pfpixelix.domain.model.Place
import com.daniebeler.pfpixelix.domain.model.UpdatePost
import com.daniebeler.pfpixelix.domain.service.editor.PostEditorService
import com.daniebeler.pfpixelix.domain.service.post.PostService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

class EditPostViewModel @Inject constructor(
    private val postService: PostService,
    private val postEditorService: PostEditorService
) : ViewModel() {
    data class MediaDescriptionItem(
        val imageId: String, var description: String, var changed: Boolean
    )

    var editPostState by mutableStateOf(EditPostState())
    var caption by mutableStateOf(TextFieldValue())
    var location by mutableStateOf<Place?>(null)
    var sensitive: Boolean by mutableStateOf(false)
    var sensitiveText: String by mutableStateOf("")
    var mediaAttachmentsEdit = mutableStateListOf<MediaAttachment>()
    var mediaAttachmentsBefore = mutableStateListOf<MediaAttachment>()
    var mediaDescriptionItems = mutableStateListOf<MediaDescriptionItem>()
    var deleteMediaDialog by mutableStateOf<String?>(null)

    fun loadData(postId: String) {
        loadPost(postId)
    }

    private fun loadPost(postId: String) {
        postService.getPostById(postId).onEach { result ->
            editPostState = when (result) {
                is Resource.Success -> {
                    caption = TextFieldValue(result.data.content)
                    result.data.place?.let {
                        location = it
                    }
                    sensitive = result.data.sensitive
                    sensitiveText = result.data.spoilerText
                    mediaAttachmentsEdit.addAll(result.data.mediaAttachments)
                    mediaAttachmentsBefore.addAll(result.data.mediaAttachments)
                    mediaDescriptionItems.addAll(result.data.mediaAttachments.map {
                        MediaDescriptionItem(
                            it.id, it.description ?: "", false
                        )
                    })
                    EditPostState(post = result.data)
                }

                is Resource.Error -> {
                    EditPostState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    EditPostState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun updatePost(postId: String, navController: NavController) {
        CoroutineScope(Dispatchers.Default).launch {
            val placeDto = if (location != null) {
                Place(
                    id = location!!.id,
                    name = location!!.name!!,
                    slug = location!!.slug,
                    country = location!!.country!!,
                    url = location!!.url
                )
            } else {
                null
            }

            val updatePostDto = UpdatePost(
                status = caption.text,
                sensitive = sensitive,
                spoilerText = sensitiveText,
                mediaIds = mediaAttachmentsEdit.map { it.id },
                location = placeDto
            )

            mediaDescriptionItems.onEach { mediaDescriptionItem ->
                updateMedia(mediaDescriptionItem)
            }

            postEditorService.updatePost(
                postId, updatePostDto
            ).onEach { result ->
                editPostState = when (result) {
                    is Resource.Success -> {
                        navController.popBackStack()
                        navController.navigate("single_post_screen/$postId?refresh=true") {
                            launchSingleTop = true
                        }

                        EditPostState(post = editPostState.post)
                    }

                    is Resource.Error -> {
                        EditPostState(error = result.message ?: "An unexpected error occurred")
                    }

                    is Resource.Loading -> {
                        EditPostState(isLoading = true, post = editPostState.post)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    private fun updateMedia(mediaDescriptionItem: MediaDescriptionItem) {
        postEditorService.updateMedia(
            mediaDescriptionItem.imageId, mediaDescriptionItem.description
        ).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    EditPostState(post = editPostState.post)
                }

                is Resource.Error -> {
                    editPostState =
                        EditPostState(error = (result.message ?: "An unexpected error occurred"))
                }

                is Resource.Loading -> {
                    editPostState = EditPostState(isLoading = true, post = editPostState.post)

                }
            }
        }.launchIn(viewModelScope)
    }

    fun deleteMedia(mediaId: String) {
        mediaAttachmentsEdit.remove(mediaAttachmentsEdit.find { it.id == mediaId })
        mediaDescriptionItems.remove(mediaDescriptionItems.find { it.imageId == mediaId })
        deleteMediaDialog = null
    }

    fun moveMediaAttachmentUp(index: Int) {
        if (index >= 1) {
            val copy = mediaAttachmentsEdit[index]
            mediaAttachmentsEdit[index] = mediaAttachmentsEdit[index - 1]
            mediaAttachmentsEdit[index - 1] = copy
        }
    }

    fun moveMediaAttachmentDown(index: Int) {
        if (index < mediaAttachmentsEdit.size - 1) {
            val copy = mediaAttachmentsEdit[index]
            mediaAttachmentsEdit[index] = mediaAttachmentsEdit[index + 1]
            mediaAttachmentsEdit[index + 1] = copy
        }
    }

    fun _setLocation(_location: Place?) {
        location = _location
    }
}

