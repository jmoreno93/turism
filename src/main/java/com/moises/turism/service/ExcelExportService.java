package com.moises.turism.service;

import com.moises.turism.domain.Reserva;
import com.moises.turism.dto.experiencia.ExperienciaDisponibilidadResponse;
import com.moises.turism.dto.experiencia.ExperienciaResponse;
import com.moises.turism.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ExperienciaService experienciaService;
    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public byte[] exportarExperiencias(String estado) {
        List<ExperienciaResponse> experiencias = experienciaService.listarAdmin(estado);
        log.info("Generando Excel de experiencias. estado={}, total={}", StringUtils.defaultIfBlank(estado, "TODAS"), experiencias.size());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Experiencias");
            CellStyle headerStyle = crearHeaderStyle(workbook);
            CellStyle moneyStyle = crearMoneyStyle(workbook);

            String[] headers = {
                    "ID", "Título", "Categoría", "Anfitrión", "Ubicación", "Precio",
                    "Capacidad", "Duración horas", "Estado", "Fecha creación", "Disponibilidades", "Fotos"
            };
            crearHeader(sheet, headers, headerStyle);

            int rowIndex = 1;
            for (ExperienciaResponse experiencia : experiencias) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, experiencia.idExperiencia());
                writeCell(row, 1, limpiar(experiencia.titulo()));
                writeCell(row, 2, limpiar(experiencia.categoria()));
                writeCell(row, 3, limpiar(experiencia.anfitrion()));
                writeCell(row, 4, limpiar(experiencia.ubicacion()));
                writeMoneyCell(row, 5, experiencia.precio(), moneyStyle);
                writeCell(row, 6, experiencia.capacidadMaxima());
                writeCell(row, 7, experiencia.duracionHoras());
                writeCell(row, 8, limpiar(experiencia.estadoPublicacion()));
                writeCell(row, 9, experiencia.fechaCreacion() == null ? "" : experiencia.fechaCreacion().format(DATE_TIME_FORMAT));
                writeCell(row, 10, formatearDisponibilidades(experiencia.disponibilidades()));
                writeCell(row, 11, String.join(" | ", CollectionUtils.emptyIfNull(experiencia.fotosUrls())));
            }

            autosize(sheet, headers.length);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el Excel de experiencias.", ex);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarReservas() {
        List<Reserva> reservas = reservaRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaReserva"));
        log.info("Generando Excel de reservas. total={}", reservas.size());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reservas");
            CellStyle headerStyle = crearHeaderStyle(workbook);

            String[] headers = {
                    "ID", "Viajero", "Email viajero", "Experiencia", "Anfitrión",
                    "Fecha reserva", "Fecha experiencia", "Personas", "Estado", "Observaciones"
            };
            crearHeader(sheet, headers, headerStyle);

            int rowIndex = 1;
            for (Reserva reserva : reservas) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, reserva.getIdReserva());
                writeCell(row, 1, limpiar(reserva.getUsuario().getNombres() + " " + reserva.getUsuario().getApellidos()));
                writeCell(row, 2, limpiar(reserva.getUsuario().getEmail()));
                writeCell(row, 3, limpiar(reserva.getExperiencia().getTitulo()));
                writeCell(row, 4, limpiar(reserva.getExperiencia().getAnfitrion().getUsuario().getNombres() + " " + reserva.getExperiencia().getAnfitrion().getUsuario().getApellidos()));
                writeCell(row, 5, reserva.getFechaReserva() == null ? "" : reserva.getFechaReserva().format(DATE_TIME_FORMAT));
                writeCell(row, 6, reserva.getFechaExperiencia() == null ? "" : reserva.getFechaExperiencia().format(DATE_FORMAT));
                writeCell(row, 7, reserva.getCantidadPersonas());
                writeCell(row, 8, limpiar(reserva.getEstado().getNombreEstado()));
                writeCell(row, 9, limpiar(reserva.getObservaciones()));
            }

            autosize(sheet, headers.length);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el Excel de reservas.", ex);
        }
    }

    private void crearHeader(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private CellStyle crearHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle crearMoneyStyle(Workbook workbook) {
        DataFormat dataFormat = workbook.createDataFormat();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(dataFormat.getFormat("S/ #,##0.00"));
        return style;
    }

    private void writeCell(Row row, int columnIndex, String value) {
        row.createCell(columnIndex).setCellValue(value == null ? "" : value);
    }

    private void writeCell(Row row, int columnIndex, Number value) {
        if (value == null) {
            row.createCell(columnIndex).setCellValue("");
            return;
        }
        row.createCell(columnIndex).setCellValue(value.doubleValue());
    }

    private void writeMoneyCell(Row row, int columnIndex, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private void autosize(Sheet sheet, int totalColumns) {
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String formatearDisponibilidades(List<ExperienciaDisponibilidadResponse> disponibilidades) {
        return CollectionUtils.emptyIfNull(disponibilidades)
                .stream()
                .map(item -> item.fechaDisponible().format(DATE_FORMAT) + " - " + item.cuposDisponibles() + " cupos")
                .reduce((left, right) -> left + " | " + right)
                .orElse("");
    }

    private String limpiar(String value) {
        return StringUtils.normalizeSpace(StringUtils.defaultString(value));
    }
}
