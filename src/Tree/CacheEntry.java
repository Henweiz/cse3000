package Tree;

public class CacheEntry {

    private int depth;
    private int nodes;
    private BinaryTree.Node optimalNode;
    private int lowerBound;

    public CacheEntry(int depth, int nodes) {
        this.depth = depth;
        this.nodes = nodes;
        this.optimalNode = BinaryTree.Node.createInfeasibleNode();
        this.lowerBound = Integer.MAX_VALUE;
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

    public int getOptimalValue() {
        if (optimalNode != null && optimalNode.isFeasible()) {
            return optimalNode.mc;
        }
        return Integer.MAX_VALUE;
    }

    public void updateLowerBound(int lb) {
        if (lb < Integer.MAX_VALUE && lb >= 0 && ((lb <= lowerBound && optimalNode.isFeasible()) || !optimalNode.isFeasible())) {
            this.lowerBound = lb;
        }
    }

    public void setOptimalNode(BinaryTree.Node node) {
        this.optimalNode = node;
        this.lowerBound = node.mc;
    }

    public int getLowerBound() {
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
