package com.apulsetech.apuls.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.command.CommandDeclaration
import com.apulsetech.apuls.command.CommandDeclarations
import com.apulsetech.apuls.command.CommandDeclarations.*
import com.apulsetech.apuls.command.ParameterizedCommandDeclaration

private fun CommandDeclaration.cast(): ParameterizedCommandDeclaration {
    return this as ParameterizedCommandDeclaration
}

@Composable
fun Settings(comm: DeviceCommViewModel, modifier: Modifier = Modifier) {
    @Composable
    fun Group(name: String, vararg v: CommandDeclarations) {
        SettingGroup(name, v.map { it.value.cast() }.toTypedArray(), comm)
    }

    Column(modifier) {
        val padding = 16.dp

        when (comm.connectionType) {
            // TODO : add tcp way to connect

            else -> {
                Group("Serial", baudrate)
            }
        }

        Spacer(Modifier.height(padding))

        Group(
            "Reader",
            state,
            auto,
            autocmd,
            alivetime,
        )

        Spacer(Modifier.height(padding))

        Group("Antenna General", antseq, idle)

        Spacer(Modifier.height(padding))

        Group("Antenna 1", ant1, power1, dwell1, filt_rssi1)
        Spacer(Modifier.height(padding))
        Group("Antenna 2", ant2, power2, dwell2, filt_rssi2)
        Spacer(Modifier.height(padding))
        Group("Antenna 3", ant3, power3, dwell3, filt_rssi3)
        Spacer(Modifier.height(padding))
        Group("Antenna 4", ant4, power4, dwell4, filt_rssi4)

        Spacer(Modifier.height(padding))

        Group("Q Algorithm", startq, minq, maxq)

        Spacer(Modifier.height(padding))

        Group("Select Filter", select1, select2, select3, select4)

        Spacer(Modifier.height(padding))

        Group(
            "Report",
            rep_pc,
            rep_ant,
            rep_rssi,
            rep_rid,
            rep_freq,
            rep_ip,
            rep_date,
            rep_cksum,
            tagreport,
            tagtimeout
        )

        Spacer(Modifier.height(padding))

        Group("Misc", buzz)

        Spacer(Modifier.height(padding))

        Group("System", version, fwtype, rid, model, region, date, serialno)

        Spacer(Modifier.height(padding))

        Group("Serve", ser_serial, ser_tcp, ser_http, ser_mqtt)

        Spacer(Modifier.height(padding))

        Group("Http", httpurl, httpauth, httpuser, httppwd)

        Spacer(Modifier.height(padding))

        Group("Mqtt", mqttbroker, mqtttopic, mqttqos, mqttalive)
    }
}
