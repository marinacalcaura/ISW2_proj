package deliverable.files;
import deliverable.entities.ClassifierEvaluation;
import java.io.*;
import java.util.List;
import java.io.IOException;

public class EvaluationReport {

    private String projectName;
    private List<ClassifierEvaluation> evaluationList;
    private String description;


    public EvaluationReport(String projectName, List<ClassifierEvaluation> evaluationList, String description) {
        this.projectName = projectName;
        this.evaluationList = evaluationList;
        this.description = description;
    }

    /*public void generateReport() throws IOException {
        Workbook workbook = new HSSFWorkbook();
        OutputStream os = new FileOutputStream(this.projectName + "_classifiers_report_" + this.description + ".csv");

        try {
            Sheet sheet = workbook.createSheet(this.projectName);
            String[] headers = {
                    "DATASET", "#TRAINING_RELEASES", "%TRAINING_INSTANCES", "CLASSIFIER",
                    "FEATURE_SELECTION", "BALANCING", "COST_SENSITIVE", "PRECISION",
                    "RECALL", "AUC", "KAPPA", "TP", "FP", "TN", "FN", "NPOFB20"
            };


            Row headerRow = sheet.createRow(0);
            for (int j = 0; j < headers.length; j++) {
                headerRow.createCell(j).setCellValue(headers[j]);
            }


            for (int i = 0; i < this.evaluationList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                ClassifierEvaluation eval = this.evaluationList.get(i);

                row.createCell(0).setCellValue(this.projectName);
                row.createCell(1).setCellValue(this.description.equals("details") ? Integer.toString(eval.getWalkForwardIterationIndex()) : "None");
                row.createCell(2).setCellValue(this.description.equals("details") ? Double.toString(eval.getTrainingPercent()) : "None");
                row.createCell(3).setCellValue(eval.getClassifier());
                row.createCell(4).setCellValue(eval.isFeatureSelection() ? "Greedy backward search" : "None");
                row.createCell(5).setCellValue(eval.isSampling() ? "Undersampling" : "None");
                row.createCell(6).setCellValue(eval.isCostSensitive() ? "Sensitive learning" : "None");
                row.createCell(7).setCellValue(eval.getPrecision());
                row.createCell(8).setCellValue(eval.getRecall());
                row.createCell(9).setCellValue(eval.getAuc());
                row.createCell(10).setCellValue(eval.getKappa());
                row.createCell(11).setCellValue(eval.getTp());
                row.createCell(12).setCellValue(eval.getFp());
                row.createCell(13).setCellValue(eval.getTn());
                row.createCell(14).setCellValue(eval.getFn());
                row.createCell(15).setCellValue(eval.getNpofb());
            }

            workbook.write(os);

        } finally {
            os.close();

        }
    }*/
    public void generateReport() throws IOException {
        String filename = this.projectName + "_classifiers_report_" + this.description + ".csv";
        try (FileWriter writer = new FileWriter(filename)) {
            String[] headers = {
                    "DATASET", "TRAINING_RELEASES", "%TRAINING_INSTANCES", "CLASSIFIER",
                    "FEATURE_SELECTION", "BALANCING", "COST_SENSITIVE", "PRECISION",
                    "RECALL", "AUC", "KAPPA", "TP", "FP", "TN", "FN", "NPOFB20", "COST\n"
            };

            // Scrive l'intestazione
            writer.append(String.join(",", headers)).append("\n");

            // Scrive i dati
            for (ClassifierEvaluation eval : this.evaluationList) {
                writer.append(this.projectName).append(",");
                writer.append(this.description.equals("details") ? Integer.toString(eval.getWalkForwardIterationIndex()) : "None").append(",");
                writer.append(this.description.equals("details") ? Double.toString(eval.getTrainingPercent()) : "None").append(",");
                writer.append(eval.getClassifier()).append(",");
                writer.append(eval.isFeatureSelection() ? "true" : "false").append(",");
                writer.append(eval.isSampling() ? "true" : "false").append(",");
                writer.append(eval.isCostSensitive() ? "true" : "false").append(",");
                writer.append(Double.toString(eval.getPrecision())).append(",");
                writer.append(Double.toString(eval.getRecall())).append(",");
                writer.append(Double.toString(eval.getAuc())).append(",");
                writer.append(Double.toString(eval.getKappa())).append(",");
                writer.append(Double.toString(eval.getTp())).append(",");
                writer.append(Double.toString(eval.getFp())).append(",");
                writer.append(Double.toString(eval.getTn())).append(",");
                writer.append(Double.toString(eval.getFn())).append(",");
                writer.append(Double.toString(eval.getNpofb())).append(",");
                writer.append(Long.toString(eval.getCost())).append("\n");
            }
        }
    }




}
