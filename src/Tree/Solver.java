package Tree;

import java.util.Arrays;

public class Solver {

    private final int features;

    private LowerBoundCalculator calculator;

    public Cache cache;

    public Solver(ASPDataset dataset, int branchSize, int depth) {
        this.features = dataset.getDataset().get(0).getFeatureSize();
        this.cache = new Cache(branchSize);
        this.calculator = new LowerBoundCalculator(depth);
    }

    public BinaryTree.Node solveSubtree(ASPDataset dataset, Branch branch, int depth, int nodes, double upperBound) {

        // Prune based on upperBound
        if (upperBound < 0){
            return BinaryTree.Node.createInfeasibleNode();
        }


        BinaryTree.Node t;

        // Base case, 2nd case. No feature node, only classification nodes.
        if (depth == 0 || nodes == 0) {
            t = createLeafNode(dataset);
            if (t.mc <= upperBound) {
                return t;
            }
            else {
                return BinaryTree.Node.createInfeasibleNode();
            }
        }

        // Check for optimal node in cache.
        BinaryTree.Node cachedNode = cache.getOptimalNode(branch, depth, nodes);
        if (cachedNode.isFeasible()) {
            return cachedNode.mc > upperBound ? BinaryTree.Node.createInfeasibleNode() : cachedNode;
        }

        // Update cache
        boolean updated = updateCache(dataset, branch, depth, nodes);
        if (updated) {
            BinaryTree.Node optimal = cache.getOptimalNode(branch, depth, nodes);
            return optimal.mc > upperBound ? BinaryTree.Node.createInfeasibleNode() : optimal;
        }

        // Get current lower bound.
        double lb = cache.getLowerBound(branch, depth, nodes);
        if (lb > upperBound) {
            return BinaryTree.Node.createInfeasibleNode();
        }

        BinaryTree.Node leaf = createLeafNode(dataset);
        if (lb == leaf.mc) {
            return leaf;
        }


        // Do the specialized depth 2 algorithm.
        if (depth <= 2) {
            BinaryTree.Node[] tree = computeThreeNodes(dataset);
            if (!cache.isOptimalCached(branch, 1, 1)) {
                BinaryTree.Node root = tree[0];
                cache.storeOptimalBranch(branch, root, 1, 1);
            }
            if (!cache.isOptimalCached(branch, 2, 2)) {
                BinaryTree.Node root = tree[1];
                cache.storeOptimalBranch(branch, root, 2, 2);
            }
            if (!cache.isOptimalCached(branch, 2, 3)) {
                BinaryTree.Node root = tree[2];
                cache.storeOptimalBranch(branch, root, 2, 3);
            }

            BinaryTree.Node best = tree[0];
            if (nodes >= 2 && isNodeBetter(tree[1], best)) {
                best = tree[1];
            }
            if (nodes == 3 && isNodeBetter(tree[2], best)) {
                best = tree[2];
            }

            calculator.updateArchive(dataset, branch, depth);

            if (best.getMc() <= upperBound) {
                return best;
            }

            return BinaryTree.Node.createInfeasibleNode();
        }


        return generalCase(dataset, branch, depth, nodes, upperBound);

    }

