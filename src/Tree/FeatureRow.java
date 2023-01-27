package Tree;

import java.util.List;

public class FeatureRow {

    private List<Integer> values;
    private int id;

    private double[] runtimes;

    private int optimalLabel;

    public FeatureRow(List<Integer> values, double[] runtimes, int id, int optimalLabel) {
        this.values = values;
        this.id = id;
        this.runtimes = runtimes;
        this.optimalLabel = optimalLabel;
    }

    public List<Integer> getValues(){
        return values;
    }

    public int getId() {
        return id;
    }

    public double[] getRuntimes() {
        return runtimes;
    }

    public int getFeatureSize() {
        return values.size();
    }

    public boolean hasFeature(int f) {
        return values.get(f) == 1;
    }

    public int getOptimalLabel() {
        return optimalLabel;
    }
}
