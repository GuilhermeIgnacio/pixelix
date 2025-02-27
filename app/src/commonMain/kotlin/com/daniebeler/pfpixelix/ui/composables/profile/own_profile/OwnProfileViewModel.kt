package com.daniebeler.pfpixelix.ui.composables.profile.own_profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniebeler.pfpixelix.common.Constants
import com.daniebeler.pfpixelix.common.Resource
import com.daniebeler.pfpixelix.domain.model.Post
import com.daniebeler.pfpixelix.domain.service.account.AccountService
import com.daniebeler.pfpixelix.domain.service.collection.CollectionService
import com.daniebeler.pfpixelix.domain.service.platform.Platform
import com.daniebeler.pfpixelix.domain.service.post.PostService
import com.daniebeler.pfpixelix.domain.service.preferences.UserPreferences
import com.daniebeler.pfpixelix.domain.service.session.AuthService
import com.daniebeler.pfpixelix.domain.usecase.OpenExternalUrlUseCase
import com.daniebeler.pfpixelix.ui.composables.profile.AccountState
import com.daniebeler.pfpixelix.ui.composables.profile.CollectionsState
import com.daniebeler.pfpixelix.ui.composables.profile.PostsState
import com.daniebeler.pfpixelix.ui.composables.profile.ViewEnum
import com.daniebeler.pfpixelix.utils.KmpContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

class OwnProfileViewModel @Inject constructor(
    private val accountService: AccountService,
    private val postService: PostService,
    private val openExternalUrlUseCase: OpenExternalUrlUseCase,
    private val prefs: UserPreferences,
    private val collectionService: CollectionService,
    private val authService: AuthService,
    private val platform: Platform
) : ViewModel() {
    private val iconManager = platform.getAppIconManager()
    var accountState by mutableStateOf(AccountState())
    var postsState by mutableStateOf(PostsState())
    var ownDomain by mutableStateOf("")
    var view by mutableStateOf(ViewEnum.Grid)
    private var collectionPage by mutableIntStateOf(1)
    var appIcon by mutableStateOf<ImageBitmap?>(null)

    var collectionsState by mutableStateOf(CollectionsState())

    init {
        loadData(false)

        viewModelScope.launch {
            prefs.showUserGridTimelineFlow.collect { res ->
                view = if (res) ViewEnum.Grid else ViewEnum.Timeline
            }
        }
        ownDomain = authService.getCurrentSession()?.serverUrl.orEmpty()
    }

    fun getAppIcon(context: KmpContext) {
        appIcon = iconManager.getCurrentIcon()
    }

    fun updateAccountSwitch() {
        loadData(false)
        ownDomain = authService.getCurrentSession()?.serverUrl.orEmpty()
    }

    fun loadData(refreshing: Boolean) {
        getAccount(refreshing)
        getPostsFirstLoad(refreshing)

        viewModelScope.launch {
            val currentLoginData = authService.getCurrentSession()
            currentLoginData?.let {
                collectionsState = collectionsState.copy(endReached = false)
                getCollections(it.accountId, false)
            }
        }
    }

    private fun getAccount(refreshing: Boolean) {
        accountService.getOwnAccount().onEach { result ->
            accountState = when (result) {
                is Resource.Success -> {
                    AccountState(account = result.data)
                }

                is Resource.Error -> {
                    AccountState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    AccountState(
                        isLoading = true, account = accountState.account, refreshing = refreshing
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getPostsFirstLoad(refreshing: Boolean) {
        postService.getOwnPosts().onEach { result ->
            postsState = when (result) {
                is Resource.Success -> {
                    val endReached = (result.data?.size ?: 0) < Constants.PROFILE_POSTS_LIMIT
                    PostsState(posts = result.data ?: emptyList(), endReached = endReached)
                }

                is Resource.Error -> {
                    PostsState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    PostsState(isLoading = true, posts = postsState.posts, refreshing = refreshing)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getPostsPaginated() {
        if (postsState.posts.isNotEmpty() && !postsState.isLoading && !postsState.endReached) {
            postService.getOwnPosts(postsState.posts.last().id).onEach { result ->
                postsState = when (result) {
                    is Resource.Success -> {
                        val endReached = (result.data?.size ?: 0) < Constants.PROFILE_POSTS_LIMIT
                        PostsState(
                            posts = postsState.posts + (result.data ?: emptyList()),
                            endReached = endReached
                        )
                    }

                    is Resource.Error -> {
                        PostsState(error = result.message ?: "An unexpected error occurred")
                    }

                    is Resource.Loading -> {
                        PostsState(isLoading = true, posts = postsState.posts)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun getCollections(userId: String, paginated: Boolean) {
        if (collectionsState.endReached) {
            return
        }
        if (!paginated) {
            collectionPage = 1
        } else {
            collectionPage++
        }
        collectionService.getCollections(userId, collectionPage).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    collectionsState = if (!paginated) {
                        CollectionsState(collections = result.data ?: emptyList())
                    } else {
                        val endReached = result.data!!.isEmpty()
                        CollectionsState(
                            collections = collectionsState.collections + result.data,
                            endReached = endReached
                        )
                    }
                }

                is Resource.Error -> {
                    collectionsState =
                        CollectionsState(error = result.message ?: "An unexpected error occurred")
                }

                is Resource.Loading -> {
                    collectionsState = CollectionsState(
                        isLoading = true, collections = collectionsState.collections
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun openUrl(url: String, context: KmpContext) {
        openExternalUrlUseCase(url, context)
    }

    fun changeView(newView: ViewEnum) {
        view = newView
        prefs.showUserGridTimeline = newView == ViewEnum.Grid
    }

    fun postGetsDeleted(postId: String) {
        postsState = postsState.copy(posts = postsState.posts.filter { post -> post.id != postId })
    }

    fun updatePost(post: Post) {
        postsState = postsState.copy(posts = postsState.posts.map {
            if (it.id == post.id) {
                post
            } else {
                it
            }
        })
    }
}