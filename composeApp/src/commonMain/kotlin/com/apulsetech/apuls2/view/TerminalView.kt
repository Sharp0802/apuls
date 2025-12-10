package com.apulsetech.apuls2.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apulsetech.apuls2.collection.ObservableRingBuffer
import com.apulsetech.apuls2.net.Session
import com.apulsetech.apuls2.platform.NoopDevice
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.Serializable
import kotlin.reflect.KClass

data class Log(val color: Color, val content: String) : Serializable

class TerminalViewModel(colors: ColorScheme, session: Session) : ViewModel() {
    companion object {
        fun factory(colors: ColorScheme, session: Session): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: KClass<T>,
                    extras: CreationExtras
                ): T {
                    return TerminalViewModel(colors, session) as T
                }
            }
    }

    val logs = ObservableRingBuffer<Log>(256)

    init {
        session.onSendRaw {
            logs.write(Log(colors.primary, it))
        }
    }
}

@Composable
@Preview
fun TerminalViewPreview() {
    Scaffold { inner ->
        TerminalView(Session(NoopDevice), Modifier.padding(inner))
    }
}

@Composable
fun TerminalView(session: Session, modifier: Modifier = Modifier) {
    val colors = colorScheme
    val factory = remember(session) { TerminalViewModel.factory(colors, session) }
    val vm: TerminalViewModel = viewModel(key = "terminal-${session.id}", factory = factory)

    LazyColumn {
        items(vm.logs.size, key = { vm.logs[it] }) {
            val log = vm.logs[it]
            Text(log.content, color = log.color)
        }
    }
}
