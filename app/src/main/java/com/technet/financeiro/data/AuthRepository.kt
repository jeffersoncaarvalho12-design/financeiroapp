package com.technet.financeiro.data

import com.technet.financeiro.model.DashboardSummary
import com.technet.financeiro.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun dashboardSummary(): DashboardSummary
}
