package FeatureExtraction;

import java.util.ArrayList;
import java.util.List;

public class Clause {
    private List<Integer> posLiterals;
    private List<Integer> negLiterals;

    private List<Integer> literals;
    private boolean isHorn;
    private boolean hornCheck;

    public int id;


    public Clause() {
        this.posLiterals = new ArrayList<>();
        this.negLiterals = new ArrayList<>();
        this.literals = new ArrayList<>();
        this.isHorn = true;
        this.hornCheck = false;
        id = 0;
    }

    public void addLiteral(int x) {
        literals.add(x);
        if (x < 0) {
            negLiterals.add(x);
        }
        else {
            if (hornCheck) {
                isHorn = false;
            }
            if (!hornCheck) {
                hornCheck = true;
            }
            posLiterals.add(x);
        }
    }

    public List<Integer> getLiterals() {
        return literals;
    }

    public int getVariable(int i) {
        return Math.abs(literals.get(i));
    }

    public double getBalanceRatio() {
        if (!hasNegative()) {
            return posLiterals.size();
        }
        return (double) posLiterals.size()/negLiterals.size();
    }

    public boolean hasLiteral(int l) {
        return literals.contains(l);
    }

    public boolean hasNegLiteral(int l) {
        return negLiterals.contains(l);
    }

    public boolean isHorn() {
        return isHorn;
    }

    public int getSize() {
        return literals.size();
    }

    public boolean hasNegative() {
        return (negLiterals.size() > 0);
    }

    public void setId(int i) {
        this.id = i;
    }

    public List<Integer> getNegativeLiterals() {
        return negLiterals;
    }
}
