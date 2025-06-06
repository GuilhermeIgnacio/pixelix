package com.daniebeler.pfpixelix.ui.composables.profile.other_profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.daniebeler.pfpixelix.di.injectViewModel
import com.daniebeler.pfpixelix.domain.model.Account
import com.daniebeler.pfpixelix.ui.composables.ButtonRowElement
import com.daniebeler.pfpixelix.ui.composables.InfiniteListHandler
import com.daniebeler.pfpixelix.ui.composables.ToTopButton
import com.daniebeler.pfpixelix.ui.composables.profile.CollectionsComposable
import com.daniebeler.pfpixelix.ui.composables.profile.MutualFollowersComposable
import com.daniebeler.pfpixelix.ui.composables.profile.PostsWrapperComposable
import com.daniebeler.pfpixelix.ui.composables.profile.ProfileTopSection
import com.daniebeler.pfpixelix.ui.composables.profile.SwitchViewComposable
import com.daniebeler.pfpixelix.ui.composables.profile.server_stats.DomainSoftwareComposable
import com.daniebeler.pfpixelix.ui.composables.states.EmptyState
import com.daniebeler.pfpixelix.ui.navigation.Destination
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pixelix.app.generated.resources.Res
import pixelix.app.generated.resources.block
import pixelix.app.generated.resources.block_account
import pixelix.app.generated.resources.block_consequence_1
import pixelix.app.generated.resources.block_consequence_10
import pixelix.app.generated.resources.block_consequence_2
import pixelix.app.generated.resources.block_consequence_3
import pixelix.app.generated.resources.block_consequence_4
import pixelix.app.generated.resources.block_consequence_5
import pixelix.app.generated.resources.block_consequence_6
import pixelix.app.generated.resources.block_consequence_7
import pixelix.app.generated.resources.block_consequence_8
import pixelix.app.generated.resources.block_consequence_9
import pixelix.app.generated.resources.block_this_profile
import pixelix.app.generated.resources.browsers_outline
import pixelix.app.generated.resources.cancel
import pixelix.app.generated.resources.chevron_back_outline
import pixelix.app.generated.resources.default_avatar
import pixelix.app.generated.resources.follow
import pixelix.app.generated.resources.message
import pixelix.app.generated.resources.mute
import pixelix.app.generated.resources.mute_account
import pixelix.app.generated.resources.mute_consequence_1
import pixelix.app.generated.resources.mute_consequence_2
import pixelix.app.generated.resources.mute_consequence_3
import pixelix.app.generated.resources.mute_consequence_4
import pixelix.app.generated.resources.mute_consequence_5
import pixelix.app.generated.resources.mute_this_profile
import pixelix.app.generated.resources.open_in_browser
import pixelix.app.generated.resources.remove_circle_outline
import pixelix.app.generated.resources.share_social_outline
import pixelix.app.generated.resources.share_this_profile
import pixelix.app.generated.resources.unblock_account
import pixelix.app.generated.resources.unblock_caps
import pixelix.app.generated.resources.unblock_this_profile
import pixelix.app.generated.resources.unfollow
import pixelix.app.generated.resources.unmute_account
import pixelix.app.generated.resources.unmute_caps
import pixelix.app.generated.resources.unmute_this_profile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun OtherProfileComposable(
    navController: NavController,
    userId: String,
    byUsername: String?,
    viewModel: OtherProfileViewModel = injectViewModel(key = "other-profile$userId$byUsername") { otherProfileViewModel }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lazyGridState = rememberLazyListState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showMuteAlert by remember { mutableStateOf(false) }
    var showUnMuteAlert by remember { mutableStateOf(false) }
    var showBlockAlert by remember { mutableStateOf(false) }
    var showUnBlockAlert by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != "") {
            viewModel.loadData(userId, false, navController)
        } else {
            viewModel.loadDataByUsername(byUsername!!, false, navController)
        }
    }


    Scaffold(contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top), topBar = {
        CenterAlignedTopAppBar(title = {
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = viewModel.accountState.account?.username ?: "",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = viewModel.domain, fontSize = 12.sp, lineHeight = 6.sp
                    )
                }

            }
        }, navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = vectorResource(Res.drawable.chevron_back_outline), contentDescription = ""
                )
            }
        }, actions = {

            if (viewModel.domain.isNotEmpty()) {
                DomainSoftwareComposable(
                    domain = viewModel.domain
                )
            }

            IconButton(onClick = {
                showBottomSheet = true
            }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert, contentDescription = ""
                )
            }
        })

    }) { paddingValues ->
        PullToRefreshBox (
            isRefreshing = viewModel.accountState.refreshing || viewModel.postsState.refreshing,
            onRefresh = { viewModel.loadData(userId, true, navController) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = lazyGridState
            ) {
                item {
                    Column {
                        if (viewModel.accountState.account != null) {
                            ProfileTopSection(account = viewModel.accountState.account,
                                relationship = viewModel.relationshipState.accountRelationship,
                                navController,
                                openUrl = { url ->
                                    viewModel.openUrl(url)
                                })
                        }

                        MutualFollowersComposable(
                            mutualFollowersState = viewModel.mutualFollowersState,
                            navController = navController
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            var containerColor by remember {
                                mutableStateOf(Color(0xFFFFFFFF))
                            }

                            var contentColor by remember {
                                mutableStateOf(Color(0xFFFFFFFF))
                            }

                            if (viewModel.relationshipState.accountRelationship?.following == true) {
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                containerColor = MaterialTheme.colorScheme.primary
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            }

                            Button(
                                onClick = {
                                    if (!viewModel.relationshipState.isLoading && viewModel.relationshipState.accountRelationship != null) {
                                        if (viewModel.relationshipState.accountRelationship?.following == true) {
                                            viewModel.unfollowAccount(viewModel.userId)
                                        } else {
                                            viewModel.followAccount(viewModel.userId)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = containerColor, contentColor = contentColor
                                )
                            ) {
                                if (viewModel.relationshipState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp), color = contentColor
                                    )
                                } else {
                                    if (viewModel.relationshipState.accountRelationship?.following == true) {
                                        Text(text = stringResource(Res.string.unfollow))
                                    } else {
                                        Text(text = stringResource(Res.string.follow))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    viewModel.accountState.account?.let { account ->
                                        navController.navigate(Destination.Chat(account.id))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(text = stringResource(Res.string.message))
                            }
                        }

                        viewModel.accountState.account?.let { account ->
                            CollectionsComposable(collectionsState = viewModel.collectionsState,
                                getMoreCollections = {viewModel.getCollections(account.id, true)},
                                navController = navController,
                                instanceDomain = viewModel.domain,
                                openUrl = { url -> viewModel.openUrl(url) })
                        }

                        HorizontalDivider(Modifier.padding(bottom = 12.dp, top = 12.dp))

                        SwitchViewComposable(postsCount = viewModel.accountState.account?.postsCount
                            ?: 0,
                            viewType = viewModel.view,
                            onViewChange = {
                                viewModel.changeView(it)
                            })
                    }
                }

                PostsWrapperComposable(
                    posts = viewModel.postsState.posts,
                    isLoading = viewModel.postsState.isLoading,
                    isRefreshing = viewModel.accountState.refreshing || viewModel.postsState.refreshing,
                    error = viewModel.postsState.error,
                    endReached = viewModel.postsState.endReached,
                    emptyMessage = EmptyState(
                        icon = Icons.Outlined.Photo, heading = "No Posts"
                    ),
                    view = viewModel.view,
                    isFirstImageLarge = true,
                    postGetsDeleted = { viewModel.postGetsDeleted(it) },
                    updatePost = { viewModel.updatePost(it) },
                    navController = navController
                )

                /*PostsWrapperComposable(
                    accountState = viewModel.accountState,
                    postsState = viewModel.postsState,
                    navController = navController,
                    emptyState = EmptyState(
                        icon = Icons.Outlined.Photo, heading = "No Posts"
                    ),
                    view = viewModel.view,
                    postGetsDeleted = { viewModel.postGetsDeleted(it) },
                    isFirstImageLarge = true
                )*/
            }
        }
    }

    ToTopButton(listState = lazyGridState, refresh = {viewModel.loadData(userId, true, navController)})

    InfiniteListHandler(lazyListState = lazyGridState) {
        viewModel.getPostsPaginated(viewModel.userId)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            }, sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                if (viewModel.relationshipState.accountRelationship != null) {
                    if (viewModel.relationshipState.accountRelationship!!.muting) {
                        ButtonRowElement(icon = Res.drawable.remove_circle_outline,
                            text = stringResource(
                                Res.string.unmute_this_profile
                            ),
                            onClick = {
                                showUnMuteAlert = true
                            })
                    } else {
                        ButtonRowElement(icon = Res.drawable.remove_circle_outline,
                            text = stringResource(
                                Res.string.mute_this_profile
                            ),
                            onClick = {
                                showMuteAlert = true
                            })
                    }

                    if (viewModel.relationshipState.accountRelationship!!.blocking) {
                        ButtonRowElement(icon = Res.drawable.remove_circle_outline, text = stringResource(
                            Res.string.unblock_this_profile
                        ), onClick = {
                            showUnBlockAlert = true
                        })
                    } else {
                        ButtonRowElement(icon = Res.drawable.remove_circle_outline, text = stringResource(
                            Res.string.block_this_profile
                        ), onClick = {
                            showBlockAlert = true
                        })
                    }
                }

                HorizontalDivider(Modifier.padding(12.dp))

                ButtonRowElement(icon = Res.drawable.browsers_outline, text = stringResource(
                    Res.string.open_in_browser
                ), onClick = {
                    viewModel.openUrl(viewModel.accountState.account!!.url)
                })

                ButtonRowElement(icon = Res.drawable.share_social_outline,
                    text = stringResource(Res.string.share_this_profile),
                    onClick = {
                        viewModel.shareAccountUrl()
                    })
            }
        }
    }

    if (showUnMuteAlert) {
        UnMuteAccountAlert(onDismissRequest = { showUnMuteAlert = false }, onConfirmation = {
            showUnMuteAlert = false
            viewModel.unMuteAccount(viewModel.userId)
        }, account = viewModel.accountState.account!!
        )
    }
    if (showMuteAlert) {
        MuteAccountAlert(onDismissRequest = { showMuteAlert = false }, onConfirmation = {
            showMuteAlert = false
            viewModel.muteAccount(viewModel.userId)
        }, account = viewModel.accountState.account!!
        )
    }
    if (showBlockAlert) {
        BlockAccountAlert(onDismissRequest = { showBlockAlert = false }, onConfirmation = {
            showBlockAlert = false
            viewModel.blockAccount(viewModel.userId)
        }, account = viewModel.accountState.account!!
        )
    }
    if (showUnBlockAlert) {
        UnBlockAccountAlert(onDismissRequest = { showUnBlockAlert = false }, onConfirmation = {
            showUnBlockAlert = false
            viewModel.unblockAccount(viewModel.userId)
        }, account = viewModel.accountState.account!!
        )
    }
}

