package com.technet.financeiro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.technet.financeiro.model.ContaPagar
import com.technet.financeiro.ui.theme.BackgroundSoft

@Composable
fun ContasPagarScreen(
    items: List<ContaPagar>,
    isLoading: Boolean,
    errorMessage: String?,
    mes: Int,
    ano: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBack: () -> Unit
) {

    var contas by remember { mutableStateOf(items) }
    var contaSelecionada by remember { mutableStateOf<ContaPagar?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {

        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Contas a pagar", style = MaterialTheme.typography.headlineSmall)

                TextButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voltar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextButton(onClick = onPreviousMonth) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = nomeMes(mes) + " / " + ano,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${contas.size} contas carregadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    TextButton(onClick = onNextMonth) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            item { Spacer(modifier = Modifier.height(10.dp)) }

            items(contas) { conta ->

                ContaPagarCard(
                    conta = conta,

                    onVerDetalhes = {
                        contaSelecionada = conta
                    },

                    onMarcarPago = {
                        contas = contas.map {
                            if (it.id == conta.id) {
                                it.copy(
                                    status = "pago",
                                    valorPago = it.valor,
                                    saldoAberto = 0.0
                                )
                            } else it
                        }
                    },

                    onPagamentoParcial = {
                        contas = contas.map {
                            if (it.id == conta.id) {
                                val pago = it.valor * 0.5
                                it.copy(
                                    status = "parcial",
                                    valorPago = pago,
                                    saldoAberto = it.valor - pago
                                )
                            } else it
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (contaSelecionada != null) {
        val conta = contaSelecionada!!

        AlertDialog(
            onDismissRequest = { contaSelecionada = null },
            confirmButton = {
                TextButton(onClick = { contaSelecionada = null }) {
                    Text("Fechar")
                }
            },
            title = { Text("Detalhes da conta") },
            text = {
                Column {
                    Text(conta.descricao)
                    Text("Valor: ${money(conta.valor)}")
                    Text("Status: ${conta.status}")
                }
            }
        )
    }
}

@Composable
private fun ContaPagarCard(
    conta: ContaPagar,
    onVerDetalhes: () -> Unit,
    onMarcarPago: () -> Unit,
    onPagamentoParcial: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    val sideColor = statusSideColor(conta.status)
    val badgeBg = statusBadgeBg(conta.status)
    val badgeFg = statusBadgeFg(conta.status)

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row {

            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(170.dp)
                    .background(sideColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(conta.descricao)

                        Text(
                            fornecedorOuTraco(conta.fornecedorNome),
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {

                        Card(shape = RoundedCornerShape(50.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(badgeBg)
                                    .padding(8.dp)
                            ) {
                                Text(formatStatus(conta.status), color = badgeFg)
                            }
                        }

                        TextButton(onClick = { expanded = true }) {
                            Text("Ações")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {

                            DropdownMenuItem(
                                text = { Text("Ver detalhes") },
                                onClick = {
                                    expanded = false
                                    onVerDetalhes()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Marcar como pago") },
                                onClick = {
                                    expanded = false
                                    onMarcarPago()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Pagamento parcial") },
                                onClick = {
                                    expanded = false
                                    onPagamentoParcial()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Total: ${money(conta.valor)}")
                Text("Pago: ${money(conta.valorPago)}")
                Text("Falta: ${money(faltaPagar(conta))}")

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

private fun statusSideColor(status: String): Color = when (status.lowercase()) {
    "pago" -> Color.Green
    "vencido" -> Color.Red
    "parcial" -> Color.Yellow
    else -> Color.Blue
}

private fun statusBadgeBg(status: String): Color = when (status.lowercase()) {
    "pago" -> Color(0xFFD9F5E3)
    "vencido" -> Color(0xFFF9DADA)
    "parcial" -> Color(0xFFFFEFC9)
    else -> Color(0xFFE7EEFA)
}

private fun statusBadgeFg(status: String): Color = when (status.lowercase()) {
    "pago" -> Color(0xFF1E7D3A)
    "vencido" -> Color(0xFFBA2E2E)
    "parcial" -> Color(0xFF9A6A00)
    else -> Color(0xFF2E5EAA)
}

private fun fornecedorOuTraco(value: String?): String =
    if (value.isNullOrBlank()) "-" else value

private fun formatStatus(status: String): String =
    status.replaceFirstChar { it.uppercase() }

private fun faltaPagar(conta: ContaPagar): Double =
    if (conta.saldoAberto > 0) conta.saldoAberto else conta.valor - conta.valorPago

private fun money(value: Double): String =
    "R$ " + String.format("%,.2f", value)
        .replace(",", "X")
        .replace(".", ",")
        .replace("X", ".")

private fun nomeMes(mes: Int): String = when (mes) {
    1 -> "Janeiro"
    2 -> "Fevereiro"
    3 -> "Março"
    4 -> "Abril"
    5 -> "Maio"
    6 -> "Junho"
    7 -> "Julho"
    8 -> "Agosto"
    9 -> "Setembro"
    10 -> "Outubro"
    11 -> "Novembro"
    12 -> "Dezembro"
    else -> "Mês"
}
