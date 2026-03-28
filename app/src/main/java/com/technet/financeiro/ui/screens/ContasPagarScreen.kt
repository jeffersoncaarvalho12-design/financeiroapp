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

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${items.size} contas carregadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
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
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
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

                StatusChip(conta.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniInfoCard(
                    titulo = "Vencimento",
                    valor = formatDate(conta.dataVencimento),
                    modifier = Modifier.weight(1f)
                )

                MiniInfoCard(
                    titulo = "Pagamento",
                    valor = formatDateOrDash(conta.dataPagamento),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MiniInfoCard(
                titulo = "Categoria",
                valor = valorOuTraco(conta.categoria),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniInfoCard(
                    titulo = "Total",
                    valor = money(conta.valor),
                    modifier = Modifier.weight(1f)
                )

                MiniInfoCard(
                    titulo = "Pago",
                    valor = money(conta.valorPago),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MiniInfoCard(
                titulo = "Falta pagar",
                valor = money(faltaPagar(conta)),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MiniInfoCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = titulo.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatusChip(status: String) {
    val normalized = status.trim().lowercase()

    val bg = when (normalized) {
        "pago" -> Color(0xFFD8F5DD)
        "vencido" -> Color(0xFFF9D9D9)
        "parcial" -> Color(0xFFFFEDBF)
        else -> Color(0xFFE7EEF9)
    }

    val fg = when (normalized) {
        "pago" -> Color(0xFF1C7C39)
        "vencido" -> Color(0xFFB3261E)
        "parcial" -> Color(0xFF9A6A00)
        else -> Color(0xFF305A8D)
    }

    Card(
        shape = RoundedCornerShape(50.dp)
    ) {
        Box(
            modifier = Modifier.background(bg),
            contentAlignment = Alignment.Center
        ) {
            Spacer(modifier = Modifier.width(74.dp))
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = formatStatus(status),
                color = fg,
                style = MaterialTheme.typography.labelLarge
            )
        }
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
