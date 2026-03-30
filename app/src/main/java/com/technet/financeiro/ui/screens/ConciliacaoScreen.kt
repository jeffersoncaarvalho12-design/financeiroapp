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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

private enum class FiltroStatusConciliacao {
    TODOS,
    PENDENTE,
    CONCILIADO
}

private enum class FiltroTipoConciliacao {
    TODOS,
    ENTRADA,
    SAIDA
}

private data class ConciliarParcialContext(
    val item: ConciliacaoItem,
    val conta: ContaPagar
)

@Composable
fun ConciliacaoScreen(
    items: List<ConciliacaoItem>,
    contasDisponiveis: List<ContaPagar>,
    contasBusca: List<ContaPagar>,
    isLoadingBuscaContas: Boolean,
    categorias: List<CategoriaItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onBuscarContas: (String) -> Unit,
    onConciliar: (Int, Int) -> Unit,
    onConciliarTotal: (Int, Int, String, String) -> Unit,
    onConciliarParcial: (Int, Int, String, String, String) -> Unit,
    onCriarDespesa: (String, String, String, Int, String, Int, Boolean) -> Unit,
    onCriarReceita: (String, String, String, Int, String, Int, Boolean) -> Unit
) {
    var itemDetalhe by remember { mutableStateOf<ConciliacaoItem?>(null) }
    var itemParaConciliar by remember { mutableStateOf<ConciliacaoItem?>(null) }
    var itemCriarDespesa by remember { mutableStateOf<ConciliacaoItem?>(null) }
    var itemCriarReceita by remember { mutableStateOf<ConciliacaoItem?>(null) }
    var mensagemIgnorar by remember { mutableStateOf<String?>(null) }
    var contextoParcial by remember { mutableStateOf<ConciliarParcialContext?>(null) }

    var busca by remember { mutableStateOf("") }
    var filtroStatus by remember { mutableStateOf(FiltroStatusConciliacao.TODOS) }
    var filtroTipo by remember { mutableStateOf(FiltroTipoConciliacao.TODOS) }

    val itensFiltrados = remember(items, busca, filtroStatus, filtroTipo) {
        items.filter { item ->
            val termo = busca.trim().lowercase()
            val descricao = item.descricao.trim().lowercase()
            val origem = item.origem.trim().lowercase()
            val tipo = item.tipo.trim().lowercase()
            val status = item.status.trim().lowercase()
            val valorTexto = String.format(Locale.US, "%.2f", item.valor)

            val passouBusca = termo.isBlank() ||
                descricao.contains(termo) ||
                origem.contains(termo) ||
                tipo.contains(termo) ||
                status.contains(termo) ||
                valorTexto.contains(termo)

            val passouStatus = when (filtroStatus) {
                FiltroStatusConciliacao.TODOS -> true
                FiltroStatusConciliacao.PENDENTE -> status == "pendente"
                FiltroStatusConciliacao.CONCILIADO -> status == "conciliado"
            }

            val passouTipo = when (filtroTipo) {
                FiltroTipoConciliacao.TODOS -> true
                FiltroTipoConciliacao.ENTRADA -> tipo == "entrada"
                FiltroTipoConciliacao.SAIDA -> tipo == "saida" || tipo == "saída"
            }

            passouBusca && passouStatus && passouTipo
        }
    }

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
                text = "${itensFiltrados.size} movimentos no filtro",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = { Text("Buscar movimento") },
                placeholder = { Text("Nome, origem, tipo ou valor") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Filtro de status",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroBotao(
                    texto = "Todos",
                    selecionado = filtroStatus == FiltroStatusConciliacao.TODOS,
                    onClick = { filtroStatus = FiltroStatusConciliacao.TODOS },
                    modifier = Modifier.weight(1f)
                )
                FiltroBotao(
                    texto = "Pendentes",
                    selecionado = filtroStatus == FiltroStatusConciliacao.PENDENTE,
                    onClick = { filtroStatus = FiltroStatusConciliacao.PENDENTE },
                    modifier = Modifier.weight(1f)
                )
                FiltroBotao(
                    texto = "Conciliados",
                    selecionado = filtroStatus == FiltroStatusConciliacao.CONCILIADO,
                    onClick = { filtroStatus = FiltroStatusConciliacao.CONCILIADO },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Filtro de tipo",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroBotao(
                    texto = "Todos",
                    selecionado = filtroTipo == FiltroTipoConciliacao.TODOS,
                    onClick = { filtroTipo = FiltroTipoConciliacao.TODOS },
                    modifier = Modifier.weight(1f)
                )
                FiltroBotao(
                    texto = "Entradas",
                    selecionado = filtroTipo == FiltroTipoConciliacao.ENTRADA,
                    onClick = { filtroTipo = FiltroTipoConciliacao.ENTRADA },
                    modifier = Modifier.weight(1f)
                )
                FiltroBotao(
                    texto = "Saídas",
                    selecionado = filtroTipo == FiltroTipoConciliacao.SAIDA,
                    onClick = { filtroTipo = FiltroTipoConciliacao.SAIDA },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = {
                    busca = ""
                    filtroStatus = FiltroStatusConciliacao.TODOS
                    filtroTipo = FiltroTipoConciliacao.TODOS
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 12.dp)
            ) {
                Text("Limpar filtros")
            }

            Spacer(modifier = Modifier.height(12.dp))
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

            itensFiltrados.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum movimento encontrado para o filtro atual")
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

                    items(itensFiltrados, key = { it.id }) { item ->
                        ConciliacaoCard(
                            item = item,
                            onVerDetalhes = { itemDetalhe = item },
                            onConciliar = { itemParaConciliar = item },
                            onCriarDespesa = { itemCriarDespesa = item },
                            onCriarReceita = { itemCriarReceita = item },
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
        var buscaConta by remember(item.id) { mutableStateOf("") }
        var dataPagamentoTotal by remember(item.id) {
            mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
        }
        var observacoesTotal by remember(item.id) { mutableStateOf("") }

        LaunchedEffect(item.id) {
            onBuscarContas("")
        }

        LaunchedEffect(buscaConta) {
            onBuscarContas(buscaConta)
        }

        val contasBase = remember(contasDisponiveis) {
            contasDisponiveis.filter { it.status.lowercase() != "pago" }
        }

        val contasUnificadas = remember(contasBase, contasBusca) {
            val mapa = linkedMapOf<Int, ContaPagar>()
            contasBase.forEach { mapa[it.id] = it }
            contasBusca.forEach { mapa[it.id] = it }
            mapa.values.toList()
        }

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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = buscaConta,
                        onValueChange = { buscaConta = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Buscar outra conta") },
                        placeholder = { Text("Descrição, fornecedor ou valor") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = dataPagamentoTotal,
                        onValueChange = { dataPagamentoTotal = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Data pagamento total (YYYY-MM-DD)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = observacoesTotal,
                        onValueChange = { observacoesTotal = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observações total") }
                    )

                    if (isLoadingBuscaContas) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (contasUnificadas.isEmpty()) {
                        Text("Nenhuma
