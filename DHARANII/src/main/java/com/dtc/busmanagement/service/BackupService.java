package com.dtc.busmanagement.service;

public interface BackupService {
    String generateBackup();
    void restoreBackup(String sqlContent);
}
