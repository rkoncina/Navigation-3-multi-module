import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data object NavigateBack
data class NavigationSelectionScreen(
    val initialSelection: List<Int>,
    val onItemsSelected: (List<Int>) -> Unit = {}, // this one is not serializable, won't survive orientation change
)

val selectionScreenEntryProviders: (page: Any, navigateTo: (Any) -> Unit) -> (@Composable () -> Unit)? =
    { page, navigateTo ->
        when (page) {
            is NavigationSelectionScreen -> {
                {
                    SelectionScreen(
                        initialSelection = page.initialSelection,
                        onApplySelection = { selectedItems ->
                            navigateTo(NavigateBack)
                            page.onItemsSelected(selectedItems)
                        },
                    )
                }
            }

            else -> null
        }
    }


@Composable
fun SelectionScreen(
    initialSelection: List<Int>,
    onApplySelection: (List<Int>) -> Unit,
    viewModel: SelectionViewModel = viewModel()
) {
    LaunchedEffect(initialSelection) {
        viewModel.init(allItems = (1..5).toList(), selectedItems = initialSelection)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.finishWithResult.collect {
            onApplySelection(it)
        }
    }

    Column {
        Text("Selection screen", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        state.forEach { (value, selected) ->
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                    .clickable { viewModel.select(value) }
                    .padding(8.dp)
            ) {
                Text("Item $value", modifier = Modifier.weight(1f))
                Checkbox(checked = selected, onCheckedChange = null)
            }
        }

        Button(onClick = { viewModel.finish() }) {
            Text("Apply")
        }
    }
}

class SelectionViewModel : ViewModel() {
    private val allItems = MutableStateFlow(listOf(1, 2, 3, 4, 5))
    private val selectedItems = MutableStateFlow(listOf<Int>())

    val state = combine(selectedItems, allItems)
    { selectedItems, allItems ->
        allItems.map { it to (it in selectedItems) }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    val finishWithResult = MutableSharedFlow<List<Int>>()

    fun init(allItems: List<Int>, selectedItems: List<Int>) {
        this.allItems.value = allItems
        this.selectedItems.value = selectedItems
    }

    fun select(item: Int) {
        selectedItems.update { list ->
            if (item in list) list - item else list + item
        }
    }

    fun finish() {
        viewModelScope.launch {
            finishWithResult.emit(selectedItems.value)
        }
    }
}