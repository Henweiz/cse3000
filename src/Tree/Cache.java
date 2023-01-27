package Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class Cache {

    private HashMap<Branch, List<CacheEntry>>[] cache;

    public Cache (int branchLength) {
        cache = new HashMap[branchLength + 1];
        for (int i = 0; i < cache.length; i++) {
            cache[i] = new HashMap<Branch, List<CacheEntry>>();
        }
    }

    // Return the most optimal node in the cache.
    public BinaryTree.Node getOptimalNode(Branch branch, int depth, int nodes) {
        HashMap<Branch, List<CacheEntry>> map = cache[branch.getDepth()];
        //System.out.println("Get branch: " + branch + " | Depth: " + depth + " | Nodes: " + nodes);
        // No nodes in the cache yet
        if (!map.containsKey(branch))
            return BinaryTree.Node.createInfeasibleNode();

        List<CacheEntry> target = map.get(branch);

        for (CacheEntry entry : target) {
            if (entry.getDepth() == depth && entry.getNodes() == nodes && entry.isOptimal()) {
                return entry.getOptimalNode();
            }
        }

        return BinaryTree.Node.createInfeasibleNode();

    }

    // Get the lower bound from cache.
    public int getLowerBound(Branch branch, int depth, int nodes) {
        HashMap<Branch, List<CacheEntry>> map = cache[branch.getDepth()];
        // No lower bound yet, return 0.
        if (!map.containsKey(branch))
            return 0;

        List<CacheEntry> target = map.get(branch);
        int best = 0;

        for (CacheEntry entry : target) {
            if (nodes <= entry.getNodes() && depth <= entry.getDepth()) {
                best = Math.max(best, entry.getLowerBound());
            }
        }

        return best;
    }

    // Update the lower bound in the cache.
    public void updateLowerBound(Branch branch, int lb, int depth, int nodes) {
        HashMap<Branch, List<CacheEntry>> map = cache[branch.getDepth()];
        // Create new cache entry if nothing exists yet.
        if (!map.containsKey(branch)) {
            List<CacheEntry> entries = new ArrayList<>();
            CacheEntry entry = new CacheEntry(depth, nodes);
            entry.updateLowerBound(lb);
            entries.add(entry);
            cache[branch.getDepth()].put(branch, entries);
        }
        // Check if current lower bound is better, if not update.
        else {
            List<CacheEntry> target = map.get(branch);
            boolean found = false;
            for (int i = 0; i < target.size(); i++) {
                if(target.get(i).getDepth() == depth && target.get(i).getNodes() == nodes) {
                    target.get(i).updateLowerBound(lb);
                    found = true;
                    break;
                }
            }
            if (!found) {
                CacheEntry entry = new CacheEntry(depth, nodes);
                entry.updateLowerBound(lb);
                map.get(branch).add(entry);
            }
        }

    }

    // Check if there is an optimal node cached.
    public boolean isOptimalCached(Branch branch, int depth, int nodes) {
        HashMap<Branch, List<CacheEntry>> map = cache[branch.getDepth()];
        if (!map.containsKey(branch))
            return false;

        List<CacheEntry> target = map.get(branch);

        for (CacheEntry entry : target) {
            if (entry.getDepth() == depth && entry.getNodes() == nodes) {
                return entry.isOptimal();
            }
        }
        return false;
    }

    // Transfer nodes from one branch cache to other.
    public void transferEqBranches(Branch branch, Branch dest) {
        if (branch.equals(dest)) {
            return;
        }

        HashMap<Branch, List<CacheEntry>> map = cache[branch.getDepth()];
        if (!map.containsKey(branch)) {
            map.put(branch, new ArrayList<>());
        }
        if (!map.containsKey(dest)) {
            List<CacheEntry> entries = map.get(branch);
            cache[dest.getDepth()].put(dest, entries);
        }
        else {
            for (CacheEntry sourceEntry : map.get(branch)) {
                boolean add = true;
                List<CacheEntry> destEntries = map.get(dest);
                for (int i = 0; i < destEntries.size(); i++) {
                    CacheEntry destEntry = destEntries.get(i);
                    if (sourceEntry.getDepth() == destEntry.getDepth() && sourceEntry.getNodes() == destEntry.getNodes()) {
                        add = false;
                        if (sourceEntry.isOptimal() && !destEntry.isOptimal() || sourceEntry.getLowerBound() > destEntry.getLowerBound()) {
                            map.get(dest).set(i,sourceEntry);
                            break;
                        }
                    }
                }
                if (add) {
                    map.get(dest).add(sourceEntry);
                }
            }
        }
    }

    // Store the optimal branch.
    public void storeOptimalBranch(Branch branch, BinaryTree.Node node, int depth, int nodes) {
        if (depth > nodes && nodes <= 0) {
            return;
        }
        HashMap<Branch, List<CacheEntry>> map = cache[branch.getDepth()];
        int optimalDepth = Math.min(depth, node.getTotalNodes());
        if (!map.containsKey(branch)){
            List<CacheEntry> entries = new ArrayList<>();
            for (int i = node.getTotalNodes(); i <= nodes; i++) {
                for (int d = optimalDepth; d <= Math.min(depth, i); d++) {
                    CacheEntry entry = new CacheEntry(d, i, node);
                    entries.add(entry);
                }
            }
            cache[branch.getDepth()].put(branch, entries);
            //System.out.println("Stored branch: " + branch + " | Depth: " + depth + " | Nodes: " + nodes);
        }
        else {
            List<CacheEntry> target = map.get(branch);
            boolean[][] seen = new boolean[nodes+1][depth+1];

            for (CacheEntry entry : target) {
                if (node.getTotalNodes() <= entry.getNodes() && entry.getNodes() <= nodes
                        && optimalDepth <= entry.getDepth() && entry.getDepth() <= depth) {
                    if (!(!entry.isOptimal() || entry.getOptimalValue() == node.mc)) {
                        System.out.println("optimal node: " + node.getTotalNodes() + ", " + node.mc);
                        System.out.println("num of nodes: " + nodes);
                        System.out.println(entry.getNodes() + " " + entry.getOptimalValue());
                    }
                    seen[entry.getNodes()][entry.getDepth()] = true;
                    if (!entry.isOptimal()) {
                        entry.setOptimalNode(node);
                    }
                }
            }

            for (int i = node.getTotalNodes(); i <= nodes; i++) {
                for (int d = optimalDepth; d <= Math.min(depth, i); d++) {
                    if (!seen[i][d]) {
                        CacheEntry entry = new CacheEntry(d, i, node);
                        map.get(branch).add(entry);
                    }
                }
            }
        }
    }

    // Return the number of entries in the cache.
    public int getNumEntries() {
        int count = 0;
        for (HashMap map : cache) {
            count += map.size();
            }
        return count;
    }

}
