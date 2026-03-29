package com.technet.financeiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technet.financeiro.ui.screens.ContasPagarScreen
import com.technet.financeiro.ui.screens.DashboardScreen
import com.technet.financeiro.ui.screens.LoginScreen
import com.technet.financeiro.ui.screens.NewExpenseScreen
import com.technet.financeiro.ui.theme.FinanceiroTechNetTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinanceiroTechNetTheme {
                val state = viewModel.uiState.collectAsStateWithLifecycle().value

                when {
                    !state.isLoggedIn || state.user == null || state.dashboard == null -> {
                        LoginScreen(
                            isLoading = state.isLoading,
                            errorMessage = state.errorMessage,
                            onLogin = viewModel::login
                        )
                    }

                    state.currentScreen == AppScreen.NEW_EXPENSE -> {
                        NewExpenseScreen(
                            isSaving = state.isSavingExpense,
                            errorMessage = state.errorMessage,
                            onBack = viewModel::backToDashboard,
                            onSave = viewModel::createExpense
                        )
                    }

                    state.currentScreen == AppScreen.CONTAS_PAGAR -> {
                        ContasPagarScreen(
                            items = state.contasPagar,
                            isLoading = state.isLoadingContas,
                            errorMessage = state.errorMessage,
                            mes = state.contasMes,
                            ano = state.contasAno,
                            onPreviousMonth = viewModel::previousMonthContas,
                            onNextMonth = viewModel::nextMonthContas,
                            onBack = viewModel::backToDashboard,
                            onMarcarPagoReal = viewModel::markContaAsPaid
                        )
                    }

                    else -> {
                        DashboardScreen(
                            user = state.user,
                            summary = state.dashboard,
                            message = state.expenseMessage,
                            onOpenNewExpense = viewModel::openNewExpense,
                            onOpenContasPagar = viewModel::openContasPagar,
                            onClearMessage = viewModel::clearMessages,
                            onLogout = viewModel::logout
                        )
                    }
                }
            }
        }
    }
}
