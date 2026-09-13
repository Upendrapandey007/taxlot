package com.taxlot.accounting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AccountingServiceApplication

fun main(args: Array<String>) {
    runApplication<AccountingServiceApplication>(*args)
}
