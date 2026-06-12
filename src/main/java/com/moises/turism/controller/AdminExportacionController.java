package com.moises.turism.controller;

import com.moises.turism.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/exportaciones")
public class AdminExportacionController {

    private static final MediaType XLSX_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ExcelExportService excelExportService;

    @GetMapping(value = "/experiencias.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportarExperiencias(@RequestParam(required = false) String estado) {
        String estadoSeguro = StringUtils.defaultIfBlank(estado, "todas").toLowerCase();
        String filename = "experiencias_" + estadoSeguro + "_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".xlsx";
        log.info("Solicitud de exportación Excel de experiencias. estado={}", estadoSeguro);
        return excelResponse(filename, excelExportService.exportarExperiencias(estado));
    }

    @GetMapping(value = "/reservas.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportarReservas() {
        String filename = "reservas_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".xlsx";
        log.info("Solicitud de exportación Excel de reservas.");
        return excelResponse(filename, excelExportService.exportarReservas());
    }

    private ResponseEntity<byte[]> excelResponse(String filename, byte[] content) {
        return ResponseEntity.ok()
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }
}
