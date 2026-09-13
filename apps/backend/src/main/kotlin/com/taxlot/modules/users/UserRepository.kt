package com.taxlot.modules.users

import java.util.UUID

interface UserRepository {
    fun findByEmail(email: String): User?
    fun findById(id: UUID): User?
    fun save(user: User): User
    fun existsByEmail(email: String): Boolean
}
