package com.apulsetech.apuls.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.Notify
import com.apulsetech.apuls.command.Command
import com.apulsetech.apuls.command.CommandDeclaration
import com.apulsetech.apuls.command.CommandDeclarations
import com.apulsetech.apuls.command.IConstraint
import com.apulsetech.apuls.command.ParameterizedCommandDeclaration
import com.apulsetech.apuls.data.compose.Renderers
import com.apulsetech.apuls.viewmodel.DeviceCommViewModel
import kotlinx.coroutines.withTimeout

@Composable
private fun dividerColor(): Color = colorScheme.surfaceContainerHigh

@Composable
private fun HDiv(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, color = dividerColor())
}

@Composable
private fun VDiv(modifier: Modifier = Modifier) {
    VerticalDivider(modifier, color = dividerColor())
}

@Composable
private fun FieldDivider() {
    HDiv(Modifier.padding(vertical = 8.dp))
}

@Composable
private fun CommandGroup(vm: DeviceCommViewModel, vararg fields: ParameterizedCommandDeclaration) {
    for (field in fields) {
        FieldDivider()
        CommandField(vm, field)
    }
}

@Composable
private fun SettingGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title)
            Column(content = content)
        }
    }
}

@Composable
private fun RowScope.Divider() {
    Spacer(Modifier.weight(1f))
    VDiv(Modifier.height(24.dp))
    Spacer(Modifier.width(16.dp))
}

@Composable
private fun ColumnScope.Divider() {
    HDiv()
}

@Composable
private fun FieldContainer(name: String, singleLine: Boolean, content: @Composable () -> Unit) {
    if (singleLine) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name)
            Divider()
            content()
        }
    } else {
        Column {
            Text(name)
            Divider()
            content()
        }
    }
}

