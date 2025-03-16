package com.uqac.generate_report.controller;


import com.uqac.generate_report.GenerateReportApplication;
import com.uqac.generate_report.dto.ServiceRequest;
import com.uqac.generate_report.service.GenerateReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportGenerate")
public class GenerateReportController {

    private final GenerateReportService generateReportService;

    /**
     * Constructeur
     * @param generateReportService Service de génération de rapport
     */
    public GenerateReportController(GenerateReportService generateReportService) {
        this.generateReportService = generateReportService;
    }

    /**
     * Génère un rapport
     * @param request Requête de génération
     * @return Message de confirmation
     */
    @PostMapping("/generate")
    public String generateReport(@RequestBody ServiceRequest request) {
        Long generateid = request.getReportId();
        // verification si le report n'est pas déjà en cours de traitement dans la map
        if (GenerateReportService.reportMapStatus.containsKey(generateid)) {
            return "Report already in progress";
        }
        generateReportService.generateReport(generateid);
        return "Report generated with ID: " + generateid;
    }

}
