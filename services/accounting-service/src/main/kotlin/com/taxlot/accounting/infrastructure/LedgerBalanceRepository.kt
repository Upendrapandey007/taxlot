package com.taxlot.accounting.infrastructure

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
class LedgerBalanceRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun updateBalance(organizationId: UUID, accountId: UUID, periodId: UUID, debit: BigDecimal, credit: BigDecimal) {
        val sql = """
            INSERT INTO ledger_balances (organization_id, account_id, period_id, debit_total, credit_total, net_balance)
            VALUES (:organizationId, :accountId, :periodId, :debit, :credit, :debit - :credit)
            ON CONFLICT (organization_id, account_id, period_id) DO UPDATE SET
                debit_total = ledger_balances.debit_total + EXCLUDED.debit_total,
                credit_total = ledger_balances.credit_total + EXCLUDED.credit_total,
                net_balance = ledger_balances.net_balance + EXCLUDED.net_balance,
                updated_at = NOW()
        """
        val params = MapSqlParameterSource()
            .addValue("organizationId", organizationId)
            .addValue("accountId", accountId)
            .addValue("periodId", periodId)
            .addValue("debit", debit)
            .addValue("credit", credit)
        jdbcTemplate.update(sql, params)
    }

    fun getTrialBalance(organizationId: UUID, periodId: UUID): List<Map<String, Any>> {
        val sql = """
            SELECT a.id as account_id, a.code, a.name, a.type, a.normal_balance, 
                   COALESCE(lb.debit_total, 0) as debit_total, 
                   COALESCE(lb.credit_total, 0) as credit_total, 
                   COALESCE(lb.net_balance, 0) as net_balance
            FROM accounts a
            LEFT JOIN ledger_balances lb ON a.id = lb.account_id AND lb.period_id = :periodId
            WHERE a.organization_id = :organizationId
            ORDER BY a.code
        """
        val params = MapSqlParameterSource()
            .addValue("organizationId", organizationId)
            .addValue("periodId", periodId)
        return jdbcTemplate.queryForList(sql, params)
    }
}
