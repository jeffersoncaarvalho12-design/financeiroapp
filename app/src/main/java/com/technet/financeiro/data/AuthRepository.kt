package com.technet.financeiro.data

import com.technet.financeiro.model.ConciliacaoItem
import com.technet.financeiro.model.ContaPagar
import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User

data class ContaPagarPaymentResult(
    val contaId: Int,
    val valorPago: Double,
    val saldoAberto: Double,
    val status: String,
    val dataPagamento: String
)

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

    suspend fun registerContaPayment(
        contaId: Int,
        valor: String,
        dataPagamento: String,
        observacoes: String
    ): Result<ContaPagarPaymentResult>

    suspend fun listConciliacao(
        mes: Int,
        ano: Int
    ): Result<List<ConciliacaoItem>>

    suspend fun conciliarMovimento(
        movimentoId: Int,
        contaId: Int
    ): Result<String>
}
