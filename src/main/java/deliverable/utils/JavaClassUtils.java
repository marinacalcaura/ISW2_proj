package deliverable.utils;

import deliverable.entities.JavaClass;
import deliverable.entities.Release;
import deliverable.entities.ReleaseCommits;
import deliverable.jira.ReleaseInfo;
import deliverable.jira.RetrieveTickets;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaClassUtils {
    private static final Logger logger = Logger.getLogger(JavaClassUtils.class.getName());


    private JavaClassUtils() {
        throw new IllegalStateException("Utility class");
    }

    //costruisce una lista di istanze di JavaClass dalle associazioni release-commit
    public static List<JavaClass> buildJavaClassList(List<ReleaseCommits> relCommAssociations) {
        List<JavaClass> javaClasses = new ArrayList<>();
        for (ReleaseCommits relComm : relCommAssociations) {
            if (relComm != null) {
                if (relComm.getJavaClasses() == null) {
                    logger.info("Attenzione: getJavaClasses() è null per la release " + relComm.getRelease().getReleaseId());
                } else {
                    for (Map.Entry<String, String> entry : relComm.getJavaClasses().entrySet()) {
                        javaClasses.add(new JavaClass(entry.getKey(), entry.getValue(), relComm.getRelease()));
                    }
                }
            }
        }
        return javaClasses;
    }

    //aggiorna lo stato di buggyness delle classi Java in base alle loro modifiche
    public static void markBuggyJavaClasses(List<JavaClass> javaClasses, String className, Release iv, Release fv) {
        for (JavaClass javaClass : javaClasses) {
            if (javaClass.getName().equals(className) &&
                    javaClass.getRelease().getReleaseId() >= iv.getReleaseId() &&
                    javaClass.getRelease().getReleaseId() < fv.getReleaseId()) {
                javaClass.setBuggy(true);
            }
        }
    }

    //associa commit alle classi Java in base alle modifiche
    public static void assignCommitsToJavaClasses(List<JavaClass> javaClasses, String className, Release associatedRelease, RevCommit commit) {
        for (JavaClass javaClass : javaClasses) {
            if (javaClass.getName().equals(className) &&
                    javaClass.getRelease().getReleaseId() == associatedRelease.getReleaseId()) {
                javaClass.getCommits().add(commit);
            }
        }
    }

    //filtra le classi Java, mantenendo solo quelle appartenenti alla prima metà delle release
    public static List<JavaClass> filterHalfReleases(List<JavaClass> javaClassesList, List<Release> releasesList) {
        List<JavaClass> filteredClasses = new ArrayList<>();
        int halfReleaseId = ReleaseInfo.getLastRelease(releasesList).getReleaseId() / 2;
        for (JavaClass javaClass : javaClassesList) {
            if (javaClass.getRelease().getReleaseId() <= halfReleaseId) {
                filteredClasses.add(javaClass);
            }
        }
        return filteredClasses;
    }

    //filtra le classi Java in base a uno specifico ID di release

    public static List<JavaClass> getJavaClassesByRelease(List<JavaClass> javaClassesList, int releaseID) {
        List<JavaClass> filteredClasses = new ArrayList<>();
        for (JavaClass javaClass : javaClassesList) {
            if (javaClass.getRelease().getReleaseId() == releaseID) {
                filteredClasses.add(javaClass);
            }
        }
        return filteredClasses;
    }

}
