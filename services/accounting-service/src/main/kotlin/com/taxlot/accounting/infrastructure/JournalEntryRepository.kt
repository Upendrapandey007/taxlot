package com.taxlot.accounting.infrastructure

import com.taxlot.accounting.domain.EntrySourceType
import com.taxlot.accounting.domain.EntryStatus
import com.taxlot.accounting.domain.JournalEntry
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class JournalEntryRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        JournalEntry(
            id = rs.getObject("id", UUID::class.java),
            organizationId = rs.getObject("organization_id", UUID::class.java),
            periodId = rs.getObject("period_id", UUID::class.java),
            entryNumber = rs.getString("entry_number"),
            entryDate = rs.getObject("entry_date", LocalDate::class.java),
            reference = rs.getString("reference"),
            description = rs.getString("description"),
            sourceType = EntrySourceType.valueOf(rs.getString("source_type")),
            sourceId = rs.getObject("source_id", UUID::class.java),
            status = EntryStatus.valueOf(rs.getString("status")),
            reversedByEntryId = rs.getObject("reversed_by_entry_id", UUID::class.java),
            createdBy = rs.getObject("created_by", UUID::class.java),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            postedAt = rs.getObject("posted_at", OffsetDateTime::class.java)
        )
    }

    fun save(entry: JournalEntry): JournalEntry {
        val sql = """
            INSERT INTO journal_entries (
                id, organization_id, period_id, entry_number, entry_date, reference, description, source_type, source_id, status, reversed_by_entry_id, created_by, created_at, posted_at
            ) VALUES (
                :id, :organizationId, :periodId, :entryNumber, :entryDate, :reference, :description, :sourceType::entry_source_type, :sourceId, :status::entry_status, :reversedByEntryId, :createdBy, :createdAt, :postedAt
            )
        """
        val params = MapSqlParameterSource()
            .addValue("id", entry.id)
            .addValue("organizationId", entry.organizationId)
            .addValue("periodId", entry.periodId)
            .addValue("entryNumber", entry.entryNumber)
            .addValue("entryDate", entry.entryDate)
            .addValue("reference", entry.reference)
            .addValue("description", entry.description)
            .addValue("sourceType", entry.sourceType.name)
            .addValue("sourceId", entry.sourceId)
            .addValue("status", entry.status.name)
            .addValue("reversedByEntryId", entry.reversedByEntryId)
            .addValue("createdBy", entry.createdBy)
            .addValue("createdAt", entry.createdAt)
            .addValue("postedAt", entry.postedAt)
        jdbcTemplate.update(sql, params)
        return entry
    }

    fun findById(id: UUID, organizationId: UUID): JournalEntry? {
        val sql = "SELECT * FROM journal_entries WHERE id = :id AND organization_id = :organizationId"
        val params = MapSqlParameterSource().addValue("id", id).addValue("organizationId", organizationId)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }
    
    fun findByEntryNumber(organizationId: UUID, entryNumber: String): JournalEntry? {
        val sql = "SELECT * FROM journal_entries WHERE organization_id = :organizationId AND entry_number = :entryNumber"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId).addValue("entryNumber", entryNumber)
        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    fun listByOrganization(organizationId: UUID, page: Int, size: Int): List<JournalEntry> {
        val sql = "SELECT * FROM journal_entries WHERE organization_id = :organizationId ORDER BY posted_at DESC LIMIT :limit OFFSET :offset"
        val params = MapSqlParameterSource()
            .addValue("organizationId", organizationId)
            .addValue("limit", size)
            .addValue("offset", page * size)
        return jdbcTemplate.query(sql, params, rowMapper)
    }

    fun countByOrganization(organizationId: UUID): Long {
        val sql = "SELECT COUNT(*) FROM journal_entries WHERE organization_id = :organizationId"
        val params = MapSqlParameterSource().addValue("organizationId", organizationId)
        return jdbcTemplate.queryForObject(sql, params, Long::class.java) ?: 0L
    }

    fun updateStatus(id: UUID, organizationId: UUID, status: EntryStatus, reversedByEntryId: UUID?) {
        val sql = "UPDATE journal_entries SET status = :status::entry_status, reversed_by_entry_id = :reversedByEntryId WHERE id = :id AND organization_id = :organizationId"
        val params = MapSqlParameterSource()
            .addValue("id", id)
            .addValue("organizationId", organizationId)
            .addValue("status", status.name)
            .addValue("reversedByEntryId", reversedByEntryId)
        jdbcTemplate.update(sql, params)
    }

    fun getNextEntryNumber(organizationId: UUID, year: Int): String {
        val pattern = "JE-$year-%"
        val sql = "SELECT entry_number FROM journal_entries WHERE organization_id = :organizationId AND entry_number LIKE :pattern ORDER BY entry_number DESC LIMIT 1"
        val params = MapSqlParameterSource()
            .addValue("organizationId", organizationId)
            .addValue("pattern", pattern)
        val lastNumber = jdbcTemplate.queryForList(sql, params, String::class.java).firstOrNull()
        
        val sequence = if (lastNumber != null) {
            val parts = lastNumber.split("-")
            if (parts.size == 3) {
                parts[2].toIntOrNull() ?: 0
            } else 0
        } else 0
        
        return "JE-$year-${String.format("%04d", sequence + 1)}"
    }
}
