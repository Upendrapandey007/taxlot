package com.taxlot.accounting.infrastructure

import com.taxlot.accounting.domain.Account
import com.taxlot.accounting.domain.AccountType
import com.taxlot.accounting.domain.NormalBalance
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class AccountRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        Account(
            id = rs.getObject("id", UUID::class.java),
            organizationId = rs.getObject("organization_id", UUID::class.java),
            code = rs.getString("code"),
            name = rs.getString("name"),
            type = AccountType.valueOf(rs.getString("type")),
            normalBalance = NormalBalance.valueOf(rs.getString("normal_balance")),
            parentId = rs.getObject("parent_id", UUID::class.java),
            isSystem = rs.getBoolean("is_system"),
            isActive = rs.getBoolean("is_active"),
            description = rs.getString("description"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    fun save(account: Account): Account {
        val sql = """
            INSERT INTO accounts (
                id, organization_id, code, name, type, normal_balance, parent_id, is_system, is_active, description, created_at, updated_at
            ) VALUES (
                :id, :organizationId, :code, :name, :type::account_type, :normalBalance::normal_balance, :parentId, :isSystem, :isActive, :description, :createdAt, :updatedAt
            ) ON CONFLICT (organization_id, code) DO UPDATE SET
                name = EXCLUDED.name,
                type = EXCLUDED.type,
                normal_balance = EXCLUDED.normal_balance,
                parent_id = EXCLUDED.parent_id,
                is_system = EXCLUDED.is_system,
                is_active = EXCLUDED.is_active,
                description = EXCLUDED.description,
                updated_at = EXCLUDED.updated_at
        """
        jdbcTemplate.update(sql, toParams(account))
        return account
    }

    fun findById(id: UUID, organizationId: UUID): Account? {
        val sql = "SELECT * FROM accounts WHERE id = :id AND organization_id = :organizationId"
        val params = MapSqlParameterSource().addValue("id", id).addValue("organizationId", organizationId)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    fun findByCode(organizationId: UUID, code: String): Account? {
        val sql = "SELECT * FROM accounts WHERE organization_id = :organizationId AND code = :code"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId).addValue("code", code)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    fun listByOrganization(organizationId: UUID): List<Account> {
        val sql = "SELECT * FROM accounts WHERE organization_id = :organizationId ORDER BY code"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId)
        return jdbcTemplate.query(sql, params, rowMapper)
    }

    fun existsByCode(organizationId: UUID, code: String): Boolean {
        val sql = "SELECT COUNT(*) FROM accounts WHERE organization_id = :organizationId AND code = :code"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId).addValue("code", code)
        return (jdbcTemplate.queryForObject(sql, params, Int::class.java) ?: 0) > 0
    }

    fun saveAll(accounts: List<Account>) {
        val sql = """
            INSERT INTO accounts (
                id, organization_id, code, name, type, normal_balance, parent_id, is_system, is_active, description, created_at, updated_at
            ) VALUES (
                :id, :organizationId, :code, :name, :type::account_type, :normalBalance::normal_balance, :parentId, :isSystem, :isActive, :description, :createdAt, :updatedAt
            ) ON CONFLICT (organization_id, code) DO NOTHING
        """
        val batchParams = accounts.map { toParams(it) }.toTypedArray()
        jdbcTemplate.batchUpdate(sql, batchParams)
    }

    private fun toParams(account: Account): MapSqlParameterSource {
        return MapSqlParameterSource()
            .addValue("id", account.id)
            .addValue("organizationId", account.organizationId)
            .addValue("code", account.code)
            .addValue("name", account.name)
            .addValue("type", account.type.name)
            .addValue("normalBalance", account.normalBalance.name)
            .addValue("parentId", account.parentId)
            .addValue("isSystem", account.isSystem)
            .addValue("isActive", account.isActive)
            .addValue("description", account.description)
            .addValue("createdAt", account.createdAt)
            .addValue("updatedAt", account.updatedAt)
    }
}