    // General case algorithm.
    private BinaryTree.Node generalCase(ASPDataset dataset, Branch branch, int depth, int nodes, double upperBound) {

        // Initial solution with single node.
        BinaryTree.Node t = createLeafNode(dataset);
        if (t.getMc() > upperBound)
            t = BinaryTree.Node.createInfeasibleNode();

        // Retrieve lowerbound from cache
        double rlb = Double.MAX_VALUE;

        double lb = cache.getLowerBound(branch, depth, nodes);

        int maxNodes = Math.min((int) Math.pow(2, (depth - 1)) - 1, nodes - 1);
        int minNodes = nodes - 1 - maxNodes;

        // Search for best split.
        for (int f = 0; f < dataset.getDataset().get(0).getFeatureSize(); f++) {
            if (t.getMc() == lb && t.isFeasible()) {
                break;
            }
            int totalF = dataset.getFreqFeature(f);
            if (totalF == 0 || totalF == dataset.getSize()) {
                continue;
            }
            for (int n = minNodes; n <= maxNodes; n++) {
                int m = nodes-n-1;
                double ub = Math.min(upperBound, (t.isFeasible() ? t.getMc() - 1 : Double.MAX_VALUE));
                Pair<BinaryTree.Node, Double> local = solveSubtreeWithRoot(dataset, branch, f, depth, n, m, ub, t);

                // If no feasible node is found, update the refined lower bound.
                if (!local.a.isFeasible()) {
                    rlb = Math.min(rlb, local.b);
                }
                else {
                    // Found new optimal node.
                    if (!t.isFeasible() || t.mc > local.b) {
                        t = local.a;
                        if (local.b == lb) {
                            break;
                        }
                    }
                }
            }
        }

        // Cache the new optimal node t.
        if (t.isFeasible() && t.mc <= upperBound) {
            cache.storeOptimalBranch(branch, t, depth, nodes);
        }
        else {
            // Update new lower bound if no feasible node is found.
            if (rlb == Double.MAX_VALUE) {
                rlb = 0;
            }
            rlb = Math.max(rlb, upperBound + 1);
            double newLowerbound = Math.max(lb, rlb);
            cache.updateLowerBound(branch, newLowerbound, depth, nodes);
        }

        calculator.updateArchive(dataset, branch, depth);
        return t;
    }

    // Solve subtree with a feature split (root node).
    private Pair<BinaryTree.Node, Double> solveSubtreeWithRoot(ASPDataset dataset, Branch branch, int rootFeature, int depth, int nodesLeft, int nodesRight, double upperBound, BinaryTree.Node node) {

        // Split into left and right node.
        int dl = Math.min(depth - 1, nodesLeft);
        int dr = Math.min(depth - 1, nodesRight);
        ASPDataset noFeature = dataset.getDatasetWithoutFeature(rootFeature);
        ASPDataset withFeature = dataset.getDatasetWithFeature(rootFeature);
        Branch left = Branch.left.leftChildBranch(branch, rootFeature);
        Branch right = Branch.right.rightChildBranch(branch, rootFeature);

        double ubl = 0d;
        double ubr = 0d;

        BinaryTree.Node t = node;

        // If left split is more likely to be feasible, do it first.
        if (getLeafMisclassification(noFeature) >= getLeafMisclassification(withFeature)) {
            ubl = upperBound - cache.getLowerBound(right, dr, nodesRight);
            //System.out.println(noFeature.getDataSize() + " " + "without feature dataset | depth: " + dl + " | nodes: " + nodesLeft + " | branch: " + left);
            BinaryTree.Node tl = solveSubtree(noFeature, left, dl, nodesLeft, ubl);

            // No need to calculate right side if no feasible node is found on left.
            if (!tl.isFeasible()) {
                double localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);

                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
            ubr = upperBound - tl.getMc();
            //System.out.println(withFeature.getDataSize() + " " + "with feature dataset | depth: " + dr + " | nodes: " + nodesRight + " | branch: " + right);
            BinaryTree.Node tr = solveSubtree(withFeature, right, dr, nodesRight, ubr);

            // Found new optimal node.
            if (tr.isFeasible()) {
                t = new BinaryTree.Node(rootFeature, Integer.MAX_VALUE, (tl.mc + tr.mc), tl.getTotalNodes(), tr.getTotalNodes());
                //System.out.println(t.mc);
                return new Pair<>(t, t.mc);
            }

            else {
                double localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);
                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
        }

        else {
            ubr = upperBound - cache.getLowerBound(left, dl, nodesLeft);
            //System.out.println(withFeature.getDataSize() + " " + "with feature dataset | depth: " + dr + " | nodes: " + nodesRight + " | branch: " + right);
            BinaryTree.Node tr = solveSubtree(withFeature, right, dr, nodesRight, ubr);

            if (!tr.isFeasible()) {
                double localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);
                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
            ubl = upperBound - tr.getMc();
            //System.out.println(noFeature.getDataSize() + " " + "without feature dataset | depth: " + dl + " | nodes: " + nodesLeft + " | branch: " + left);
            BinaryTree.Node tl = solveSubtree(noFeature, left, dl, nodesLeft, ubl);

            if (tl.isFeasible()) {
                t = new BinaryTree.Node(rootFeature, Integer.MAX_VALUE, (tl.mc + tr.mc), tl.getTotalNodes(), tr.getTotalNodes());
                return new Pair<>(t, t.mc);
            }

            else {
                double localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);
                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
        }
    }

