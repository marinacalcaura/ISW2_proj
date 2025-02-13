package deliverable.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import deliverable.entities.JavaClass;
import deliverable.entities.Release;
import deliverable.entities.ReleaseCommits;
import deliverable.entities.Ticket;
import deliverable.jira.ReleaseInfo;
import deliverable.utils.JavaClassUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;



public class RetrieveGitInfo {

    private final Repository repo;
    private final Git git;
    private final List<Ticket> ticketsWithAV;
    private final List<Release> releases;

    public RetrieveGitInfo(String path, List<Ticket> ticketsList, List<Release> releasesList) throws IOException {
        this.repo = new FileRepository(path + "/.git");
        this.git = new Git(this.repo);
        this.releases = releasesList;
        this.ticketsWithAV = filterValidTickets(ticketsList);
    }

    //filtra i ticket con AV valide
    private List<Ticket> filterValidTickets(List<Ticket> ticketsList) {
        List<Ticket> validTickets = new ArrayList<>();
        for (Ticket ticket : ticketsList) {
            if (ticket.getAffectedVersions() != null && !ticket.getAffectedVersions().isEmpty()) {
                validTickets.add(ticket);
            }
        }
        return validTickets;
    }

    //recupera tutti i commit da tutti i branch nel repository
    public List<RevCommit> retrieveAllCommits() throws GitAPIException, IOException {
        List<RevCommit> allCommits = new ArrayList<>();
        List<Ref> branches = git.branchList().setListMode(ListMode.ALL).call();

        for (Ref branch : branches) {
            List<RevCommit> branchCommits = getCommitsFromBranch(branch);
            for (RevCommit commit : branchCommits) {
                if (!allCommits.contains(commit)) {
                    allCommits.add(commit);
                }
            }
        }
        return allCommits;
    }

    private List<RevCommit> getCommitsFromBranch(Ref branch) {
        List<RevCommit> commits = new ArrayList<>();
        try {
            Iterable<RevCommit> log = git.log().add(repo.resolve(branch.getName())).call();
            for (RevCommit commit : log) {
                commits.add(commit);
            }
        } catch (GitAPIException | IOException e) {

        }
        return commits;
    }


