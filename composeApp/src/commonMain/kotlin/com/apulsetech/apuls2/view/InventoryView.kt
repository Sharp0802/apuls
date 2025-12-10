package com.apulsetech.apuls2.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.standardFloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
import com.apulsetech.apuls2.platform.NoopDevice
import com.apulsetech.apuls2.platform.pickFileSaveLocation
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.time.TimeSource

class InventoryViewModel(val session: Session) : ViewModel() {
    companion object {
        fun factory(session: Session): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: KClass<T>, extras: CreationExtras
                ): T {
                    return InventoryViewModel(session) as T
                }
            }

        private val time = TimeSource.Monotonic
    }

    private val _tags = MutableStateFlow(emptyMap<Tag, Int>())
    val tags = _tags.asStateFlow()

    private val _timestamps = ConcurrentLinkedQueue<TimeSource.Monotonic.ValueTimeMark>()

    private val _tps = MutableStateFlow(0)
    val tps = _tps.asStateFlow()

    private val timestampJob = viewModelScope.launch(Dispatchers.IO) {
        while (isActive) {
            while (_timestamps.isNotEmpty() && _timestamps.first()
                    .elapsedNow().inWholeSeconds >= 1
            ) {
                _timestamps.remove()
            }

            _tps.value = _timestamps.size
        }
    }

    private val optionLock = ReentrantLock()
    private val _options = MutableStateFlow(InventoryViewOptions())
    private val _optionsReady = MutableStateFlow(TagViewOptions())
    val options = _options.asStateFlow()

    private val job: suspend (Command) -> Unit = cb@{
        if (it.declaration != CommandDeclarations.tag.value) return@cb
        val state = it.state as? Tag ?: return@cb
        val cnt = (_tags.value[state] ?: 0) + 1

        _tags.value += (state to cnt)

        _timestamps.add(time.markNow())
    }

    init {
        session.onReceive(job)
    }

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            session.send(CommandDeclarations.inventory.value.build())
        }
    }

    fun stop() {
        viewModelScope.launch(Dispatchers.IO) {
            session.send(CommandDeclarations.stop.value.cast<String>().setter("inventory"))
        }
    }

    fun clear() {
        _tags.value = emptyMap()
    }

    fun serialize(): String {
        val builder = StringBuilder()
        builder.append("Count,Tag,Antenna,RSSI,Reader ID,Frequency,IP,Date,Checksum\n")
        for ((tag, count) in _tags.value) {
            builder.append(count).append(',').append(tag).append('\n')
        }
        return builder.toString()
    }

    fun save() {
        viewModelScope.launch {
            val file = pickFileSaveLocation(
                suggestedName = "inventory",
                extension = "csv"
            ) ?: return@launch

            file.writeString(serialize())
        }
    }

    fun setMultiBaseOptions(options: MultiBaseOptions) {
        _options.value = _options.value.copy(multiBase = options)
    }

    fun setSortBy(by: SortBy) {
        _options.value = _options.value.copy(sortBy = by)
    }

    private fun setReady(id: String) {
        _optionsReady.value = when (id) {
            "rep_pc" -> _optionsReady.value.copy(pc = true)
            "rep_ant" -> _optionsReady.value.copy(ant = true)
            "rep_rssi" -> _optionsReady.value.copy(rssi = true)
            "rep_rid" -> _optionsReady.value.copy(rid = true)
            "rep_freq" -> _optionsReady.value.copy(freq = true)
            "rep_ip" -> _optionsReady.value.copy(ip = true)
            "rep_date" -> _optionsReady.value.copy(date = true)
            "rep_cksum" -> _optionsReady.value.copy(cs = true)
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getReady(id: String): Boolean {
        return when (id) {
            "rep_pc" -> _optionsReady.value.pc
            "rep_ant" -> _optionsReady.value.ant
            "rep_rssi" -> _optionsReady.value.rssi
            "rep_rid" -> _optionsReady.value.rid
            "rep_freq" -> _optionsReady.value.freq
            "rep_ip" -> _optionsReady.value.ip
            "rep_date" -> _optionsReady.value.date
            "rep_cksum" -> _optionsReady.value.cs
            else -> throw IndexOutOfBoundsException()
        }
    }

    fun setReportOption(
        command: TypeParameterizedCommandDeclaration<Boolean>,
        value: Boolean,
        flush: Boolean
    ) {
        optionLock.withLock {
            val opt = _options.value.tagView
            val newOpt = when (command.name) {
                "rep_pc" -> opt.copy(pc = value)
                "rep_ant" -> opt.copy(ant = value)
                "rep_rssi" -> opt.copy(rssi = value)
                "rep_rid" -> opt.copy(rid = value)
                "rep_freq" -> opt.copy(freq = value)
                "rep_ip" -> opt.copy(ip = value)
                "rep_date" -> opt.copy(date = value)
                "rep_cksum" -> opt.copy(cs = value)
                else -> throw IndexOutOfBoundsException()
            }
            _options.value = _options.value.copy(tagView = newOpt)
            setReady(command.name)
        }

        if (flush) {
            viewModelScope.launch(Dispatchers.IO) {
                session.send(command.setter(value))
            }
        }
    }

    fun getReportOption(id: String): Boolean? {
        return optionLock.withLock {
            if (!getReady(id)) return null

            when (id) {
                "rep_pc" -> _options.value.tagView.pc
                "rep_ant" -> _options.value.tagView.ant
                "rep_rssi" -> _options.value.tagView.rssi
                "rep_rid" -> _options.value.tagView.rid
                "rep_freq" -> _options.value.tagView.freq
                "rep_ip" -> _options.value.tagView.ip
                "rep_date" -> _options.value.tagView.date
                "rep_cksum" -> _options.value.tagView.cs
                else -> throw IndexOutOfBoundsException()
            }
        }
    }

    suspend fun loadReportOption(command: TypeParameterizedCommandDeclaration<Boolean>) {
        var callback: (suspend (Command) -> Unit)? = null
        callback = cb@{
            if (it.declaration != command) return@cb
            setReportOption(command, it.state as Boolean, false)
            session.unregisterOnReceive(callback!!)
        }

        session.onReceive(callback)
        session.send(command.getter())
    }

    suspend fun loadReportOptions() {
        CommandDeclarations.entries
            .filter { it.value.name.startsWith("rep_") }
            .forEach { loadReportOption(it.value.cast()) }
    }

    override fun onCleared() {
        timestampJob.cancel()
        session.unregisterOnReceive(job)
        super.onCleared()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InventoryView(session: Session, modifier: Modifier = Modifier) {
    val factory = remember(session) { InventoryViewModel.factory(session) }
    val vm: InventoryViewModel = viewModel(key = "inventory-${session.id}", factory = factory)

    val tags by vm.tags.collectAsState()
    val tps by vm.tps.collectAsState()
    val options by vm.options.collectAsState()

    var playing by rememberSaveable { mutableStateOf(false) }
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            vm.loadReportOptions()
        }
    }

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetContent = {
            InventoryOptionsView(
                vm = vm,
                session = session,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        },
        sheetPeekHeight = 48.dp,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column {
                val modifier = Modifier.weight(1f).fillMaxSize()
                if (tags.isEmpty()) {
                    Box(modifier = modifier, contentAlignment = Alignment.Center) {
                        Text(
                            text = "No tag detected",
                            color = colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    val sorted = tags.toList().sortedWith { a, b -> -options.sortBy.compare(a, b) }
                    LazyColumn(modifier) {
                        items(sorted) {
                            val (tag, cnt) = it
                            TagView(tag, cnt, options.tagView, options.multiBase)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FieldCell("t/s", tps)
                FieldCell("total", tags.size)

                HorizontalFloatingToolbar(
                    expanded = tags.isNotEmpty(),
                    floatingActionButton = {
                        FloatingToolbarDefaults.StandardFloatingActionButton(
                            onClick = {
                                if (playing) {
                                    vm.stop()
                                } else {
                                    vm.start()
                                }

                                playing = !playing
                            },
                            containerColor = if (playing) {
                                colorScheme.tertiaryContainer
                            } else {
                                standardFloatingToolbarColors().fabContainerColor
                            }
                        ) {
                            if (playing) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                            }
                        }
                    },
                    content = {
                        IconButton(onClick = { vm.clear() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                        IconButton(onClick = { vm.save() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                )
            }
        }
    }
}

@Composable
@Preview
private fun InventoryViewPreview() {
    val session = remember { Session(NoopDevice) }
    Scaffold { padding ->
        InventoryView(session, Modifier.padding(padding))
    }
}
