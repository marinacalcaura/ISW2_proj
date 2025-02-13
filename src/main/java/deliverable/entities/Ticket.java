package deliverable.entities;

import java.util.List;

public class Ticket {

    private String ticketKey;
    private Release injectedVersion;
    private Release openingVersion;
    private Release fixedVersion;
    private List<Release> affectedVersions;

    public Ticket(String ticketKey, Release observedVersion, Release fixedVersion, List<Release> affectedVersions) {
        this.ticketKey = ticketKey;
        this.injectedVersion = null;
        this.openingVersion = observedVersion;
        this.fixedVersion = fixedVersion;
        this.affectedVersions = affectedVersions;
    }

    public String getTicketKey() {
        return ticketKey;
    }
    public void setTicketKey(String ticketKey) {
        this.ticketKey = ticketKey;
    }

    public Release getInjectedVersion() {
        return injectedVersion;
    }
    public void setInjectedVersion(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }
    public void setOpeningVersion(Release openingVersion) {
        this.openingVersion = openingVersion;
    }

    public Release getFixedVersion() {
        return fixedVersion;
    }
    public void setFixedVersion(Release fixedVersion) {
        this.fixedVersion = fixedVersion;
    }

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }
    public void setAffectedVersions(List<Release> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }
}
