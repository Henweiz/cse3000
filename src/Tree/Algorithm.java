package Tree;

import java.util.List;

public class Algorithm {

    private double totalRunTime;
    private final double[] freqCounter;
    private final double[][] freqCounterPair;

    private int label;

    private boolean calculated;


    public Algorithm(int size, int label) {
        this.totalRunTime = 0;
        this.freqCounter = new double[size];
        this.freqCounterPair = new double[size][size];
        this.label = label;
        this.calculated = false;
    }

    public void setTotalRunTime(int totalruntime) {
        this.totalRunTime = totalruntime;
    }

    public void addRunTime(double f) {
        this.totalRunTime += f;
    }

    public void calcFreqCounters(List<FeatureRow> data){
        if (calculated) {
            return;
        }

        for (FeatureRow features: data) {
            List<Integer> featureVector = features.getValues();

            for (int i = 0; i < featureVector.size(); i++) {
                if (featureVector.get(i) == 1) {
                    //System.out.println(freqCounter[i]);
                    freqCounter[i] += features.getRuntimes()[this.label];
                }

                for (int j = i; j < featureVector.size(); j++) {
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

    public double[] getFreqCounter() {
        return freqCounter;
    }

    public double[][] getFreqCounterPair() {
        return freqCounterPair;
    }

    public int getLabel() {
        return label;
    }

    public double getTotalRunTime() {
        return totalRunTime;
    }
}
