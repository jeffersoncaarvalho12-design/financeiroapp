package com.technet.financeiro.data

import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User
import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        delay(700)
        return if (email.isNotBlank() && password.isNotBlank()) {
            Result.success(User(name = "Jefferson Carvalho", email = email))
        } else {
            Result.failure(IllegalArgumentException("E-mail e senha são obrigatórios."))
        }
    }

    override suspend fun dashboardSummary(): DashboardSummary {
        delay(400)
        return DashboardSummary(
            receitaScm = 50250.0,
            combustivel = 18430.0,
            manutencao = 9200.0,
            energia = 7600.0,
            agua = 3200.0
        )
    }
}
