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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.technet.financeiro.model.CategoriaItem
import com.technet.financeiro.model.ConciliacaoItem
import com.technet.financeiro.model.ContaPagar
import com.technet.financeiro.ui.theme.BackgroundSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConciliacaoScreen(
    items: List<ConciliacaoItem>,
    contasDisponiveis: List<ContaPagar>,
    categorias: List<CategoriaItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onConciliar: (Int, Int) -> Unit,
    onCriarDespesa: (String, String, String, Int, String, Int, Boolean) -> Unit
) {
    var itemDetalhe by remember { mutableStateOf<ConciliacaoItem?>(null) }
    var itemParaConciliar by remember { mutableStateOf<ConciliacaoItem?>(null) }
    var itemCriarDespesa by remember { mutableStateOf<ConciliacaoItem?>(null) }
    var mensagemIgnorar by remember { mutableStateOf<String?>(null) }

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
                        ConciliacaoCard(
                            item = item,
                            onVerDetalhes = { itemDetalhe = item },
                            onConciliar = { itemParaConciliar = item },
                            onCriarDespesa = { itemCriarDespesa = item },
                            onIgnorar = { mensagemIgnorar = "Movimento marcado como ignorado (próxima etapa)" }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }

    if (itemDetalhe != null) {
        val item = itemDetalhe!!

        AlertDialog(
            onDismissRequest = { itemDetalhe = null },
            confirmButton = {
                TextButton(onClick = { itemDetalhe = null }) {
                    Text("Fechar")
                }
            },
            title = { Text("Detalhes do movimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetalheLinha("Descrição", item.descricao.ifBlank { "-" })
                    DetalheLinha("Origem", item.origem.ifBlank { "-" })
                    DetalheLinha("Valor", money(item.valor))
                    DetalheLinha("Data", formatDate(item.data))
                    DetalheLinha("Tipo", formatTipo(item.tipo))
                    DetalheLinha("Status", formatStatus(item.status))
                }
            }
        )
    }

    if (itemParaConciliar != null) {
        val item = itemParaConciliar!!
        val contasFiltradas = contasDisponiveis.filter { it.status != "pago" }

        AlertDialog(
            onDismissRequest = { itemParaConciliar = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { itemParaConciliar = null }) {
                    Text("Fechar")
                }
            },
            title = { Text("Escolher conta para conciliar") },
            text = {
                if (contasFiltradas.isEmpty()) {
                    Text("Nenhuma conta disponível carregada para conciliar.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(contasFiltradas) { conta ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = conta.descricao.ifBlank { "-" },
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    Text(
                                        text = fornecedorOuTraco(conta.fornecedorNome),
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        text = "Valor: ${money(conta.valor)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "Vencimento: ${formatDate(conta.dataVencimento)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    TextButton(
                                        onClick = {
                                            onConciliar(item.id, conta.id)
                                            itemParaConciliar = null
                                        }
                                    ) {
                                        Text("Conciliar com esta conta")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    if (itemCriarDespesa != null) {
        val item = itemCriarDespesa!!
        var descricao by remember(item.id) { mutableStateOf(item.descricao) }
        var valor by remember(item.id) { mutableStateOf(item.valor.toString()) }
        var vencimento by remember(item.id) {
            mutableStateOf(
                if (item.data.length >= 10) item.data.substring(0, 10)
                else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
        }
        var observacoes by remember(item.id) { mutableStateOf("Criado pela conciliação do app") }
        var categoriaSelecionadaId by remember(item.id) {
            mutableStateOf(if (categorias.isNotEmpty()) categorias.first().id else 0)
        }
        var conciliarAposCriar by remember(item.id) { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { itemCriarDespesa = null },
            confirmButton = {
                Button(
                    onClick = {
                        onCriarDespesa(
                            descricao.trim(),
                            valor.trim().replace(",", "."),
                            vencimento.trim(),
                            categoriaSelecionadaId,
                            observacoes.trim(),
                            item.id,
                            conciliarAposCriar
                        )
                        itemCriarDespesa = null
                    },
                    enabled = descricao.isNotBlank() &&
                        valor.isNotBlank() &&
                        vencimento.isNotBlank() &&
                        categoriaSelecionadaId > 0
                ) {
                    Text("Criar despesa")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemCriarDespesa = null }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Criar despesa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it },
                        label = { Text("Valor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = vencimento,
                        onValueChange = { vencimento = it },
                        label = { Text("Vencimento (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Categoria",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )

                    if (categorias.isEmpty()) {
                        Text("Nenhuma categoria carregada")
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(categorias) { categoria ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(categoria.nome)
                                        Checkbox(
                                            checked = categoriaSelecionadaId == categoria.id,
                                            onCheckedChange = {
                                                categoriaSelecionadaId = categoria.id
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = observacoes,
                        onValueChange = { observacoes = it },
                        label = { Text("Observações") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = conciliarAposCriar,
                            onCheckedChange = { conciliarAposCriar = it }
                        )
                        Text("Já conciliar após criar")
                    }
                }
            }
        )
    }

    if (mensagemIgnorar != null) {
        AlertDialog(
            onDismissRequest = { mensagemIgnorar = null },
            confirmButton = {
                TextButton(onClick = { mensagemIgnorar = null }) {
                    Text("OK")
                }
            },
            title = { Text("Ação") },
            text = { Text(mensagemIgnorar!!) }
        )
    }
}

@Composable
private fun ConciliacaoCard(
    item: ConciliacaoItem,
    onVerDetalhes: () -> Unit,
    onConciliar: () -> Unit,
    onCriarDespesa: () -> Unit,
    onIgnorar: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusNormalizado = item.status.trim().lowercase()
    val tipoNormalizado = item.tipo.trim().lowercase()

    val badgeBg = if (statusNormalizado == "conciliado") {
        Color(0xFFD9F5E3)
    } else {
        Color(0xFFFFEFC9)
    }

    val badgeFg = if (statusNormalizado == "conciliado") {
        Color(0xFF1E7D3A)
    } else {
        Color(0xFF9A6A00)
    }

    val sideColor = if (tipoNormalizado == "entrada") {
        Color(0xFF23A55A)
    } else {
        Color(0xFFE14D4D)
    }

    val valorColor = if (tipoNormalizado == "entrada") {
        Color(0xFF1E7D3A)
    } else {
        Color(0xFFB3261E)
    }

    val tipoColor = if (tipoNormalizado == "entrada") {
        Color(0xFF1E7D3A)
    } else {
        Color(0xFFB3261E)
    }

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
                    .height(170.dp)
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

                    Column(horizontalAlignment = Alignment.End) {
                        Card(shape = RoundedCornerShape(50.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(badgeBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (statusNormalizado == "conciliado") "Conciliado" else "Pendente",
                                    color = badgeFg,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

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

                                DropdownMenuItem(
                                    text = { Text("Conciliar") },
                                    onClick = {
                                        expanded = false
                                        onConciliar()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Criar despesa") },
                                    onClick = {
                                        expanded = false
                                        onCriarDespesa()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Ignorar") },
                                    onClick = {
                                        expanded = false
                                        onIgnorar()
                                    }
                                )
                            }
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
                            text = formatTipo(item.tipo),
                            style = MaterialTheme.typography.bodyLarge,
                            color = tipoColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
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

private fun formatTipo(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    val v = value.trim().lowercase()
    return when (v) {
        "entrada" -> "Entrada"
        "saida" -> "Saída"
        "saída" -> "Saída"
        else -> value.replaceFirstChar { it.uppercase() }
    }
}

private fun formatStatus(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    val v = value.trim().lowercase()
    return when (v) {
        "conciliado" -> "Conciliado"
        "pendente" -> "Pendente"
        else -> value.replaceFirstChar { it.uppercase() }
    }
}

private fun fornecedorOuTraco(value: String?): String =
    if (value.isNullOrBlank() || value == "null") "-" else value
