package com.technet.financeiro.data

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

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
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

            val postData = buildString {
                append("email=")
                append(URLEncoder.encode(email, "UTF-8"))
                append("&password=")
                append(URLEncoder.encode(password, "UTF-8"))
            }

            BufferedWriter(OutputStreamWriter(conn.outputStream, Charsets.UTF_8)).use { writer ->
                writer.write(postData)
                writer.flush()
            }

            val setCookie = conn.headerFields["Set-Cookie"] ?: emptyList()
            if (setCookie.isNotEmpty()) {
                sessionCookie = setCookie.joinToString("; ") { it.substringBefore(";") }
            }

            val response = readResponse(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return@withContext Result.failure(
                    Exception(json.optString("message", "Falha no login"))
                )
            }

            val userJson = json.optJSONObject("user")
                ?: return@withContext Result.failure(
                    Exception("Usuário não retornado pela API")
                )

            Result.success(
                User(
                    name = userJson.optString("name", ""),
                    email = userJson.optString("email", email)
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao conectar na API: ${e.message}"))
        }
    }

    override suspend fun dashboardSummary(): DashboardSummary = withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiConfig.BASE_URL + "dashboard.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                doInput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
                sessionCookie?.let { setRequestProperty("Cookie", it) }
            }

            val response = readResponse(conn)
            val json = JSONObject(response)
            val data = json.optJSONObject("data") ?: JSONObject()

            DashboardSummary(
                receitaScm = data.optDouble("receita_scm", 0.0),
                combustivel = data.optDouble("combustivel", 0.0),
                manutencao = data.optDouble("manutencao", 0.0),
                energia = data.optDouble("energia", 0.0),
                agua = data.optDouble("agua", 0.0)
            )
        } catch (_: Exception) {
            DashboardSummary(
                receitaScm = 0.0,
                combustivel = 0.0,
                manutencao = 0.0,
                energia = 0.0,
                agua = 0.0
            )
        }
    }

    override suspend fun createExpense(
        descricao: String,
        valor: String,
        vencimento: String,
        parcelas: Int,
        observacoes: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiConfig.BASE_URL + "despesa_create.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doInput = true
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("Accept", "application/json")
                sessionCookie?.let { setRequestProperty("Cookie", it) }
            }

            val postData = buildString {
                append("descricao=")
                append(URLEncoder.encode(descricao, "UTF-8"))
                append("&valor=")
                append(URLEncoder.encode(valor, "UTF-8"))
                append("&vencimento=")
                append(URLEncoder.encode(vencimento, "UTF-8"))
                append("&parcelas=")
                append(URLEncoder.encode(parcelas.toString(), "UTF-8"))
                append("&observacoes=")
                append(URLEncoder.encode(observacoes, "UTF-8"))
            }

            BufferedWriter(OutputStreamWriter(conn.outputStream, Charsets.UTF_8)).use { writer ->
                writer.write(postData)
                writer.flush()
            }

            val response = readResponse(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return@withContext Result.failure(
                    Exception(json.optString("message", "Erro ao cadastrar despesa"))
                )
            }

            Result.success(json.optString("message", "Despesa cadastrada com sucesso"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao enviar despesa: ${e.message}"))
        }
    }

    override suspend fun listContasPagar(
        mes: Int,
        ano: Int
    ): Result<List<ContaPagar>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiConfig.BASE_URL + "contas_pagar_list.php?mes=$mes&ano=$ano")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                doInput = true
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("Accept", "application/json")
                sessionCookie?.let { setRequestProperty("Cookie", it) }
            }

            val response = readResponse(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return@withContext Result.failure(
                    Exception(json.optString("message", "Erro ao listar contas"))
                )
            }

            val arr = json.optJSONArray("items")
            val items = mutableListOf<ContaPagar>()

            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)

                    items.add(
                        ContaPagar(
                            id = o.optInt("id", 0),
                            descricao = o.optString("descricao", "-"),
                            dataVencimento = o.optString("data_vencimento", "-"),
                            dataPagamento = o.optString("data_pagamento", "-"),
                            valor = o.optDouble("valor", 0.0),
                            valorPago = o.optDouble("valor_pago", 0.0),
                            saldoAberto = o.optDouble("saldo_aberto", 0.0),
                            status = o.optString("status", "pendente"),
                            categoria = o.optString("categoria", "-"),
                            fornecedorNome = o.optString("fornecedor_nome", "-")
                        )
                    )
                }
            }

            Result.success(items)
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao carregar contas: ${e.message}"))
        }
    }

    private fun readResponse(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }
}
