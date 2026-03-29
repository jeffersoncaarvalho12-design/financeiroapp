package com.technet.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technet.financeiro.data.AuthRepository
import com.technet.financeiro.data.FakeAuthRepository
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
    CONTAS_PAGAR
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
    val contasMes: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val contasAno: Int = Calendar.getInstance().get(Calendar.YEAR)
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

    private fun loadContasPagar(mes: Int, ano: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.CONTAS_PAGAR,
                isLoadingContas = true,
                errorMessage = null,
                expenseMessage = null,
                contasMes = mes,
                contasAno = ano
            )

            repository.listContasPagar(mes, ano)
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        contasPagar = items,
                        isLoadingContas = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingContas = false,
                        errorMessage = error.message ?: "Erro ao listar contas."
                    )
                }
        }
    }

    fun markContaAsPaid(contaId: Int) {
        viewModelScope.launch {
            repository.markContaAsPaid(contaId)
                .onSuccess {
                    val listaAtualizada = _uiState.value.contasPagar.map { conta ->
                        if (conta.id == contaId) {
                            conta.copy(
                                status = "pago",
                                valorPago = conta.valor,
                                saldoAberto = 0.0,
                                dataPagamento = _uiState.value.contasAno.toString()
                            )
                        } else conta
                    }

                    _uiState.value = _uiState.value.copy(
                        contasPagar = listaAtualizada,
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Erro ao marcar conta como paga"
                    )
                }
        }
    }

    fun backToDashboard() {
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.DASHBOARD,
            expenseMessage = null,
            errorMessage = null
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
                expenseMessage = null,
                errorMessage = null
            )

            repository.createExpense(
                descricao = descricao,
                valor = valor,
                vencimento = vencimento,
                parcelas = parcelas,
                observacoes = observacoes
            ).onSuccess { message ->
                val dashboard = repository.dashboardSummary()
                _uiState.value = _uiState.value.copy(
                    isSavingExpense = false,
                    dashboard = dashboard,
                    currentScreen = AppScreen.DASHBOARD,
                    expenseMessage = message
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSavingExpense = false,
                    errorMessage = error.message ?: "Erro ao salvar despesa."
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            expenseMessage = null,
            errorMessage = null
        )
    }

    fun logout() {
        _uiState.value = MainUiState()
    }
}