    // Specialized algorithm for depth 2 or lower.
    private BinaryTree.Node[] computeThreeNodes(ASPDataset dataset){

        // Initialize and calculate frequency counters for the dataset.
        int featureSize = dataset.getDataset().get(0).getFeatureSize();
        dataset.calcFreqCounters();
        BinaryTree.Node root2 = BinaryTree.Node.createInfeasibleNode();
        BinaryTree.Node root1 = BinaryTree.Node.createInfeasibleNode();
        BinaryTree.Node root0 = BinaryTree.Node.createInfeasibleNode();

        // Calculate Left side of the tree
        double[] bestLeftSubtreeMC = new double[featureSize];

        Arrays.fill(bestLeftSubtreeMC, Double.MAX_VALUE);

        // Calculate Right side of the tree
        double[] bestRightSubtreeMC = new double[featureSize];

        Arrays.fill(bestRightSubtreeMC, Double.MAX_VALUE);

        for (int fi = 0; fi < featureSize; fi++) {
            double mcLeft = calcClassificationScoreNegNeg(dataset, fi, fi);
            double mcRight = calcClassificationScorePosPos(dataset, fi, fi);
            double mcOneNode = mcLeft + mcRight;

            // Calculate optimal node for depth 1, node 1.
            if (mcOneNode < root0.mc) {
                root0.setFeature(fi);
                root0.setMc(mcOneNode);
                root0.assignRightNode(0);
                root0.assignLeftNode(0);
            }

            for (int fj = fi + 1; fj < featureSize; fj++) {
                double csNegPos = calcClassificationScoreNegPos(dataset, fi, fj);
                double csNegNeg = calcClassificationScoreNegNeg(dataset, fi, fj);
                double csPosNeg = calcClassificationScorePosNeg(dataset, fi, fj);
                double csPosPos = calcClassificationScorePosPos(dataset, fi, fj);

                //System.out.println(csNegPos);
                //System.out.println(csNegNeg);
                //System.out.println(csPosNeg);
               // System.out.println(csPosPos);

                double misCSl = csNegNeg + csNegPos;
                double misCSr = csPosPos + csPosNeg;

                //System.out.println(misCSl);
                //System.out.println(misCSr);

                if (bestLeftSubtreeMC[fi] > misCSl) {
                    bestLeftSubtreeMC[fi] = misCSl;
                }

                if (bestRightSubtreeMC[fi] > misCSr) {
                    bestRightSubtreeMC[fi] = misCSr;
                }

                if (bestLeftSubtreeMC[fj] > (csPosNeg + csNegNeg) ){
                    bestLeftSubtreeMC[fj] = (csPosNeg + csNegNeg);
                }

                if (bestRightSubtreeMC[fj] > (csNegPos + csPosPos) ){
                    bestRightSubtreeMC[fj] = (csNegPos + csPosPos);
                }

                // Update optimal node for depth 2, maximum nodes 2.
                updateTwoNodes(root1, fi, mcLeft, mcRight, bestLeftSubtreeMC, bestRightSubtreeMC);
                updateTwoNodes(root1, fj, calcClassificationScoreNegNeg(dataset, fj, fj), calcClassificationScorePosPos(dataset, fj, fj), bestLeftSubtreeMC, bestRightSubtreeMC);
            }
            // Update optimal node for depth 2, maximum nodes 3.
            updateRootNode(root2, fi, bestLeftSubtreeMC, bestRightSubtreeMC);
        }

        BinaryTree.Node[] nodes = new BinaryTree.Node[3];
        nodes[0] = root0;
        nodes[1] = root1;
        nodes[2] = root2;

        return nodes;
    }

