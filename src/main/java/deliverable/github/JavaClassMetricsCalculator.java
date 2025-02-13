package deliverable.github;

import deliverable.entities.JavaClass;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class JavaClassMetricsCalculator {


    private final RetrieveGitInfo retGitInfo;
    private final List<JavaClass> javaClassesList;

    public JavaClassMetricsCalculator(RetrieveGitInfo retGitInfo, List<JavaClass> javaClassesList) {
        this.retGitInfo = retGitInfo;
        this.javaClassesList = javaClassesList;
    }

    //calcolo la dimensione (LOC) per ogni classe Java
    private void computeSize() {
        for (JavaClass javaClass : javaClassesList) {
            javaClass.setSize(javaClass.getContent().split("\r\n|\r|\n").length);
        }
    }

    //calcolo il numero di revisioni (NR) per ogni classe.

    private void computeNR() {
        for (JavaClass javaClass : javaClassesList) {
            javaClass.setNr(javaClass.getCommits().size());
        }
    }

    //calcolo il numero di autori unici (NAuth) per classe
    private void computeAuthors() {
        for (JavaClass javaClass : javaClassesList) {
            Set<String> uniqueAuthors = new HashSet<>();
            for (RevCommit commit : javaClass.getCommits()) {
                uniqueAuthors.add(commit.getAuthorIdent().getName());
            }
            javaClass.setnAuth(uniqueAuthors.size());
        }
    }

    //calcolo le metriche LOC e Churn per ogni classe Java
    private void computeLocAndChurn() throws IOException {
        for (JavaClass javaClass : javaClassesList) {
            retGitInfo.computeAddedAndDeletedLinesList(javaClass);
            calculateLocAndChurnMetrics(javaClass);
        }
    }

    //calcolo le metriche LOC e Churn per una classe Java specifica
    private void calculateLocAndChurnMetrics(JavaClass javaClass) {
        List<Integer> addedLines = javaClass.getAddedLinesList();
        List<Integer> deletedLines = javaClass.getDeletedLinesList();

        int sumLOC = 0;
        int maxLOC = 0;
        for (int line : addedLines) {
            sumLOC += line;
            if (line > maxLOC) {
                maxLOC = line;
            }
        }
        double avgLOC = addedLines.isEmpty() ? 0 : sumLOC / (double) addedLines.size();

        List<Integer> churnValues = new ArrayList<>();
        int totalChurn = 0;
        int maxChurn = 0;
        for (int i = 0; i < addedLines.size(); i++) {
            int churn = Math.abs(addedLines.get(i) - deletedLines.get(i));
            churnValues.add(churn);
            totalChurn += churn;
            if (churn > maxChurn) {
                maxChurn = churn;
            }
        }
        double avgChurn = churnValues.isEmpty() ? 0 : totalChurn / (double) churnValues.size();

        javaClass.setLocAdded(sumLOC);
        javaClass.setMaxLocAdded(maxLOC);
        javaClass.setAvgLocAdded(avgLOC);
        javaClass.setChurn(totalChurn);
        javaClass.setMaxChurn(maxChurn);
        javaClass.setAvgChurn(avgChurn);
    }

    public List<JavaClass> computeAllMetrics() throws IOException {
        computeSize();
        computeNR();
        computeAuthors();
        computeLocAndChurn();
        return javaClassesList;
    }


    /* LocAdded = sum of number of added LOC in all the commit of the given release
     * MaxLocAdded = max number of added LOC in all the commit of the given release
     * AvgLocAdded = average number of added LOC in all the commit of the given release
     * Churn = sum of |number of added LOC - number of deleted LOC| in all the commit of the given release
     * MaxChurn = max |number of added LOC - number of deleted LOC| in all the commit of the given release
     * Churn = average of |number of added LOC - number of deleted LOC| in all the commit of the given release */


}

