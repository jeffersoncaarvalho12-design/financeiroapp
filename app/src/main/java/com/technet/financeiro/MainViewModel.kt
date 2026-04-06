package com.technet.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technet.financeiro.data.AuthRepository
import com.technet.financeiro.data.FakeAuthRepository
import com.technet.financeiro.model.CategoriaItem
import com.technet.financeiro.model.ConciliacaoItem
import com.technet.financeiro.model.ContaPagar
import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    DASHBOARD,
    NEW_EXPENSE,
    CONTAS_PAGAR,
    CONCILIACAO
}

data class MainUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val dashboard: DashboardSummary? = null,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val isSavingExpense: Boolean = false,
    val expenseMessage: String? = null,
    val contasPagar: List<ContaPagar> = emptyList(),
    val contasBuscaConciliacao: List<ContaPagar> = emptyList(),
    val isLoadingBuscaContasConciliacao: Boolean = false,
    val isLoadingContas: Boolean = false,
    val conciliacao: List<ConciliacaoItem> = emptyList(),
    val isLoadingConciliacao: Boolean = false,
    val categorias: List<CategoriaItem> = emptyList(),
    val contasMes: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val contasAno: Int = Calendar.getInstance().get(Calendar.YEAR),
    val conciliacaoBusca: String = "",
    val conciliacaoStatus: String = "",
    val conciliacaoTipo: String = ""
)