    public BinaryTree.Node constructTree(ASPDataset dataset, Branch branch, int depth, int nodes) {
        assert nodes >= 0;

        double lb = cache.getLowerBound(branch, depth, nodes);
        if (depth == 0 || nodes == 0 || getLeafMisclassification(dataset) == lb) {
            return createLeafNode(dataset);
        } else if (cache.isOptimalCached(branch, depth, nodes)) {
            BinaryTree.Node optimal = cache.getOptimalNode(branch, depth, nodes);
            BinaryTree.Node featureNode = new BinaryTree.Node(optimal.feature, Integer.MAX_VALUE, optimal.mc);
            ASPDataset dataWith = dataset.getDatasetWithFeature(optimal.feature);
            ASPDataset dataWithout = dataset.getDatasetWithoutFeature(optimal.feature);

            Branch left = Branch.left.leftChildBranch(branch, optimal.feature);
            Branch right = Branch.right.rightChildBranch(branch, optimal.feature);

            int leftDepth = Math.min(depth-1, optimal.left);
            int rightDepth = Math.min(depth-1, optimal.right);
            BinaryTree.Node leftChild = constructTree(dataWithout, left, leftDepth, optimal.left);
            BinaryTree.Node rightChild = constructTree(dataWith, right, rightDepth, optimal.right);

            featureNode.setLeftNode(leftChild);
            featureNode.setRightNode(rightChild);

            return featureNode;
        } else {
            assert nodes == 1 || nodes == 2;
            BinaryTree.Node[] results = computeThreeNodes(dataset);

            if ((nodes == 1 && results[0].mc == getLeafMisclassification(dataset))
                || nodes == 2 && results[1].mc == getLeafMisclassification(dataset)) {
                return new BinaryTree.Node(Integer.MAX_VALUE, getBestLabel(dataset), 0);
            }

            BinaryTree.Node featureNode = BinaryTree.Node.createInfeasibleNode();
            if (nodes == 1) {
                featureNode.setFeature(results[0].feature);
                featureNode.setMc(results[0].mc);
                ASPDataset dataWith = dataset.getDatasetWithFeature(results[0].feature);
                ASPDataset dataWithout = dataset.getDatasetWithoutFeature(results[0].feature);

                Branch left = Branch.left.leftChildBranch(branch, results[0].feature);
                Branch right = Branch.right.rightChildBranch(branch, results[0].feature);

                int leftDepth = Math.min(depth-1, results[0].left);
                int rightDepth = Math.min(depth-1, results[0].right);
                BinaryTree.Node leftChild = constructTree(dataWithout, left, leftDepth, results[0].left);
                BinaryTree.Node rightChild = constructTree(dataWith, right, rightDepth, results[0].right);

                featureNode.setLeftNode(leftChild);
                featureNode.setRightNode(rightChild);

                return featureNode;
            } else {
                featureNode.setFeature(results[1].feature);
                featureNode.setMc(results[1].mc);
                ASPDataset dataWith = dataset.getDatasetWithFeature(results[1].feature);
                ASPDataset dataWithout = dataset.getDatasetWithoutFeature(results[1].feature);

                Branch left = Branch.left.leftChildBranch(branch, results[1].feature);
                Branch right = Branch.right.rightChildBranch(branch, results[1].feature);

                int leftDepth = Math.min(depth-1, results[1].left);
                int rightDepth = Math.min(depth-1, results[1].right);
                BinaryTree.Node leftChild = constructTree(dataWithout, left, leftDepth, results[1].left);
                BinaryTree.Node rightChild = constructTree(dataWith, right, rightDepth, results[1].right);

                featureNode.setLeftNode(leftChild);
                featureNode.setRightNode(rightChild);

                return featureNode;
            }


        }
    }

