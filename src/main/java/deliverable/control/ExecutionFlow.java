package deliverable.control;

import deliverable.entities.Release;
import deliverable.entities.Ticket;
import deliverable.files.EvaluationReport;
import deliverable.jira.ReleaseInfo;
import deliverable.jira.RetrieveTickets;
import deliverable.utils.EvaluationResults;
import deliverable.utils.IterationProcessor;
import deliverable.utils.Proportion;
import deliverable.weka.WekaInfo;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ExecutionFlow {

    private ExecutionFlow() {
        throw new IllegalStateException("Utility class");
    }

    public static void analyzeProject(String projectName) throws Exception {
        //ottengo la lista delle release
        ReleaseInfo releaseInfo = new ReleaseInfo(projectName);
        List<Release> releaseList = releaseInfo.getReleases();

        /*for (Release rel : releaseList){
            System.out.println(rel.getReleaseId());
        }*/

        //ottengo la lista dei ticket
        RetrieveTickets retrieveTickets = new RetrieveTickets(projectName);
        List<Ticket> ticketList = retrieveTickets.getTickets(releaseList);

        int lastReleaseId = ReleaseInfo.getLastRelease(releaseList).getReleaseId();

        //recupero ticket da progetti simili per il cold start
        List<Ticket> coldStartTickets = Proportion.coldStartRetrieveConsistentIssues();


        IterationProcessor iterationProcessor = new IterationProcessor(releaseInfo, retrieveTickets);
        // Processo di iterazione per costruire training e testing set (walk-forward approach)
        for (int i = 1; i <= lastReleaseId; i++) {
            if (i < lastReleaseId / 2 || i == lastReleaseId) {
                try {
                    iterationProcessor.processIteration(i, projectName, releaseList, ticketList, coldStartTickets, lastReleaseId);
                } catch (IOException | GitAPIException | ParseException e) {
                    throw new RuntimeException("Errore durante il processamento dell'iterazione: " + i, e);
                }
            }
        }

        // Eseguo la valutazione con Weka
        WekaInfo wekaInfo = new WekaInfo(projectName, (lastReleaseId / 2) - 1);
        EvaluationResults evaluationResults = wekaInfo.retrieveClassifiersEvaluation();

        // Stampa i risultati medi della valutazione
        System.out.println("=== Valutazioni Medie ===");
        evaluationResults.getAvgEvaluationsList().forEach(avgEval -> {
            System.out.println("Classificatore: " + avgEval.getClassifier());
            System.out.println("Precisione media: " + avgEval.getPrecision());
            System.out.println("Recall media: " + avgEval.getRecall());
            System.out.println("AUC medio: " + avgEval.getAuc());
            System.out.println("Kappa medio: " + avgEval.getKappa());
            System.out.println("NPofb: " + avgEval.getNpofb());

            System.out.println("--------------------------------");
        });

        System.out.println("Numero totale di valutazioni: " + evaluationResults.getMergeEvaluationsList().size());

        // Genera i report della valutazione
        new EvaluationReport(projectName, evaluationResults.getAvgEvaluationsList(), "avg").generateReport();
        new EvaluationReport(projectName, evaluationResults.getMergeEvaluationsList(), "details").generateReport();

    }




}
