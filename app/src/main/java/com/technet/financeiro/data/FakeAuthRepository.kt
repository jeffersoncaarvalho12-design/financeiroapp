package com.technet.financeiro.data

import com.technet.financeiro.model.CategoriaItem
import com.technet.financeiro.model.ConciliacaoItem
import com.technet.financeiro.model.ContaPagar
import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class FakeAuthRepository : AuthRepository {

    private var sessionCookie: String? = null

    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiConfig.BASE_URL + "login.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doInput = true
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Accept", "application/json")
            }

            val postData =
                "email=${URLEncoder.encode(email, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"

            BufferedWriter(OutputStreamWriter(conn.outputStream)).use {
                it.write(postData)
            }

            val setCookie = conn.headerFields["Set-Cookie"]
            if (!setCookie.isNullOrEmpty()) {
                sessionCookie = setCookie.joinToString("; ") { cookie ->
                    cookie.substringBefore(";")
                }
            }

            val response = read(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success")) {
                return@withContext Result.failure(Exception(json.optString("message", "Falha no login")))
            }

            val u = json.getJSONObject("user")

            Result.success(
                User(
                    u.optString("name", ""),
                    u.optString("email", "")
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro no login"))
        }
    }

    override suspend fun dashboardSummary(): DashboardSummary {
        return DashboardSummary(0.0, 0.0, 0.0, 0.0, 0.0)
    }

    override suspend fun createExpense(
        descricao: String,
        valor: String,
        vencimento: String,
        parcelas: Int,
        observacoes: String
    ): Result<String> = Result.success("ok")

    override suspend fun listContasPagar(
        mes: Int,
        ano: Int,
        busca: String,
        status: String,
        tipo: String
    ): Result<List<ContaPagar>> = withContext(Dispatchers.IO) {
        try {
            val queryParts = mutableListOf(
                "mes=${URLEncoder.encode(mes.toString(), "UTF-8")}",
                "ano=${URLEncoder.encode(ano.toString(), "UTF-8")}"
            )

            val queryString = queryParts.joinToString("&")
            val url = URL(ApiConfig.BASE_URL + "contas_pagar_list.php?$queryString")

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                doInput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
                sessionCookie?.let { setRequestProperty("Cookie", it) }
            }

            val response = read(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return@withContext Result.failure(
                    Exception(json.optString("message", "Erro ao carregar contas a pagar"))
                )
            }

            val list = mutableListOf<ContaPagar>()
            val arr = json.optJSONArray("items")

            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    list.add(
                        ContaPagar(
                            id = o.optInt("id"),
                            descricao = o.optString("descricao"),
                            valor = o.optDouble("valor"),
                            dataVencimento = o.optString("data_vencimento"),
                            status = o.optString("status"),
                            categoria = o.optString("categoria", "-"),
                            fornecedorNome = o.optString("fornecedor_nome", "-"),
                            valorPago = o.optDouble("valor_pago", 0.0),
                            saldoAberto = o.optDouble("saldo_aberto", 0.0),
                            dataPagamento = o.optString("data_pagamento", "")
                        )
                    )
                }
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao listar contas a pagar"))
        }
    }

    override suspend fun markContaAsPaid(contaId: Int): Result<String> =
        Result.success("ok")

    override suspend fun registerContaPayment(
        contaId: Int,
        valor: String,
        dataPagamento: String,
        observacoes: String
    ): Result<ContaPagarPaymentResult> =
        Result.failure(Exception("Não implementado"))

    override suspend fun listConciliacao(
        mes: Int,
        ano: Int,
        busca: String,
        status: String,
        tipo: String
    ): Result<List<ConciliacaoItem>> = withContext(Dispatchers.IO) {
        try {
            val queryParts = mutableListOf(
                "mes=${URLEncoder.encode(mes.toString(), "UTF-8")}",
                "ano=${URLEncoder.encode(ano.toString(), "UTF-8")}"
            )

            if (busca.isNotBlank()) {
                queryParts.add("busca=${URLEncoder.encode(busca, "UTF-8")}")
            }

            if (status.isNotBlank()) {
                queryParts.add("status=${URLEncoder.encode(status, "UTF-8")}")
            }

            if (tipo.isNotBlank()) {
                queryParts.add("tipo=${URLEncoder.encode(tipo, "UTF-8")}")
            }

            val queryString = queryParts.joinToString("&")
            val url = URL(ApiConfig.BASE_URL + "conciliacao_list.php?$queryString")

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                doInput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
                sessionCookie?.let { setRequestProperty("Cookie", it) }
            }

            val response = read(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return@withContext Result.failure(
                    Exception(json.optString("message", "Erro ao carregar conciliação"))
                )
            }

            val list = mutableListOf<ConciliacaoItem>()
            val arr = json.optJSONArray("items")

            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    list.add(
                        ConciliacaoItem(
                            id = o.optInt("id"),
                            descricao = o.optString("descricao"),
                            valor = o.optDouble("valor"),
                            tipo = o.optString("tipo"),
                            data = o.optString("data"),
                            origem = o.optString("origem"),
                            status = o.optString("status")
                        )
                    )
                }
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro na conciliação"))
        }
    }

    override suspend fun conciliarMovimento(
        movimentoId: Int,
        contaId: Int
    ): Result<String> = Result.success("ok")

    override suspend fun listCategorias(): Result<List<CategoriaItem>> =
        Result.success(emptyList())

    override suspend fun criarDespesaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ): Result<String> = Result.success("ok")

    override suspend fun criarReceitaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ): Result<String> = Result.success("ok")

    private fun read(conn: HttpURLConnection): String {
        val stream =
            if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream

        return BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.readText()
        }
    }
}