    private double calcClassificationScoreNegPos(ASPDataset dataset, int i, int j) {
        double min = Double.MAX_VALUE;

        for (Algorithm a : dataset.getAlgorithms()) {
            min = Math.min((a.getTotalRunTime() - (a.getFreqCounter()[j] - a.getFreqCounterPair()[i][j])), min);
        }


        return min;
    }

    private double calcClassificationScorePosNeg(ASPDataset dataset, int i, int j) {
        double min = Double.MAX_VALUE;

        for (Algorithm a : dataset.getAlgorithms()) {
            min = Math.min((a.getTotalRunTime() - (a.getFreqCounter()[i] - a.getFreqCounterPair()[i][j])), min);
        }

        return min;
    }

    private double calcClassificationScoreNegNeg(ASPDataset dataset, int i, int j) {
        double min = Double.MAX_VALUE;

        for (Algorithm a : dataset.getAlgorithms()) {
            double score = a.getTotalRunTime() - a.getFreqCounter()[i] - a.getFreqCounter()[j] + a.getFreqCounterPair()[i][j];

            min = Math.min(score, min);
        }
        if (min < 0) {
            System.out.println(min);
        }


        return min;
    }

    private double calcClassificationScorePosPos(ASPDataset dataset, int i, int j) {
        double min = Double.MAX_VALUE;

        for (Algorithm a : dataset.getAlgorithms()) {
            double score = a.getFreqCounterPair()[i][j];
            min = Math.min(score, min);
        }


        return min;
    }

    // Update root node for depth 2, maximum nodes of 3.
    private void updateRootNode(BinaryTree.Node node, int f, double[] left, double[] right) {
        double total = left[f] + right[f];
        if (node.mc > total) {
            node.setMc(total);
            node.setFeature(f);
            node.assignLeftNode(1);
            node.assignRightNode(1);
        }
    }

    // Update optimal nodes for depth 2, maximum nodes 2.
    private void updateTwoNodes(BinaryTree.Node node, int f, double mcLeft, double mcRight, double[] left, double[] right) {
        double total = left[f] + mcRight;
        if (node.mc > total) {
            node.setMc(total);
            node.setFeature(f);
            node.assignLeftNode(1);
            node.assignRightNode(0);
        }

        total = right[f] + mcLeft;
        if (node.mc > total) {
            node.setMc(total);
            node.setFeature(f);
            node.assignLeftNode(0);
            node.assignRightNode(1);
        }
    }

    private double calcMisclassification(ASPDataset dataset) {
        double min = Double.MAX_VALUE;
        for (double f : dataset.getTotalRunTime()) {
            if (min > f) {
                min = f;
            }
        }

        return min;
    }

    // Create optimal leaf node of the dataset.
    private BinaryTree.Node createLeafNode(ASPDataset dataset) {
        int feature = Integer.MAX_VALUE;
        int label = getBestLabel(dataset);
        double ms = calcMisclassification(dataset);

        return new BinaryTree.Node(feature, label, ms);
    }

    // Return optimal leaf misclassification.
    private double getLeafMisclassification(ASPDataset dataset) {
        return calcMisclassification(dataset);
    }

    // Return labelinstances with the least misclassifications.
    private int getBestLabel(ASPDataset dataset) {
        return dataset.getLowestRunTimeLabel();
    }

    // Update the lower bound in cache.
    private boolean updateCache(ASPDataset dataset, Branch branch, int depth, int nodes) {
        Pair<Boolean, Double> res = calculator.computeLowerBound(dataset, branch, depth, nodes, cache);
        if (res.a) {
            return true;
        }
        if (res.b > 0) {
            cache.updateLowerBound(branch, res.b, depth, nodes);
        }
        return false;
    }

    // Return true if first node is better than second node in terms of misclassifications.
    private boolean isNodeBetter(BinaryTree.Node first, BinaryTree.Node second) {
        if (first.mc < second.mc)
            return true;
        else if (first.mc > second.mc)
            return false;
        else
            return first.getTotalNodes() < second.getTotalNodes();
    }


}
