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

    public BinaryTree.Node solveSubtree(ASPDataset dataset, Branch branch, int depth, int nodes, int upperBound) {

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
        int lb = cache.getLowerBound(branch, depth, nodes);
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
            //System.out.println(tree[0].getTotalNodes() + " " +tree[0].getMc());
            //System.out.println(best.getTotalNodes() + " " + best.getMc());

            calculator.updateArchive(dataset, branch, depth);

            if (best.getMc() <= upperBound) {
                return best;
            }

            return BinaryTree.Node.createInfeasibleNode();
        }


        return generalCase(dataset, branch, depth, nodes, upperBound);

    }

    // General case algorithm.
    private BinaryTree.Node generalCase(ASPDataset dataset, Branch branch, int depth, int nodes, int upperBound) {

        // Initial solution with single node.
        BinaryTree.Node t = createLeafNode(dataset);
        if (t.getMc() > upperBound)
            t = BinaryTree.Node.createInfeasibleNode();

        // Retrieve lowerbound from cache
        int rlb = Integer.MAX_VALUE;

        int lb = cache.getLowerBound(branch, depth, nodes);

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
                int ub = Math.min(upperBound, (t.isFeasible() ? t.getMc() - 1 : Integer.MAX_VALUE));
                Pair<BinaryTree.Node, Integer> local = solveSubtreeWithRoot(dataset, branch, f, depth, n, m, ub, t);

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
            if (rlb == Integer.MAX_VALUE) {
                rlb = 0;
            }
            rlb = Math.max(rlb, upperBound + 1);
            int newLowerbound = Math.max(lb, rlb);
            cache.updateLowerBound(branch, newLowerbound, depth, nodes);
        }

        calculator.updateArchive(dataset, branch, depth);
        return t;
    }

    // Solve subtree with a feature split (root node).
    private Pair<BinaryTree.Node, Integer> solveSubtreeWithRoot(ASPDataset dataset, Branch branch, int rootFeature, int depth, int nodesLeft, int nodesRight, int upperBound, BinaryTree.Node node) {
        // Split into left and right node.
        int dl = Math.min(depth - 1, nodesLeft);
        int dr = Math.min(depth - 1, nodesRight);
        ASPDataset noFeature = dataset.getDatasetWithoutFeature(rootFeature);
        ASPDataset withFeature = dataset.getDatasetWithFeature(rootFeature);
        Branch left = Branch.left.leftChildBranch(branch, rootFeature);
        Branch right = Branch.right.rightChildBranch(branch, rootFeature);

        int ubl = 0;
        int ubr = 0;

        BinaryTree.Node t = node;

        // If left split is more likely to be feasible, do it first.
        if (getLeafMisclassification(noFeature) >= getLeafMisclassification(withFeature)) {
            ubl = upperBound - cache.getLowerBound(right, dr, nodesRight);
            //System.out.println(noFeature.getSize() + " " + "without feature dataset | depth: " + dl + " | nodes: " + nodesLeft + " | branch: " + left);
            BinaryTree.Node tl = solveSubtree(noFeature, left, dl, nodesLeft, ubl);

            // No need to calculate right side if no feasible node is found on left.
            if (!tl.isFeasible()) {
                int localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);

                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
            ubr = upperBound - tl.getMc();
            //System.out.println(withFeature.getSize() + " " + "with feature dataset | depth: " + dr + " | nodes: " + nodesRight + " | branch: " + right);
            BinaryTree.Node tr = solveSubtree(withFeature, right, dr, nodesRight, ubr);

            // Found new optimal node.
            if (tr.isFeasible()) {
                t = new BinaryTree.Node(rootFeature, Integer.MAX_VALUE, (tl.mc + tr.mc), tl.getTotalNodes(), tr.getTotalNodes());
                //System.out.println(t.mc);
                return new Pair<>(t, t.mc);
            }

            else {
                int localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);
                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
        }

        else {
            ubr = upperBound - cache.getLowerBound(left, dl, nodesLeft);
            //System.out.println(withFeature.getSize() + " " + "with feature dataset | depth: " + dr + " | nodes: " + nodesRight + " | branch: " + right);
            BinaryTree.Node tr = solveSubtree(withFeature, right, dr, nodesRight, ubr);

            if (!tr.isFeasible()) {
                int localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);
                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
            ubl = upperBound - tr.getMc();
            //System.out.println(noFeature.getSize() + " " + "without feature dataset | depth: " + dl + " | nodes: " + nodesLeft + " | branch: " + left);
            BinaryTree.Node tl = solveSubtree(noFeature, left, dl, nodesLeft, ubl);

            if (tl.isFeasible()) {
                t = new BinaryTree.Node(rootFeature, Integer.MAX_VALUE, (tl.mc + tr.mc), tl.getTotalNodes(), tr.getTotalNodes());
                return new Pair<>(t, t.mc);
            }

            else {
                int localbound = cache.getLowerBound(left, left.getDepth(), nodesLeft) + cache.getLowerBound(right, right.getDepth(), nodesRight);
                return new Pair<>(BinaryTree.Node.createInfeasibleNode(), localbound);
            }
        }
    }

    // Specialized algorithm for depth 2 or lower.
    private BinaryTree.Node[] computeThreeNodes(ASPDataset dataset){

        // Initialize and calculate frequency counters for the dataset.
        int featureSize = features;
        dataset.calcFreqCounters();
        BinaryTree.Node root2 = BinaryTree.Node.createInfeasibleNode();
        BinaryTree.Node root1 = BinaryTree.Node.createInfeasibleNode();
        BinaryTree.Node root0 = BinaryTree.Node.createInfeasibleNode();

        // Calculate Left side of the tree
        int[] bestLeftSubtreeMC = new int[featureSize];

        Arrays.fill(bestLeftSubtreeMC, Integer.MAX_VALUE);

        // Calculate Right side of the tree
        int[] bestRightSubtreeMC = new int[featureSize];

        Arrays.fill(bestRightSubtreeMC, Integer.MAX_VALUE);

        for (int fi = 0; fi < featureSize; fi++) {

            int mcLeft = calcClassificationScoreNegNeg(dataset, fi, fi);
            int mcRight = calcClassificationScorePosPos(dataset, fi, fi);
            int mcOneNode = mcLeft + mcRight;

            // Calculate optimal node for depth 1, node 1.
            if (mcOneNode < root0.mc) {
                root0.setFeature(fi);
                root0.setMc(mcOneNode);
                root0.assignRightNode(0);
                root0.assignLeftNode(0);
            }

            for (int fj = fi + 1; fj < featureSize; fj++) {
                int csNegPos = calcClassificationScoreNegPos(dataset, fi, fj);
                int csNegNeg = calcClassificationScoreNegNeg(dataset, fi, fj);
                int csPosNeg = calcClassificationScorePosNeg(dataset, fi, fj);
                int csPosPos = calcClassificationScorePosPos(dataset, fi, fj);

                //System.out.println(csNegPos);
                //System.out.println(csNegNeg);
                //System.out.println(csPosNeg);
                //System.out.println(csPosPos);

                int misCSl = csNegNeg + csNegPos;
                int misCSr = csPosPos + csPosNeg;

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

        int lb = cache.getLowerBound(branch, depth, nodes);
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
        } else if (depth <= 2){
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

        } else {
            BinaryTree.Node[] results = computeThreeNodes(dataset);
            if (results[0].getMc() == getLeafMisclassification(dataset)) {
                return createLeafNode(dataset);
            }
            BinaryTree.Node featureNode = BinaryTree.Node.createInfeasibleNode();

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
        }
    }

    private int calcClassificationScoreNegPos(ASPDataset dataset, int i, int j) {
        int min = Integer.MAX_VALUE;
        int sum = 0;

        for (Algorithm a : dataset.getAlgorithms()) {
            sum += (a.getFreqCounter()[j] - a.getFreqCounterPair()[i][j]);
        }

        for (Algorithm a : dataset.getAlgorithms()) {
            min = Math.min(((a.getFreqCounter()[j] - a.getFreqCounterPair()[i][j])), min);
        }


        return min;
    }

    private int calcClassificationScorePosNeg(ASPDataset dataset, int i, int j) {
        int min = Integer.MAX_VALUE;
        int sum = 0;

        for (Algorithm a : dataset.getAlgorithms()) {
            sum += (a.getFreqCounter()[i] - a.getFreqCounterPair()[i][j]);
        }

        for (Algorithm a : dataset.getAlgorithms()) {
            min = Math.min(((a.getFreqCounter()[i] - a.getFreqCounterPair()[i][j])), min);
        }

        return min;
    }

    private int calcClassificationScoreNegNeg(ASPDataset dataset, int i, int j) {
        int min = Integer.MAX_VALUE;
        int sum = 0;

        for (Algorithm a : dataset.getAlgorithms()) {
            sum += a.getTotalRunTime() - (a.getFreqCounter()[i] + a.getFreqCounter()[j] - a.getFreqCounterPair()[i][j]);
        }

        for (Algorithm a : dataset.getAlgorithms()) {
            int score = (a.getTotalRunTime() - (a.getFreqCounter()[i] + a.getFreqCounter()[j] - a.getFreqCounterPair()[i][j]));
            min = Math.min(score, min);
        }



        return min;
    }

    private int calcClassificationScorePosPos(ASPDataset dataset, int i, int j) {
        int min = Integer.MAX_VALUE;
        int sum = 0;

        for (Algorithm a : dataset.getAlgorithms()) {
            sum += a.getFreqCounterPair()[i][j];
        }

        for (Algorithm a : dataset.getAlgorithms()) {
            int score = a.getFreqCounterPair()[i][j];
            min = Math.min(score, min);
        }


        return min;
    }

    // Update root node for depth 2, maximum nodes of 3.
    private void updateRootNode(BinaryTree.Node node, int f, int[] left, int[] right) {
        int total = left[f] + right[f];
        if (node.mc > total) {
            //System.out.println("root: " +total + " feature: " + f);
            node.setMc(total);
            node.setFeature(f);
            node.assignLeftNode(1);
            node.assignRightNode(1);
        }
    }

    // Update optimal nodes for depth 2, maximum nodes 2.
    private void updateTwoNodes(BinaryTree.Node node, int f, int mcLeft, int mcRight, int[] left, int[] right) {
        int total = left[f] + mcRight;
        if (node.mc > total) {
            //System.out.println("2 nodes left: " +total + " feature: " + f);
            node.setMc(total);
            node.setFeature(f);
            node.assignLeftNode(1);
            node.assignRightNode(0);
        }

        total = right[f] + mcLeft;
        if (node.mc > total) {
            //System.out.println("2 nodes right: " +total + " feature: " + f);
            node.setMc(total);
            node.setFeature(f);
            node.assignLeftNode(0);
            node.assignRightNode(1);
        }
    }

    private int calcMisclassification(ASPDataset dataset, int label) {
        //int total = dataset.getSumRunTimes();

        return dataset.getTotalRunTime()[label];
    }

    // Create optimal leaf node of the dataset.
    private BinaryTree.Node createLeafNode(ASPDataset dataset) {
        int feature = Integer.MAX_VALUE;
        int label = getBestLabel(dataset);
        int ms = calcMisclassification(dataset, label);

        return new BinaryTree.Node(feature, label, ms);
    }

    // Return optimal leaf misclassification.
    private int getLeafMisclassification(ASPDataset dataset) {
        int label = getBestLabel(dataset);
        return calcMisclassification(dataset, label);
    }

    // Return labelinstances with the least misclassifications.
    private int getBestLabel(ASPDataset dataset) {
        return dataset.getLowestRunTimeLabel();
    }

    // Update the lower bound in cache.
    private boolean updateCache(ASPDataset dataset, Branch branch, int depth, int nodes) {
        Pair<Boolean, Integer> res = calculator.computeLowerBound(dataset, branch, depth, nodes, cache);
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
