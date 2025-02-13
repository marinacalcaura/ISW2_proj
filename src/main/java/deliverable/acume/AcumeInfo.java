package deliverable.acume;

import deliverable.entities.Acume;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static deliverable.files.DataExporter.acumeReport;

public class AcumeInfo {
    private static List<Acume> acumeInputList;
    private static String acumeScriptPath;
    private static String acumeOutputPath;

    public AcumeInfo() {
        String currentDirectory = System.getProperty("user.dir");
        this.acumeScriptPath = Paths.get(currentDirectory, "ACUME/main.py").toString();
        this.acumeOutputPath = Paths.get(currentDirectory, "ACUME/EAM_NEAM_output.csv").toString();

    }
    public double computeNpofb(String projectName, Instances testing, Classifier classifier) throws Exception{
        List<Acume> acumeInputList = prepareAcumeData(testing, classifier);
        acumeReport(projectName, acumeInputList);

        String scriptPath = "ACUME/main.py";
        String argument = "NPofB";
        executePythonScript(scriptPath,argument);

        return readNpofb20(acumeOutputPath);
    }
    private static List<Acume> prepareAcumeData(Instances testing, Classifier classifier) throws Exception {
        acumeInputList=new ArrayList<>();

        int lastAttrIndex= testing.numAttributes()-1;
        for(int i=0;i< testing.numInstances();i++) {
            Instance currInstance = testing.get(i);
            //System.out.println("ISTANZA test:"+ currInstance.toString());
            double size = currInstance.value(0);
            double prediction = getPredictionTrue(currInstance, classifier);
            //System.out.println("Prediction:" + prediction);
            boolean actual = currInstance.toString(lastAttrIndex).equals("true");
            Acume entry = new Acume(i, size, prediction, actual);
            acumeInputList.add(entry);
        }
        return acumeInputList;
    }
    public static double readNpofb20(String acumeOutputPath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(acumeOutputPath))) {
            //leggi la prima riga (intestazione del CSV)
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new RuntimeException("Errore: il file CSV è vuoto.");
            }

            //trova l'indice della colonna "Npofb20"
            int npofbIndex = findColumnIndex(headerLine, "Npofb20");
            if (npofbIndex == -1) {
                throw new RuntimeException("Errore: colonna 'Npofb20' non trovata nel CSV.");
            }


            String npofbValue = getLastColumnValue(reader, npofbIndex);
            if (npofbValue.isEmpty()) {
                throw new RuntimeException("Errore: il valore di NPOFB non è stato letto correttamente.");
            }

            double npofb = Double.parseDouble(npofbValue);
            System.out.println("Valore ritornato da readNPOFB: " + npofb);
            return npofb;

        } catch (IOException e) {
            throw new RuntimeException("Errore nella lettura del file CSV", e);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static String getLastColumnValue(BufferedReader reader, int columnIndex) throws IOException {
        String lastValue = "";
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length > columnIndex) {
                lastValue = fields[columnIndex].trim();
            }
        }
        return lastValue;
    }

    public static void executePythonScript(String scriptPath, String argument) {
        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, argument);
        processBuilder.redirectErrorStream(true);  // Unifica stdout e stderr

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;

            //legge ogni riga dell'output e la salva in output
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            reader.close();


            if (exitCode == 0) {
                System.out.println("Script Python eseguito con successo:\n" + output);
            } else {
                System.err.println("Errore nell'esecuzione dello script Python (Exit Code: " + exitCode + ")\n" + output);
                throw new RuntimeException("Errore durante l'esecuzione dello script Python");
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Errore nell'invocazione dello script Python", e);
        }
    }

    private static double getPredictionTrue(Instance inst, Classifier classifier) throws Exception {
        double[] predDist = classifier.distributionForInstance(inst);


        //System.out.println("Distribuzione delle probabilità: " + Arrays.toString(predDist));

        //controlla se ci sono abbastanza classi nel modello
        if (predDist.length < inst.classAttribute().numValues()) {
            System.err.println("Errore: la distribuzione delle probabilità ha meno valori del previsto.");
            System.err.println("PredDist length: " + predDist.length);
            System.err.println("Numero di classi nel dataset: " + inst.classAttribute().numValues());
            return -1; // Valore di default per evitare crash
        }

        for (int i = 0; i < predDist.length; i++) {
            if (inst.classAttribute().value(i).equals("true")) {
                //System.out.println("predDist["+i+"] è :" + predDist[i]);
                return predDist[i];
            }
        }

        throw new Exception("Errore: la classe 'true' non è stata trovata.");
    }

}