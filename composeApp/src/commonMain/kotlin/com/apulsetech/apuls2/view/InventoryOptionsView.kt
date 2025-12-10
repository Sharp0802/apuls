package com.apulsetech.apuls2.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apulsetech.apuls2.command.Command
import com.apulsetech.apuls2.command.CommandDeclarations
import com.apulsetech.apuls2.command.TypeParameterizedCommandDeclaration
import com.apulsetech.apuls2.data.Tag
import com.apulsetech.apuls2.net.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.chunked
import kotlin.collections.forEach
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

enum class SortBy(val label: String) : Comparator<Pair<Tag, Int>>, Serializable {
    Count("Count") {
        override fun compare(p0: Pair<Tag, Int>, p1: Pair<Tag, Int>): Int {
            return p0.second - p1.second
        }
    },
    Antenna("Antenna") {
        override fun compare(p0: Pair<Tag, Int>, p1: Pair<Tag, Int>): Int {
            return (p0.first.ant ?: 0) - (p1.first.ant ?: 0)
        }
    },
    Rssi("RSSI") {
        override fun compare(p0: Pair<Tag, Int>, p1: Pair<Tag, Int>): Int {
            return (p0.first.rssi ?: 0) - (p1.first.rssi ?: 0)
        }
    },
    ReaderId("Reader ID") {
        override fun compare(p0: Pair<Tag, Int>, p1: Pair<Tag, Int>): Int {
            return (p0.first.rid ?: 0) - (p1.first.rid ?: 0)
        }
    },
    Frequency("Frequency") {
        override fun compare(p0: Pair<Tag, Int>, p1: Pair<Tag, Int>): Int {
            return (p0.first.rid ?: 0) - (p1.first.rid ?: 0)
        }
    },
    Ip("IP") {
        override fun compare(p0: Pair<Tag, Int>, p1: Pair<Tag, Int>): Int {
            if (p0.first.ip == p1.first.ip) return 0
            if (p0.first.ip == null) return -1
            if (p1.first.ip == null) return 1
            return p0.first.ip!!.compareTo(p1.first.ip!!)
        }
    },
    Date("Date") {
        override fun compare(p0: Pair<Tag, Int>, p1: Pair<Tag, Int>): Int {
            return (p0.first.date ?: "").compareTo(p1.first.date ?: "")
        }
    };

    companion object {
        fun from(string: String): SortBy = SortBy.entries.single { it.label == string }
    }
}

data class InventoryViewOptions(
    val tagView: TagViewOptions = TagViewOptions(),
    val multiBase: MultiBaseOptions = MultiBaseOptions(),
    val sortBy: SortBy = SortBy.Count,
) : Serializable

@Composable
fun InventoryOptionsView(
    vm: InventoryViewModel,
    session: Session,
    modifier: Modifier = Modifier
) {
    val options by vm.options.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val bases = arrayOf("Hex", "Octal", "Binary")
            MultiChoiceSegmentedButtonRow {
                bases.forEachIndexed { i, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = i, count = bases.size
                        ),
                        checked = options.multiBase[i],
                        onCheckedChange = {
                            if (!it && options.multiBase.enabledCount() <= 1) return@SegmentedButton
                            vm.setMultiBaseOptions(options.multiBase.set(i, it))
                        },
                        icon = { SegmentedButtonDefaults.Icon(options.multiBase[i]) },
                        label = { Text(label) }
                    )
                }
            }
        }

        Dropdown(
            value = options.sortBy.label,
            onChange = { vm.setSortBy(SortBy.from(it)) },
            label = "Sort By",
            options = SortBy.entries.map { it.label },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        val reports = CommandDeclarations.entries.filter { it.value.name.startsWith("rep_") }

        reports.chunked(2).forEach { chunk ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chunk.forEach { command ->
                    val command = command.value.cast<Boolean>()
                    val value = vm.getReportOption(command.name)
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = value ?: false,
                            enabled = value != null,
                            onCheckedChange = {
                                vm.setReportOption(command, it, true)
                                vm.viewModelScope.launch(Dispatchers.IO) {
                                    session.send(command.setter(it))
                                }
                            }
                        )
                        Text(command.label)
                    }
                }
            }
        }
    }
}
