package com.taxlot.customer.infrastructure.repository

import com.taxlot.customer.domain.*
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class CustomerRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        Customer(
            id = UUID.fromString(rs.getString("id")),
            organizationId = UUID.fromString(rs.getString("organization_id")),
            name = rs.getString("name"),
            email = rs.getString("email"),
            phone = rs.getString("phone"),
            companyName = rs.getString("company_name"),
            taxNumber = rs.getString("tax_number"),
            currencyCode = rs.getString("currency_code"),
            status = CustomerStatus.valueOf(rs.getString("status")),
            notes = rs.getString("notes"),
            createdAt = rs.getObject("created_at", OffsetDateTime::class.java),
            updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java)
        )
    }

    fun save(customer: Customer): Customer {
        val sql = """
            INSERT INTO customers (id, organization_id, name, email, phone, company_name, tax_number, currency_code, status, notes, created_at, updated_at)
            VALUES (:id, :organizationId, :name, :email, :phone, :companyName, :taxNumber, :currencyCode, :status::customer_status, :notes, :createdAt, :updatedAt)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                email = EXCLUDED.email,
                phone = EXCLUDED.phone,
                company_name = EXCLUDED.company_name,
                tax_number = EXCLUDED.tax_number,
                currency_code = EXCLUDED.currency_code,
                status = EXCLUDED.status,
                notes = EXCLUDED.notes,
                updated_at = EXCLUDED.updated_at
        """
        val params = MapSqlParameterSource()
            .addValue("id", customer.id)
            .addValue("organizationId", customer.organizationId)
            .addValue("name", customer.name)
            .addValue("email", customer.email)
            .addValue("phone", customer.phone)
            .addValue("companyName", customer.companyName)
            .addValue("taxNumber", customer.taxNumber)
            .addValue("currencyCode", customer.currencyCode)
            .addValue("status", customer.status.name)
            .addValue("notes", customer.notes)
            .addValue("createdAt", customer.createdAt)
            .addValue("updatedAt", customer.updatedAt)
        jdbcTemplate.update(sql, params)
        return customer
    }

    fun findByIdAndOrganizationId(id: UUID, organizationId: UUID): Customer? {
        val sql = "SELECT * FROM customers WHERE id = :id AND organization_id = :organizationId"
        return jdbcTemplate.query(sql, MapSqlParameterSource("id", id).addValue("organizationId", organizationId), rowMapper).firstOrNull()
    }

    fun findAllByOrganizationId(organizationId: UUID, search: String?): List<Customer> {
        val sql = if (search != null) {
            "SELECT * FROM customers WHERE organization_id = :orgId AND name ILIKE :search ORDER BY created_at DESC"
        } else {
            "SELECT * FROM customers WHERE organization_id = :orgId ORDER BY created_at DESC"
        }
        val params = MapSqlParameterSource("orgId", organizationId)
        if (search != null) {
            params.addValue("search", "%$search%")
        }
        return jdbcTemplate.query(sql, params, rowMapper)
    }
}

@Repository
class CustomerAddressRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun saveAll(addresses: List<CustomerAddress>) {
        if (addresses.isEmpty()) return
        val sql = """
            INSERT INTO customer_addresses (id, customer_id, organization_id, type, line1, line2, city, state, postal_code, country_code, created_at)
            VALUES (:id, :customerId, :organizationId, :type::address_type, :line1, :line2, :city, :state, :postalCode, :countryCode, :createdAt)
        """
        val batchParams = addresses.map { addr ->
            MapSqlParameterSource()
                .addValue("id", addr.id)
                .addValue("customerId", addr.customerId)
                .addValue("organizationId", addr.organizationId)
                .addValue("type", addr.type.name)
                .addValue("line1", addr.line1)
                .addValue("line2", addr.line2)
                .addValue("city", addr.city)
                .addValue("state", addr.state)
                .addValue("postalCode", addr.postalCode)
                .addValue("countryCode", addr.countryCode)
                .addValue("createdAt", addr.createdAt)
        }.toTypedArray()
        jdbcTemplate.batchUpdate(sql, batchParams)
    }
    
    fun findByCustomerId(customerId: UUID): List<CustomerAddress> {
        val sql = "SELECT * FROM customer_addresses WHERE customer_id = :customerId"
        return jdbcTemplate.query(sql, MapSqlParameterSource("customerId", customerId)) { rs, _ ->
            CustomerAddress(
                id = UUID.fromString(rs.getString("id")),
                customerId = UUID.fromString(rs.getString("customer_id")),
                organizationId = UUID.fromString(rs.getString("organization_id")),
                type = AddressType.valueOf(rs.getString("type")),
                line1 = rs.getString("line1"),
                line2 = rs.getString("line2"),
                city = rs.getString("city"),
                state = rs.getString("state"),
                postalCode = rs.getString("postal_code"),
                countryCode = rs.getString("country_code"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }
    }
    
    fun deleteByCustomerId(customerId: UUID) {
        jdbcTemplate.update("DELETE FROM customer_addresses WHERE customer_id = :customerId", MapSqlParameterSource("customerId", customerId))
    }
}

@Repository
class CustomerContactRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {
    fun saveAll(contacts: List<CustomerContact>) {
        if (contacts.isEmpty()) return
        val sql = """
            INSERT INTO customer_contacts (id, customer_id, organization_id, name, email, phone, is_primary, created_at)
            VALUES (:id, :customerId, :organizationId, :name, :email, :phone, :isPrimary, :createdAt)
        """
        val batchParams = contacts.map { contact ->
            MapSqlParameterSource()
                .addValue("id", contact.id)
                .addValue("customerId", contact.customerId)
                .addValue("organizationId", contact.organizationId)
                .addValue("name", contact.name)
                .addValue("email", contact.email)
                .addValue("phone", contact.phone)
                .addValue("isPrimary", contact.isPrimary)
                .addValue("createdAt", contact.createdAt)
        }.toTypedArray()
        jdbcTemplate.batchUpdate(sql, batchParams)
    }
    
    fun findByCustomerId(customerId: UUID): List<CustomerContact> {
        val sql = "SELECT * FROM customer_contacts WHERE customer_id = :customerId"
        return jdbcTemplate.query(sql, MapSqlParameterSource("customerId", customerId)) { rs, _ ->
            CustomerContact(
                id = UUID.fromString(rs.getString("id")),
                customerId = UUID.fromString(rs.getString("customer_id")),
                organizationId = UUID.fromString(rs.getString("organization_id")),
                name = rs.getString("name"),
                email = rs.getString("email"),
                phone = rs.getString("phone"),
                isPrimary = rs.getBoolean("is_primary"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java)
            )
        }
    }
    
    fun deleteByCustomerId(customerId: UUID) {
        jdbcTemplate.update("DELETE FROM customer_contacts WHERE customer_id = :customerId", MapSqlParameterSource("customerId", customerId))
    }
}
