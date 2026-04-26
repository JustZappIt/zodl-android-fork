package co.electriccoin.zcash.ui.screen.offramp

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfframpScreen(args: OfframpArgs) {
    val vm = koinViewModel<OfframpVM> { parametersOf(args) }
    val state by vm.state.collectAsStateWithLifecycle()
    OfframpView(state)
}
