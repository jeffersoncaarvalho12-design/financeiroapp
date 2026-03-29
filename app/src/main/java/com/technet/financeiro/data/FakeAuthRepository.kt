package com.technet.financeiro.data

import com.technet.financeiro.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
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
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }

            val postData = "email=${URLEncoder.encode(email, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"

            BufferedWriter(OutputStreamWriter(conn.outputStream)).use {
                it.write(postData)
            }

            val setCookie = conn.headerFields["Set-Cookie"]
            if (!setCookie.isNullOrEmpty()) {
                sessionCookie = setCookie.joinToString("; ") { it.substringBefore(";") }
            }

            val response = read(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success")) {
                return@withContext Result.failure(Exception(json.optString("message")))
            }

            val u = json.getJSONObject("user")

            Result.success(User(u.getString("name"), u.getString("email")))

        } catch (e: Exception) {
            Result.failure(Exception(e.message))
        }
    }

    override suspend fun dashboardSummary(): DashboardSummary = DashboardSummary(0.0,0.0,0.0,0.0,0.0)

    override suspend fun createExpense(
        descricao: String,
        valor: String,
        vencimento: String,
        parcelas: Int,
        observacoes: String
    ): Result<String> = Result.success("ok")

    override suspend fun listContasPagar(mes: Int, ano: Int): Result<List<ContaPagar>> =
        Result.success(emptyList())

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
        ano: Int
    ): Result<List<ConciliacaoItem>> = withContext(Dispatchers.IO) {

        try {
            val url = URL(ApiConfig.BASE_URL + "conciliacao_list.php?mes=$mes&ano=$ano")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                sessionCookie?.let { setRequestProperty("Cookie", it) }
            }

            val response = read(conn)
            val json = JSONObject(response)

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
            Result.failure(Exception(e.message))
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
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).readText()
    }
}
