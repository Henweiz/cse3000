package Tree;

import java.util.ArrayList;
import java.util.List;

public class Dataset {

    private List<LabelInstances> datasets;
    private int size;

    private float[] totalRunTime;


    public Dataset(){
        datasets = new ArrayList<LabelInstances>();
        size = 0;
        totalRunTime = new float[1];
    }

    public Dataset(int numberOfAlgs) {
        datasets = new ArrayList<>();
        size = 0;
        totalRunTime = new float[numberOfAlgs];
    }

    public void addInstances(LabelInstances instances) {
        datasets.add(instances);
        size += instances.getSize();
        for (FeatureRow f : instances.getList()) {
            for (int i = 0; i < totalRunTime.length; i++) {
                totalRunTime[i] += f.getRuntimes()[i];
            }
        }
    }

    public int getSize() {
        return size;
    }
    public List<LabelInstances> getDatasets() {
        return datasets;
    }

    public LabelInstances getLabelInstances(int label) {
        for (LabelInstances instances : datasets) {
            if (label == instances.getLabel()){
                return instances;
            }
        }
        return new LabelInstances(label, datasets.get(0).getFeatureSize());
    }

    public void calcFreqCounters(){
        for (LabelInstances set : datasets) {
            set.calcFreqCounter();
        }
    }

    public int getFreqFeature(int f) {
        int count = 0;
        for (LabelInstances labelInstances : this.datasets) {
            for (FeatureRow row: labelInstances.getList()) {
                if (row.hasFeature(f)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getDataSize() {
        int count = 0;
        for (LabelInstances labelInstances: datasets) {
            count += labelInstances.getSize();
        }
        return count;
    }

    public Dataset getDatasetWithFeature(int f) {
        Dataset data = new Dataset();

        for (LabelInstances labelInstances : this.datasets) {
            LabelInstances newLabelInstances = new LabelInstances(labelInstances.getLabel(), labelInstances.getFeatureSize());
            for (FeatureRow row: labelInstances.getList()) {
                if (row.hasFeature(f)) {
                    newLabelInstances.addList(row);
                }
            }
            data.addInstances(newLabelInstances);
        }

        return data;
    }

    public Dataset getDatasetWithoutFeature(int f) {
        Dataset data = new Dataset();

        for (LabelInstances labelInstances : this.datasets) {
            LabelInstances newLabelInstances = new LabelInstances(labelInstances.getLabel(), labelInstances.getFeatureSize());
            for (FeatureRow row: labelInstances.getList()) {
                if (!row.hasFeature(f)) {
                    newLabelInstances.addList(row);
                }
            }
            data.addInstances(newLabelInstances);
        }

        return data;
    }

    public float[] getTotalRunTime() {
        return totalRunTime;
    }
}
