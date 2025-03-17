import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class NavigationFeature(val message: String)

val featureScreenEntryProviders: (page: Any, navigateTo: (Any) -> Unit) -> (@Composable () -> Unit)? =
    { page, navigateTo ->
        when (page) {
            is NavigationFeature -> {
                { FeatureScreen(navigateTo = navigateTo, message = page.message) }
            }

            else -> null
        }
    }

@Composable
fun FeatureScreen(
    navigateTo: (Any) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = viewModel(),
    message: String,
) {

    val state by viewModel.state.collectAsState()
    val events = viewModel.events

    LaunchedEffect(message) {
        viewModel.init(message)
    }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is FeatureViewModel.FeatureEvent.NavigateToFirstSelection -> navigateTo(
                    NavigationSelectionScreen(event.initialSelection) {
                        viewModel.onFirstItemsSelected(it)
                    }
                )

                is FeatureViewModel.FeatureEvent.NavigateToSecondSelection -> navigateTo(
                    NavigationSelectionScreen(event.initialSelection) {
                        viewModel.onSecondItemsSelected(it)
                    }
                )

                else -> error("Unknown event: $event")
            }
        }
    }

    Column(modifier = modifier) {
        Text("Feature screen", fontSize = 24.sp)
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

        Text(text = "Message: ${state.third}")
    }
}

class FeatureViewModel : ViewModel() {
    private val firstNumbers = MutableStateFlow(listOf(1, 2, 3, 4, 5))
    private val secondNumbers = MutableStateFlow(listOf(1, 2, 3, 4, 5))
    private val message = MutableStateFlow("")

    val state = combine(
        firstNumbers,
        secondNumbers,
        message,
        ::Triple,
    )
        .stateIn(viewModelScope, SharingStarted.Eagerly, Triple(listOf(), listOf(), ""))

    val events = MutableSharedFlow<FeatureEvent>()

    fun init(message: String) {
        this.message.value = message
    }

    fun onFirstItemsClicked() {
        viewModelScope.launch {
            events.emit(FeatureEvent.NavigateToFirstSelection(firstNumbers.value))
        }
    }

    fun onFirstItemsSelected(items: List<Int>) {
        firstNumbers.value = items
    }

    fun onSecondItemsClicked() {
        viewModelScope.launch {
            events.emit(FeatureEvent.NavigateToSecondSelection(secondNumbers.value))
        }
    }

    fun onSecondItemsSelected(items: List<Int>) {
        secondNumbers.value = items
    }

    sealed interface FeatureEvent {
        data class NavigateToFirstSelection(val initialSelection: List<Int>) : FeatureEvent
        data class NavigateToSecondSelection(val initialSelection: List<Int>) : FeatureEvent
    }
}
