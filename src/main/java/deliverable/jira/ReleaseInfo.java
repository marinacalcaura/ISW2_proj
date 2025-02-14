package deliverable.jira;

import deliverable.entities.Release;
import deliverable.utils.JSON;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReleaseInfo {

    private String proj;

    public ReleaseInfo(String projName){
        proj = projName.toUpperCase();
    }

    //ottengo la lista delle releases rilasciate (con campo released = true)
    //non considero quelle con la data mancante e inoltre la lista viene ordinata by date
    public List<Release> getReleases() throws JSONException, IOException, ParseException {
        Map<Date, String> releaseMap = new HashMap<>();
        List<Release> releaseInfoList = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String apiUrl = "https://issues.apache.org/jira/rest/api/latest/project/" + this.proj + "/version";
        JSONObject jsonResponse = JSON.jsonObjectFromUrl(apiUrl);
        JSONArray releaseArray = jsonResponse.getJSONArray("values");

        for (int i = 0; i < releaseArray.length(); i++) {
            JSONObject release = releaseArray.getJSONObject(i);

            if (release.optBoolean("released", false)) {
                String releaseDateString = release.optString("releaseDate", "");
                if (!releaseDateString.isEmpty()) {
                    Date releaseDate = dateFormat.parse(releaseDateString);
                    String releaseName = release.optString("name", "Unknown");
                    releaseMap.put(releaseDate, releaseName);
                }
            }
        }

        Map<Date, String> sortedReleases = new TreeMap<>(releaseMap);
        int index = 1;

        for (Map.Entry<Date, String> entry : sortedReleases.entrySet()) {
            releaseInfoList.add(new Release(index++, entry.getValue(), entry.getKey()));
        }

        return releaseInfoList;
    }

    public static Release getReleaseByName(String releaseName, List<Release> releasesList) {
        for (Release rel : releasesList) {
            if (rel.getReleaseName().equals(releaseName)) {
                return rel;
            }
        }
        return null; // Nessuna release trovata
    }

    public static Release getReleaseByDate(Date date, List<Release> releasesList) {
        for (Release rel : releasesList) {
            if (rel.getReleaseDate().after(date)) {
                return rel;
            }
        }
        return null; // Nessuna release trovata dopo la data
    }

    public static Release getLastRelease(List<Release> releasesList) {
        if (releasesList.isEmpty()) {
            return null;
        }

        Release lastRelease = releasesList.get(0);
        for (Release rel : releasesList) {
            if (rel.getReleaseDate().after(lastRelease.getReleaseDate())) {
                lastRelease = rel;
            }
        }
        return lastRelease;
    }

    public static List<Release> getFirstReleases(List<Release> releasesList, int maxReleaseID) {
        List<Release> filteredReleases = new ArrayList<>();
        for (Release rel : releasesList) {
            if (rel.getReleaseId() <= maxReleaseID) {
                filteredReleases.add(rel);
            }
        }
        return filteredReleases;
    }
}
