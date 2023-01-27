package FeatureExtraction;

public class Variable {

    private int posOccurrence;
    private int negOccurrence;
    private final int x;

    public Variable(int x) {
        this.x = Math.abs(x);
        posOccurrence = 0;
        negOccurrence = 0;
    }

    public void addOccurrence(int x) {
        assert Math.abs(x) == this.x;
        if (isPositive(x)) {
            posOccurrence++;
        }
        else
            negOccurrence++;
    }

    public int getPosOccurrence() {
        return posOccurrence;
    }

    public int getNegOccurrence() {
        return negOccurrence;
    }

    public double getBalanceRatio() {
        if (negOccurrence == 0) {
            return posOccurrence;
        }
        return (double) posOccurrence/negOccurrence;
    }

    private boolean isPositive(int x) {
        return x > 0;
    }
}
