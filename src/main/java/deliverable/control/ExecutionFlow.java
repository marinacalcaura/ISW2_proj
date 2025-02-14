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
import java.util.logging.Logger;

public class ExecutionFlow {

    private ExecutionFlow() {
        throw new IllegalStateException("Utility class");
    }
    private static final Logger logger = Logger.getLogger(ExecutionFlow.class.getName());

    public static void analyzeProject(String projectName) throws Exception {
        //ottengo la lista delle release
        ReleaseInfo releaseInfo = new ReleaseInfo(projectName);
        List<Release> releaseList = releaseInfo.getReleases();


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
                    logger.severe("Errore durante il processamento dell'iterazione: " + i + " - " + e.getMessage());
                }
            }
        }

        // Eseguo la valutazione con Weka
        WekaInfo wekaInfo = new WekaInfo(projectName, (lastReleaseId / 2) - 1);
        EvaluationResults evaluationResults = wekaInfo.retrieveClassifiersEvaluation();


        // Genera i report della valutazione
        new EvaluationReport(projectName, evaluationResults.getAvgEvaluationsList(), "avg").generateReport();
        new EvaluationReport(projectName, evaluationResults.getMergeEvaluationsList(), "details").generateReport();

    }




}
