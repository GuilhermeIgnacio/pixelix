package com.daniebeler.pfpixelix.ui.composables.settings.followed_hashtags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.daniebeler.pfpixelix.di.injectViewModel
import com.daniebeler.pfpixelix.ui.composables.CustomHashtag
import com.daniebeler.pfpixelix.ui.composables.states.EmptyState
import com.daniebeler.pfpixelix.ui.composables.states.FullscreenEmptyStateComposable
import com.daniebeler.pfpixelix.ui.composables.states.FullscreenErrorComposable
import com.daniebeler.pfpixelix.ui.composables.states.FullscreenLoadingComposable
import com.daniebeler.pfpixelix.ui.navigation.Destination
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pixelix.app.generated.resources.Res
import pixelix.app.generated.resources.chevron_back_outline
import pixelix.app.generated.resources.followed_hashtags
import pixelix.app.generated.resources.no_followed_hashtags

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun FollowedHashtagsComposable(
    navController: NavController,
    viewModel: FollowedHashtagsViewModel = injectViewModel(key = "followed-hashtags-key") { followedHashtagsViewModel }
) {
    Scaffold(contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top), topBar = {
        CenterAlignedTopAppBar(title = {
            Text(stringResource(Res.string.followed_hashtags), fontWeight = FontWeight.Bold)
        }, navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = vectorResource(Res.drawable.chevron_back_outline), contentDescription = ""
                )
            }
        })

    }) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = viewModel.followedHashtagsState.isRefreshing,
            onRefresh = { viewModel.getFollowedHashtags(true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
                content = {
                    items(viewModel.followedHashtagsState.followedHashtags) { tag ->
                        CustomHashtag(hashtag = tag, navController = navController)
                    }
                })

            if (viewModel.followedHashtagsState.followedHashtags.isEmpty()) {
                if (viewModel.followedHashtagsState.isLoading && !viewModel.followedHashtagsState.isRefreshing) {
                    FullscreenLoadingComposable()
                }

                if (viewModel.followedHashtagsState.error.isNotEmpty()) {
                    FullscreenErrorComposable(message = viewModel.followedHashtagsState.error)
                }

                if (!viewModel.followedHashtagsState.isLoading && viewModel.followedHashtagsState.error.isEmpty()) {
                    FullscreenEmptyStateComposable(
                        EmptyState(icon = Icons.Outlined.Tag,
                            heading = stringResource(Res.string.no_followed_hashtags),
                            message = "Followed hashtags will appear here",
                            buttonText = "Explore trending hashtags",
                            onClick = {
                                navController.navigate(Destination.Search(2))
                            })
                    )
                }
            }
        }

    }
}