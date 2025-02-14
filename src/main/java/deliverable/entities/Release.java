package deliverable.entities;

import java.util.Date;

public class Release {

    private int releaseId;
    private String releaseName;
    private Date releaseDate;

    public Release(int releaseId, String releaseName, Date releaseDate) {
        this.releaseId = releaseId;
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
    }

    public int getReleaseId() {
        return releaseId;
    }
    public void setReleaseId(int releaseId) {
        this.releaseId = releaseId;
    }


    public String getReleaseName() {
        return releaseName;
    }
    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }


    public Date getReleaseDate() {
        return releaseDate;
    }
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }



}
