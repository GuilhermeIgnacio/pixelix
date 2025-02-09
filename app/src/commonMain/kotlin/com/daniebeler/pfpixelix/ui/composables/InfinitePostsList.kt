package com.daniebeler.pfpixelix.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.daniebeler.pfpixelix.domain.model.Post
import com.daniebeler.pfpixelix.ui.composables.profile.PostsWrapperComposable
import com.daniebeler.pfpixelix.ui.composables.profile.SwitchViewComposable
import com.daniebeler.pfpixelix.ui.composables.profile.ViewEnum
import com.daniebeler.pfpixelix.ui.composables.states.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfinitePostsList(
    items: List<Post>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: String,
    endReached: Boolean,
    navController: NavController,
    getItemsPaginated: () -> Unit,
    emptyMessage: EmptyState = EmptyState(
        icon = Icons.Outlined.Photo, heading = "No Posts"
    ),
    onRefresh: () -> Unit,
    itemGetsDeleted: (postId: String) -> Unit,
    view: ViewEnum = ViewEnum.Timeline,
    changeView: (view: ViewEnum) -> Unit = {},
    isFirstItemLarge: Boolean = false,
    postsCount: Int? = null,
) {
    var posts by remember { mutableStateOf(listOf<Post>()) } // Your list of posts
    val lazyListState = rememberLazyListState()

    LaunchedEffect(items) {
        posts = items
    }

    fun delete(postId: String) {
        itemGetsDeleted(postId)
    }

    fun updatePost(post: Post) {
        posts = posts.map {
            if (it.id == post.id) {
                post
            } else {
                it
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp), state = lazyListState, contentPadding = PaddingValues(top = if (postsCount != null) {0.dp} else {12.dp} )
        ) {
            postsCount?.let {
                item {
                    SwitchViewComposable(postsCount = postsCount,
                        viewType = view,
                        onViewChange = { changeView(it) })
                }
            }
            PostsWrapperComposable(
                posts = posts,
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                error = error,
                endReached = endReached,
                emptyMessage = emptyMessage,
                view = view,
                isFirstImageLarge = isFirstItemLarge,
                postGetsDeleted = ::delete,
                updatePost = ::updatePost,
                navController = navController
            )
        }
        ToTopButton(lazyListState) { onRefresh() }
    }

    InfiniteListHandler(lazyListState = lazyListState) {
        getItemsPaginated()
    }
}