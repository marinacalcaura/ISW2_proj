package deliverable.utils;

import deliverable.entities.Release;
import deliverable.entities.Ticket;
import deliverable.jira.ReleaseInfo;
import deliverable.jira.RetrieveTickets;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;


public class Proportion {


    public enum ProjectsEnum {

        AVRO,
        OPENJPA,
        SYNCOPE,
        TAJO,
        ZOOKEEPER

    }

    private Proportion() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger logger = Logger.getLogger(Proportion.class.getName());

    //recupero i ticket consistenti dai progetti specificati sopra
    public static List<Ticket> coldStartRetrieveConsistentIssues() throws JSONException {
        List<Ticket> allConsistentTickets = new ArrayList<>();
        for (ProjectsEnum proj : ProjectsEnum.values()) {
            List<Ticket> projectTickets = retrieveProjectConsistentIssues(proj.toString());
            allConsistentTickets.addAll(projectTickets);
        }
        return allConsistentTickets;
    }

    //recupero le issue consistenti per un progetto specifico
    private static List<Ticket> retrieveProjectConsistentIssues(String projectName) {
        try {
            ReleaseInfo releaseInfo = new ReleaseInfo(projectName);
            List<Release> coldStartReleases = releaseInfo.getReleases();

            RetrieveTickets retrieveTickets = new RetrieveTickets(projectName);
            List<Ticket> coldStartTickets = retrieveTickets.getTickets(coldStartReleases);
            return retrieveTickets.filterConsistentTickets(coldStartTickets, coldStartReleases);
        } catch (JSONException | IOException | ParseException e) {
            logger.info("Error retrieving issues for project: " + projectName);
            return Collections.emptyList();
        }
    }

    //calcolo il valore medio della proporzione per una lista di ticket

    public static Double calculateAverageProportion(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Ticket ticket : tickets) {
            sum += computeTicketProportion(ticket);
        }
        return sum / tickets.size();
    }

    /*calcolo la proporzione P per un singolo ticket.
    Formula: P = (FV - IV) / (FV - OV)*/

    private static double computeTicketProportion(Ticket ticket) {
        int fv = ticket.getFixedVersion().getReleaseId();
        int iv = ticket.getInjectedVersion().getReleaseId();
        int ov = ticket.getOpeningVersion().getReleaseId();

        return (fv > iv && fv > ov) ? (double) (fv - iv) / (fv - ov) : 0.0;
    }
}
