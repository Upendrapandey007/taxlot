package com.taxlot.organization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OrganizationServiceApplication

fun main(args: Array<String>) {
    runApplication<OrganizationServiceApplication>(*args)
}
