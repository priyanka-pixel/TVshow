package com.tes.android.projects.tvshowsapp.presentation.show_listings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tes.android.projects.tvshowsapp.R
import com.tes.android.projects.tvshowsapp.core.components.RatingBar
import com.tes.android.projects.tvshowsapp.core.navigation.SHOW_DETAIL_SCREEN
import com.tes.android.projects.tvshowsapp.domain.model.ShowListing


@Composable
fun ShowListingsScreen(
    navController: NavController,
    viewModel: ShowListingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = uiState.isRefreshing
    )

    // Launch a coroutine bound to the scope of the composable, viewModel relaunched
    LaunchedEffect(key1 = viewModel, block = {
        viewModel.onEvent(ShowListingsEvent.LoadShows)
    })

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TopAppBarContent()

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = {
                viewModel.onEvent(
                    ShowListingsEvent.OnSearchQueryChange(it)
                )
            },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            placeholder = {
                Text(text = "Search...")
            },
            maxLines = 1,
            singleLine = true,
            label = { Text("Search") }
        )
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                viewModel.onEvent(ShowListingsEvent.Refresh)
            }
        ) {
            LazyColumn(
                //modifier = Modifier.fillMaxSize()
                modifier = Modifier.padding(10.dp)
            ) {
                items(uiState.shows.size) { i ->
                    val show = uiState.shows[i]

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        ShowItem(
                            show = show,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(route = "${SHOW_DETAIL_SCREEN}/${show.name}")
                                }
                                .padding(8.dp)
                        )
                        FavoriteButton(show = show, viewModel = viewModel)
                    }
                    if (i < uiState.shows.size) {
                        Divider(
                            modifier = Modifier.padding(
                                vertical = 16.dp
                            )
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun FavoriteButton(
    viewModel: ShowListingsViewModel = hiltViewModel(),
    show: ShowListing
) {
    var isFavorite by rememberSaveable(show) { mutableStateOf(show.isFavorite) }

    IconButton(onClick = {
        isFavorite = !isFavorite
        show.isFavorite = isFavorite
        if (isFavorite) {
            viewModel.onEvent(ShowListingsEvent.OnFavoriteSelected(show))
        } else {
            viewModel.onEvent(ShowListingsEvent.DeleteFavorite(show.id))
        }
    }) {
        val tintColor = if (isFavorite) Red else White
        Icon(
            painter = rememberVectorPainter(Icons.Default.Favorite),
            contentDescription = null,
            tint = tintColor
        )
    }
}


@Composable
fun TopAppBarContent() {
    TopAppBar(

        title = {
            Text(

                text = stringResource(R.string.tvmazshows),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 20.dp)
            )
        },
        modifier = Modifier.padding(10.dp),
        backgroundColor = MaterialTheme.colors.background,
        elevation = 4.dp,
    )
}

@Composable
fun ShowItem(
    show: ShowListing,
    modifier: Modifier = Modifier
) {
    val imagePainter = rememberAsyncImagePainter(show.image.medium)
    Row(
        modifier = modifier
            .background(MaterialTheme.colors.surface)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 2.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                modifier = Modifier
                    .height(150.dp)
                    .width(100.dp),
                painter = imagePainter, contentDescription = "Show",
            )
        }
        Column {
            Text(
                text = show.name,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(start = 20.dp)
            )
            Text(
                text = show.type,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(start = 20.dp)
            )
            Text(
                text = "(${show.runtime})",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(start = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.padding(start = 20.dp)) {
                RatingBar(rating = show.rating.average)
                Text(
                    text = "${show.rating.average/2.0}",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}
