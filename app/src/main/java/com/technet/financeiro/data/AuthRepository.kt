package com.technet.financeiro.data

import com.technet.financeiro.model.ContaPagar
import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>

    suspend fun dashboardSummary(): DashboardSummary

    suspend fun createExpense(
        descricao: String,
        valor: String,
        vencimento: String,
        parcelas: Int,
        observacoes: String
    ): Result<String>

    suspend fun listContasPagar(
        mes: Int,
        ano: Int
    ): Result<List<ContaPagar>>

    suspend fun markContaAsPaid(
        contaId: Int
    ): Result<String>
}
