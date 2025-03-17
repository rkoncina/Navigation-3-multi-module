package com.hubspot.navigation3test

import NavigateBack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.NavEntry
import androidx.navigation3.SinglePaneNavDisplay
import com.hubspot.home.NavigationHome
import com.hubspot.home.homeScreenEntryProviders
import com.hubspot.navigation3test.ui.ui.theme.Navigation3TestTheme
import featureScreenEntryProviders
import selectionScreenEntryProviders

// this would be collected using dependency injection, without knowing individual implementations
val entryProviders = setOf(
    homeScreenEntryProviders,
    selectionScreenEntryProviders,
    featureScreenEntryProviders
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Navigation3TestTheme {
                val backStack = remember { mutableStateListOf<Any>(NavigationHome) }

                val navigateTo: (Any) -> Unit = { page ->
                    if (page == NavigateBack) {
                        backStack.removeLastOrNull()
                    } else {
                        backStack.add(page)
                    }
                }

                BackHandler(enabled = backStack.size > 1) { backStack.removeLastOrNull() }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SinglePaneNavDisplay(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp),
                        backStack = backStack,
                        entryProvider = { page ->
                            val screen = entryProviders
                                .firstNotNullOfOrNull { it(page, navigateTo) }
                                ?: error("Unknown page: $page")
                            NavEntry(key = page, content = { screen() })
                        }
                    )
                }
            }
        }
    }
}


