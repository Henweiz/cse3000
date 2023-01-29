package Tree;

import java.util.ArrayList;
import java.util.List;

public class ASPDataset {

    // List of solution algorithms (labels).
    private List<Algorithm> algorithms;

    // Total size of the dataset.
    private int size;

    // Total runtime of each solution algorithm.
    private int[] totalRunTime;

    // List of feature instances.
    private List<FeatureRow> dataset;

    // Size of the feature set.
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

    public Algorithm getSpecificAlgorithm(int label) {
        for (Algorithm a : algorithms) {
            if (a.getLabel() == label) {
                return a;
            }
        }
        return null;
    }

    // Return dataset with only positive occurrences of feature f.
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

    // Return dataset with only negative occurrences of feature f.
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

    // Get the label of the algorithm with the lowest total run time.
    public int getLowestRunTimeLabel() {
        int label = Integer.MAX_VALUE;
        int min = Integer.MAX_VALUE;
        //int total = getSumRunTimes();
        for (int i = 0; i < totalRunTime.length; i++) {
            //System.out.println("label: " + i + " Runtime: " + totalRunTime[i]);
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

    // Return sum of all run times.
    public int getSumRunTimes() {
        int sum = 0;
        for (int t : totalRunTime) {
            sum += t;
        }
        return sum;
    }

    public List<Algorithm> getAlgorithms() {
        return algorithms;
    }
}

