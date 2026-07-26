package com.dtc.busmanagement.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

public interface ReportService {
    ByteArrayInputStream generateBusReportExcel();
    ByteArrayInputStream generateBusReportPdf();
    
    ByteArrayInputStream generateDriverReportExcel();
    ByteArrayInputStream generateDriverReportPdf();
    
    ByteArrayInputStream generateRouteReportExcel();
    ByteArrayInputStream generateRouteReportPdf();
    
    ByteArrayInputStream generateScheduleReportExcel();
    ByteArrayInputStream generateScheduleReportPdf();
    
    ByteArrayInputStream generateMaintenanceReportExcel();
    ByteArrayInputStream generateMaintenanceReportPdf();
    
    ByteArrayInputStream generateOperationsReportExcel(LocalDate date);
    ByteArrayInputStream generateOperationsReportPdf(LocalDate date);
}