@Composable
fun MuteAccountAlert(
    onDismissRequest: () -> Unit, onConfirmation: () -> Unit, account: Account
) {
    AlertDialog(title = {
        Text(text = stringResource(Res.string.mute_account))
    }, text = {
        Column {

            AlertTopSection(account = account)

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(text = stringResource(Res.string.mute_consequence_1))
            Text(text = stringResource(Res.string.mute_consequence_2))
            Text(text = stringResource(Res.string.mute_consequence_3))
            Text(text = stringResource(Res.string.mute_consequence_4))

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(text = stringResource(Res.string.mute_consequence_5))

        }
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {
        TextButton(onClick = {
            onConfirmation()
        }) {
            Text(stringResource(Res.string.mute))
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text(stringResource(Res.string.cancel))
        }
    })
}

@Composable
fun UnMuteAccountAlert(
    onDismissRequest: () -> Unit, onConfirmation: () -> Unit, account: Account
) {
    AlertDialog(title = {
        Text(text = stringResource(Res.string.unmute_account))
    }, text = {
        AlertTopSection(account = account)


    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {
        TextButton(onClick = {
            onConfirmation()
        }) {
            Text(stringResource(Res.string.unmute_caps))
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text(stringResource(Res.string.cancel))
        }
    })
}

@Composable
fun BlockAccountAlert(
    onDismissRequest: () -> Unit, onConfirmation: () -> Unit, account: Account
) {
    AlertDialog(title = {
        Text(text = stringResource(Res.string.block_account))
    }, text = {
        Column {

            AlertTopSection(account = account)

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(text = stringResource(Res.string.block_consequence_1))
            Text(text = stringResource(Res.string.block_consequence_2))
            Text(text = stringResource(Res.string.block_consequence_3))
            Text(text = stringResource(Res.string.block_consequence_4))
            Text(text = stringResource(Res.string.block_consequence_5))

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(text = stringResource(Res.string.block_consequence_6))
            Text(text = stringResource(Res.string.block_consequence_7))
            Text(text = stringResource(Res.string.block_consequence_8))
            Text(text = stringResource(Res.string.block_consequence_9))

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text(text = stringResource(Res.string.block_consequence_10))
        }
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {
        TextButton(onClick = {
            onConfirmation()
        }) {
            Text(stringResource(Res.string.block))
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text(stringResource(Res.string.cancel))
        }
    })

}

@Composable
fun UnBlockAccountAlert(
    onDismissRequest: () -> Unit, onConfirmation: () -> Unit, account: Account
) {
    AlertDialog(title = {
        Text(text = stringResource(Res.string.unblock_account))
    }, text = {
        AlertTopSection(account = account)
    }, onDismissRequest = {
        onDismissRequest()
    }, confirmButton = {
        TextButton(onClick = {
            onConfirmation()
        }) {
            Text(stringResource(Res.string.unblock_caps))
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismissRequest()
        }) {
            Text(stringResource(Res.string.cancel))
        }
    })
}

@Composable
fun AlertTopSection(account: Account) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = account.avatar,
            error = painterResource(Res.drawable.default_avatar),
            contentDescription = "",
            modifier = Modifier
                .height(46.dp)
                .width(46.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {

            Column {
                if (account.displayname != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.displayname,
                            lineHeight = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = account.username, fontSize = 12.sp)
                    Text(
                        text = " • " + (account.url.substringAfter("https://").substringBefore("/")
                            ?: ""), color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp
                    )
                }

            }
        }
    }
}