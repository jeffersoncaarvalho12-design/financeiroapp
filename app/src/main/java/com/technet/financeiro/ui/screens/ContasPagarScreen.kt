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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.technet.financeiro.model.ContaPagar
import com.technet.financeiro.ui.theme.BackgroundSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContasPagarScreen(
    items: List<ContaPagar>,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    mes: Int,
    ano: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBack: () -> Unit,
    onClearMessage: () -> Unit,
    onInformarPagamentoTotal: (Int, String, String) -> Unit,
    onRegistrarPagamentoParcial: (Int, String, String, String) -> Unit,
    onAlterarVencimento: (Int, String) -> Unit,
    onEditarLancamento: (Int, String, String, String, String) -> Unit,
    onAlterarDataPagamento: (Int, String) -> Unit,
    onExcluirLancamento: (Int) -> Unit
) {
    var contaSelecionada by remember { mutableStateOf<ContaPagar?>(null) }
    var contaPagamentoTotal by remember { mutableStateOf<ContaPagar?>(null) }
    var contaPagamentoParcial by remember { mutableStateOf<ContaPagar?>(null) }
    var contaAlterarVencimento by remember { mutableStateOf<ContaPagar?>(null) }
    var contaEditar by remember { mutableStateOf<ContaPagar?>(null) }
    var contaAlterarPagamento by remember { mutableStateOf<ContaPagar?>(null) }
    var contaExcluir by remember { mutableStateOf<ContaPagar?>(null) }

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
                Text("Contas a pagar", style = MaterialTheme.typography.headlineSmall)

                TextButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voltar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!successMessage.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEAF8EE))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1E7D3A)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sucesso",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF1E7D3A)
                            )
                            Text(
                                text = successMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF185C2C)
                            )
                        }

                        IconButton(onClick = onClearMessage) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar mensagem",
                                tint = Color(0xFF1E7D3A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    items(items) { conta ->
                        ContaPagarCard(
                            conta = conta,
                            onVerDetalhes = { contaSelecionada = conta },
                            onInformarPagamentoTotal = { contaPagamentoTotal = conta },
                            onPagamentoParcial = { contaPagamentoParcial = conta },
                            onAlterarVencimento = { contaAlterarVencimento = conta },
                            onEditarLancamento = { contaEditar = conta },
                            onAlterarDataPagamento = { contaAlterarPagamento = conta },
                            onExcluirLancamento = { contaExcluir = conta }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetalheLinha("Descrição", conta.descricao.ifBlank { "-" })
                    DetalheLinha("Fornecedor", fornecedorOuTraco(conta.fornecedorNome))
                    DetalheLinha("Vencimento", formatDate(conta.dataVencimento))
                    DetalheLinha("Pagamento", formatDate(conta.dataPagamento))
                    DetalheLinha("Categoria", valorOuTraco(conta.categoria))
                    DetalheLinha("Total", money(conta.valor))
                    DetalheLinha("Pago", money(conta.valorPago))
                    DetalheLinha("Saldo", money(faltaPagar(conta)))
                    DetalheLinha("Status", formatStatus(conta.status))
                }
            }
        )
    }

    if (contaPagamentoTotal != null) {
        val conta = contaPagamentoTotal!!
        var dataPagamento by remember(conta.id) {
            mutableStateOf(
                if (conta.dataPagamento.isNotBlank() && conta.dataPagamento != "null") {
                    conta.dataPagamento.take(10)
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }
            )
        }
        var observacoes by remember(conta.id) { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { contaPagamentoTotal = null },
            confirmButton = {
                Button(
                    onClick = {
                        onInformarPagamentoTotal(conta.id, dataPagamento.trim(), observacoes.trim())
                        contaPagamentoTotal = null
                    },
                    enabled = dataPagamento.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { contaPagamentoTotal = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Informar pagamento total") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(conta.descricao)
                    Text("Valor total: ${money(conta.valor)}")

                    OutlinedTextField(
                        value = dataPagamento,
                        onValueChange = { dataPagamento = it },
                        label = { Text("Data pagamento (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Observações") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (contaPagamentoParcial != null) {
        val conta = contaPagamentoParcial!!
        var valor by remember(conta.id) { mutableStateOf("") }
        var dataPagamento by remember(conta.id) {
            mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }
        var observacoes by remember(conta.id) { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { contaPagamentoParcial = null },
            confirmButton = {
                Button(
                    onClick = {
                        onRegistrarPagamentoParcial(
                            conta.id,
                            valor.trim().replace(",", "."),
                            dataPagamento.trim(),
                            observacoes.trim()
                        )
                        contaPagamentoParcial = null
                    },
                    enabled = valor.isNotBlank() && dataPagamento.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { contaPagamentoParcial = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Pagamento parcial") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(conta.descricao)
                    Text("Saldo atual: ${money(faltaPagar(conta))}")

                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it },
                        label = { Text("Valor pago") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dataPagamento,
                        onValueChange = { dataPagamento = it },
                        label = { Text("Data pagamento (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Observações") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (contaAlterarVencimento != null) {
        val conta = contaAlterarVencimento!!
        var dataVencimento by remember(conta.id) { mutableStateOf(conta.dataVencimento.take(10)) }

        AlertDialog(
            onDismissRequest = { contaAlterarVencimento = null },
            confirmButton = {
                Button(
                    onClick = {
                        onAlterarVencimento(conta.id, dataVencimento.trim())
                        contaAlterarVencimento = null
                    },
                    enabled = dataVencimento.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { contaAlterarVencimento = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Alterar vencimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(conta.descricao)
                    OutlinedTextField(
                        value = dataVencimento,
                        onValueChange = { dataVencimento = it },
                        label = { Text("Novo vencimento (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (contaEditar != null) {
        val conta = contaEditar!!
        var descricao by remember(conta.id) { mutableStateOf(conta.descricao) }
        var fornecedor by remember(conta.id) { mutableStateOf(fornecedorOuTraco(conta.fornecedorNome).takeIf { it != "-" } ?: "") }
        var valor by remember(conta.id) { mutableStateOf(doubleToInput(conta.valor)) }
        var dataVencimento by remember(conta.id) { mutableStateOf(conta.dataVencimento.take(10)) }

        AlertDialog(
            onDismissRequest = { contaEditar = null },
            confirmButton = {
                Button(
                    onClick = {
                        onEditarLancamento(
                            conta.id,
                            descricao.trim(),
                            fornecedor.trim(),
                            valor.trim().replace(",", "."),
                            dataVencimento.trim()
                        )
                        contaEditar = null
                    },
                    enabled = descricao.isNotBlank() && valor.isNotBlank() && dataVencimento.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { contaEditar = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Editar lançamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fornecedor,
                        onValueChange = { fornecedor = it },
                        label = { Text("Fornecedor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it },
                        label = { Text("Valor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dataVencimento,
                        onValueChange = { dataVencimento = it },
                        label = { Text("Vencimento (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (contaAlterarPagamento != null) {
        val conta = contaAlterarPagamento!!
        var dataPagamento by remember(conta.id) {
            mutableStateOf(
                if (conta.dataPagamento.isNotBlank() && conta.dataPagamento != "null") {
                    conta.dataPagamento.take(10)
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                }
            )
        }

        AlertDialog(
            onDismissRequest = { contaAlterarPagamento = null },
            confirmButton = {
                Button(
                    onClick = {
                        onAlterarDataPagamento(conta.id, dataPagamento.trim())
                        contaAlterarPagamento = null
                    },
                    enabled = dataPagamento.isNotBlank()
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { contaAlterarPagamento = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Alterar data de pagamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(conta.descricao)
                    OutlinedTextField(
                        value = dataPagamento,
                        onValueChange = { dataPagamento = it },
                        label = { Text("Nova data (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (contaExcluir != null) {
        val conta = contaExcluir!!

        AlertDialog(
            onDismissRequest = { contaExcluir = null },
            confirmButton = {
                Button(
                    onClick = {
                        onExcluirLancamento(conta.id)
                        contaExcluir = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { contaExcluir = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Excluir lançamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deseja realmente excluir este lançamento?")
                    Text(conta.descricao)
                }
            }
        )
    }
}

@Composable
private fun ContaPagarCard(
    conta: ContaPagar,
    onVerDetalhes: () -> Unit,
    onInformarPagamentoTotal: () -> Unit,
    onPagamentoParcial: () -> Unit,
    onAlterarVencimento: () -> Unit,
    onEditarLancamento: () -> Unit,
    onAlterarDataPagamento: () -> Unit,
    onExcluirLancamento: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val sideColor = statusSideColor(conta.status)
    val badgeBg = statusBadgeBg(conta.status)
    val badgeFg = statusBadgeFg(conta.status)
    val cardBg = statusCardBg(conta.status)
    val saldo = faltaPagar(conta)
    val saldoColor = if (saldo <= 0.0) Color(0xFF1E7D3A) else Color(0xFFB3261E)

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
                    .height(178.dp)
                    .background(sideColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
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
                                modifier = Modifier
                                    .background(badgeBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = formatStatus(conta.status),
                                    color = badgeFg,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Ações")
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

                                if (conta.status.lowercase() != "pago") {
                                    DropdownMenuItem(
                                        text = { Text("Informar pagamento total") },
                                        onClick = {
                                            expanded = false
                                            onInformarPagamentoTotal()
                                        }
                                    )
                                }

                                if (faltaPagar(conta) > 0.0) {
                                    DropdownMenuItem(
                                        text = { Text("Pagamento parcial") },
                                        onClick = {
                                            expanded = false
                                            onPagamentoParcial()
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = { Text("Alterar vencimento") },
                                    onClick = {
                                        expanded = false
                                        onAlterarVencimento()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Editar lançamento") },
                                    onClick = {
                                        expanded = false
                                        onEditarLancamento()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Alterar data pagamento") },
                                    onClick = {
                                        expanded = false
                                        onAlterarDataPagamento()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Excluir lançamento") },
                                    onClick = {
                                        expanded = false
                                        onExcluirLancamento()
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ResumoItem(
                        titulo = "Vencimento",
                        valor = formatDate(conta.dataVencimento)
                    )

                    ResumoItem(
                        titulo = "Pagamento",
                        valor = formatDate(conta.dataPagamento)
                    )
                }

                ResumoItem(
                    titulo = "Categoria",
                    valor = valorOuTraco(conta.categoria)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ResumoItem(
                        titulo = "Total",
                        valor = money(conta.valor)
                    )

                    ResumoItem(
                        titulo = "Pago",
                        valor = money(conta.valorPago)
                    )
                }

                Column {
                    Text(
                        text = "SALDO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = money(saldo),
                        style = MaterialTheme.typography.titleMedium,
                        color = saldoColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ResumoItem(
    titulo: String,
    valor: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
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
}

@Composable
private fun DetalheLinha(
    titulo: String,
    valor: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

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

private fun statusCardBg(status: String): Color = when (status.lowercase()) {
    "pago" -> Color(0xFFF4FBF6)
    "vencido" -> Color(0xFFFFF6F6)
    "parcial" -> Color(0xFFFFFCF2)
    else -> Color.White
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
        .replace(",", "X")
        .replace(".", ",")
        .replace("X", ".")

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

private fun doubleToInput(value: Double): String =
    String.format(Locale.US, "%.2f", value)
