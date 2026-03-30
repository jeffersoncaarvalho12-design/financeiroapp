package com.technet.financeiro.data

import com.technet.financeiro.model.CategoriaItem
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
        ano: Int,
        busca: String = "",
        status: String = "",
        tipo: String = ""
    ): Result<List<ContaPagar>>

    suspend fun searchContasPagarParaConciliacao(
        busca: String
    ): Result<List<ContaPagar>>

    suspend fun markContaAsPaid(
        contaId: Int,
        dataPagamento: String,
        observacoes: String
    ): Result<String>

    suspend fun registerContaPayment(
        contaId: Int,
        valor: String,
        dataPagamento: String,
        observacoes: String
    ): Result<ContaPagarPaymentResult>

    suspend fun updateContaDueDate(
        contaId: Int,
        dataVencimento: String
    ): Result<String>

    suspend fun updateContaLaunch(
        contaId: Int,
        descricao: String,
        fornecedorNome: String,
        valor: String,
        dataVencimento: String
    ): Result<String>

    suspend fun updateContaPaymentDate(
        contaId: Int,
        dataPagamento: String
    ): Result<String>

    suspend fun deleteConta(
        contaId: Int
    ): Result<String>

    suspend fun listConciliacao(
        mes: Int,
        ano: Int,
        busca: String = "",
        status: String = "",
        tipo: String = ""
    ): Result<List<ConciliacaoItem>>

    suspend fun conciliarMovimento(
        movimentoId: Int,
        contaId: Int
    ): Result<String>

    suspend fun conciliarMovimentoTotal(
        movimentoId: Int,
        contaId: Int,
        dataPagamento: String,
        observacoes: String
    ): Result<String>

    suspend fun conciliarMovimentoParcial(
        movimentoId: Int,
        contaId: Int,
        valorPagamento: String,
        dataPagamento: String,
        observacoes: String
    ): Result<String>

    suspend fun listCategorias(): Result<List<CategoriaItem>>

    suspend fun criarDespesaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ): Result<String>

    suspend fun criarReceitaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ): Result<String>
}
