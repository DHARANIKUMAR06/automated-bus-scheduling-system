package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.service.BackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BackupController {

    @Autowired
    private BackupService backupService;

    @GetMapping("/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadBackup() {
        String backupSql = backupService.generateBackup();
        byte[] data = backupSql.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=dtc_backup.sql");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> restoreBackup(@RequestBody String sqlContent) {
        backupService.restoreBackup(sqlContent);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Database successfully restored from backup script!");
        return ResponseEntity.ok(response);
    }
}
