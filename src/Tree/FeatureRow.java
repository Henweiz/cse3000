package Tree;

import java.util.List;

public class FeatureRow {

    private final List<Integer> values;
    private final int id;

    private final int[] runtimes;

    private final int optimalLabel;

    public FeatureRow(List<Integer> values, int[] runtimes, int id, int optimalLabel) {
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

    public int[] getRuntimes() {
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
