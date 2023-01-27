package Tree;

public class CacheEntry {

    private int depth;
    private int nodes;
    private BinaryTree.Node optimalNode;
    private double lowerBound;

    public CacheEntry(int depth, int nodes) {
        this.depth = depth;
        this.nodes = nodes;
        this.optimalNode = BinaryTree.Node.createInfeasibleNode();
        this.lowerBound = Double.MAX_VALUE;
    }

    public CacheEntry(int depth, int nodes, BinaryTree.Node node) {
        this.depth = depth;
        this.nodes = nodes;
        this.optimalNode = node;
        this.lowerBound = node.mc;
    }

    public BinaryTree.Node getOptimalNode() {
        if (optimalNode != null && optimalNode.isFeasible()) {
            return optimalNode;
        }
        return optimalNode;
    }

    public double getOptimalValue() {
        if (optimalNode != null && optimalNode.isFeasible()) {
            return optimalNode.mc;
        }
        return Float.MAX_VALUE;
    }

    public void updateLowerBound(double lb) {
        if (lb < Double.MAX_VALUE && lb >= 0 && ((lb <= lowerBound && optimalNode.isFeasible()) || !optimalNode.isFeasible())) {
            this.lowerBound = lb;
        }
    }

    public void setOptimalNode(BinaryTree.Node node) {
        this.optimalNode = node;
        this.lowerBound = node.mc;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public int getDepth() {
        return depth;
    }

    public int getNodes() {
        return nodes;
    }

    public boolean isOptimal() {
        return optimalNode.isFeasible();
    }
}
