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
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.technet.financeiro.ui.theme.BackgroundSoft

@Composable
fun NewExpenseScreen(
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: (String, String, String, Int, String) -> Unit
) {
    var descricao by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var vencimento by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var tipoLancamento by remember { mutableStateOf("unica") }
    var parcelas by remember { mutableIntStateOf(1) }

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
                    "Versão 1 ligada na sua API. Use a data no formato YYYY-MM-DD.",
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

                Text("Tipo de lançamento", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tipoLancamento == "unica",
                        onClick = {
                            tipoLancamento = "unica"
                            parcelas = 1
                        }
                    )
                    Text("Única")

                    Spacer(modifier = Modifier.weight(1f))

                    RadioButton(
                        selected = tipoLancamento == "parcelada",
                        onClick = {
                            tipoLancamento = "parcelada"
                            if (parcelas < 2) parcelas = 2
                        }
                    )
                    Text("Parcelada")
                }

                if (tipoLancamento == "parcelada") {
                    OutlinedTextField(
                        value = parcelas.toString(),
                        onValueChange = {
                            val parsed = it.toIntOrNull() ?: 2
                            parcelas = if (parsed < 2) 2 else parsed
                        },
                        label = { Text("Quantidade de parcelas") },
                        leadingIcon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

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
                            if (tipoLancamento == "parcelada") parcelas else 1,
                            observacoes.trim()
                        )
                    },
                    enabled = !isSaving &&
                        descricao.isNotBlank() &&
                        valor.isNotBlank() &&
                        vencimento.isNotBlank(),
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
