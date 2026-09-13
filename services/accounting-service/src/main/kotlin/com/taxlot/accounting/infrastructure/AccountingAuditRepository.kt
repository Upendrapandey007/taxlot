package com.taxlot.accounting.infrastructure

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AccountingAuditRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun record(organizationId: UUID, action: String, entityType: String, entityId: UUID, actorId: UUID?, details: String?) {
        val sql = """
            INSERT INTO accounting_audit_records (
                organization_id, action, entity_type, entity_id, actor_id, details
            ) VALUES (
                :organizationId, :action, :entityType, :entityId, :actorId, :details
            )
        """
        val params = MapSqlParameterSource()
            .addValue("organizationId", organizationId)
            .addValue("action", action)
            .addValue("entityType", entityType)
            .addValue("entityId", entityId)
            .addValue("actorId", actorId)
            .addValue("details", details)
        jdbcTemplate.update(sql, params)
    }
}
