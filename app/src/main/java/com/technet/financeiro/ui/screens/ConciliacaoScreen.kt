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
import androidx.compose.foundation.layout.padding
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
import com.technet.financeiro.model.ConciliacaoItem
import com.technet.financeiro.ui.theme.BackgroundSoft

@Composable
fun ConciliacaoScreen(
    items: List<ConciliacaoItem>,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Conciliação", style = MaterialTheme.typography.headlineSmall)

                TextButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voltar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${items.size} movimentos carregados",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

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
                    Text("Nenhum movimento encontrado")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    items(items) { item ->
                        ConciliacaoCard(item)
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ConciliacaoCard(item: ConciliacaoItem) {
    val badgeBg = if (item.status == "conciliado") Color(0xFFD9F5E3) else Color(0xFFFFEFC9)
    val badgeFg = if (item.status == "conciliado") Color(0xFF1E7D3A) else Color(0xFF9A6A00)
    val sideColor = when (item.status) {
        "conciliado" -> Color(0xFF23A55A)
        else -> Color(0xFFF0B429)
    }
    val valorColor = if (item.tipo == "entrada") Color(0xFF1E7D3A) else Color(0xFFB3261E)

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(148.dp)
                    .background(sideColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
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
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = item.descricao.ifBlank { "-" },
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = item.origem.ifBlank { "-" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Card(shape = RoundedCornerShape(50.dp)) {
                        Box(
                            modifier = Modifier
                                .background(badgeBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (item.status == "conciliado") "Conciliado" else "Pendente",
                                color = badgeFg,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "VALOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = money(item.valor),
                        color = valorColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "DATA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = formatDate(item.data),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TIPO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = item.tipo.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

private fun money(value: Double): String =
    "R$ " + String.format("%,.2f", value)
        .replace(",", "X")
        .replace(".", ",")
        .replace("X", ".")

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank() || value == "null") return "-"
    return if (value.length >= 10) {
        "${value.substring(8, 10)}/${value.substring(5, 7)}/${value.substring(0, 4)}"
    } else value
}
