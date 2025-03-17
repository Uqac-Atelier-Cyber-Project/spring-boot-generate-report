package com.uqac.generate_report.service;

import com.uqac.generate_report.Entity.PendingAnalysis;
import com.uqac.generate_report.Entity.Report;
import com.uqac.generate_report.Entity.Result;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.uqac.generate_report.repository.PendingAnalysisRepository;
import com.uqac.generate_report.repository.ReportRepository;
import com.uqac.generate_report.repository.ResultRepository;

@Service
@RequiredArgsConstructor
public class GenerateReportService {

    private static Logger log = LoggerFactory.getLogger(GenerateReportService.class);

    public static final Map<Long, String> reportMapStatus = new ConcurrentHashMap<>();

    private final ReportRepository reportRepository;
    private final PendingAnalysisRepository pendingAnalysisRepository;
    private final ResultRepository resultRepository;

    /**
     * Génère un rapport
     * @param generateId ID du rapport à générer
     */
    @Async
    public void generateReport(Long generateId) {
        try {
            reportMapStatus.put(generateId, "IN_PROGRESS");

            // Récupérer les données de la table Result pour l'ID donné
            List<Result> results = resultRepository.findByReportId(generateId);
            if (results.isEmpty()) {
                throw new IllegalArgumentException("Aucun résultat trouvé pour l'ID : " + generateId);
            }

            Result result = results.getFirst();
            List<String> steps = new ArrayList<>();
            steps.add(result.getStep1());
            steps.add(result.getStep2());
            steps.add(result.getStep3());
            steps.add(result.getStep4());

            // Récupérer le rapport correspondant
            Optional<Report> optionalReport = reportRepository.findByReportId(generateId);
            if (optionalReport.isEmpty()) {
                throw new IllegalArgumentException("Aucun rapport trouvé pour l'ID : " + generateId);
            }
            Report report = optionalReport.get();

            Optional<PendingAnalysis> optionalPendingAnalysis = pendingAnalysisRepository.findByReportId(generateId);
            if (optionalPendingAnalysis.isEmpty()) {
                throw new IllegalArgumentException("Aucune analyse en attente trouvée pour l'ID : " + generateId);
            }
            PendingAnalysis pendingAnalysis = optionalPendingAnalysis.get();

            String outputPath = "/home/kiurow590/WebstormProjects/vue-js-front/front/public/pdf/" + report.getReportName() + ".pdf";
            File pdfFile = new File(outputPath);
            try (PDDocument document = new PDDocument()) {
                PDType0Font font = PDType0Font.load(document, new File("src/main/resources/fonts/Courier Regular.ttf"));
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(font, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                float yPosition = 750;
                float lineHeight = 15;

                for (String step : steps) {
                    if (step == null) {
                        continue;
                    }

                    String[] lines = step.split("\n");
                    for (String line : lines) {
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        String filteredLine = line.replaceAll("[\\x00-\\x1F]", "");
                        contentStream.showText(filteredLine);
                        yPosition -= lineHeight;

                        if (yPosition < 50) { // Check if we need a new page
                            contentStream.endText();
                            contentStream.close();

                            page = new PDPage(PDRectangle.A4);
                            document.addPage(page);
                            contentStream = new PDPageContentStream(document, page);
                            contentStream.setFont(font, 12);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, 750);
                            yPosition = 750;
                        } else {
                            contentStream.newLineAtOffset(0, -lineHeight);
                        }
                    }
                }
                contentStream.endText();
                contentStream.close();

                AccessPermission accessPermission = new AccessPermission();
                StandardProtectionPolicy protectionPolicy = new StandardProtectionPolicy(pendingAnalysis.getPdfPassword(), "userPassword", accessPermission);
                protectionPolicy.setEncryptionKeyLength(128);
                protectionPolicy.setPermissions(accessPermission);
                document.protect(protectionPolicy);

                document.save(pdfFile);
            }
            // Ajout du rapport a la base de données dans Report
            report.setEncryptedFile(pdfFile.getAbsolutePath());
            reportRepository.save(report);

            // Suppression de la ligne dans PendingAnalysis
            pendingAnalysisRepository.delete(pendingAnalysis);

            // Mettre à jour le statut
            reportMapStatus.put(generateId, "COMPLETED");

        } catch (Exception e) {
            reportMapStatus.put(generateId, "FAILED");
            e.printStackTrace();
        }
    }
}
