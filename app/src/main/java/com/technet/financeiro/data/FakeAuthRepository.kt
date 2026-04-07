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
        observacoes: String,
        categoriaId: Int,
        modoLancamento: String,
        qtdParcelas: Int,
        qtdRepeticoes: Int,
        fornecedorNome: String,
        formaPagamento: String,
        contaPagamento: String,
        marcarPago: Boolean,
        agendado: Boolean
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

            val fields = linkedMapOf(
                "descricao" to descricao,
                "valor" to valor,
                "vencimento" to vencimento,
                "observacoes" to observacoes,
                "categoria_id" to categoriaId.toString(),
                "modo_lancamento" to modoLancamento,
                "qtd_parcelas" to qtdParcelas.toString(),
                "qtd_repeticoes" to qtdRepeticoes.toString(),
                "fornecedor_nome" to fornecedorNome,
                "forma_pagamento" to formaPagamento,
                "conta_pagamento" to contaPagamento,
                "marcar_pago" to if (marcarPago) "1" else "0",
                "agendado" to if (agendado) "1" else "0",
                "data_competencia" to vencimento
            )

            val postData = fields.entries.joinToString("&") { entry ->
                URLEncoder.encode(entry.key, "UTF-8") + "=" + URLEncoder.encode(entry.value, "UTF-8")
            }

            BufferedWriter(OutputStreamWriter(conn.outputStream)).use {
                it.write(postData)
            }

            val response = read(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                return@withContext Result.failure(
                    Exception(json.optString("message", "Erro ao criar despesa"))
                )
            }

            Result.success(json.optString("message", "Despesa cadastrada com sucesso"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao criar despesa"))
        }
    }

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

            Result.success(parseContasPagar(json))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao listar contas a pagar"))
        }
    }

    override suspend fun searchContasPagarParaConciliacao(
        busca: String
    ): Result<List<ContaPagar>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                ApiConfig.BASE_URL + "contas_pagar_search.php?busca=" +
                    URLEncoder.encode(busca, "UTF-8")
            )

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
                    Exception(json.optString("message", "Erro ao buscar contas"))
                )
            }

            Result.success(parseContasPagar(json))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao buscar contas"))
        }
    }

    override suspend fun markContaAsPaid(
        contaId: Int,
        dataPagamento: String,
        observacoes: String,
        formaPagamento: String,
        contaPagamento: String,
        juros: String,
        desconto: String,
        movimentoExtratoId: Int?
    ): Result<String> = withContext(Dispatchers.IO) {
        val fields = linkedMapOf(
            "acao" to "informar_pagamento_total",
            "conta_id" to contaId.toString(),
            "data_pagamento_total" to dataPagamento,
            "observacoes_pagamento_total" to observacoes,
            "forma_pagamento" to formaPagamento,
            "conta_pagamento" to contaPagamento,
            "juros" to juros,
            "desconto" to desconto
        )
        movimentoExtratoId?.let { fields["movimento_extrato_id"] = it.toString() }
        postAction(fields)
    }

    override suspend fun registerContaPayment(
        contaId: Int,
        valor: String,
        dataPagamento: String,
        observacoes: String,
        formaPagamento: String,
        contaPagamento: String,
        juros: String,
        desconto: String,
        movimentoExtratoId: Int?
    ): Result<ContaPagarPaymentResult> = withContext(Dispatchers.IO) {
        try {
            val fields = linkedMapOf(
                "acao" to "informar_pagamento_parcial",
                "conta_id" to contaId.toString(),
                "valor_pagamento_parcial" to valor,
                "data_pagamento_parcial" to dataPagamento,
                "observacoes_pagamento_parcial" to observacoes,
                "forma_pagamento" to formaPagamento,
                "conta_pagamento" to contaPagamento,
                "juros" to juros,
                "desconto" to desconto
            )
            movimentoExtratoId?.let { fields["movimento_extrato_id"] = it.toString() }

            val json = postActionJson(fields)

            if (!json.optBoolean("success", false)) {
                return@withContext Result.failure(
                    Exception(json.optString("message", "Erro ao registrar pagamento parcial"))
                )
            }

            val payload = json.optJSONObject("payload")
            Result.success(
                ContaPagarPaymentResult(
                    contaId = contaId,
                    valorPago = payload?.optDouble("valor_pago", 0.0) ?: 0.0,
                    saldoAberto = payload?.optDouble("saldo_aberto", 0.0) ?: 0.0,
                    status = payload?.optString("status", "parcial") ?: "parcial",
                    dataPagamento = payload?.optString("data_pagamento", dataPagamento) ?: dataPagamento
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao registrar pagamento parcial"))
        }
    }

    override suspend fun updateContaDueDate(
        contaId: Int,
        dataVencimento: String
    ): Result<String> = withContext(Dispatchers.IO) {
        postAction(
            mapOf(
                "acao" to "alterar_vencimento",
                "conta_id" to contaId.toString(),
                "data_vencimento" to dataVencimento
            )
        )
    }

    override suspend fun updateContaLaunch(
        contaId: Int,
        descricao: String,
        fornecedorNome: String,
        valor: String,
        dataVencimento: String
    ): Result<String> = withContext(Dispatchers.IO) {
        postAction(
            mapOf(
                "acao" to "editar_lancamento",
                "conta_id" to contaId.toString(),
                "descricao" to descricao,
                "fornecedor_nome" to fornecedorNome,
                "valor" to valor,
                "data_vencimento" to dataVencimento
            )
        )
    }

    override suspend fun updateContaPaymentDate(
        contaId: Int,
        dataPagamento: String
    ): Result<String> = withContext(Dispatchers.IO) {
        postAction(
            mapOf(
                "acao" to "alterar_data_pagamento",
                "conta_id" to contaId.toString(),
                "data_pagamento" to dataPagamento
            )
        )
    }

    override suspend fun deleteConta(contaId: Int): Result<String> = withContext(Dispatchers.IO) {
        postAction(
            mapOf(
                "acao" to "excluir_lancamento",
                "conta_id" to contaId.toString()
            )
        )
    }

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
    ): Result<String> = conciliarMovimentoTotal(
        movimentoId = movimentoId,
        contaId = contaId,
        dataPagamento = "",
        observacoes = ""
    )

    override suspend fun conciliarMovimentoTotal(
        movimentoId: Int,
        contaId: Int,
        dataPagamento: String,
        observacoes: String
    ): Result<String> = withContext(Dispatchers.IO) {
        postConciliacaoAction(
            mapOf(
                "acao" to "conciliar_total",
                "movimento_id" to movimentoId.toString(),
                "conta_id" to contaId.toString(),
                "data_pagamento" to if (dataPagamento.isBlank()) hojeIso() else dataPagamento,
                "observacoes" to observacoes
            )
        )
    }

    override suspend fun conciliarMovimentoParcial(
        movimentoId: Int,
        contaId: Int,
        valorPagamento: String,
        dataPagamento: String,
        observacoes: String
    ): Result<String> = withContext(Dispatchers.IO) {
        postConciliacaoAction(
            mapOf(
                "acao" to "conciliar_parcial",
                "movimento_id" to movimentoId.toString(),
                "conta_id" to contaId.toString(),
                "valor_pagamento" to valorPagamento,
                "data_pagamento" to if (dataPagamento.isBlank()) hojeIso() else dataPagamento,
                "observacoes" to observacoes
            )
        )
    }

    override suspend fun listCategorias(): Result<List<CategoriaItem>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiConfig.BASE_URL + "categorias_list.php")

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
                    Exception(json.optString("message", "Erro ao carregar categorias"))
                )
            }

            val list = mutableListOf<CategoriaItem>()
            val arr = json.optJSONArray("items")

            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        CategoriaItem(
                            id = o.optInt("id"),
                            nome = o.optString("nome")
                        )
                    )
                }
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao carregar categorias"))
        }
    }

    override suspend fun criarDespesaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        postConciliacaoAction(
            mapOf(
                "acao" to "criar_despesa",
                "movimento_id" to movimentoId.toString(),
                "descricao" to descricao,
                "valor" to valor,
                "vencimento" to vencimento,
                "categoria_id" to categoriaId.toString(),
                "observacoes" to observacoes,
                "conciliar_apos_criar" to if (conciliarAposCriar) "1" else "0"
            )
        )
    }

    override suspend fun criarReceitaDaConciliacao(
        descricao: String,
        valor: String,
        vencimento: String,
        categoriaId: Int,
        observacoes: String,
        movimentoId: Int,
        conciliarAposCriar: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        postConciliacaoAction(
            mapOf(
                "acao" to "criar_receita_scm",
                "movimento_id" to movimentoId.toString(),
                "descricao" to descricao,
                "valor" to valor,
                "vencimento" to vencimento,
                "categoria_id" to categoriaId.toString(),
                "observacoes" to observacoes,
                "conciliar_apos_criar" to if (conciliarAposCriar) "1" else "0"
            )
        )
    }

    private fun parseContasPagar(json: JSONObject): List<ContaPagar> {
        val list = mutableListOf<ContaPagar>()
        val arr = json.optJSONArray("items")

        if (arr != null) {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)

                list.add(
                    ContaPagar(
                        id = o.optInt("id"),
                        descricao = o.optString("descricao"),
                        dataVencimento = o.optString("data_vencimento"),
                        dataPagamento = o.optString("data_pagamento", ""),
                        valor = o.optDouble("valor"),
                        valorPago = o.optDouble("valor_pago", 0.0),
                        saldoAberto = o.optDouble("saldo_aberto", 0.0),
                        status = o.optString("status"),
                        categoria = o.optString("categoria", "-"),
                        fornecedorNome = o.optString("fornecedor_nome", "-")
                    )
                )
            }
        }

        return list
    }

    private fun postAction(fields: Map<String, String>): Result<String> {
        return try {
            val json = postActionJson(fields)
            if (!json.optBoolean("success", false)) {
                Result.failure(Exception(json.optString("message", "Erro ao processar ação")))
            } else {
                Result.success(json.optString("message", "Operação realizada com sucesso"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao processar ação"))
        }
    }

    private fun postActionJson(fields: Map<String, String>): JSONObject {
        val url = URL(ApiConfig.BASE_URL + "contas_pagar_action.php")
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

        val postData = fields.entries.joinToString("&") { entry ->
            URLEncoder.encode(entry.key, "UTF-8") + "=" + URLEncoder.encode(entry.value, "UTF-8")
        }

        BufferedWriter(OutputStreamWriter(conn.outputStream)).use {
            it.write(postData)
        }

        val response = read(conn)
        return JSONObject(response)
    }

    private fun postConciliacaoAction(fields: Map<String, String>): Result<String> {
        return try {
            val url = URL(ApiConfig.BASE_URL + "conciliacao_action.php")
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

            val postData = fields.entries.joinToString("&") { entry ->
                URLEncoder.encode(entry.key, "UTF-8") + "=" + URLEncoder.encode(entry.value, "UTF-8")
            }

            BufferedWriter(OutputStreamWriter(conn.outputStream)).use {
                it.write(postData)
            }

            val response = read(conn)
            val json = JSONObject(response)

            if (!json.optBoolean("success", false)) {
                Result.failure(Exception(json.optString("message", "Erro ao conciliar movimento")))
            } else {
                Result.success(json.optString("message", "Movimento conciliado com sucesso"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao conciliar movimento"))
        }
    }

    private fun read(conn: HttpURLConnection): String {
        val stream =
            if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream

        return BufferedReader(InputStreamReader(stream)).use { reader ->
            reader.readText()
        }
    }

    private fun hojeIso(): String {
        val hoje = java.time.LocalDate.now()
        return hoje.toString()
    }
}
