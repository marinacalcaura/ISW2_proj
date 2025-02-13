package deliverable.files;

import deliverable.entities.Acume;
import deliverable.entities.JavaClass;
import deliverable.utils.CsvNamesEnum;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataExporter {


    private final String projectName;
    private final CsvNamesEnum csvType;
    private final int csvIndex;
    private final List<JavaClass> javaClassesList;


    private static final Logger logger = Logger.getLogger(EvaluationReport.class.getName());

    public DataExporter(String projectName, CsvNamesEnum csvType, int csvIndex, List<JavaClass> javaClassesList) {
        this.projectName = projectName;
        this.csvType = csvType;
        this.csvIndex = csvIndex;
        this.javaClassesList = javaClassesList;
    }

    private String enumToString() {
        switch (csvType) {
            case TRAINING:
                return "_TR" + csvIndex;
            case TESTING:
                return "_TE" + csvIndex;
            case BUGGY:
                return "_buggy_classes";
            case CURRENT:
                return "_current_classes";
            default:
                throw new IllegalArgumentException("Invalid CsvNamesEnum value: " + csvType);
        }
    }


    public void writeOnCsv() throws IOException {
        String csvFileName = projectName + enumToString() + ".csv";
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(csvFileName), StandardCharsets.UTF_8))) {

            List<String> headers = Arrays.asList("JAVA_CLASS", "RELEASE", "SIZE", "NR", "N_AUTH",
                    "LOC_ADDED", "MAX_LOC_ADDED", "AVG_LOC_ADDED",
                    "CHURN", "MAX_CHURN", "AVG_CHURN", "IS_BUGGY");

            writer.println(String.join(",", headers));

            for (JavaClass javaClass : javaClassesList) {
                List<String> values = Arrays.asList(
                        javaClass.getName(),
                        String.valueOf(javaClass.getRelease().getReleaseId()),
                        String.valueOf(javaClass.getSize()),
                        String.valueOf(javaClass.getNr()),
                        String.valueOf(javaClass.getnAuth()),
                        String.valueOf(javaClass.getLocAdded()),
                        String.valueOf(javaClass.getMaxLocAdded()),
                        String.valueOf(javaClass.getAvgLocAdded()),
                        String.valueOf(javaClass.getChurn()),
                        String.valueOf(javaClass.getMaxChurn()),
                        String.valueOf(javaClass.getAvgChurn()),
                        String.valueOf(javaClass.isBuggy())
                );
                writer.println(String.join(",", values));
            }
        }
    }

    public void writeOnArff(boolean deleteCsv) throws IOException {
        String fileName = projectName + enumToString();
        writeOnCsv();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName + ".arff"), StandardCharsets.UTF_8))) {
            writer.println("@relation " + fileName);
            writer.println("@attribute SIZE numeric");
            writer.println("@attribute NR numeric");
            writer.println("@attribute N_AUTH numeric");
            writer.println("@attribute LOC_ADDED numeric");
            writer.println("@attribute MAX_LOC_ADDED numeric");
            writer.println("@attribute AVG_LOC_ADDED numeric");
            writer.println("@attribute CHURN numeric");
            writer.println("@attribute MAX_CHURN numeric");
            writer.println("@attribute AVG_CHURN numeric");
            writer.println("@attribute IS_BUGGY {'true', 'false'}");
            writer.println("@data");

            try (BufferedReader br = new BufferedReader(new FileReader(fileName + ".csv"))) {
                br.readLine(); // Salta l'header
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    writer.println(String.join(",", Arrays.copyOfRange(parts, 2, parts.length)));
                }
            }
        }

        if (deleteCsv) {
            Files.deleteIfExists(Paths.get(fileName + ".csv"));
        }
    }
    public static void acumeReport(String projName, List<Acume> acumeEntries) {
        Path outputDir = Paths.get("output", "acumeFiles");
        Path outputFile = outputDir.resolve("Acume.csv");

        try {

            Files.createDirectories(outputDir);


            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                writer.write("ID,Size,prob,actual");
                writer.newLine();


                for (Acume entry : acumeEntries) {
                    String line = String.join(",",
                            String.valueOf(entry.getId()),
                            String.valueOf(entry.getSize()),
                            String.valueOf(entry.getPredictedProbability()),
                            entry.getActualStringValue());
                    writer.write(line);
                    writer.newLine();
                }
            }

            logger.log(Level.INFO, "File CSV creato con successo: " + outputFile.toAbsolutePath());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore durante la creazione del file CSV", e);
        }
    }
}
