# Projet de Génération de Rapports

## Description
Ce projet est une application Spring Boot qui génère des rapports PDF à partir de données stockées dans une base de données. L'application utilise Apache PDFBox pour créer et manipuler les fichiers PDF.

## Prérequis
- Java 21 ou supérieur
- Maven 3.6 ou supérieur
- Une base de données compatible (par exemple, MySQL, PostgreSQL)

## Installation
1. Clonez le dépôt :
    ```bash
    git clone https://github.com/Uqac-Atelier-Cyber-Project/spring-boot-generate-report.git
    cd spring-boot-generate-report
    ```

2. Configurez la base de données dans le fichier `application.properties` :
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/votre_base_de_donnees
    spring.datasource.username=votre_utilisateur
    spring.datasource.password=votre_mot_de_passe
    spring.jpa.hibernate.ddl-auto=update
    ```

3. Compilez et packagez l'application avec Maven :
    ```bash
    mvn clean install
    ```

4. Exécutez l'application :
    ```bash
    java -jar target/votre-application.jar
    ```

## Utilisation
L'application expose une API REST pour générer des rapports. Pour générer un rapport, envoyez une requête POST à l'endpoint `/reportGenerate/generate` avec un corps JSON contenant l'ID du rapport à générer.

Exemple de requête :
```bash
curl -X POST http://localhost:8086/reportGenerate/generate -H "Content-Type: application/json" -d '{"reportId": 1}'
```

## Structure du Projet
- `src/main/java/com/uqac/generate_report/controller/GenerateReportController.java` : Contrôleur REST pour gérer les requêtes de génération de rapports.
- `src/main/java/com/uqac/generate_report/service/GenerateReportService.java` : Service pour la logique de génération de rapports.
- `src/main/resources/fonts/` : Dossier contenant les polices utilisées pour la génération des PDF.
