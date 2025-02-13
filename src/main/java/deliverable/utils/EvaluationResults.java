package deliverable.utils;


import deliverable.entities.ClassifierEvaluation;

import java.util.ArrayList;
import java.util.List;

public class EvaluationResults {

    private List<ClassifierEvaluation> simpleRandomForestList;
    private List<ClassifierEvaluation> simpleNaiveBayesList;
    private List<ClassifierEvaluation> simpleIBkList;
    private List<ClassifierEvaluation> featureSelRandomForestList;
    private List<ClassifierEvaluation> featureSelNaiveBayesList;
    private List<ClassifierEvaluation> featureSelIBkList;
    private List<ClassifierEvaluation> samplingRandomForestList;
    private List<ClassifierEvaluation> samplingNaiveBayesList;
    private List<ClassifierEvaluation> samplingIBkList;
    private List<ClassifierEvaluation> costSensRandomForestList;
    private List<ClassifierEvaluation> costSensNaiveBayesList;
    private List<ClassifierEvaluation> costSensIBkList;

    private List<ClassifierEvaluation> avgEvaluationsList;
    private List<ClassifierEvaluation> mergeEvaluationsList = new ArrayList<>();

    public void mergeAll() {
        this.mergeEvaluationsList.addAll(simpleRandomForestList);
        this.mergeEvaluationsList.addAll(simpleNaiveBayesList);
        this.mergeEvaluationsList.addAll(simpleIBkList);
        this.mergeEvaluationsList.addAll(featureSelRandomForestList);
        this.mergeEvaluationsList.addAll(featureSelNaiveBayesList);
        this.mergeEvaluationsList.addAll(featureSelIBkList);
        this.mergeEvaluationsList.addAll(samplingRandomForestList);
        this.mergeEvaluationsList.addAll(samplingNaiveBayesList);
        this.mergeEvaluationsList.addAll(samplingIBkList);
        this.mergeEvaluationsList.addAll(costSensRandomForestList);
        this.mergeEvaluationsList.addAll(costSensNaiveBayesList);
        this.mergeEvaluationsList.addAll(costSensIBkList);

    }


    public List<ClassifierEvaluation> getSimpleRandomForestList() {
        return simpleRandomForestList;
    }

    public void setSimpleRandomForestList(List<ClassifierEvaluation> simpleRandomForestList) {
        this.simpleRandomForestList = simpleRandomForestList;
    }

    public List<ClassifierEvaluation> getSimpleNaiveBayesList() {
        return simpleNaiveBayesList;
    }

    public void setSimpleNaiveBayesList(List<ClassifierEvaluation> simpleNaiveBayesList) {
        this.simpleNaiveBayesList = simpleNaiveBayesList;
    }

    public List<ClassifierEvaluation> getSimpleIBkList() {
        return simpleIBkList;
    }

    public void setSimpleIBkList(List<ClassifierEvaluation> simpleIBkList) {
        this.simpleIBkList = simpleIBkList;
    }

    public List<ClassifierEvaluation> getFeatureSelRandomForestList() {
        return featureSelRandomForestList;
    }

    public void setFeatureSelRandomForestList(List<ClassifierEvaluation> featureSelRandomForestList) {
        this.featureSelRandomForestList = featureSelRandomForestList;
    }

    public List<ClassifierEvaluation> getFeatureSelNaiveBayesList() {
        return featureSelNaiveBayesList;
    }

    public void setFeatureSelNaiveBayesList(List<ClassifierEvaluation> featureSelNaiveBayesList) {
        this.featureSelNaiveBayesList = featureSelNaiveBayesList;
    }

    public List<ClassifierEvaluation> getFeatureSelIBkList() {
        return featureSelIBkList;
    }

    public void setFeatureSelIBkList(List<ClassifierEvaluation> featureSelIBkList) {
        this.featureSelIBkList = featureSelIBkList;
    }

    public List<ClassifierEvaluation> getSamplingRandomForestList() {
        return samplingRandomForestList;
    }

    public void setSamplingRandomForestList(List<ClassifierEvaluation> samplingRandomForestList) {
        this.samplingRandomForestList = samplingRandomForestList;
    }

    public List<ClassifierEvaluation> getSamplingNaiveBayesList() {
        return samplingNaiveBayesList;
    }

    public void setSamplingNaiveBayesList(List<ClassifierEvaluation> samplingNaiveBayesList) {
        this.samplingNaiveBayesList = samplingNaiveBayesList;
    }

    public List<ClassifierEvaluation> getSamplingIBkList() {
        return samplingIBkList;
    }

    public void setSamplingIBkList(List<ClassifierEvaluation> samplingIBkList) {
        this.samplingIBkList = samplingIBkList;
    }

    public List<ClassifierEvaluation> getCostSensRandomForestList() {
        return costSensRandomForestList;
    }

    public void setCostSensRandomForestList(List<ClassifierEvaluation> costSensRandomForestList) {
        this.costSensRandomForestList = costSensRandomForestList;
    }

    public List<ClassifierEvaluation> getCostSensNaiveBayesList() {
        return costSensNaiveBayesList;
    }

    public void setCostSensNaiveBayesList(List<ClassifierEvaluation> costSensNaiveBayesList) {
        this.costSensNaiveBayesList = costSensNaiveBayesList;
    }

    public List<ClassifierEvaluation> getCostSensIBkList() {
        return costSensIBkList;
    }

    public void setCostSensIBkList(List<ClassifierEvaluation> costSensIBkList) {
        this.costSensIBkList = costSensIBkList;
    }

    public List<ClassifierEvaluation> getAvgEvaluationsList() {
        return avgEvaluationsList;
    }

    public void setAvgEvaluationsList(List<ClassifierEvaluation> avgEvaluationsList) {
        this.avgEvaluationsList = avgEvaluationsList;
    }

    public List<ClassifierEvaluation> getMergeEvaluationsList() {
        return mergeEvaluationsList;
    }


    public void setMergeEvaluationsList(List<ClassifierEvaluation> mergeEvaluationsList) {
        this.mergeEvaluationsList = mergeEvaluationsList;
    }


}