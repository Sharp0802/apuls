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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.apulsetech.apuls2.platform.MailSender
import com.apulsetech.apuls2.platform.Network
import com.apulsetech.apuls2.platform.NoopDevice
import jakarta.activation.DataHandler
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.util.ByteArrayDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.Serializable
import java.util.Properties
import kotlin.reflect.KClass

data class Interval(
    val hour: Int = 24,
    val minute: Int = 0,
    val second: Int = 0
) : Serializable {
    val totalSeconds: Long
        get() = (hour.toLong() * 60 + minute) * 60 + second
}

data class Mail(
    val address: String = "",
    val password: String = "",
)

data class MailTarget(
    val mail: Mail = Mail(),
    val interval: Interval = Interval()
) : Serializable {
    fun send(data: String) {
        val props = Properties()
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"

        val session = jakarta.mail.Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(mail.address, mail.password)
            }
        })

        val body = MimeBodyPart()
        body.setText(
            "This is sent by configured Apuls app\n" +
                    "To disable, Please use Settings panel of Apuls app"
        )

        val attachment = MimeBodyPart()
        val source = ByteArrayDataSource(data, "text/csv")
        attachment.dataHandler = DataHandler(source)
        attachment.fileName = "inventory.csv"

        val multipart = MimeMultipart()
        multipart.addBodyPart(body)
        multipart.addBodyPart(attachment)

        val message = MimeMessage(session)

        val address = InternetAddress(mail.address)
        message.setFrom(address)
        message.addRecipient(Message.RecipientType.TO, address)
        message.subject = "[Apuls] Inventory Report"
        message.setContent(multipart)

        try {
            Transport.send(message)
        } catch (t: Throwable) {
            println(t)
        }
    }
}

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
        var lastEpoch = 0L
        while (isActive) {
            val epoch = System.currentTimeMillis() / 1000
            var delay = 0L

            val vm = InventoryViewModel.current ?: return
            val csv = vm.serialize()

            for (mail in mails) {
                val interval = mail.interval.totalSeconds

                val base = lastEpoch / interval * interval

                val lastOfs = lastEpoch - base + 1
                val ofs = epoch - base

                if (interval in lastOfs..ofs) {
                    launch {
                        mail.send(csv)
                    }
                }

                delay = maxOf(interval - ofs, 0)
            }

            delay(delay * 1000)
            lastEpoch = epoch
        }
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
    val password = rememberTextFieldState(initialText = mail.mail.password)

    LaunchedEffect(password) {
        onChange(mail.copy(mail = mail.mail.copy(password = password.text.toString())))
    }

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

            OutlinedSecureTextField(
                label = { Text("Password") },
                state = password,
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
