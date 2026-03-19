package com.technet.financeiro.data

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

            val success = json.optBoolean("success", false)
            if (!success) {
                val message = json.optString("message", "Falha no login")
                return@withContext Result.failure(Exception(message))
            }

            val userJson = json.optJSONObject("user")
                ?: return@withContext Result.failure(Exception("Usuário não retornado pela API"))

            val user = User(
                name = userJson.optString("name", ""),
                email = userJson.optString("email", email)
            )

            Result.success(user)
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
                sessionCookie?.let {
                    setRequestProperty("Cookie", it)
                }
            }

            val response = readResponse(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return@withContext DashboardSummary(
                    receitaScm = 0.0,
                    combustivel = 0.0,
                    manutencao = 0.0,
                    energia = 0.0,
                    agua = 0.0
                )
            }

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

    private fun readResponse(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            reader.readText()
        }
    }
}
