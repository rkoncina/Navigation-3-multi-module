package com.hubspot.home

import NavigationFeature
import NavigationSelectionScreen
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object NavigationHome

val homeScreenEntryProviders: (page: Any, navigateTo: (Any) -> Unit) -> (@Composable () -> Unit)? =
    { page, navigateTo ->
        when (page) {
            is NavigationHome -> {
                { HomeScreen(navigateTo = navigateTo) }
            }

            else -> null
        }
    }


@Composable
fun HomeScreen(
    navigateTo: (Any) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val events = viewModel.events

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is HomeViewModel.MainEvent.NavigateToFirstSelection -> navigateTo(
                    NavigationSelectionScreen(event.initialSelection) {
                        viewModel.onFirstItemsSelected(it)
                    }
                )

                is HomeViewModel.MainEvent.NavigateToSecondSelection -> navigateTo(
                    NavigationSelectionScreen(event.initialSelection) {
                        viewModel.onSecondItemsSelected(it)
                    }
                )

                is HomeViewModel.MainEvent.NavigateToFeature -> navigateTo(NavigationFeature(message = event.message))
            }
        }
    }

    Column(modifier = modifier) {
        Text("Home screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "First numbers: ${state.first}!",
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .clickable { viewModel.onFirstItemsClicked() }
                .padding(8.dp),
        )
        Text(
            text = "Second numbers: ${state.second}!",
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .clickable { viewModel.onSecondItemsClicked() }
                .padding(8.dp),
        )

        Button(onClick = { viewModel.onOpenFeatureClicked() }) { Text("Open feature") }
    }
}

class HomeViewModel : ViewModel() {
    private val firstNumbers = MutableStateFlow(listOf(1, 2, 3, 4, 5))
    private val secondNumbers = MutableStateFlow(listOf(1, 2, 3, 4, 5))

    val state = combine(firstNumbers, secondNumbers)
    { firstNumbers, secondNumbers -> firstNumbers to secondNumbers }
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf<Int>() to listOf<Int>())

    val events = MutableSharedFlow<MainEvent>()

    fun onFirstItemsClicked() {
        viewModelScope.launch {
            events.emit(MainEvent.NavigateToFirstSelection(firstNumbers.value))
        }
    }

    fun onFirstItemsSelected(items: List<Int>) {
        firstNumbers.value = items
    }

    fun onSecondItemsClicked() {
        viewModelScope.launch {
            events.emit(MainEvent.NavigateToSecondSelection(secondNumbers.value))
        }
    }

    fun onSecondItemsSelected(items: List<Int>) {
        secondNumbers.value = items
    }

    fun onOpenFeatureClicked() {
        viewModelScope.launch {
            events.emit(MainEvent.NavigateToFeature("Main screen has ${state.value.run { first.count() + second.count() }} values selected"))
        }
    }

    sealed interface MainEvent {
        data class NavigateToFirstSelection(val initialSelection: List<Int>) : MainEvent
        data class NavigateToSecondSelection(val initialSelection: List<Int>) : MainEvent
        data class NavigateToFeature(val message: String) : MainEvent
    }
}