class MainViewModel(
    private val repository: AuthRepository = FakeAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                expenseMessage = null
            )

            repository.login(email, password)
                .onSuccess { user ->
                    val dashboard = repository.dashboardSummary()
                    _uiState.value = MainUiState(
                        isLoading = false,
                        user = user,
                        dashboard = dashboard,
                        isLoggedIn = true,
                        currentScreen = AppScreen.DASHBOARD
                    )
                    carregarCategoriasSilencioso()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Falha no login."
                    )
                }
        }
    }

    fun openNewExpense() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.NEW_EXPENSE,
            expenseMessage = null,
            errorMessage = null
        )
        carregarCategoriasSilencioso()
    }

    fun openContasPagar() {
        val state = _uiState.value
        loadContasPagar(state.contasMes, state.contasAno)
    }

    fun openConciliacao() {
        val state = _uiState.value
        loadConciliacao(
            mes = state.contasMes,
            ano = state.contasAno,
            busca = state.conciliacaoBusca,
            status = state.conciliacaoStatus,
            tipo = state.conciliacaoTipo
        )
        loadContasPagarSilencioso(state.contasMes, state.contasAno)
        carregarCategoriasSilencioso()
        buscarContasParaConciliacao("")
    }

    fun previousMonthContas() {
        val state = _uiState.value
        var mes = state.contasMes - 1
        var ano = state.contasAno

        if (mes < 1) {
            mes = 12
            ano--
        }

        loadContasPagar(mes, ano)
    }

    fun nextMonthContas() {
        val state = _uiState.value
        var mes = state.contasMes + 1
        var ano = state.contasAno

        if (mes > 12) {
            mes = 1
            ano++
        }

        loadContasPagar(mes, ano)
    }

    fun buscarConciliacao(busca: String, status: String, tipo: String) {
        val state = _uiState.value

        _uiState.value = state.copy(
            conciliacaoBusca = busca,
            conciliacaoStatus = status,
            conciliacaoTipo = tipo
        )

        loadConciliacao(
            mes = state.contasMes,
            ano = state.contasAno,
            busca = busca,
            status = status,
            tipo = tipo
        )
    }

    fun buscarContasParaConciliacao(busca: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingBuscaContasConciliacao = true
            )

            repository.searchContasPagarParaConciliacao(busca)
                .onSuccess { contas ->
                    _uiState.value = _uiState.value.copy(
                        contasBuscaConciliacao = contas,
                        isLoadingBuscaContasConciliacao = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingBuscaContasConciliacao = false,
                        errorMessage = error.message ?: "Erro ao buscar contas para conciliar."
                    )
                }
        }
    }

    fun createExpense(
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
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSavingExpense = true,
                errorMessage = null,
                expenseMessage = null
            )

            repository.createExpense(
                descricao = descricao,
                valor = valor,
                vencimento = vencimento,
                observacoes = observacoes,
                categoriaId = categoriaId,
                modoLancamento = modoLancamento,
                qtdParcelas = qtdParcelas,
                qtdRepeticoes = qtdRepeticoes,
                fornecedorNome = fornecedorNome,
                formaPagamento = formaPagamento,
                contaPagamento = contaPagamento,
                marcarPago = marcarPago,
                agendado = agendado
            ).onSuccess { message ->
                val mesDestino = vencimento.substring(5, 7).toIntOrNull()
                    ?: _uiState.value.contasMes
                val anoDestino = vencimento.substring(0, 4).toIntOrNull()
                    ?: _uiState.value.contasAno

                _uiState.value = _uiState.value.copy(
                    isSavingExpense = false,
                    errorMessage = null,
                    expenseMessage = message,
                    currentScreen = AppScreen.CONTAS_PAGAR,
                    isLoadingContas = true,
                    contasMes = mesDestino,
                    contasAno = anoDestino
                )

                repository.listContasPagar(
                    mes = mesDestino,
                    ano = anoDestino,
                    busca = "",
                    status = "",
                    tipo = ""
                ).onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        contasPagar = items,
                        isLoadingContas = false
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingContas = false,
                        errorMessage = error.message ?: "Erro ao atualizar contas a pagar."
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSavingExpense = false,
                    errorMessage = error.message ?: "Erro ao criar despesa."
                )
            }
        }
    }

    fun markContaAsPaid(
        contaId: Int,
        dataPagamento: String,
        observacoes: String
    ) {
        viewModelScope.launch {
            repository.markContaAsPaid(
                contaId = contaId,
                dataPagamento = dataPagamento,
                observacoes = observacoes
            ).onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    expenseMessage = message,
                    errorMessage = null
                )
                loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Erro ao informar pagamento total."
                )
            }
        }
    }

    fun registerContaPayment(
        contaId: Int,
        valor: String,
        dataPagamento: String,
        observacoes: String
    ) {
        viewModelScope.launch {
            repository.registerContaPayment(
                contaId = contaId,
                valor = valor,
                dataPagamento = dataPagamento,
                observacoes = observacoes
            ).onSuccess {
                _uiState.value = _uiState.value.copy(
                    expenseMessage = "Pagamento registrado com sucesso.",
                    errorMessage = null
                )
                loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Erro ao registrar pagamento."
                )
            }
        }
    }

    fun updateContaDueDate(contaId: Int, dataVencimento: String) {
        viewModelScope.launch {
            repository.updateContaDueDate(contaId, dataVencimento)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        expenseMessage = message,
                        errorMessage = null
                    )
                    loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao alterar vencimento."
                    )
                }
        }
    }

    fun updateContaLaunch(
        contaId: Int,
        descricao: String,
        fornecedorNome: String,
        valor: String,
        dataVencimento: String
    ) {
        viewModelScope.launch {
            repository.updateContaLaunch(
                contaId = contaId,
                descricao = descricao,
                fornecedorNome = fornecedorNome,
                valor = valor,
                dataVencimento = dataVencimento
            ).onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    expenseMessage = message,
                    errorMessage = null
                )
                loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Erro ao editar lançamento."
                )
            }
        }
    }

    fun updateContaPaymentDate(contaId: Int, dataPagamento: String) {
        viewModelScope.launch {
            repository.updateContaPaymentDate(contaId, dataPagamento)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        expenseMessage = message,
                        errorMessage = null
                    )
                    loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao alterar data de pagamento."
                    )
                }
        }
    }

    fun deleteConta(contaId: Int) {
        viewModelScope.launch {
            repository.deleteConta(contaId)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        expenseMessage = message,
                        errorMessage = null
                    )
                    loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao excluir lançamento."
                    )
                }
        }
    }

    private fun loadContasPagar(mes: Int, ano: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.CONTAS_PAGAR,
                isLoadingContas = true,
                errorMessage = null,
                contasMes = mes,
                contasAno = ano
            )

            repository.listContasPagar(
                mes = mes,
                ano = ano,
                busca = "",
                status = "",
                tipo = ""
            ).onSuccess { items ->
                _uiState.value = _uiState.value.copy(
                    contasPagar = items,
                    isLoadingContas = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingContas = false,
                    errorMessage = error.message ?: "Erro ao listar contas."
                )
            }
        }
    }

    private fun loadContasPagarSilencioso(mes: Int, ano: Int) {
        viewModelScope.launch {
            repository.listContasPagar(
                mes = mes,
                ano = ano,
                busca = "",
                status = "",
                tipo = ""
            ).onSuccess { items ->
                _uiState.value = _uiState.value.copy(
                    contasPagar = items
                )
            }.onFailure { }
        }
    }

    private fun loadConciliacao(
        mes: Int,
        ano: Int,
        busca: String = "",
        status: String = "",
        tipo: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.CONCILIACAO,
                isLoadingConciliacao = true,
                errorMessage = null,
                expenseMessage = null,
                contasMes = mes,
                contasAno = ano,
                conciliacaoBusca = busca,
                conciliacaoStatus = status,
                conciliacaoTipo = tipo
            )

            repository.listConciliacao(
                mes = mes,
                ano = ano,
                busca = busca,
                status = status,
                tipo = tipo
            ).onSuccess { items ->
                _uiState.value = _uiState.value.copy(
                    conciliacao = items,
                    isLoadingConciliacao = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingConciliacao = false,
                    errorMessage = error.message ?: "Erro ao carregar conciliação."
                )
            }
        }
    }

    private fun carregarCategoriasSilencioso() {
        viewModelScope.launch {
            repository.listCategorias()
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        categorias = items
                    )
                }
                .onFailure { }
        }
    }

    private fun removerMovimentoDaLista(movimentoId: Int) {
        _uiState.value = _uiState.value.copy(
            conciliacao = _uiState.value.conciliacao.filterNot { it.id == movimentoId },
            errorMessage = null
        )
    }

    fun conciliarMovimento(movimentoId: Int, contaId: Int) {
        viewModelScope.launch {
            repository.conciliarMovimento(movimentoId, contaId)
                .onSuccess {
                    removerMovimentoDaLista(movimentoId)
                    loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao conciliar movimento"
                    )
                }
        }
    }

    fun conciliarMovimentoTotal(
        movimentoId: Int,
        contaId: Int,
        dataPagamento: String,
        observacoes: String
    ) {
        viewModelScope.launch {
            repository.conciliarMovimentoTotal(
                movimentoId = movimentoId,
                contaId = contaId,
                dataPagamento = dataPagamento,
                observacoes = observacoes
            ).onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    expenseMessage = message,
                    errorMessage = null
                )
                removerMovimentoDaLista(movimentoId)
                loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                buscarContasParaConciliacao("")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Erro ao conciliar como total."
                )
            }
        }
    }

    fun conciliarMovimentoParcial(
        movimentoId: Int,
        contaId: Int,
        valorPagamento: String,
        dataPagamento: String,
        observacoes: String
    ) {
        viewModelScope.launch {
            repository.conciliarMovimentoParcial(
                movimentoId = movimentoId,
                contaId = contaId,
                valorPagamento = valorPagamento,
                dataPagamento = dataPagamento,
                observacoes = observacoes
            ).onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    expenseMessage = message,
                    errorMessage = null
                )
                removerMovimentoDaLista(movimentoId)
                loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                buscarContasParaConciliacao("")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Erro ao conciliar como parcial."
                )
            }
        }
    }

    fun criarDespesaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ) {
        viewModelScope.launch {
            repository.criarDespesaDaConciliacao(
                descricao = descricao,
                valor = valor,
                vencimento = vencimento,
                categoriaId = categoriaId,
                observacoes = observacoes,
                movimentoId = movimentoId,
                conciliarAposCriar = conciliarAposCriar
            ).onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    expenseMessage = message,
                    errorMessage = null
                )

                if (conciliarAposCriar) {
                    removerMovimentoDaLista(movimentoId)
                }

                loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                buscarContasParaConciliacao("")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Erro ao criar despesa"
                )
            }
        }
    }

    fun criarReceitaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ) {
        viewModelScope.launch {
            repository.criarReceitaDaConciliacao(
                descricao = descricao,
                valor = valor,
                vencimento = vencimento,
                categoriaId = categoriaId,
                observacoes = observacoes,
                movimentoId = movimentoId,
                conciliarAposCriar = conciliarAposCriar
            ).onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    expenseMessage = message,
                    errorMessage = null
                )

                if (conciliarAposCriar) {
                    removerMovimentoDaLista(movimentoId)
                }

                loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                buscarContasParaConciliacao("")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "Erro ao criar receita"
                )
            }
        }
    }

    fun backToDashboard() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.DASHBOARD,
            errorMessage = null,
            expenseMessage = null
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            expenseMessage = null
        )
    }

    fun logout() {
        _uiState.value = MainUiState()
    }
}
