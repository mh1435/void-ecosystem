package com.voidecosystem.feature.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private enum class Operator(val symbol: String) {
    ADD("+"), SUBTRACT("−"), MULTIPLY("×"), DIVIDE("÷")
}

data class CalculatorUiState(
    val display: String = "0",
    val expression: String = "",
    val history: List<String> = emptyList(),
)

/**
 * Standard four-function calculator with percent, sign toggle, sqrt/square/
 * reciprocal, and a running session history — a real, self-contained state
 * machine, no network or persistence required.
 */
class CalculatorViewModel : ViewModel() {

    var uiState by mutableStateOf(CalculatorUiState())
        private set

    private var operand: BigDecimal? = null
    private var pendingOperator: Operator? = null
    private var startFreshInput = true

    fun onDigit(digit: Char) {
        val current = uiState.display
        val next = when {
            startFreshInput -> digit.toString()
            current == "0" -> digit.toString()
            else -> current + digit
        }
        startFreshInput = false
        uiState = uiState.copy(display = next)
    }

    fun onDecimalPoint() {
        if (startFreshInput) {
            uiState = uiState.copy(display = "0.")
            startFreshInput = false
            return
        }
        if (!uiState.display.contains('.')) {
            uiState = uiState.copy(display = uiState.display + ".")
        }
    }

    fun onOperator(operator: String) {
        val op = Operator.entries.first { it.symbol == operator }
        val current = currentValue()
        if (pendingOperator != null && !startFreshInput) {
            val result = compute(operand!!, current, pendingOperator!!)
            operand = result
            uiState = uiState.copy(display = formatResult(result), expression = "${formatResult(result)} ${op.symbol}")
        } else {
            operand = current
            uiState = uiState.copy(expression = "${formatResult(current)} ${op.symbol}")
        }
        pendingOperator = op
        startFreshInput = true
    }

    fun onEquals() {
        val op = pendingOperator ?: return
        val left = operand ?: return
        val right = currentValue()
        val result = compute(left, right, op)
        val entry = "${formatResult(left)} ${op.symbol} ${formatResult(right)} = ${formatResult(result)}"
        uiState = uiState.copy(
            display = formatResult(result),
            expression = "",
            history = (listOf(entry) + uiState.history).take(50),
        )
        operand = null
        pendingOperator = null
        startFreshInput = true
    }

    fun onClear() {
        operand = null
        pendingOperator = null
        startFreshInput = true
        uiState = uiState.copy(display = "0", expression = "")
    }

    fun onBackspace() {
        if (startFreshInput) return
        val trimmed = uiState.display.dropLast(1)
        uiState = uiState.copy(display = trimmed.ifEmpty { "0" })
    }

    fun onToggleSign() {
        val value = currentValue().negate()
        uiState = uiState.copy(display = formatResult(value))
    }

    fun onPercent() {
        val value = currentValue().divide(BigDecimal(100), MathContext.DECIMAL64)
        uiState = uiState.copy(display = formatResult(value))
        startFreshInput = true
    }

    fun onSquareRoot() {
        val value = currentValue()
        if (value.signum() < 0) return
        val result = BigDecimal(Math.sqrt(value.toDouble()), MathContext.DECIMAL64)
        uiState = uiState.copy(display = formatResult(result))
        startFreshInput = true
    }

    fun onSquare() {
        val value = currentValue()
        val result = value.multiply(value, MathContext.DECIMAL64)
        uiState = uiState.copy(display = formatResult(result))
        startFreshInput = true
    }

    fun onReciprocal() {
        val value = currentValue()
        if (value.signum() == 0) return
        val result = BigDecimal.ONE.divide(value, MathContext.DECIMAL64)
        uiState = uiState.copy(display = formatResult(result))
        startFreshInput = true
    }

    private fun currentValue(): BigDecimal =
        uiState.display.toBigDecimalOrNull() ?: BigDecimal.ZERO

    private fun compute(left: BigDecimal, right: BigDecimal, op: Operator): BigDecimal = when (op) {
        Operator.ADD -> left.add(right)
        Operator.SUBTRACT -> left.subtract(right)
        Operator.MULTIPLY -> left.multiply(right, MathContext.DECIMAL64)
        Operator.DIVIDE -> if (right.signum() == 0) BigDecimal.ZERO else left.divide(right, MathContext.DECIMAL64)
    }

    private fun formatResult(value: BigDecimal): String =
        value.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
}
