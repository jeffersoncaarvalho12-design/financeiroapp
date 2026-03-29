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

    fun createExpense(
        descricao: String,
        valor: String,
        vencimento: String,
        parcelas: Int,
        observacoes: String
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
                parcelas = parcelas,
                observacoes = observacoes
            ).onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    isSavingExpense = false,
                    expenseMessage = message,
                    errorMessage = null,
                    currentScreen = AppScreen.NEW_EXPENSE
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSavingExpense = false,
                    errorMessage = error.message ?: "Erro ao criar despesa."
                )
            }
        }
    }

    fun markContaAsPaid(contaId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                errorMessage = null,
                expenseMessage = null
            )

            repository.markContaAsPaid(contaId)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        expenseMessage = message,
                        errorMessage = null
                    )
                    loadContasPagarSilencioso(_uiState.value.contasMes, _uiState.value.contasAno)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao marcar conta como paga."
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
            _uiState.value = _uiState.value.copy(
                errorMessage = null,
                expenseMessage = null
            )

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

    private fun loadContasPagar(mes: Int, ano: Int) {
        viewModel
