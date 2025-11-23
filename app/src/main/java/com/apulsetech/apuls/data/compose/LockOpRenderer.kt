package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.data.Access
import com.apulsetech.apuls.data.LockOp
import com.apulsetech.apuls.data.Mask

class LockOpRenderer : IRenderer<LockOp> {
    private val maskRenderer = MaskRenderer()
    private val accessRenderer = AccessRenderer()

    @Composable
    override fun Render(
        value: LockOp,
        onValueChanged: (LockOp) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        Column(modifier) {
            val entries = listOf(
                Triple("User", value.user) { newPair: Pair<Mask, Access> ->
                    onValueChanged(value.copy(user = newPair))
                },
                Triple("TID", value.tid) { newPair: Pair<Mask, Access> ->
                    onValueChanged(value.copy(tid = newPair))
                },
                Triple("EPC", value.epc) { newPair: Pair<Mask, Access> ->
                    onValueChanged(value.copy(epc = newPair))
                },
                Triple("Access", value.access) { newPair: Pair<Mask, Access> ->
                    onValueChanged(value.copy(access = newPair))
                },
                Triple("Kill", value.kill) { newPair: Pair<Mask, Access> ->
                    onValueChanged(value.copy(kill = newPair))
                }
            )

            entries.forEachIndexed { index, (label, pair, updater) ->
                LockRow(label = label, pair = pair, enabled = enabled, onPairChanged = updater)

                if (index != entries.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    @Composable
    private fun LockRow(
        label: String,
        pair: Pair<Mask, Access>,
        enabled: Boolean,
        onPairChanged: (Pair<Mask, Access>) -> Unit
    ) {
        Row {
            Text(
                text = label,
                modifier = Modifier.width(64.dp)
            )

            Spacer(Modifier.width(8.dp))

            maskRenderer.Render(
                value = pair.first,
                onValueChanged = { onPairChanged(Pair(it, pair.second)) },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            accessRenderer.Render(
                value = pair.second,
                onValueChanged = { onPairChanged(Pair(pair.first, it)) },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
