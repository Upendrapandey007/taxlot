package com.taxlot.reporting.api

import com.taxlot.reporting.domain.*
import com.taxlot.reporting.service.FinancialReportService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

data class CreateSnapshotRequest(
    val reportType: ReportType,
    val periodId: UUID?,
    val fromDate: LocalDate,
    val toDate: LocalDate
)

@RestController
@RequestMapping("/api/v1/reports")
class FinancialReportsController(
    private val reportService: FinancialReportService
) {

    @GetMapping("/profit-and-loss")
    fun getProfitAndLoss(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?,
        @RequestParam(required = false) periodId: UUID?,
        @RequestParam(required = false, defaultValue = "false") snapshot: Boolean
    ): ResponseEntity<ProfitAndLossReport> {
        val today = LocalDate.now()
        val from = fromDate ?: today.withDayOfMonth(1)
        val to = toDate ?: today

        val report = reportService.getProfitAndLoss(organizationId, from, to, periodId, snapshot)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/balance-sheet")
    fun getBalanceSheet(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) asOfDate: LocalDate?,
        @RequestParam(required = false) periodId: UUID?,
        @RequestParam(required = false, defaultValue = "false") snapshot: Boolean
    ): ResponseEntity<BalanceSheetReport> {
        val asOf = asOfDate ?: LocalDate.now()
        val report = reportService.getBalanceSheet(organizationId, asOf, periodId, snapshot)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/cash-flow")
    fun getCashFlow(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?,
        @RequestParam(required = false) periodId: UUID?
    ): ResponseEntity<CashFlowReport> {
        val today = LocalDate.now()
        val from = fromDate ?: today.withDayOfMonth(1)
        val to = toDate ?: today

        val report = reportService.getCashFlow(organizationId, from, to, periodId)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/aging/ar")
    fun getArAging(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) asOfDate: LocalDate?
    ): ResponseEntity<ArAgingReport> {
        val asOf = asOfDate ?: LocalDate.now()
        val report = reportService.getArAging(organizationId, asOf)
        return ResponseEntity.ok(report)
    }

    @GetMapping("/kpis")
    fun getKpis(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) asOfDate: LocalDate?
    ): ResponseEntity<FinancialKpiSummary> {
        val asOf = asOfDate ?: LocalDate.now()
        val kpis = reportService.getKpis(organizationId, asOf)
        return ResponseEntity.ok(kpis)
    }

    @GetMapping("/snapshots")
    fun getSnapshots(
        @RequestHeader("X-Tenant-Id") organizationId: UUID,
        @RequestParam(required = false) reportType: ReportType?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fromDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) toDate: LocalDate?
    ): ResponseEntity<List<FinancialReportSnapshot>> {
        val snapshots = reportService.getSnapshots(organizationId, reportType, fromDate, toDate)
        return ResponseEntity.ok(snapshots)
    }
}
