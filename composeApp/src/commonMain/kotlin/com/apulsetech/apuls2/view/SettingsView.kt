package com.apulsetech.apuls2.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apulsetech.apuls2.net.Session
import com.apulsetech.apuls2.platform.NoopDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.Serializable
import kotlin.reflect.KClass

data class Interval(
    val hour: Int = 24,
    val minute: Int = 0,
    val second: Int = 0
) : Serializable

data class Mail(
    val address: String = "",
    val password: String = "",
) : Serializable {
    fun send(data: String) {
        // TODO("Send mail")
    }
}

data class MailTarget(
    val mail: Mail = Mail(),
    val interval: Interval = Interval()
) : Serializable

class SettingsViewModel(val session: Session) : ViewModel() {
    companion object {
        fun factory(session: Session): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: KClass<T>, extras: CreationExtras
                ): T {
                    return SettingsViewModel(session) as T
                }
            }
    }

    val mails = mutableStateListOf<MailTarget>()

    private var job: Job? = null

    init {
        job = viewModelScope.launch(Dispatchers.Unconfined) {
            loop()
        }
    }

    suspend fun CoroutineScope.loop() {
        // TODO("Send inventory report periodically")
    }

    override fun onCleared() {
        job?.cancel()
        super.onCleared()
    }
}

@Composable
@Preview
fun SettingsViewPreview() {
    SettingsView(Session(NoopDevice))
}

@Composable
fun SettingsView(session: Session, modifier: Modifier = Modifier) {
    val factory = remember(session) { SettingsViewModel.factory(session) }
    val vm: SettingsViewModel = viewModel(key = "settings-${session.id}", factory = factory)

    Scaffold(modifier) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            Column(Modifier.padding(8.dp)) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Mail Targets",
                            style = typography.titleMedium
                        )

                        HorizontalDivider()

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (vm.mails.isNotEmpty()) {
                                itemsIndexed(vm.mails) { i, mail ->
                                    MailView(mail = mail, modifier = Modifier.fillMaxWidth()) {
                                        vm.mails[i] = it
                                    }
                                }
                            } else {
                                item {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "No mail registered",
                                            color = colorScheme.onSurface.copy(0.4f)
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilledTonalButton(onClick = { vm.mails.add(MailTarget()) }) {
                                Row {
                                    Icon(Icons.Default.Add, contentDescription = "Add")
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun MailViewPreview() {
    MailView(MailTarget(Mail("test@mail.com", "this is password"))) {}
}

@Composable
fun MailView(mail: MailTarget, modifier: Modifier = Modifier, onChange: (MailTarget) -> Unit) {
    Card(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Target",
                style = typography.titleSmall
            )

            OutlinedTextField(
                label = { Text("Address") },
                value = mail.mail.address,
                onValueChange = {
                    onChange(mail.copy(mail = mail.mail.copy(address = it)))
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text("Password") },
                value = mail.mail.password,
                onValueChange = {
                    onChange(mail.copy(mail = mail.mail.copy(password = it)))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Interval",
                style = typography.titleSmall,
                modifier = Modifier.padding(top = 12.dp)
            )

            IntervalView(mail.interval, Modifier.fillMaxWidth()) {
                onChange(mail.copy(interval = it))
            }
        }
    }
}

@Composable
fun IntervalView(interval: Interval, modifier: Modifier = Modifier, onChange: (Interval) -> Unit) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            label = { Text("Hour") },
            value = interval.hour.toString(),
            onValueChange = {
                val int = it.toIntOrNull() ?: return@OutlinedTextField
                if (int !in 0..24) return@OutlinedTextField

                onChange(interval.copy(hour = int))
            },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            label = { Text("Minute") },
            value = interval.minute.toString(),
            onValueChange = {
                val int = it.toIntOrNull() ?: return@OutlinedTextField
                if (int !in 0..60) return@OutlinedTextField

                onChange(interval.copy(minute = int))
            },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            label = { Text("Second") },
            value = interval.second.toString(),
            onValueChange = {
                val int = it.toIntOrNull() ?: return@OutlinedTextField
                if (int !in 0..60) return@OutlinedTextField

                onChange(interval.copy(second = int))
            },
            modifier = Modifier.weight(1f)
        )
    }
}
