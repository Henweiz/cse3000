package Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Branch {

    private List<Integer> nodes;
    private float ms;

    public static Branch left = new Branch();

    public static Branch right = new Branch();

    public Branch(){
        this.nodes = new ArrayList<>();
        this.ms = 0;
    }

    public Branch(Branch branch) {
        this.nodes = branch.nodes;
        this.ms = branch.ms;
    }

    public void addNode(int f) {
        nodes.add(f);
    }

    public void setNodes(List<Integer> nodes) {
        this.nodes = nodes;
    }

    public void updateMS(int ms) {
        this.ms = ms;
    }

    public int getDepth() {
        return nodes.size();
    }

    public float getMs() {
        return ms;
    }

    public int getNodesSize() {
        return nodes.size();
    }

    public void addFeatureBranch(int f, int present) {
        int c = getCode(f, present);
        nodes.add(c);
        nodes = nodes.stream().distinct().collect(Collectors.toList());

        // No need to sort(?).
        //Collections.sort(nodes);
    }

    public Branch leftChildBranch(Branch branch, int feature) {
        left = new Branch(branch);
        left.addFeatureBranch(feature, 0);
        return left;
    }

    public Branch rightChildBranch(Branch branch, int feature) {
        right = new Branch(branch);
        right.addFeatureBranch(feature, 1);
        return right;
    }

    @Override
    public boolean equals(Object o) {
        Branch branch = (Branch) o;
        if (this.nodes.size() != branch.nodes.size()) return false;
        for (int i = 0; i < nodes.size(); i++) {
            if (!Objects.equals(this.nodes.get(i), branch.nodes.get(i))) return false;
        }
        return true;
    }

/* Apparently this does not work in java compared to cpp
    @Override
    public int hashCode() {
        int k = getDepth();
        for (int i = 0; i < getDepth(); i++) {
            k ^= (nodes.get(i) + 0x9e3779b9 + (64*k) + (k/4));
        }
        return k;
    }
*/
    public int getCode(int f, int present) {
        return (2 * f) + present;
    }
}
