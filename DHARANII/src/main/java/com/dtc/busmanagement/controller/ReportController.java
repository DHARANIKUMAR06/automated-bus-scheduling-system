package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportController {

    @Autowired
    private ReportService reportService;

    // --- BUS REPORTS ---
    @GetMapping("/buses/excel")
    public ResponseEntity<InputStreamResource> downloadBusExcel() {
        ByteArrayInputStream in = reportService.generateBusReportExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=buses_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/buses/pdf")
    public ResponseEntity<InputStreamResource> downloadBusPdf() {
        ByteArrayInputStream in = reportService.generateBusReportPdf();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=buses_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    // --- DRIVER REPORTS ---
    @GetMapping("/drivers/excel")
    public ResponseEntity<InputStreamResource> downloadDriverExcel() {
        ByteArrayInputStream in = reportService.generateDriverReportExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=drivers_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/drivers/pdf")
    public ResponseEntity<InputStreamResource> downloadDriverPdf() {
        ByteArrayInputStream in = reportService.generateDriverReportPdf();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=drivers_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    // --- ROUTE REPORTS ---
    @GetMapping("/routes/excel")
    public ResponseEntity<InputStreamResource> downloadRouteExcel() {
        ByteArrayInputStream in = reportService.generateRouteReportExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=routes_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/routes/pdf")
    public ResponseEntity<InputStreamResource> downloadRoutePdf() {
        ByteArrayInputStream in = reportService.generateRouteReportPdf();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=routes_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    // --- SCHEDULE REPORTS ---
    @GetMapping("/schedules/excel")
    public ResponseEntity<InputStreamResource> downloadScheduleExcel() {
        ByteArrayInputStream in = reportService.generateScheduleReportExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=schedules_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/schedules/pdf")
    public ResponseEntity<InputStreamResource> downloadSchedulePdf() {
        ByteArrayInputStream in = reportService.generateScheduleReportPdf();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=schedules_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    // --- MAINTENANCE REPORTS ---
    @GetMapping("/maintenance/excel")
    public ResponseEntity<InputStreamResource> downloadMaintenanceExcel() {
        ByteArrayInputStream in = reportService.generateMaintenanceReportExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=maintenance_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/maintenance/pdf")
    public ResponseEntity<InputStreamResource> downloadMaintenancePdf() {
        ByteArrayInputStream in = reportService.generateMaintenanceReportPdf();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=maintenance_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }

    // --- DAILY OPERATIONS REPORTS ---
    @GetMapping("/operations/excel")
    public ResponseEntity<InputStreamResource> downloadOperationsExcel(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ByteArrayInputStream in = reportService.generateOperationsReportExcel(date);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=daily_operations_report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/operations/pdf")
    public ResponseEntity<InputStreamResource> downloadOperationsPdf(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ByteArrayInputStream in = reportService.generateOperationsReportPdf(date);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=daily_operations_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
