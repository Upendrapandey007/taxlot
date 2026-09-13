package com.taxlot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class TaxlotApplication

fun main(args: Array<String>) {
    runApplication<TaxlotApplication>(*args)
}
