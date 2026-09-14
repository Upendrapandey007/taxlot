package com.taxlot.tax.scraper

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["taxlot.scraper.enabled"], havingValue = "true", matchIfMissing = true)
class ScheduledRegulatoryScraper(
    private val scraperService: RegulatoryScraperService
) {
    private val logger = LoggerFactory.getLogger(ScheduledRegulatoryScraper::class.java)

    @Scheduled(cron = "\${taxlot.scraper.cron:0 0 2 * * *}")
    fun runScheduledScrape() {
        logger.info("Executing scheduled regulatory tax scrape job...")
        val results = scraperService.scrapeAllSources()
        logger.info("Scheduled regulatory scrape finished with {} sources processed", results.size)
    }
}
