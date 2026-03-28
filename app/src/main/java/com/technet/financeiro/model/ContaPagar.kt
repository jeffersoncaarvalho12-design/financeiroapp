package com.technet.financeiro.model

data class ContaPagar(
    val id: Int,
    val descricao: String,
    val dataVencimento: String,
    val dataPagamento: String,
    val valor: Double,
    val valorPago: Double,
    val saldoAberto: Double,
    val status: String,
    val categoria: String,
    val fornecedorNome: String
)
