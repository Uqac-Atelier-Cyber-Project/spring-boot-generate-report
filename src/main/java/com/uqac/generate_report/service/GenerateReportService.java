package com.uqac.generate_report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uqac.generate_report.Entity.PendingAnalysis;
import com.uqac.generate_report.Entity.Report;
import com.uqac.generate_report.Entity.Result;
import com.uqac.generate_report.dto.ApiProperties;
import com.uqac.generate_report.dto.IARequest;
import com.uqac.generate_report.repository.PendingAnalysisRepository;
import com.uqac.generate_report.repository.ReportRepository;
import com.uqac.generate_report.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GenerateReportService {

    @Autowired
    private ApiProperties apiProperties;

    private static final Logger log = LoggerFactory.getLogger(GenerateReportService.class);
    public static final Map<Long, String> reportMapStatus = new ConcurrentHashMap<>();

    private final ReportRepository reportRepository;
    private final PendingAnalysisRepository pendingAnalysisRepository;
    private final ResultRepository resultRepository;

    private final RestTemplate restTemplate = new RestTemplate();


    /**
     * Génère un rapport en appelant un service IA.
     *
     * @param generateId ID du rapport à générer
     */
    @Async
    public void generateReport(Long generateId) {
        try {
            reportMapStatus.put(generateId, "IN_PROGRESS");

            // Récupérer les données de la base
            List<Result> results = resultRepository.findByReportId(generateId);
            if (results.isEmpty())
                throw new IllegalArgumentException("Aucun résultat trouvé pour l'ID : " + generateId);
            Result result = results.getFirst();

            List<String> steps = Arrays.asList(result.getStep1(), result.getStep2(), result.getStep3(), result.getStep4());

            Optional<Report> optionalReport = reportRepository.findByReportId(generateId);
            if (optionalReport.isEmpty())
                throw new IllegalArgumentException("Aucun rapport trouvé pour l'ID : " + generateId);
            Report report = optionalReport.get();

            Optional<PendingAnalysis> optionalPendingAnalysis = pendingAnalysisRepository.findByReportId(generateId);
            if (optionalPendingAnalysis.isEmpty())
                throw new IllegalArgumentException("Aucune analyse en attente trouvée pour l'ID : " + generateId);
            PendingAnalysis pendingAnalysis = optionalPendingAnalysis.get();

            // Générer la requête pour l'IA
            String markdownContent = callIAService(steps);
            if (markdownContent == null || markdownContent.isEmpty()) throw new IOException("Réponse vide de l'IA");

            String reportName = report.getReportName();
            // Écrire le Markdown dans un fichier
            String markdownFilePath = saveMarkdownFile(markdownContent, reportName);


            // Convertir en PDF avec Pandoc
            String pdfFilePath = convertMarkdownToPdf(markdownFilePath, pendingAnalysis.getPdfPassword());

            // Sauvegarde du rapport
            report.setEncryptedFile(pdfFilePath);
            reportRepository.save(report);

            // Suppression de l'analyse en attente
            pendingAnalysisRepository.delete(pendingAnalysis);

            reportMapStatus.put(generateId, "COMPLETED");
            log.info("Rapport généré avec succès : {}", pdfFilePath);

        } catch (Exception e) {
            reportMapStatus.put(generateId, "FAILED");
            log.error("Erreur lors de la génération du rapport", e);
        }
    }

    /**
     * Envoie une requête au service IA et récupère la réponse en Markdown.
     */
    private String callIAService(List<String> steps) {
        try {
            // Concaténer le texte à envoyer
            String message = String.join("\n", steps);

            // Créer l'objet IARequest
            IARequest iaRequest = IARequest.builder().prompt(message).build();

            // Effectuer la requête HTTP
            ResponseEntity<String> response = restTemplate.postForEntity(apiProperties.getUrl() + "/generate", iaRequest, String.class);

            // Parse the JSON response to extract the 'reponse' attribute
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            log.info("Réponse de l'IA : {}", rootNode.path("response").asText());
            return rootNode.path("response").asText();
        } catch (Exception e) {
            log.error("Erreur lors de l'appel au service IA", e);
            return null;
        }
    }

    /**
     * Sauvegarde le texte Markdown dans un fichier local.
     */
    private String saveMarkdownFile(String content, String reportName) throws IOException {
        String homeDirectory = System.getProperty("user.home");
        log.info(
                "Sauvegarde du fichier Markdown dans le répertoire : {} avec le nom : {}", homeDirectory + apiProperties.getPath(), reportName
        );
        String filePath = homeDirectory + apiProperties.getPath() + reportName + ".md";
        Files.write(Paths.get(filePath), content.getBytes());
        return filePath;
    }

    /**
     * Convertit un fichier Markdown en PDF en utilisant Pandoc.
     */
    private String convertMarkdownToPdf(String markdownFilePath, String filePassword) throws IOException, InterruptedException {
        String pdfFilePath = markdownFilePath.replace(".md", ".pdf");
        log.info("Converting Markdown file to PDF: {}", markdownFilePath);

        // Step 1: Generate the PDF with Pandoc
        ProcessBuilder processBuilder = new ProcessBuilder(
                "pandoc", markdownFilePath, "-o", pdfFilePath, "--from=markdown+smart",
                "--pdf-engine=xelatex", "-V", "geometry:margin=1in", "-V", "mainfont=DejaVuSerif"
        );
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new IOException("Erreur lors de la génération du PDF avec Pandoc");
        }

        // Step 2: Protect the PDF with QPDF
        ProcessBuilder qpdfProcessBuilder = new ProcessBuilder(
                "qpdf", "--encrypt", filePassword, filePassword, "256", "--", "--replace-input", pdfFilePath
        );
        Process qpdfProcess = qpdfProcessBuilder.start();

        // Capture the output and error streams
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(qpdfProcess.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(qpdfProcess.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                log.info("QPDF output: {}", line);
            }
            while ((line = errorReader.readLine()) != null) {
                log.error("QPDF error: {}", line);
            }
        }

        if (qpdfProcess.waitFor() != 0) {
            throw new IOException("Erreur lors de la protection du PDF avec QPDF");
        }

        log.info("PDF généré et protégé avec succès : {}", pdfFilePath);
        return pdfFilePath;
    }


}