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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {

        // HEADER
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

            // SELETOR DE MÊS
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
                            text = "${items.size} contas carregadas",
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

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !errorMessage.isNullOrBlank() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color.Red)
                }
            }

            items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma conta encontrada")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    items(items) { conta ->
                        ContaPagarCard(conta)
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ContaPagarCard(conta: ContaPagar) {

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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column(modifier = Modifier.weight(1f)) {

                        Text(
                            text = conta.descricao.ifBlank { "-" },
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = fornecedorOuTraco(conta.fornecedorNome),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {

                        Card(shape = RoundedCornerShape(50.dp)) {
                            Box(
                                modifier = Modifier.background(badgeBg)
                            ) {
                                Text(
                                    text = formatStatus(conta.status),
                                    color = badgeFg,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(onClick = { expanded = true }) {
                            Text("Ações")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {

                            DropdownMenuItem(
                                text = { Text("Ver detalhes") },
                                onClick = { expanded = false }
                            )

                            DropdownMenuItem(
                                text = { Text("Marcar como pago") },
                                onClick = { expanded = false }
                            )

                            DropdownMenuItem(
                                text = { Text("Pagamento parcial") },
                                onClick = { expanded = false }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem("Vencimento", formatDate(conta.dataVencimento))
                    InfoItem("Pagamento", formatDate(conta.dataPagamento))
                }

                InfoItem("Categoria", valorOuTraco(conta.categoria))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem("Total", money(conta.valor))
                    InfoItem("Pago", money(conta.valorPago))
                }

                InfoItem("Falta pagar", money(faltaPagar(conta)), true)

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Composable
private fun InfoItem(titulo: String, valor: String, destaque: Boolean = false) {
    Column {
        Text(titulo.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            valor,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
        )
    }
}

// ===== HELPERS =====

private fun statusSideColor(status: String): Color = when (status.lowercase()) {
    "pago" -> Color(0xFF23A55A)
    "vencido" -> Color(0xFFE14D4D)
    "parcial" -> Color(0xFFF0B429)
    else -> Color(0xFF4E7AC7)
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
    if (value.isNullOrBlank() || value == "null") "-" else value

private fun valorOuTraco(value: String?): String =
    if (value.isNullOrBlank() || value == "null") "-" else value

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank() || value == "null") return "-"
    return if (value.length >= 10) {
        "${value.substring(8, 10)}/${value.substring(5, 7)}/${value.substring(0, 4)}"
    } else value
}

private fun faltaPagar(conta: ContaPagar): Double =
    if (conta.saldoAberto > 0) conta.saldoAberto else conta.valor - conta.valorPago

private fun money(value: Double): String =
    "R$ " + String.format("%,.2f", value)
        .replace(",", "X").replace(".", ",").replace("X", ".")

private fun formatStatus(status: String): String =
    status.replaceFirstChar { it.uppercase() }

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
