package Tree;

import java.util.Iterator;

public class BinaryTree {

    public static class Node{

        int feature;

        int label;
        int left;
        int right;
        double mc;

        Node rightNode;
        Node leftNode;
        public Node(int feature, int label, double mc){
            //Assign data to the new node, set left and right children to null
            this.feature = feature;
            this.label = label;
            this.left = 0;
            this.right = 0;
            this.mc = mc;
            this.leftNode = null;
            this.rightNode = null;
        }

        public Node(int feature, int label, double mc, int left, int right){
            //Assign data to the new node
            this.feature = feature;
            this.label = label;
            this.left = left;
            this.right = right;
            this.mc = mc;
            this.leftNode = null;
            this.rightNode = null;
        }

        public void assignLeftNode(int node){
            this.left = node;
        }

        public void assignRightNode(int node){
            this.right = node;
        }

        public int getTotalNodes() {
            if (feature == Integer.MAX_VALUE) {
                return 0;
            }
            return 1 + left + right;
        }

        public boolean isFeasible(){
            return this.mc != Double.MAX_VALUE;
        }

        public int getLeftSize() {
            return left;
        }

        public int getRightSize(){
            return right;
        }

        public int getLabel(){
            return label;
        }

        public int getFeature() {
            return feature;
        }

        public double getMc() {
            return mc;
        }

        public void setMc(double mc) {
            this.mc = mc;
        }

        public void setFeature(int f) {
            this.feature = f;
        }

        public void setLeftNode(Node leftNode) {
            this.leftNode = leftNode;
        }

        public void setRightNode(Node rightNode) {
            this.rightNode = rightNode;
        }

        public boolean isLabelNode() {
            return label != Integer.MAX_VALUE;
        }

        public static BinaryTree.Node createInfeasibleNode() {
            return new BinaryTree.Node(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE);
        }

        public int computeMisclassification(ASPDataset dataset) {
            int misclassifications = 0;
            for (FeatureRow row : dataset.getDataset()) {
                if (classify(row) != row.getOptimalLabel()) {
                    misclassifications++;
                }
            }
            return misclassifications;
        }
        
        private int classify(FeatureRow row) {
            if (isLabelNode()) {
                return label;
            } else if (row.hasFeature(this.feature)) {
                return this.rightNode.classify(row);
            } else {
                return this.leftNode.classify(row);
            }
        }

        // Function to print binary tree in 2D
        // It does reverse inorder traversal
        private void print2DUtil(BinaryTree.Node node, int space)
        {
            if (node == null)
                return;
            // Base case
            if (node.feature == Integer.MAX_VALUE && node.label == Integer.MAX_VALUE)
                return;

            // Increase distance between levels
            space++;

            // Process right child first
            print2DUtil(node.rightNode, space);

            // Print current node after space
            // count
            System.out.print("\n");
            for (int i = 0; i < space; i++)
                System.out.print(" ");
            if (node.isLabelNode()) {
                System.out.println("(Label: " + node.label + " )\n");
            } else {
                System.out.print(node.feature + "\n");
            }

            // Process left child
            print2DUtil(node.leftNode, space);
        }

        // Wrapper over print2DUtil()
        public void print2D()
        {
            int space = 0;
            // Base case
            if (this.feature == Integer.MAX_VALUE && this.label == Integer.MAX_VALUE)
                return;

            // Pass initial space count as 0
            // Increase distance between levels

            // Process right child first
            print2DUtil(rightNode, space);

            // Print current node after space
            // count
            System.out.print("\n");
            if (isLabelNode()) {
                System.out.println("(Label: " + this.label + " )\n");
            } else {
                System.out.print("Root: " + this.feature + "\n");
            }

            // Process left child
            print2DUtil(leftNode, space);
        }

    }

    public Node root;

    private double ms;

    public BinaryTree(){
        root = null;
        ms = 0;
    }

    public BinaryTree(Node root, int ms) {
        this.root = root;
        this.ms = ms;
    }

    public void setRoot(Node node) {
        this.root = node;
    }

    public void setMS(int ms) {
        this.ms = ms;
    }

    public double getMS(){
        return this.ms;
    }
}