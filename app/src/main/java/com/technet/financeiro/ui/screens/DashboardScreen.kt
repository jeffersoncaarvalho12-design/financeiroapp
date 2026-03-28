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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User
import com.technet.financeiro.ui.theme.BackgroundSoft
import com.technet.financeiro.ui.theme.CardBlue
import com.technet.financeiro.ui.theme.CardGreen
import com.technet.financeiro.ui.theme.CardOrange
import com.technet.financeiro.ui.theme.CardRed
import com.technet.financeiro.ui.theme.CardYellow
import kotlinx.coroutines.delay

data class DashboardCardUi(
    val title: String,
    val value: String,
    val subtitle: String,
    val color: Color,
    val icon: @Composable () -> Unit
)

@Composable
fun DashboardScreen(
    user: User,
    summary: DashboardSummary,
    message: String?,
    onOpenNewExpense: () -> Unit,
    onClearMessage: () -> Unit,
    onLogout: () -> Unit
) {
    val cards = listOf(
        DashboardCardUi("Receita SCM", money(summary.receitaScm), "Receita principal", CardGreen) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White)
        },
        DashboardCardUi("Combustível", money(summary.combustivel), "Custo operacional", CardRed) {
            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = Color.White)
        },
        DashboardCardUi("Manutenção", money(summary.manutencao), "Veículos e equipamentos", CardBlue) {
            Icon(Icons.Default.Build, contentDescription = null, tint = Color.White)
        },
        DashboardCardUi("Energia", money(summary.energia), "Infraestrutura", CardYellow) {
            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White)
        },
        DashboardCardUi("Água", money(summary.agua), "Custo fixo", CardOrange) {
            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.White)
        }
    )

    if (!message.isNullOrBlank()) {
        LaunchedEffect(message) {
            delay(2500)
            onClearMessage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Financeiro TECH NET", style = MaterialTheme.typography.headlineSmall)
                            Text("Olá, ${user.name}", style = MaterialTheme.typography.bodyLarge)
                            Text(user.email, style = MaterialTheme.typography.bodyMedium)
                        }

                        Button(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sair")
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(onClick = onOpenNewExpense) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nova despesa")
                        }

                        TextButton(onClick = {}) {
                            Text("Contas em breve")
                        }
                    }
                }
            }
        }

        if (!message.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(horizontal = 16.dp)
            ) {
                items(cards) { card ->
                    Card(shape = RoundedCornerShape(22.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(card.color)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                                    .padding(10.dp)
                            ) {
                                card.icon()
                            }

                            Column {
                                Text(card.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(card.value, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    card.subtitle,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Próximas telas", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("• Contas a pagar")
                    Text("• Pagamento com comprovante")
                    Text("• Conciliação")
                    Text("• Relatórios")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun money(value: Double): String {
    return "R$ " + String.format("%,.2f", value)
        .replace(",", "X")
        .replace(".", ",")
        .replace("X", ".")
}