@Composable
private fun CommandField(vm: DeviceCommViewModel, command: ParameterizedCommandDeclaration) {
    val renderer by remember { mutableStateOf(Renderers.of(command.type)) }
    var value by remember { mutableStateOf<Any?>(null) }

    val begin by remember { mutableStateOf(System.currentTimeMillis()) }
    var end by remember { mutableStateOf(System.currentTimeMillis()) }

    val callback: suspend (Command) -> Unit = remember(vm, command) {
        { received ->
            if (received.declaration == command) {
                value = received.state
            } else {
                end = System.currentTimeMillis()
            }
        }
    }

    DisposableEffect(vm, command) {
        vm.onReceived.register(callback)
        onDispose {
            vm.onReceived.unregister(callback)
        }
    }

    LaunchedEffect(command) {
        vm.send(command.getter())
    }

    if (value == null && end - begin > 1000) {
        FieldContainer(command.label, true) {
            Text(
                text = "Not Supported",
                color = colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    } else {
        FieldContainer(command.label, renderer.singleLine) {
            if (value != null) {
                renderer.Render(
                    value = value!!,
                    constraints = command.constraints,
                    onValueChanged = {
                        vm.send(command.setter(it))
                        value = it
                    },
                    enabled = vm.state.connected
                )
            } else {
                LinearProgressIndicator(Modifier.width(52.dp))
            }
        }
    }
}

@Composable
private inline fun <reified T : Any> Field(
    name: String,
    default: T,
    crossinline onValueChanged: (T) -> Unit,
    constraints: Array<IConstraint> = emptyArray(),
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    val renderer by remember { mutableStateOf(Renderers.of(T::class)) }
    var state by rememberSaveable { mutableStateOf(default) }

    FieldContainer(name, singleLine) {
        renderer.TypedRender(
            value = state,
            constraints = constraints,
            onValueChanged = {
                state = it
                onValueChanged(it)
            },
            enabled = enabled
        )
    }
}

private fun CommandDeclaration.to(): ParameterizedCommandDeclaration {
    return this as ParameterizedCommandDeclaration
}

@Composable
fun SettingsView(
    vm: DeviceCommViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier
            .padding(horizontal = 16.dp)
    ) {
        item {
            SettingGroup("General") {
                FieldDivider()
                Field("Buzz", Notify.beep, { Notify.beep = it })
                FieldDivider()
                Field("Vibration", Notify.vibrate, { Notify.vibrate = it })
            }
        }

        item {
            SettingGroup("Network") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ip.value.to(),
                    CommandDeclarations.gateway.value.to(),
                    CommandDeclarations.subnet.value.to(),
                    CommandDeclarations.port.value.to(),
                    CommandDeclarations.subport.value.to(),
                    CommandDeclarations.tcpmode.value.to(),
                    CommandDeclarations.serverip.value.to(),
                    CommandDeclarations.retrytime.value.to(),
                    CommandDeclarations.mac.value.to(),
                )
            }
        }

        item {
            SettingGroup("Serial") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ser_serial.value.to(),
                    CommandDeclarations.baudrate.value.to()
                )
            }
        }

        item {
            SettingGroup("TCP") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ser_tcp.value.to(),
                )
            }
        }

        item {
            SettingGroup("HTTP") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ser_http.value.to(),
                    CommandDeclarations.httpurl.value.to(),
                    CommandDeclarations.httpauth.value.to(),
                    CommandDeclarations.httpuser.value.to(),
                    CommandDeclarations.httppwd.value.to(),
                )
            }
        }

        item {
            SettingGroup("MQTT") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ser_http.value.to(),
                    CommandDeclarations.httpurl.value.to(),
                    CommandDeclarations.httpauth.value.to(),
                    CommandDeclarations.httpuser.value.to(),
                    CommandDeclarations.httppwd.value.to(),
                )
            }
        }

        item {
            SettingGroup("Reader") {
                CommandGroup(
                    vm,
                    CommandDeclarations.auto.value.to(),
                    CommandDeclarations.autocmd.value.to(),
                    CommandDeclarations.alivetime.value.to(),
                    CommandDeclarations.rfmode.value.to(),
                    CommandDeclarations.dual.value.to(),
                    CommandDeclarations.session.value.to(),
                )
            }
        }

        item {
            SettingGroup("Antenna General") {
                CommandGroup(
                    vm,
                    CommandDeclarations.antseq.value.to(),
                    CommandDeclarations.idle.value.to(),
                )
            }
        }

        item {
            SettingGroup("Antenna 1") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ant1.value.to(),
                    CommandDeclarations.power1.value.to(),
                    CommandDeclarations.dwell1.value.to(),
                    CommandDeclarations.filt_rssi1.value.to(),
                )
            }
        }

        item {
            SettingGroup("Antenna 2") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ant2.value.to(),
                    CommandDeclarations.power2.value.to(),
                    CommandDeclarations.dwell2.value.to(),
                    CommandDeclarations.filt_rssi2.value.to(),
                )
            }
        }

        item {
            SettingGroup("Antenna 3") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ant3.value.to(),
                    CommandDeclarations.power3.value.to(),
                    CommandDeclarations.dwell3.value.to(),
                    CommandDeclarations.filt_rssi3.value.to(),
                )
            }
        }

        item {
            SettingGroup("Antenna 4") {
                CommandGroup(
                    vm,
                    CommandDeclarations.ant4.value.to(),
                    CommandDeclarations.power4.value.to(),
                    CommandDeclarations.dwell4.value.to(),
                    CommandDeclarations.filt_rssi4.value.to(),
                )
            }
        }

        item {
            SettingGroup("Q Algorithm") {
                CommandGroup(
                    vm,
                    CommandDeclarations.startq.value.to(),
                    CommandDeclarations.minq.value.to(),
                    CommandDeclarations.maxq.value.to(),
                )
            }
        }

        item {
            SettingGroup("Select Filter") {
                CommandGroup(
                    vm,
                    CommandDeclarations.selectcnt.value.to(),
                    CommandDeclarations.select1.value.to(),
                    CommandDeclarations.select2.value.to(),
                    CommandDeclarations.select3.value.to(),
                    CommandDeclarations.select4.value.to(),
                )
            }
        }

        item {
            SettingGroup("Report") {
                CommandGroup(
                    vm,
                    CommandDeclarations.rep_pc.value.to(),
                    CommandDeclarations.rep_ant.value.to(),
                    CommandDeclarations.rep_rssi.value.to(),
                    CommandDeclarations.rep_rid.value.to(),
                    CommandDeclarations.rep_freq.value.to(),
                    CommandDeclarations.rep_ip.value.to(),
                    CommandDeclarations.rep_date.value.to(),
                    CommandDeclarations.rep_cksum.value.to(),
                    CommandDeclarations.tagreport.value.to(),
                    CommandDeclarations.tagtimeout.value.to(),
                )
            }
        }

        item {
            SettingGroup("Misc") {
                CommandGroup(
                    vm,
                    CommandDeclarations.buzz.value.to()
                )
            }
        }

        item {
            SettingGroup("System") {
                CommandGroup(
                    vm,
                    CommandDeclarations.version.value.to(),
                    CommandDeclarations.fwtype.value.to(),
                    CommandDeclarations.rid.value.to(),
                    CommandDeclarations.rep_rid.value.to(),
                    CommandDeclarations.model.value.to(),
                    CommandDeclarations.region.value.to(),
                    CommandDeclarations.date.value.to(),
                    CommandDeclarations.serialno.value.to(),
                )
            }
        }
    }
}
