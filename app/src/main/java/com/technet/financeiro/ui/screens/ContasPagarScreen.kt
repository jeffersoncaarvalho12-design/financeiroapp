package com.technet.financeiro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Carregando contas...")
                }
            }

            !errorMessage.isNullOrBlank() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            items.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nenhuma conta encontrada nesse mês")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    items(items) { conta ->
                        ContaPagarCard(conta = conta)
                    }

                    item { Spacer(modifier = Modifier.height(14.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ContaPagarCard(conta: ContaPagar) {
    val sideColor = statusSideColor(conta.status)
    val badgeBg = statusBadgeBg(conta.status)
    val badgeFg = statusBadgeFg(conta.status)
    val cardBg = Color.White

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg)
        ) {
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
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

                    Card(shape = RoundedCornerShape(50.dp)) {
                        Box(
                            modifier = Modifier.background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Spacer(modifier = Modifier.width(80.dp))
                            Spacer(modifier = Modifier.height(30.dp))
                            Text(
                                text = formatStatus(conta.status),
                                color = badgeFg,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem("Vencimento", formatDate(conta.dataVencimento))
                    InfoItem("Pagamento", formatDateOrDash(conta.dataPagamento))
                }

                InfoItem("Categoria", valorOuTraco(conta.categoria))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem("Total", money(conta.valor))
                    InfoItem("Pago", money(conta.valorPago))
                }

                InfoItem(
                    "Falta pagar",
                    money(faltaPagar(conta)),
                    destaque = true
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Composable
private fun InfoItem(
    titulo: String,
    valor: String,
    destaque: Boolean = false
) {
    Column {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Text(
            text = valor,
            style = if (destaque)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyLarge
        )
    }
}

private fun statusSideColor(status: String): Color {
    return when (status.trim().lowercase()) {
        "pago" -> Color(0xFF23A55A)
        "vencido" -> Color(0xFFE14D4D)
        "parcial" -> Color(0xFFF0B429)
        else -> Color(0xFF4E7AC7)
    }
}

private fun statusBadgeBg(status: String): Color {
    return when (status.trim().lowercase()) {
        "pago" -> Color(0xFFD9F5E3)
        "vencido" -> Color(0xFFF9DADA)
        "parcial" -> Color(0xFFFFEFC9)
        else -> Color(0xFFE7EEFA)
    }
}

private fun statusBadgeFg(status: String): Color {
    return when (status.trim().lowercase()) {
        "pago" -> Color(0xFF1E7D3A)
        "vencido" -> Color(0xFFBA2E2E)
        "parcial" -> Color(0xFF9A6A00)
        else -> Color(0xFF2E5EAA)
    }
}

private fun fornecedorOuTraco(value: String?): String {
    if (value == null) return "-"
    val v = value.trim()
    return if (v.isBlank() || v.lowercase() == "null") "-" else v
}

private fun valorOuTraco(value: String?): String {
    if (value == null) return "-"
    val v = value.trim()
    return if (v.isBlank() || v.lowercase() == "null") "-" else v
}

private fun formatDate(value: String?): String {
    if (value == null) return "-"
    val v = value.trim()
    if (v.isBlank() || v.lowercase() == "null") return "-"
    return if (v.length >= 10 && v[4] == '-' && v[7] == '-') {
        "${v.substring(8, 10)}/${v.substring(5, 7)}/${v.substring(0, 4)}"
    } else {
        v
    }
}

private fun formatDateOrDash(value: String?): String {
    return formatDate(value)
}

private fun formatStatus(status: String): String {
    val v = status.trim()
    if (v.isBlank()) return "Pendente"
    return v.replaceFirstChar { it.uppercase() }
}

private fun faltaPagar(conta: ContaPagar): Double {
    return when {
        conta.saldoAberto > 0.0 -> conta.saldoAberto
        conta.valor > conta.valorPago -> conta.valor - conta.valorPago
        else -> 0.0
    }
}

private fun money(value: Double): String {
    return "R$ " + String.format("%,.2f", value)
        .replace(",", "X")
        .replace(".", ",")
        .replace("X", ".")
}

private fun nomeMes(mes: Int): String {
    return when (mes) {
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
}
