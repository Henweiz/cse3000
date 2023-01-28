package Tree;

import java.util.ArrayList;
import java.util.List;

public class ASPDataset {

    private List<Algorithm> algorithms;
    private int size;

    private int[] totalRunTime;

    private List<FeatureRow> dataset;

    private int featureSize;


    public ASPDataset(int numberOfAlgs) {
        algorithms = new ArrayList<>();
        dataset = new ArrayList<>();
        size = 0;
        totalRunTime = new int[numberOfAlgs];
    }

    public void addAlgorithm(Algorithm a) {
        algorithms.add(a);
    }

    public void addFeatureRow(FeatureRow f) {
        this.dataset.add(f);
        size++;
        this.featureSize = f.getFeatureSize();
        for (int i = 0; i < algorithms.size(); i++) {
            algorithms.get(i).addRunTime(f.getRuntimes()[i]);
            totalRunTime[i] += f.getRuntimes()[i];
        }
    }

    public int getSize() {
        return size;
    }
    public List<FeatureRow> getDataset() {
        return dataset;
    }

    public void calcFreqCounters(){
        for (Algorithm a : algorithms) {
            a.calcFreqCounters(dataset);
        }
    }

    public int getFreqFeature(int f) {
        int count = 0;
            for (FeatureRow row : dataset) {
                if (row.hasFeature(f)) {
                    count++;
            }
        }
        return count;
    }

    public FeatureRow getSpecificArray(int i) {
        return dataset.get(i);
    }

    public ASPDataset getDatasetWithFeature(int f) {
        ASPDataset data = new ASPDataset(totalRunTime.length);
        for (Algorithm a : algorithms) {
            data.addAlgorithm(new Algorithm(featureSize, a.getLabel()));
        }

        for (FeatureRow row: dataset) {
            if (row.hasFeature(f)) {
                data.addFeatureRow(row);
            }

        }

        return data;
    }

    public ASPDataset getDatasetWithoutFeature(int f) {
        ASPDataset data = new ASPDataset(totalRunTime.length);
        for (Algorithm a : algorithms) {
            data.addAlgorithm(new Algorithm(featureSize, a.getLabel()));
        }

        for (FeatureRow row: dataset) {
            if (!row.hasFeature(f)) {
                data.addFeatureRow(row);
            }

        }

        return data;
    }

    public int getLowestRunTimeLabel() {
        int label = Integer.MAX_VALUE;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < totalRunTime.length; i++) {
            System.out.println("label: " + i + " Runtime: " + totalRunTime[i]);
            if (min > totalRunTime[i]) {
                min = totalRunTime[i];
                label = i;
            }
        }
        return label;
    }

    public int[] getTotalRunTime() {
        return totalRunTime;
    }

    public List<Algorithm> getAlgorithms() {
        return algorithms;
    }
}

