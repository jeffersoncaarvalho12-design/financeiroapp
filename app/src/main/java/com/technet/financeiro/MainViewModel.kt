package com.technet.financeiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technet.financeiro.data.AuthRepository
import com.technet.financeiro.data.FakeAuthRepository
import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    NEW_EXPENSE
}

data class MainUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val dashboard: DashboardSummary? = null,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val isSavingExpense: Boolean = false,
    val expenseMessage: String? = null
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
