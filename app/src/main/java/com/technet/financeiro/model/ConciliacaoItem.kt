package com.technet.financeiro.model

data class ConciliacaoItem(
    val id: Int,
    val descricao: String,
    val valor: Double,
    val tipo: String,
    val data: String,
    val origem: String,
    val status: String
)
