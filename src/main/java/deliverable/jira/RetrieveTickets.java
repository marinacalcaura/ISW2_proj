package deliverable.jira;

import deliverable.entities.Release;
import deliverable.entities.Ticket;
import deliverable.utils.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RetrieveTickets {
    private String proj;

    public RetrieveTickets(String projName){
        proj = projName.toUpperCase();
    }

    private static Ticket createTicketInstance(int index, JSONArray issues, List<Release> releasesList) throws ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        try {
            JSONObject issueObject = issues.getJSONObject(index % 1000);
            String ticketKey = issueObject.getString("key");
            JSONObject fields = issueObject.getJSONObject("fields");

            Date resolutionDate = formatter.parse(fields.getString("resolutiondate"));
            Date creationDate = formatter.parse(fields.getString("created"));

            //recupero le affected version
            List<Release> affectedVersions = new ArrayList<>();
            JSONArray versionsArray = fields.getJSONArray("versions");
            for (int i = 0; i < versionsArray.length(); i++) {
                JSONObject versionObject = versionsArray.getJSONObject(i);
                String versionName = versionObject.optString("name", null);
                if (versionName != null) {
                    Release release = ReleaseInfo.getReleaseByName(versionName, releasesList);
                    if (release != null) {
                        affectedVersions.add(release);
                    }
                }
            }


            Release openVersion = ReleaseInfo.getReleaseByDate(creationDate, releasesList);
            Release fixVersion = ReleaseInfo.getReleaseByDate(resolutionDate, releasesList);

            //creo il ticket se entrambe le versioni sono valide
            if (openVersion != null && fixVersion != null) {
                return new Ticket(ticketKey, openVersion, fixVersion, affectedVersions);
            } else {
                return null;
            }

        } catch (JSONException e) {
            // Salto il ticket se il JSON non è completo
            return null;
        }
    }

    public List<Ticket> getTickets(List<Release> releasesList) throws JSONException, IOException, ParseException {

        List<Ticket> ticketsList = new ArrayList<>();
        int startAt = 0;
        int maxResults = 1000;
        int totalResults;

        do {
            String url = buildJiraQueryUrl(startAt, maxResults);
            JSONObject jsonResponse = JSON.jsonObjectFromUrl(url);
            JSONArray issuesArray = jsonResponse.getJSONArray("issues");
            totalResults = jsonResponse.getInt("total");

            List<Ticket> newTickets = new ArrayList<>();
            for (int index = 0; index < issuesArray.length(); index++) {
                try {
                    Ticket ticket = createTicketInstance(index, issuesArray, releasesList);
                    if (ticket != null) {
                        newTickets.add(ticket);
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            ticketsList.addAll(newTickets);
            startAt += maxResults;

        } while (startAt < totalResults);

        return ticketsList;
    }

    private String buildJiraQueryUrl(int startAt, int maxResults) {
        return String.format(
                "https://issues.apache.org/jira/rest/api/2/search?jql=project=%%22%s%%22AND%%22issueType%%22=%%22Bug%%22AND"
                        + "(%%22status%%22=%%22closed%%22OR%%22status%%22=%%22resolved%%22)AND"
                        + "%%22resolution%%22=%%22fixed%%22&fields=key,resolutiondate,versions,created&startAt=%d&maxResults=%d",
                this.proj, startAt, maxResults
        );
    }


    /*un ticket è consistente se ha le affected versions disponibili, nessuna affected version
     viene dopo o è uguale alla fixed version. l'opening version deve essere affected
     */
    public boolean isConsistentTicket(Ticket ticket) {
        if (ticket.getAffectedVersions() == null || ticket.getAffectedVersions().isEmpty()) {
            return false;
        }

        boolean hasOVInAffectedVersions = false;
        boolean hasInvalidAV = false;

        for (Release av : ticket.getAffectedVersions()) {
            if (av.getReleaseId() == ticket.getOpeningVersion().getReleaseId()) {
                hasOVInAffectedVersions = true;
            }
            if (av.getReleaseId() >= ticket.getFixedVersion().getReleaseId()) {
                hasInvalidAV = true;
                break;
            }
        }

        return hasOVInAffectedVersions && !hasInvalidAV;
    }


    /*imposto l'injected version e aggiornando le affectedversion garantendo che siano
    comprese tra IV e FV-1
     */
    public Ticket adjustTicket(Ticket ticket, List<Release> releasesList) {
        if (ticket.getAffectedVersions().isEmpty()) {
            return ticket;
        }
        ticket.setInjectedVersion(ticket.getAffectedVersions().get(0)); // IV è la prima AV

        List<Release> updatedAVs = new ArrayList<>();
        for (Release rel : releasesList) {
            if (rel.getReleaseId() >= ticket.getInjectedVersion().getReleaseId() && rel.getReleaseId() < ticket.getFixedVersion().getReleaseId()) {
                updatedAVs.add(new Release(rel.getReleaseId(), rel.getReleaseName(), rel.getReleaseDate()));
            }
        }
        ticket.setAffectedVersions(updatedAVs);
        return ticket;
    }

    /* imposto l'injected version (AV) utilizzando la tecnica della proporzione
     IV= max(1, FV - (FV - OV) * P).
     */
    public Ticket setInitialAV(Ticket ticket, List<Release> releasesList, Double proportion) {
        int initialAVid = Math.max(1, (int) (ticket.getFixedVersion().getReleaseId() - (ticket.getFixedVersion().getReleaseId() - ticket.getOpeningVersion().getReleaseId()) * proportion));

        for (Release rel : releasesList) {
            if (rel.getReleaseId() == initialAVid) {
                ticket.setAffectedVersions(Collections.singletonList(new Release(rel.getReleaseId(), rel.getReleaseName(), rel.getReleaseDate())));
                break;
            }
        }
        return ticket;
    }

    public static List<Ticket> getFirstTickets(List<Ticket> ticketsList, int maxFVid) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            if (ticket.getFixedVersion().getReleaseId() <= maxFVid) {
                result.add(ticket);
            }
        }
        return result;
    }

    public List<Ticket> filterConsistentTickets(List<Ticket> tickets, List<Release> releases) {
        List<Ticket> consistentTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (isConsistentTicket(ticket)) {
                consistentTickets.add(adjustTicket(ticket, releases));
            }
        }
        return consistentTickets;
    }

    /*recupero i ticket inconsistenti verificando quali ticket mancano nell'insieme dei ticket consistenti
     */
    public List<Ticket> extractInconsistentTickets(List<Ticket> allTickets, List<Ticket> consistentTickets) {
        Set<String> consistentKeys = new HashSet<>();
        for (Ticket ticket : consistentTickets) {
            consistentKeys.add(ticket.getTicketKey());
        }

        List<Ticket> inconsistentTickets = new ArrayList<>();
        for (Ticket ticket : allTickets) {
            if (!consistentKeys.contains(ticket.getTicketKey())) {
                inconsistentTickets.add(ticket);
            }
        }
        return inconsistentTickets;
    }

    /*aggiusta i ticket inconsistenti utilizzando la tecnica della proporzione di cold-start */
    public List<Ticket> refineTicketsWithProportion(List<Ticket> allTickets, List<Ticket> consistentTickets, List<Release> releases, Double proportion) {
        List<Ticket> inconsistentTickets = extractInconsistentTickets(allTickets, consistentTickets);
        List<Ticket> adjustedTickets = new ArrayList<>();

        for (Ticket ticket : inconsistentTickets) {
            Ticket adjustedTicket = setInitialAV(ticket, releases, proportion);
            adjustedTicket = adjustTicket(adjustedTicket, releases);
            adjustedTickets.add(adjustedTicket);
        }

        consistentTickets.addAll(adjustedTickets);
        return consistentTickets;
    }


}
