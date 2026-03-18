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

data class MainUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val dashboard: DashboardSummary? = null,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class MainViewModel(
    private val repository: AuthRepository = FakeAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            repository.login(email, password)
                .onSuccess { user ->
                    val dashboard = repository.dashboardSummary()
                    _uiState.value = MainUiState(
                        isLoading = false,
                        user = user,
                        dashboard = dashboard,
                        isLoggedIn = true
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

    fun logout() {
        _uiState.value = MainUiState()
    }
}
