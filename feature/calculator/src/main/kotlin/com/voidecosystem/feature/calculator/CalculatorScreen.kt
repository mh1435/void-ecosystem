package com.voidecosystem.feature.calculator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

object CalculatorDestination {
    const val ROUTE = "calculator"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorRoute(
    onBack: () -> Unit = {},
    viewModel: CalculatorViewModel = viewModel(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        CalculatorContent(
            state = viewModel.uiState,
            modifier = Modifier.padding(padding),
            onDigit = viewModel::onDigit,
            onDecimalPoint = viewModel::onDecimalPoint,
            onOperator = viewModel::onOperator,
            onEquals = viewModel::onEquals,
            onClear = viewModel::onClear,
            onBackspace = viewModel::onBackspace,
            onToggleSign = viewModel::onToggleSign,
            onPercent = viewModel::onPercent,
            onSquareRoot = viewModel::onSquareRoot,
            onSquare = viewModel::onSquare,
            onReciprocal = viewModel::onReciprocal,
        )
    }
}

@Composable
private fun CalculatorContent(
    state: CalculatorUiState,
    modifier: Modifier = Modifier,
    onDigit: (Char) -> Unit,
    onDecimalPoint: () -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onPercent: () -> Unit,
    onSquareRoot: () -> Unit,
    onSquare: () -> Unit,
    onReciprocal: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = state.expression,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BasicText(
                text = state.display,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }

        val rows = listOf(
            listOf(CalcKey.Function("√", onSquareRoot), CalcKey.Function("x²", onSquare), CalcKey.Function("1/x", onReciprocal), CalcKey.Op("÷", onOperator)),
            listOf(CalcKey.Digit('7', onDigit), CalcKey.Digit('8', onDigit), CalcKey.Digit('9', onDigit), CalcKey.Op("×", onOperator)),
            listOf(CalcKey.Digit('4', onDigit), CalcKey.Digit('5', onDigit), CalcKey.Digit('6', onDigit), CalcKey.Op("−", onOperator)),
            listOf(CalcKey.Digit('1', onDigit), CalcKey.Digit('2', onDigit), CalcKey.Digit('3', onDigit), CalcKey.Op("+", onOperator)),
        )

        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key -> CalculatorButton(key, Modifier.weight(1f)) }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(CalcKey.Function("AC", { onClear() }), Modifier.weight(1f))
            CalculatorButton(CalcKey.Function("+/−", onToggleSign), Modifier.weight(1f))
            CalculatorButton(CalcKey.Function("%", onPercent), Modifier.weight(1f))
            CalculatorButton(CalcKey.Function("⌫", onBackspace), Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            CalculatorButton(CalcKey.Digit('0', onDigit), Modifier.weight(2f))
            CalculatorButton(CalcKey.Function(".", { onDecimalPoint() }), Modifier.weight(1f))
            CalculatorButton(CalcKey.Function("=", { onEquals() }), Modifier.weight(1f), isAccent = true)
        }
    }
}

private sealed interface CalcKey {
    val label: String

    data class Digit(val char: Char, val action: (Char) -> Unit) : CalcKey {
        override val label = char.toString()
    }

    data class Op(override val label: String, val action: (String) -> Unit) : CalcKey

    data class Function(override val label: String, val action: () -> Unit) : CalcKey
}

@Composable
private fun CalculatorButton(key: CalcKey, modifier: Modifier = Modifier, isAccent: Boolean = false) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(6.dp),
    ) {
        Button(
            onClick = {
                when (key) {
                    is CalcKey.Digit -> key.action(key.char)
                    is CalcKey.Op -> key.action(key.label)
                    is CalcKey.Function -> key.action()
                }
            },
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            colors = if (isAccent) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            },
        ) {
            if (key.label == "⌫") {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
            } else {
                Text(key.label, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
