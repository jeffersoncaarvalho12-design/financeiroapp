package com.technet.financeiro.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.technet.financeiro.model.CategoriaItem
import com.technet.financeiro.ui.theme.BackgroundSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewExpenseScreen(
    categorias: List<CategoriaItem>,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (
        descricao: String,
        valor: String,
        vencimento: String,
        observacoes: String,
        categoriaId: Int,
        modoLancamento: String,
        qtdParcelas: Int,
        qtdRepeticoes: Int,
        fornecedorNome: String,
        formaPagamento: String,
        contaPagamento: String,
        marcarPago: Boolean,
        agendado: Boolean
    ) -> Unit
) {
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var vencimento by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    var observacoes by remember { mutableStateOf("") }
    var fornecedorNome by remember { mutableStateOf("") }
    var formaPagamento by remember { mutableStateOf("") }
    var contaPagamento by remember { mutableStateOf("") }
    var marcarPago by remember { mutableStateOf(false) }
    var agendado by remember { mutableStateOf(false) }

    var modoLancamento by remember { mutableStateOf("simples") }
    var qtdParcelas by remember { mutableIntStateOf(2) }
    var qtdRepeticoes by remember { mutableIntStateOf(1) }
    var categoriaId by remember { mutableIntStateOf(if (categorias.isNotEmpty()) categorias.first().id else 0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nova despesa", style = MaterialTheme.typography.headlineSmall)

                    TextButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Text("Voltar")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Mesma lógica principal do web: simples, parcelado e repetição mensal.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fornecedorNome,
                    onValueChange = { fornecedorNome = it },
                    label = { Text("Fornecedor manual") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valor,
                    onValueChange = { valor = it },
                    label = { Text("Valor") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vencimento,
                    onValueChange = { vencimento = it },
                    label = { Text("Vencimento (YYYY-MM-DD)") },
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoria", style = MaterialTheme.typography.titleMedium)

                if (categorias.isEmpty()) {
                    Text("Nenhuma categoria carregada")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categorias.forEach { categoria ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = categoriaId == categoria.id,
                                    onClick = { categoriaId = categoria.id }
                                )
                                Text(categoria.nome)
                            }
                        }
                    }
                }

                Text("Modo de lançamento", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = modoLancamento == "simples",
                        onClick = { modoLancamento = "simples" }
                    )
                    Text("Simples")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = modoLancamento == "parcelado",
                        onClick = {
                            modoLancamento = "parcelado"
                            if (qtdParcelas < 2) qtdParcelas = 2
                        }
                    )
                    Text("Parcelado")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = modoLancamento == "repeticao",
                        onClick = {
                            modoLancamento = "repeticao"
                            if (qtdRepeticoes < 1) qtdRepeticoes = 1
                        }
                    )
                    Text("Repetição mensal")
                }

                if (modoLancamento == "parcelado") {
                    OutlinedTextField(
                        value = qtdParcelas.toString(),
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 2
                            qtdParcelas = if (parsed < 2) 2 else parsed
                        },
                        label = { Text("Quantidade de parcelas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (modoLancamento == "repeticao") {
                    OutlinedTextField(
                        value = qtdRepeticoes.toString(),
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 1
                            qtdRepeticoes = if (parsed < 1) 1 else parsed
                        },
                        label = { Text("Meses para repetir") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = formaPagamento,
                    onValueChange = { formaPagamento = it },
                    label = { Text("Forma de pagamento") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contaPagamento,
                    onValueChange = { contaPagamento = it },
                    label = { Text("Conta de pagamento") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = marcarPago,
                        onCheckedChange = { marcarPago = it }
                    )
                    Text("Marcar como pago")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agendado,
                        onCheckedChange = { agendado = it }
                    )
                    Text("Agendado")
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        onSave(
                            descricao.trim(),
                            valor.trim().replace(",", "."),
                            vencimento.trim(),
                            observacoes.trim(),
                            categoriaId,
                            modoLancamento,
                            if (modoLancamento == "parcelado") qtdParcelas else 1,
                            if (modoLancamento == "repeticao") qtdRepeticoes else 0,
                            fornecedorNome.trim(),
                            formaPagamento.trim(),
                            contaPagamento.trim(),
                            marcarPago,
                            agendado
                        )
                    },
                    enabled = !isSaving &&
                        descricao.isNotBlank() &&
                        valor.isNotBlank() &&
                        vencimento.isNotBlank() &&
                        categoriaId > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    } else {
                        Text("Salvar despesa")
                    }
                }
            }
        }
    }
}
