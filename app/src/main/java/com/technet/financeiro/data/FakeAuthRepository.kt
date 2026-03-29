package com.technet.financeiro.data

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class FakeAuthRepository {

    data class ConciliacaoItem(
        val id: Int,
        val descricao: String,
        val valor: Double,
        val tipo: String,
        val data: String,
        val origem: String,
        val status: String,
        val contaId: Int,
        val conciliadoEm: String
    )

    fun listarConciliacao(
        mes: Int,
        ano: Int,
        busca: String = "",
        status: String = "",
        tipo: String = ""
    ): List<ConciliacaoItem> {

        val lista = mutableListOf<ConciliacaoItem>()

        try {
            val baseUrl = ApiConfig.BASE_URL + "conciliacao_list.php"

            val params = StringBuilder()
            params.append("mes=$mes&ano=$ano")

            if (busca.isNotBlank()) {
                params.append("&busca=${URLEncoder.encode(busca, "UTF-8")}")
            }

            if (status.isNotBlank()) {
                params.append("&status=${URLEncoder.encode(status, "UTF-8")}")
            }

            if (tipo.isNotBlank()) {
                params.append("&tipo=${URLEncoder.encode(tipo, "UTF-8")}")
            }

            val fullUrl = "$baseUrl?$params"

            Log.d("API_DEBUG", "URL: $fullUrl")

            val url = URL(fullUrl)
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("Accept", "application/json")

            val responseCode = conn.responseCode
            Log.d("API_DEBUG", "Response code: $responseCode")

            val inputStream = if (responseCode in 200..299) {
                conn.inputStream
            } else {
                conn.errorStream
            }

            val response = inputStream.bufferedReader().use { it.readText() }

            Log.d("API_DEBUG", "Response: $response")

            val json = JSONObject(response)

            if (json.getBoolean("success")) {
                val items = json.getJSONArray("items")

                for (i in 0 until items.length()) {
                    val obj = items.getJSONObject(i)

                    val item = ConciliacaoItem(
                        id = obj.optInt("id"),
                        descricao = obj.optString("descricao"),
                        valor = obj.optDouble("valor"),
                        tipo = obj.optString("tipo"),
                        data = obj.optString("data"),
                        origem = obj.optString("origem"),
                        status = obj.optString("status"),
                        contaId = obj.optInt("conta_id"),
                        conciliadoEm = obj.optString("conciliado_em")
                    )

                    lista.add(item)
                }
            } else {
                Log.e("API_ERROR", "Erro API: ${json.optString("message")}")
            }

        } catch (e: Exception) {
            Log.e("API_ERROR", "Erro conexão: ${e.message}")
            e.printStackTrace()
        }

        return lista
    }
}
