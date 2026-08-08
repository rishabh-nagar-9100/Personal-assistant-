package com.jarvis.dsa.dto;

public record ExcelImportResponse(
        int totalRows,
        int importedCount,
        int skippedCount,
        String message
) {
}
