package com.apulsetech.apuls.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.command.ParameterizedCommandDeclaration

@Composable
fun SettingGroup(
    name: String,
    declarations: Array<ParameterizedCommandDeclaration>,
    comm: DeviceCommViewModel,
    modifier: Modifier = Modifier
) {
    val viewModels by remember {
        mutableStateOf(declarations.map { SettingViewModel(comm, it) }.toTypedArray())
    }

    Card(modifier) {
        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            HorizontalDivider()
            Spacer(Modifier.height(4.dp))

            for (vm in viewModels) {
                Setting(vm = vm, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider()

            Row(Modifier.padding(8.dp)) {
                Spacer(Modifier.weight(1f))

                Button(onClick = {
                    for (vm in viewModels) {
                        vm.apply()
                    }
                }) {
                    Text("Apply")
                }
            }
        }
    }
}
