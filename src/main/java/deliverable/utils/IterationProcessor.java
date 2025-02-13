package deliverable.utils;

import deliverable.entities.JavaClass;
import deliverable.entities.Release;
import deliverable.entities.ReleaseCommits;
import deliverable.entities.Ticket;
import deliverable.files.DataExporter;
import deliverable.github.JavaClassMetricsCalculator;
import deliverable.github.RetrieveGitInfo;
import deliverable.jira.ReleaseInfo;
import deliverable.jira.RetrieveTickets;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;


public class IterationProcessor {


    private final ReleaseInfo releaseInfo;
    private final RetrieveTickets retrieveTickets;

    public IterationProcessor(ReleaseInfo releaseInfo,RetrieveTickets retrieveTickets) {
        this.releaseInfo = releaseInfo;
        this.retrieveTickets = retrieveTickets;
    }



    //processa un'iterazione della fase di training/testing.

    public void processIteration(int iteration, String projName, List<Release> releasesList, List<Ticket> ticketsList,
                                 List<Ticket> coldStartProjConsistentTickets, int lastReleaseID) throws IOException, GitAPIException, ParseException {

        List<Release> iterReleasesList = releaseInfo.getFirstReleases(releasesList, iteration);
        List<Ticket> iterTicketsList = retrieveTickets.getFirstTickets(ticketsList, iteration);
        List<Ticket> consistentTicketsList = retrieveTickets.filterConsistentTickets(iterTicketsList, iterReleasesList);

        Double proportion;
        if (consistentTicketsList.size() >= 5) {
            proportion = Proportion.calculateAverageProportion(consistentTicketsList);
        } else {
            proportion = Proportion.calculateAverageProportion(coldStartProjConsistentTickets);
        }

        List<Ticket> adjustedTicketsList = retrieveTickets.refineTicketsWithProportion(iterTicketsList, consistentTicketsList, iterReleasesList, proportion);

        RetrieveGitInfo retGitInfo = new RetrieveGitInfo("C:\\Users\\Marina\\Desktop\\" + projName,
                adjustedTicketsList, iterReleasesList);
        List<RevCommit> allCommitsList = retGitInfo.retrieveAllCommits();
        List<ReleaseCommits> relCommAssociationsList = retGitInfo.getRelCommAssociations(allCommitsList);
        retGitInfo.getRelClassesAssociations(relCommAssociationsList);

        List<JavaClass> javaClassesList = retGitInfo.labelClasses(relCommAssociationsList);
        retGitInfo.assignCommitsToClasses(javaClassesList, allCommitsList, relCommAssociationsList);

        JavaClassMetricsCalculator computeMetrics = new JavaClassMetricsCalculator(retGitInfo, javaClassesList);
        javaClassesList = computeMetrics.computeAllMetrics();

        if (iteration == lastReleaseID) {
            processTestingPhase(projName, lastReleaseID, javaClassesList, iterReleasesList, retGitInfo, allCommitsList);
        } else {
            DataExporter labelingTraining = new DataExporter(projName, CsvNamesEnum.TRAINING, iteration, javaClassesList);
            labelingTraining.writeOnArff(true);
        }
    }

    private static void generateCsvForReleases(String projectName, List<JavaClass> javaClasses, int lastReleaseId) throws IOException {
        System.out.println("Inizio generazione dei file ARFF per le release...");

        for (int releaseId = 2; releaseId <= lastReleaseId; releaseId++) {
            List<JavaClass> filteredClasses = JavaClassUtils.getJavaClassesByRelease(javaClasses, releaseId);
            DataExporter labelingFile = new DataExporter(projectName, CsvNamesEnum.TESTING, releaseId - 1, filteredClasses);

            try {
                labelingFile.writeOnArff(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Generazione completata per tutte le release!");
    }

    private void processTestingPhase(String projName, int lastReleaseID, List<JavaClass> javaClassesList,
                                     List<Release> iterReleasesList, RetrieveGitInfo retGitInfo, List<RevCommit> allCommitsList) throws IOException {

        List<JavaClass> javaClassesHalfList = JavaClassUtils.filterHalfReleases(javaClassesList, iterReleasesList);
        generateCsvForReleases(projName, javaClassesHalfList, lastReleaseID / 2);

        DataExporter labelingBuggy = new DataExporter(projName, CsvNamesEnum.BUGGY, 0, javaClassesHalfList);
        labelingBuggy.writeOnCsv();

        List<JavaClass> currentJavaClassesList = retGitInfo.getCurrentClasses(allCommitsList);
        JavaClassMetricsCalculator computeCurrentMetrics = new JavaClassMetricsCalculator(retGitInfo, currentJavaClassesList);
        currentJavaClassesList = computeCurrentMetrics.computeAllMetrics();

        DataExporter labelingCurrent = new DataExporter(projName, CsvNamesEnum.CURRENT, 0, currentJavaClassesList);
        labelingCurrent.writeOnCsv();
    }
}

