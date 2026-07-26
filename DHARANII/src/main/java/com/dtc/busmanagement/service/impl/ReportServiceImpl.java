package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.ReportService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MaintenanceRecordRepository maintenanceRepository;

    @Autowired
    private TripRepository tripRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // Colors for PDF styling
    private static final Color DTC_PRIMARY = new Color(0, 94, 184); // DTC Blue
    private static final Color DTC_SECONDARY = new Color(74, 85, 104);
    private static final Color WHITE = Color.WHITE;

    // --- HELPER METHODS FOR EXCEL ---
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    // --- BUS REPORTS ---
    @Override
    public ByteArrayInputStream generateBusReportExcel() {
        List<Bus> buses = busRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Buses Fleet");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Bus Number", "Model", "Capacity", "Fuel Type", "Status", "Depot", "Mileage (KM)", "Last Service"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Bus bus : buses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(bus.getId());
                row.createCell(1).setCellValue(bus.getBusNumber());
                row.createCell(2).setCellValue(bus.getModel());
                row.createCell(3).setCellValue(bus.getCapacity());
                row.createCell(4).setCellValue(bus.getFuelType().name());
                row.createCell(5).setCellValue(bus.getStatus().name());
                row.createCell(6).setCellValue(bus.getDepot() != null ? bus.getDepot().getName() : "Unassigned");
                row.createCell(7).setCellValue(bus.getMileageKm());
                row.createCell(8).setCellValue(bus.getLastServiceDate() != null ? bus.getLastServiceDate().format(DATE_FORMAT) : "N/A");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to import data to Excel file: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream generateBusReportPdf() {
        List<Bus> buses = busRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Add PDF Title
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, DTC_PRIMARY);
            Paragraph title = new Paragraph("Delhi Transport Corporation (DTC)\nBus Fleet Audit Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph("Report Generated On: " + LocalDate.now().format(DATE_FORMAT) + "\n\n", 
                    new Font(Font.HELVETICA, 10, Font.ITALIC, DTC_SECONDARY));
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            // Create Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            String[] headers = {"ID", "Bus Number", "Model", "Capacity", "Fuel Type", "Status", "Depot"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, WHITE);
            
            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(col, headerFont));
                cell.setBackgroundColor(DTC_PRIMARY);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font rowFont = new Font(Font.HELVETICA, 9);
            for (Bus bus : buses) {
                table.addCell(new PdfPCell(new Paragraph(bus.getId().toString(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(bus.getBusNumber(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(bus.getModel(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(String.valueOf(bus.getCapacity()), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(bus.getFuelType().name(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(bus.getStatus().name(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(bus.getDepot() != null ? bus.getDepot().getName() : "N/A", rowFont)));
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate bus PDF report: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // --- DRIVER REPORTS ---
    @Override
    public ByteArrayInputStream generateDriverReportExcel() {
        List<Driver> drivers = driverRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Drivers Directory");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Name", "License Number", "License Expiry", "Experience (Years)", "Status", "Depot", "Phone"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Driver driver : drivers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(driver.getId());
                row.createCell(1).setCellValue(driver.getUser().getFullName());
                row.createCell(2).setCellValue(driver.getLicenseNumber());
                row.createCell(3).setCellValue(driver.getLicenseExpiryDate().format(DATE_FORMAT));
                row.createCell(4).setCellValue(driver.getExperienceYears());
                row.createCell(5).setCellValue(driver.getStatus().name());
                row.createCell(6).setCellValue(driver.getDepot() != null ? driver.getDepot().getName() : "N/A");
                row.createCell(7).setCellValue(driver.getUser().getPhone() != null ? driver.getUser().getPhone() : "N/A");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate driver Excel report: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream generateDriverReportPdf() {
        List<Driver> drivers = driverRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, DTC_PRIMARY);
            Paragraph title = new Paragraph("Delhi Transport Corporation (DTC)\nDriver Registry Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph("Generated on: " + LocalDate.now().format(DATE_FORMAT) + "\n\n", 
                    new Font(Font.HELVETICA, 10, Font.ITALIC, DTC_SECONDARY));
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            
            String[] headers = {"ID", "Name", "License Number", "License Expiry", "Status", "Depot"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, WHITE);

             for (String col : headers) {
                 PdfPCell cell = new PdfPCell(new Paragraph(col, headerFont));
                 cell.setBackgroundColor(DTC_PRIMARY);
                 cell.setPadding(6);
                 cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                 table.addCell(cell);
             }

             Font rowFont = new Font(Font.HELVETICA, 9);
             for (Driver driver : drivers) {
                 table.addCell(new PdfPCell(new Paragraph(driver.getId().toString(), rowFont)));
                 table.addCell(new PdfPCell(new Paragraph(driver.getUser().getFullName(), rowFont)));
                 table.addCell(new PdfPCell(new Paragraph(driver.getLicenseNumber(), rowFont)));
                 table.addCell(new PdfPCell(new Paragraph(driver.getLicenseExpiryDate().format(DATE_FORMAT), rowFont)));
                 table.addCell(new PdfPCell(new Paragraph(driver.getStatus().name(), rowFont)));
                 table.addCell(new PdfPCell(new Paragraph(driver.getDepot() != null ? driver.getDepot().getName() : "N/A", rowFont)));
             }

             document.add(table);
         } catch (Exception e) {
             throw new RuntimeException("Fail to generate driver PDF report: " + e.getMessage());
         }
         return new ByteArrayInputStream(out.toByteArray());
     }

    // --- ROUTE REPORTS ---
    @Override
    public ByteArrayInputStream generateRouteReportExcel() {
        List<Route> routes = routeRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Routes List");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Route Number", "Start Location", "End Location", "Distance (KM)", "Estimated Time (Mins)", "Status", "Stops Count"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Route r : routes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getRouteNumber());
                row.createCell(2).setCellValue(r.getStartLocation());
                row.createCell(3).setCellValue(r.getEndLocation());
                row.createCell(4).setCellValue(r.getDistanceKm());
                row.createCell(5).setCellValue(r.getEstimatedDurationMins());
                row.createCell(6).setCellValue(r.getStatus().name());
                row.createCell(7).setCellValue(r.getStops().size());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate Route Excel: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream generateRouteReportPdf() {
        List<Route> routes = routeRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();
            Paragraph title = new Paragraph("Delhi Transport Corporation (DTC)\nTransit Routes Audit Report", 
                    new Font(Font.HELVETICA, 18, Font.BOLD, DTC_PRIMARY));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph("Generated on: " + LocalDate.now().format(DATE_FORMAT) + "\n\n", 
                    new Font(Font.HELVETICA, 10, Font.ITALIC, DTC_SECONDARY));
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"Route No", "Start Terminal", "End Terminal", "Distance (KM)", "Time (Min)", "Status"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, WHITE);

            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(col, headerFont));
                cell.setBackgroundColor(DTC_PRIMARY);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font rowFont = new Font(Font.HELVETICA, 9);
            for (Route r : routes) {
                table.addCell(new PdfPCell(new Paragraph(r.getRouteNumber(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getStartLocation(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getEndLocation(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getDistanceKm().toString(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getEstimatedDurationMins().toString(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getStatus().name(), rowFont)));
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate route PDF report: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // --- SCHEDULE REPORTS ---
    @Override
    public ByteArrayInputStream generateScheduleReportExcel() {
        List<Schedule> list = scheduleRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Schedules");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Date", "Route No", "Departure", "Arrival", "Bus Number", "Driver", "Conductor", "Shift Type", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Schedule s : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getScheduleDate().format(DATE_FORMAT));
                row.createCell(2).setCellValue(s.getRoute().getRouteNumber());
                row.createCell(3).setCellValue(s.getDepartureTime().format(TIME_FORMAT));
                row.createCell(4).setCellValue(s.getArrivalTime().format(TIME_FORMAT));
                row.createCell(5).setCellValue(s.getBus().getBusNumber());
                row.createCell(6).setCellValue(s.getDriver().getUser().getFullName());
                row.createCell(7).setCellValue(s.getConductor().getUser().getFullName());
                row.createCell(8).setCellValue(s.getShiftType().name());
                row.createCell(9).setCellValue(s.getStatus());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate schedule Excel: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream generateScheduleReportPdf() {
        List<Schedule> list = scheduleRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();
            Paragraph title = new Paragraph("Delhi Transport Corporation (DTC)\nMaster Timetable & Operations Schedule", 
                    new Font(Font.HELVETICA, 18, Font.BOLD, DTC_PRIMARY));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph("Generated on: " + LocalDate.now().format(DATE_FORMAT) + "\n\n", 
                    new Font(Font.HELVETICA, 10, Font.ITALIC, DTC_SECONDARY));
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = {"Date", "Route", "Departure", "Arrival", "Bus Number", "Driver", "Status"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, WHITE);

            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(col, headerFont));
                cell.setBackgroundColor(DTC_PRIMARY);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font rowFont = new Font(Font.HELVETICA, 9);
            for (Schedule s : list) {
                table.addCell(new PdfPCell(new Paragraph(s.getScheduleDate().format(DATE_FORMAT), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getRoute().getRouteNumber(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getDepartureTime().format(TIME_FORMAT), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getArrivalTime().format(TIME_FORMAT), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getBus().getBusNumber(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getDriver().getUser().getFullName(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(s.getStatus(), rowFont)));
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate schedule PDF report: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // --- MAINTENANCE REPORTS ---
    @Override
    public ByteArrayInputStream generateMaintenanceReportExcel() {
        List<MaintenanceRecord> records = maintenanceRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Maintenance Records");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Bus Number", "Service Date", "Service Type", "Description", "Cost (INR)", "Status", "Technician Notes"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (MaintenanceRecord r : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getBus().getBusNumber());
                row.createCell(2).setCellValue(r.getServiceDate().format(DATE_FORMAT));
                row.createCell(3).setCellValue(r.getServiceType());
                row.createCell(4).setCellValue(r.getDescription());
                row.createCell(5).setCellValue(r.getCost());
                row.createCell(6).setCellValue(r.getStatus().name());
                row.createCell(7).setCellValue(r.getTechnicianNotes() != null ? r.getTechnicianNotes() : "None");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate maintenance Excel: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream generateMaintenanceReportPdf() {
        List<MaintenanceRecord> records = maintenanceRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();
            Paragraph title = new Paragraph("Delhi Transport Corporation (DTC)\nFleet Maintenance & Servicing Ledger", 
                    new Font(Font.HELVETICA, 18, Font.BOLD, DTC_PRIMARY));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph("Generated on: " + LocalDate.now().format(DATE_FORMAT) + "\n\n", 
                    new Font(Font.HELVETICA, 10, Font.ITALIC, DTC_SECONDARY));
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"Bus", "Service Date", "Type", "Description", "Cost (INR)", "Status"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, WHITE);

            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(col, headerFont));
                cell.setBackgroundColor(DTC_PRIMARY);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font rowFont = new Font(Font.HELVETICA, 9);
            for (MaintenanceRecord r : records) {
                table.addCell(new PdfPCell(new Paragraph(r.getBus().getBusNumber(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getServiceDate().format(DATE_FORMAT), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getServiceType(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getDescription(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getCost().toString(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(r.getStatus().name(), rowFont)));
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate maintenance PDF report: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // --- DAILY OPERATIONS REPORTS ---
    @Override
    public ByteArrayInputStream generateOperationsReportExcel(LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        List<Trip> trips = tripRepository.findByTripDate(searchDate);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Daily Operations");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"Trip ID", "Route No", "Bus Number", "Driver Name", "Scheduled Dep", "Actual Dep", "Delay (Mins)", "Status", "Current Terminal / Stop"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Trip t : trips) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getId());
                row.createCell(1).setCellValue(t.getSchedule().getRoute().getRouteNumber());
                row.createCell(2).setCellValue(t.getSchedule().getBus().getBusNumber());
                row.createCell(3).setCellValue(t.getSchedule().getDriver().getUser().getFullName());
                row.createCell(4).setCellValue(t.getSchedule().getDepartureTime().format(TIME_FORMAT));
                row.createCell(5).setCellValue(t.getActualDepartureTime() != null ? t.getActualDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "N/A");
                row.createCell(6).setCellValue(t.getDelayMinutes());
                row.createCell(7).setCellValue(t.getStatus().name());
                row.createCell(8).setCellValue(t.getCurrentStop() != null ? t.getCurrentStop() : "N/A");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate operations Excel: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream generateOperationsReportPdf(LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        List<Trip> trips = tripRepository.findByTripDate(searchDate);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Document document = new Document(PageSize.A4)) {
            PdfWriter.getInstance(document, out);
            document.open();
            Paragraph title = new Paragraph("Delhi Transport Corporation (DTC)\nDaily Operations Transit Report", 
                    new Font(Font.HELVETICA, 18, Font.BOLD, DTC_PRIMARY));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph("Operations Date: " + searchDate.format(DATE_FORMAT) + " | Generated on: " + LocalDate.now().format(DATE_FORMAT) + "\n\n", 
                    new Font(Font.HELVETICA, 10, Font.ITALIC, DTC_SECONDARY));
            info.setAlignment(Element.ALIGN_CENTER);
            document.add(info);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = {"Route", "Bus", "Driver", "Scheduled Dep", "Actual Dep", "Delay (Min)", "Status"};
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, WHITE);

            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(col, headerFont));
                cell.setBackgroundColor(DTC_PRIMARY);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Font rowFont = new Font(Font.HELVETICA, 9);
            for (Trip t : trips) {
                table.addCell(new PdfPCell(new Paragraph(t.getSchedule().getRoute().getRouteNumber(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getSchedule().getBus().getBusNumber(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getSchedule().getDriver().getUser().getFullName(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getSchedule().getDepartureTime().format(TIME_FORMAT), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getActualDepartureTime() != null ? t.getActualDepartureTime().format(TIME_FORMAT) : "N/A", rowFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getDelayMinutes().toString(), rowFont)));
                table.addCell(new PdfPCell(new Paragraph(t.getStatus().name(), rowFont)));
            }

            document.add(table);
        } catch (Exception e) {
            throw new RuntimeException("Fail to generate operations PDF report: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
}
