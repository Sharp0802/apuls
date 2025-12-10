package com.apulsetech.apuls2.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.Serializable

data class MultiBaseOptions(
    val hex: Boolean = true,
    val oct: Boolean = false,
    val bin: Boolean = false
) : Serializable {
    operator fun get(i: Int): Boolean {
        return when (i) {
            0 -> hex
            1 -> oct
            2 -> bin
            else -> throw ArrayIndexOutOfBoundsException(i)
        }
    }

    fun set(i: Int, v: Boolean): MultiBaseOptions {
        return when (i) {
            0 -> copy(hex = v)
            1 -> copy(oct = v)
            2 -> copy(bin = v)
            else -> throw ArrayIndexOutOfBoundsException(i)
        }
    }

    fun enabledCount(): Int {
        var cnt = 0
        if (hex) cnt += 1
        if (oct) cnt += 1
        if (bin) cnt += 1
        return cnt
    }

    fun indicatorRequired(): Boolean {
        return enabledCount() > 1
    }
}

@Composable
fun MultiBaseView(hex: String, options: MultiBaseOptions, modifier: Modifier) {
    Row(
        modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp)
    ) {
        if (options.indicatorRequired()) {
            Column(horizontalAlignment = Alignment.End) {
                if (options.hex) {
                    Text(
                        text = "Hex",
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.secondary
                    )
                }

                if (options.oct) {
                    Text(
                        text = "Oct",
                        fontFamily = FontFamily.Monospace,
                        style = typography.labelSmall,
                        color = colorScheme.secondary
                    )
                }

                if (options.bin) {
                    Text(
                        text = "Bin",
                        fontFamily = FontFamily.Monospace,
                        style = typography.labelSmall,
                        color = colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.width(4.dp))
        }

        hex.chunked(2).forEachIndexed { i, hex ->
            if (i > 0) {
                Spacer(Modifier.width(4.dp))
            }

            val color = if (i % 2 == 0) Color.Unspecified else colorScheme.secondary

            val byte = hex.toInt(16)

            Column {
                if (options.hex) {
                    Text(
                        text = hex.padStart(2, '0'),
                        fontFamily = FontFamily.Monospace,
                        color = color
                    )
                }

                if (options.oct) {
                    val oct = byte.toString(8).padStart(3, '0')
                    Text(
                        text = oct,
                        fontFamily = FontFamily.Monospace,
                        style = typography.labelSmall,
                        color = color,
                        letterSpacing = (-0.5).sp
                    )
                }

                if (options.bin) {
                    val bin = byte.toString(2).padStart(8, '0')
                    Text(
                        text = bin,
                        fontFamily = FontFamily.Monospace,
                        style = typography.labelSmall,
                        color = color,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }
    }
}
