package Tree;

import java.util.ArrayList;
import java.util.List;

// Not used anymore.
public class LabelInstances {

    private final int label;
    private final ArrayList<FeatureRow> list;

    private final int[] freqCounter;

    private final int[][] freqCounterPair;

    private boolean calculated;

    private int size;

    private final int featureSize;

    public LabelInstances(int label, FeatureRow features) {
        this.label = label;
        this.list = new ArrayList<>();
        list.add(features);
        int size = features.getFeatureSize();
        this.freqCounter = new int[size];
        this.freqCounterPair = new int[size][size];
        this.size = 1;
        this.calculated = false;
        this.featureSize = size;
    }

    public LabelInstances(int label, int size) {
        this.label = label;
        this.list = new ArrayList<>();
        this.freqCounter = new int[size];
        this.freqCounterPair = new int[size][size];
        this.size = 0;
        this.calculated = false;
        this.featureSize = size;
    }

    public void addList(FeatureRow newList) {
        this.list.add(newList);
        size++;
    }

    public void calcFreqCounter() {
        if (calculated) {
            return;
        }

        for (FeatureRow features: list) {
            List<Integer> featureVector = features.getValues();

            for (int i = 0; i < featureVector.size(); i++) {
                freqCounter[i] += featureVector.get(i);

                for (int j = i; j < featureVector.size(); j++) {
                    if (i == j) {
                        if (featureVector.get(i) == 1) {
                            freqCounterPair[i][i]++;
                        }
                    }
                    if (featureVector.get(i) == 1 && i < j) {
                        if (featureVector.get(j) == 1) {
                            freqCounterPair[i][j]++;
                            freqCounterPair[j][i]++;
                        }
                    }
                }
            }
        }

        calculated = true;
    }

    public int getSize() {
        return size;
    }

    public int getLabel(){
        return label;
    }

    public ArrayList<FeatureRow> getList() {
        return list;
    }

    public FeatureRow getSpecificArray(int i) {
        return list.get(i);
    }

    public void print() {
        System.out.println("Label: " + label);
        for (FeatureRow array:list) {
            System.out.println(array);
        }
    }

    public int[] getFreqCounter() {
        return freqCounter;
    }

    public int[][] getFreqCounterPair() {
        return freqCounterPair;
    }

    public int getFeatureSize() {
        return featureSize;
    }
}
