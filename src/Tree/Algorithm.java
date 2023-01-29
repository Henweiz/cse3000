package Tree;

import java.util.List;

public class Algorithm {

    // Total runtime across all instances.
    private int totalRunTime;
    // Frequency counters
    private final int[] freqCounter;
    private final int[][] freqCounterPair;

    private final int label;

    // Check whether the frequency counters are already counted.
    private boolean calculated;


    public Algorithm(int size, int label) {
        this.totalRunTime = 0;
        this.freqCounter = new int[size];
        this.freqCounterPair = new int[size][size];
        this.label = label;
        this.calculated = false;
    }

    public void setTotalRunTime(int totalruntime) {
        this.totalRunTime = totalruntime;
    }

    public void addRunTime(int f) {
        this.totalRunTime += f;
    }

    public void calcFreqCounters(List<FeatureRow> data){
        // This extra check should not happen anymore
        if (calculated) {
            return;
        }

        for (FeatureRow features: data) {
            List<Integer> featureVector = features.getValues();

            for (int i = 0; i < featureVector.size(); i++) {
                // Count single features first.
                if (featureVector.get(i) == 1) {
                    freqCounter[i] += features.getRuntimes()[this.label];
                }

                for (int j = i; j < featureVector.size(); j++) {
                    // Count pair of features.
                    if (i == j) {
                        if (featureVector.get(i) == 1) {
                            freqCounterPair[i][i] += features.getRuntimes()[this.label];
                        }
                    }
                    if (featureVector.get(i) == 1 && i < j) {
                        if (featureVector.get(j) == 1) {
                            freqCounterPair[i][j] += features.getRuntimes()[this.label];
                            freqCounterPair[j][i] += features.getRuntimes()[this.label];
                        }
                    }
                }
            }
        }

        calculated = true;
    }

    public int[] getFreqCounter() {
        return freqCounter;
    }

    public int[][] getFreqCounterPair() {
        return freqCounterPair;
    }

    public int getLabel() {
        return label;
    }

    public int getTotalRunTime() {
        return totalRunTime;
    }
}