    //recupera una mappatura dei nomi delle classi al loro contenuto da un commit
    private Map<String, String> getClasses(RevCommit commit) throws IOException {
        Map<String, String> javaClasses = new HashMap<>();

        if (repo == null) {
            throw new IllegalStateException("Il repository Git non è stato inizializzato correttamente!");
        }

        if (commit == null) {
            throw new IllegalArgumentException("Il commit fornito è nullo!");
        }

        if (commit.getTree() == null) {
            throw new IllegalStateException("Il commit " + commit.getId().getName() + " non ha un albero valido!");
        }

        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("/test/")) {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    if (objectId == null) {
                        System.out.println("Attenzione: impossibile ottenere l'ID dell'oggetto per " + treeWalk.getPathString());
                        continue;
                    }
                    ObjectLoader loader = repo.open(objectId);
                    String fileContent = new String(loader.getBytes(), StandardCharsets.UTF_8);
                    javaClasses.put(treeWalk.getPathString(), fileContent);
                }
            }
        }
        return javaClasses;
    }

    //trova l'ultimo commit in base alla data del commit
    private static Optional<RevCommit> getLastCommit(List<RevCommit> commitsList) {
        return commitsList.stream()
                .max(Comparator.comparing(commit -> commit.getCommitterIdent().getWhen()));
    }

    //recupera i commit di una specifica release

    public static ReleaseCommits getCommitsOfRelease(List<RevCommit> commitsList, Release release, Date firstDate) {
        Date lastDate = release.getReleaseDate();
        List<RevCommit> matchingCommits = new ArrayList<>();

        for (RevCommit commit : commitsList) {
            Date commitDate = commit.getCommitterIdent().getWhen();
            if (commitDate.after(firstDate) && !commitDate.after(lastDate)) {
                matchingCommits.add(commit);
            }
        }

        if (matchingCommits.isEmpty()) {
            System.out.println("Nessun commit valido per release " + release.getReleaseId());
            return null;
        }

        RevCommit lastCommit = getLastCommit(matchingCommits).orElse(null);
        return new ReleaseCommits(release, matchingCommits, lastCommit);
    }

    //trova la release associata a un determinato commit

    public static Optional<Release> getReleaseOfCommit(RevCommit commit, List<ReleaseCommits> relCommAssociations) {
        if (commit == null) {
            System.out.println("Errore: Il commit fornito è null!");
            return Optional.empty();
        }

        if (relCommAssociations == null || relCommAssociations.isEmpty()) {
            System.out.println("Attenzione: Lista relCommAssociations è vuota o null!");
            return Optional.empty();
        }

        for (ReleaseCommits relComm : relCommAssociations) {
            if (relComm != null && relComm.getCommits() != null && relComm.getCommits().contains(commit)) {
                return Optional.of(relComm.getRelease());
            }
        }
        return Optional.empty();
    }

    //associa le release ai loro commit
    public List<ReleaseCommits> getRelCommAssociations(List<RevCommit> allCommitsList) throws ParseException {
        List<ReleaseCommits> relCommAssociations = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date firstDate = formatter.parse("1900-01-01");

        for (Release release : releases) {
            relCommAssociations.add(getCommitsOfRelease(allCommitsList, release, firstDate));
            firstDate = release.getReleaseDate();
        }

        return relCommAssociations;
    }

    //associa le release alle classi Java

    public void getRelClassesAssociations(List<ReleaseCommits> relCommAssociations) throws IOException {
        for (ReleaseCommits relComm : relCommAssociations) {
            if (relComm == null || relComm.getLastCommit() == null) {
                System.out.println("⚠ Nessun commit valido per release " + (relComm != null ? relComm.getRelease().getReleaseId() : "UNKNOWN"));
                continue;
            }

            try {
                relComm.setJavaClasses(getClasses(relComm.getLastCommit()));
            } catch (IOException ignored) {
                System.err.println("Errore nel recupero delle classi per release " + relComm.getRelease().getReleaseId());
            }
        }
    }

    //recupera i commit associati a un determinato ticket
    private List<RevCommit> getTicketCommits(Ticket ticket) throws GitAPIException, IOException {
        List<RevCommit> associatedCommits = new ArrayList<>();
        List<Ref> branchesList = this.git.branchList().setListMode(ListMode.ALL).call();

        for (Ref branch : branchesList) {
            Iterable<RevCommit> commitsList = this.git.log().add(repo.resolve(branch.getName())).call();

            for (RevCommit commit : commitsList) {
                if (commit == null) {
                    System.out.println("ATTENZIONE: Commit nullo trovato, saltato.");
                    continue;
                }
                String comment = commit.getFullMessage();

                if ((comment.contains(ticket.getTicketKey() + ":") ||
                        comment.contains(ticket.getTicketKey() + ".") ||
                        comment.contains(ticket.getTicketKey() + "]") ||
                        comment.contains(ticket.getTicketKey() + " ")) &&
                        !associatedCommits.contains(commit)) {

                    associatedCommits.add(commit);
                    System.out.println("Commit associato al ticket: " + commit.getId().getName());
                }
            }
        }
        System.out.println("Lista finale di commit associati:");
        for (RevCommit c : associatedCommits) {
            System.out.println("✔ " + c.getId().getName());
        }
        return associatedCommits;
    }




    //recupera le classi Java modificate da un commit
    private List<String> getModifiedClasses(RevCommit commit) throws IOException {
        List<String> modifiedClasses = new ArrayList<>();

        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
             ObjectReader reader = repo.newObjectReader()) {

            diffFormatter.setRepository(repo);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, commit.getTree());

            RevCommit parent = commit.getParent(0);
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, parent.getTree());

            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            for (DiffEntry entry : entries) {
                if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY && entry.getNewPath().endsWith(".java") && !entry.getNewPath().contains("/test/")) {
                    modifiedClasses.add(entry.getNewPath());
                }
            }

        } catch (ArrayIndexOutOfBoundsException ignored) {}

        return modifiedClasses;
    }

    //etichetta le classi Java come buggy in base ai commit
    private void doLabeling(List<JavaClass> javaClasses, Ticket ticket, List<ReleaseCommits> relCommAssociations) throws GitAPIException, IOException {
        List<RevCommit> commitsAssociatedWIssue = getTicketCommits(ticket);

        System.out.println("Processing ticket: " + ticket.getTicketKey());
        System.out.println("Number of associated commits: " + commitsAssociatedWIssue.size());

        for (RevCommit commit : commitsAssociatedWIssue) {
            Optional<Release> releaseOpt = getReleaseOfCommit(commit, relCommAssociations);
            if (releaseOpt.isPresent()) {
                List<String> modifiedClasses = getModifiedClasses(commit);
                System.out.println("Commit: " + commit.getName() + " | Release: " + releaseOpt.get().getReleaseId());

                for (String modifClass : modifiedClasses) {
                    JavaClassUtils.markBuggyJavaClasses(javaClasses, modifClass, ticket.getInjectedVersion(), releaseOpt.get());
                }
            }
        }
    }

    //etichetta le classi sulla base delle informazioni sui ticket
    public List<JavaClass> labelClasses(List<ReleaseCommits> relCommAssociations) throws GitAPIException, IOException {
        List<JavaClass> javaClasses = JavaClassUtils.buildJavaClassList(relCommAssociations);

        for (Ticket ticket : this.ticketsWithAV) {
            try {
                doLabeling(javaClasses, ticket, relCommAssociations);
            } catch (Exception e) {
                System.err.println("Errore nell'etichettare il ticket: " + ticket.getTicketKey());
                e.printStackTrace();
            }
        }

        return javaClasses;
    }


    //assegna i commit alle classi Java
    public void assignCommitsToClasses(List<JavaClass> javaClasses, List<RevCommit> commits, List<ReleaseCommits> relCommAssociations) throws IOException {
        for (RevCommit commit : commits) {
            Optional<Release> optionalRelease = getReleaseOfCommit(commit, relCommAssociations);
            if (optionalRelease.isPresent()) {
                Release associatedRelease = optionalRelease.get();
                List<String> modifiedClasses = getModifiedClasses(commit);
                for (String modifClass : modifiedClasses) {
                    JavaClassUtils.assignCommitsToJavaClasses(javaClasses, modifClass, associatedRelease, commit);
                }
            }
        }
    }

    //recupera le classi Java attualmente presenti nel repository
    public List<JavaClass> getCurrentClasses(List<RevCommit> allCommits) throws IOException {
        Release lastRelease = ReleaseInfo.getLastRelease(this.releases);
        Date lastReleaseDate = lastRelease.getReleaseDate();
        Release futureRelease = new Release(lastRelease.getReleaseId() + 1, "Future", new Date());

        ReleaseCommits currentRelComm = getCommitsOfRelease(allCommits, futureRelease, lastReleaseDate);
        RevCommit lastCommit = currentRelComm.getLastCommit();

        if (lastCommit != null) {
            currentRelComm.setJavaClasses(getClasses(lastCommit));
        }

        List<ReleaseCommits> currentRelCommList = new ArrayList<>();
        currentRelCommList.add(currentRelComm);
        List<JavaClass> javaClassInstances = JavaClassUtils.buildJavaClassList(currentRelCommList);

        for (RevCommit commit : currentRelComm.getCommits()) {
            List<String> modifiedClasses = getModifiedClasses(commit);
            for (String modifClass : modifiedClasses) {
                JavaClassUtils.assignCommitsToJavaClasses(javaClassInstances, modifClass, futureRelease, commit);
            }
        }
        return javaClassInstances;
    }

    //calcola il numero di righe aggiunte in una entry di commit
    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int total = 0;
        for (Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            total += edit.getEndA() - edit.getBeginA();
        }
        return total;
    }

    //calcola il numero di righe eliminate in una entry di commit
    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {
        int total = 0;
        for (Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            total += edit.getEndB() - edit.getBeginB();
        }
        return total;
    }

    //calcola la lista delle righe aggiunte ed eliminate per ogni commit in una classe Java
    public void computeAddedAndDeletedLinesList(JavaClass javaClass) throws IOException {
        for (RevCommit commit : javaClass.getCommits()) {
            if (commit.getParentCount() == 0) continue; //salta i commit iniziali

            try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                diffFormatter.setRepository(this.repo);
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

                RevCommit parentCommit = commit.getParent(0);
                List<DiffEntry> diffs = diffFormatter.scan(parentCommit.getTree(), commit.getTree());

                for (DiffEntry entry : diffs) {
                    if (entry.getNewPath().equals(javaClass.getName())) {
                        javaClass.getAddedLinesList().add(getAddedLines(diffFormatter, entry));
                        javaClass.getDeletedLinesList().add(getDeletedLines(diffFormatter, entry));
                    }
                }
            }
        }
    }
}

