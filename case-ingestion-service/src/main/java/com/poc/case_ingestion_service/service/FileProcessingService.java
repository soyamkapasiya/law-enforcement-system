package com.poc.case_ingestion_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.case_ingestion_service.model.CaseReport;
import com.poc.case_ingestion_service.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final ObjectMapper objectMapper;

    public List<CaseReport> processFile(MultipartFile file, String fileType) throws Exception {
        String fileName = file.getOriginalFilename();
        String detectedType = detectFileType(fileName, fileType);

        log.info("Processing file: {} of type: {}", fileName, detectedType);

        List<CaseReport> cases = switch (detectedType.toUpperCase()) {
            case "CSV" -> processCsvFile(file);
            case "EXCEL", "XLS", "XLSX" -> processExcelFile(file);
            case "PDF" -> processPdfFile(file);
            case "JSON" -> processJsonFile(file);
            case "XML" -> processXmlFile(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + detectedType);
        };

        for (CaseReport caseReport : cases) {
            caseReport.setSourceFile(fileName);
            caseReport.setProcessedAt(LocalDateTime.now());
            caseReport.setProcessedBy("FILE_PROCESSOR");
        }

        return cases;
    }

    private String detectFileType(String fileName, String providedType) {
        if (providedType != null && !providedType.isEmpty()) {
            return providedType;
        }

        if (fileName == null) {
            throw new IllegalArgumentException("Cannot determine file type");
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "csv" -> "CSV";
            case "xls" -> "XLS";
            case "xlsx" -> "XLSX";
            case "pdf" -> "PDF";
            case "json" -> "JSON";
            case "xml" -> "XML";
            default -> throw new IllegalArgumentException("Unsupported file extension: " + extension);
        };
    }

    private List<CaseReport> processCsvFile(MultipartFile file) throws IOException {
        List<CaseReport> cases = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            String[] headers = null;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (isFirstLine) {
                    headers = values;
                    isFirstLine = false;
                    continue;
                }

                CaseReport caseReport = mapCsvRowToCaseReport(headers, values);
                if (caseReport != null) {
                    cases.add(caseReport);
                }
            }
        }

        return cases;
    }

    private List<CaseReport> processExcelFile(MultipartFile file) throws IOException {
        List<CaseReport> cases = new ArrayList<>();

        Workbook workbook = null;
        try {
            try {
                workbook = new XSSFWorkbook(file.getInputStream());
            } catch (Exception e) {
                workbook = new HSSFWorkbook(file.getInputStream());
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException("Excel file must have a header row");
            }

            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                headers[i] = cell != null ? cell.getStringCellValue() : "";
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String[] values = new String[headers.length];
                    for (int j = 0; j < headers.length; j++) {
                        Cell cell = row.getCell(j);
                        values[j] = getCellValueAsString(cell);
                    }

                    CaseReport caseReport = mapCsvRowToCaseReport(headers, values);
                    if (caseReport != null) {
                        cases.add(caseReport);
                    }
                }
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }

        return cases;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private List<CaseReport> processPdfFile(MultipartFile file) throws IOException {
        List<CaseReport> cases;

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            cases = parsePdfTextToCases(text);
        }

        return cases;
    }

    private List<CaseReport> processJsonFile(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            try {
                return objectMapper.readValue(jsonContent.toString(), new TypeReference<>() {
                });
            } catch (Exception e) {
                CaseReport singleCase = objectMapper.readValue(jsonContent.toString(), CaseReport.class);
                return Collections.singletonList(singleCase);
            }
        }
    }

    private List<CaseReport> processXmlFile(MultipartFile file) throws IOException {
        List<CaseReport> cases;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            StringBuilder xmlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xmlContent.append(line);
            }

            cases = parseXmlToCases(xmlContent.toString());
        }

        return cases;
    }

    private CaseReport mapCsvRowToCaseReport(String[] headers, String[] values) {
        if (values.length < headers.length) {
            return null;
        }

        CaseReport caseReport = new CaseReport();

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].toLowerCase().trim();
            String value = values[i].trim();

            if (value.isEmpty()) continue;

            switch (header) {
                case "caseid":
                case "case_id":
                    caseReport.setCaseId(value);
                    break;
                case "casetype":
                case "case_type":
                    caseReport.setCaseType(value);
                    break;
                case "status":
                    caseReport.setStatus(value);
                    break;
                case "description":
                    caseReport.setDescription(value);
                    break;
                case "reportingofficer":
                case "reporting_officer":
                    caseReport.setReportingOfficer(value);
                    break;
                case "assignedofficer":
                case "assigned_officer":
                    caseReport.setAssignedOfficer(value);
                    break;
                case "priority":
                    caseReport.setPriority(value);
                    break;
                case "department":
                    caseReport.setDepartment(value);
                    break;
                case "incidentdate":
                case "incident_date":
                    caseReport.setIncidentDate(value);
                    break;
                case "address":
                    if (caseReport.getLocation() == null) {
                        caseReport.setLocation(new Location());
                    }
                    caseReport.getLocation().setAddress(value);
                    break;
                case "city":
                    if (caseReport.getLocation() == null) {
                        caseReport.setLocation(new Location());
                    }
                    caseReport.getLocation().setCity(value);
                    break;
                case "state":
                    if (caseReport.getLocation() == null) {
                        caseReport.setLocation(new Location());
                    }
                    caseReport.getLocation().setState(value);
                    break;
            }
        }

        if (caseReport.getCaseId() == null || caseReport.getCaseId().isEmpty()) {
            caseReport.setCaseId("CASE-" + System.currentTimeMillis());
        }

        if (caseReport.getStatus() == null || caseReport.getStatus().isEmpty()) {
            caseReport.setStatus("OPEN");
        }

        if (caseReport.getReportedAt() == null) {
            caseReport.setReportedAt(LocalDateTime.now().toString());
        }

        return caseReport;
    }

    private List<CaseReport> parsePdfTextToCases(String text) {
        List<CaseReport> cases = new ArrayList<>();

        String[] caseSections = text.split("(?i)case\\s+(?:id|number):");

        for (int i = 1; i < caseSections.length; i++) { // Skip first empty section
            String section = caseSections[i];
            CaseReport caseReport = extractCaseFromText(section);
            cases.add(caseReport);
        }

        return cases;
    }

    private CaseReport extractCaseFromText(String text) {
        CaseReport caseReport = new CaseReport();

        caseReport.setCaseId(extractWithPattern(text, "(?i)case\\s+(?:id|number):\\s*([\\w-]+)"));
        caseReport.setCaseType(extractWithPattern(text, "(?i)case\\s+type:\\s*([\\w\\s]+)"));
        caseReport.setStatus(extractWithPattern(text, "(?i)status:\\s*([\\w]+)"));
        caseReport.setDescription(extractWithPattern(text, "(?i)description:\\s*([^\\n]+)"));
        caseReport.setReportingOfficer(extractWithPattern(text, "(?i)reporting\\s+officer:\\s*([\\w\\s]+)"));

        if (caseReport.getCaseId() == null) {
            caseReport.setCaseId("PDF-CASE-" + System.currentTimeMillis());
        }
        if (caseReport.getStatus() == null) {
            caseReport.setStatus("OPEN");
        }
        if (caseReport.getReportedAt() == null) {
            caseReport.setReportedAt(LocalDateTime.now().toString());
        }

        return caseReport;
    }

    private String extractWithPattern(String text, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private List<CaseReport> parseXmlToCases(String xmlContent) {
        List<CaseReport> cases = new ArrayList<>();

        Pattern casePattern = Pattern.compile("<case[^>]*>(.*?)</case>", Pattern.DOTALL);
        Matcher matcher = casePattern.matcher(xmlContent);

        while (matcher.find()) {
            String caseXml = matcher.group(1);
            CaseReport caseReport = new CaseReport();

            caseReport.setCaseId(extractXmlValue(caseXml, "caseId"));
            caseReport.setCaseType(extractXmlValue(caseXml, "caseType"));
            caseReport.setStatus(extractXmlValue(caseXml, "status"));
            caseReport.setDescription(extractXmlValue(caseXml, "description"));

            if (caseReport.getCaseId() != null) {
                cases.add(caseReport);
            }
        }

        return cases;
    }

    private String extractXmlValue(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}