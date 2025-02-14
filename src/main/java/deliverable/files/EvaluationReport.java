package deliverable.files;
import deliverable.entities.ClassifierEvaluation;
import java.io.*;
import java.util.List;
import java.io.IOException;

public class EvaluationReport {

    private String projectName;
    private List<ClassifierEvaluation> evaluationList;
    private String description;

    private static final String BOOLEAN_FALSE = "false";
    private static final String BOOLEAN_TRUE = "true";


    public EvaluationReport(String projectName, List<ClassifierEvaluation> evaluationList, String description) {
        this.projectName = projectName;
        this.evaluationList = evaluationList;
        this.description = description;
    }

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
                writer.append(eval.isFeatureSelection() ? BOOLEAN_TRUE : BOOLEAN_FALSE).append(",");
                writer.append(eval.isSampling() ? BOOLEAN_TRUE : BOOLEAN_FALSE).append(",");
                writer.append(eval.isCostSensitive() ? BOOLEAN_TRUE : BOOLEAN_FALSE).append(",");
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
