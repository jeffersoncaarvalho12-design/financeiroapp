package com.technet.financeiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.technet.financeiro.ui.screens.DashboardScreen
import com.technet.financeiro.ui.screens.LoginScreen
import com.technet.financeiro.ui.theme.FinanceiroTechNetTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinanceiroTechNetTheme {
                val state = viewModel.uiState.collectAsStateWithLifecycle().value

                if (state.isLoggedIn && state.user != null && state.dashboard != null) {
                    DashboardScreen(
                        user = state.user,
                        summary = state.dashboard,
                        onLogout = viewModel::logout
                    )
                } else {
                    LoginScreen(
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage,
                        onLogin = viewModel::login
                    )
                }
            }
        }
    }
}
