package Tree;

import java.util.ArrayList;
import java.util.List;

public class LowerBoundCalculator {

    public static class ArchiveEntry {

        public ASPDataset data;
        public Branch branch;

        public ArchiveEntry(ASPDataset dataset, Branch branch) {
            this.data = dataset;
            this.branch = branch;
        }

    }

    public static class Archive {
        public List<ArchiveEntry> entries;

        public Archive() {
            entries = new ArrayList<>();
        }
    }

    private Archive[] archive;

    // Similarity bounding is currently disabled due to the lower bound is based on the algorithm run times.
    public LowerBoundCalculator(int depth) {
        archive = new Archive[depth + 1];
        for (int i = 0; i < archive.length; i++) {
            archive[i] = new Archive();
        }
    }

    public Pair<Boolean, Integer> computeLowerBound(ASPDataset dataset, Branch branch, int depth, int nodes, Cache cache) {
        boolean check = false;
        int lowerbound = 0;
        // Disable similarity bounding
        return new Pair<>(false, 0);
/*
        for (ArchiveEntry entry : archive[depth].entries) {
            int lb = cache.getLowerBound(entry.branch, depth, nodes);
            if (entry.data.getSize() > dataset.getSize() && entry.data.getSize() - dataset.getSize() >= lb) {
                continue;
            }
            Pair<Integer, Integer> res = computeDifference(entry.data, dataset);
            lowerbound = Math.max(lowerbound, (lb - res.b));

            if (res.a == 0) {
                cache.transferEqBranches(entry.branch, branch);
                if (cache.isOptimalCached(branch,depth,nodes)) {
                    check = true;
                    break;
                }
            }
        }

        return new Pair<>(check, lowerbound);
*/
    }

    public static Pair<Integer, Integer> computeDifference(ASPDataset newData, ASPDataset oldData) {

        int totaldiff = 0;
        int removals = 0;
        int lSizeNew = newData.getSize();
        int lSizeOld = oldData.getSize();
        int indexNew = 0, indexOld = 0;
        while (indexNew < lSizeNew && indexOld < lSizeOld) {
            int newId = newData.getSpecificArray(indexNew).getId();
            int oldId = oldData.getSpecificArray(indexOld).getId();
            if (newId < oldId) {
                totaldiff++;
                indexNew++;
            } else if (newId > oldId) {
                totaldiff++;
                removals++;
                indexOld++;
            } else {
                indexNew++;
                indexOld++;
            }
        }
        int newInstances = (lSizeNew - indexNew);
        int removedInstances = (lSizeOld - indexOld);
        totaldiff += newInstances;
        totaldiff += removedInstances;
        removals += removedInstances;

        return new Pair<>(totaldiff,removals);

    }

    public void updateArchive(ASPDataset dataset, Branch branch, int depth) {
        if (archive[depth].entries.size() < 2) {
            archive[depth].entries.add(new ArchiveEntry(dataset, branch));
        }
        else {
            int i = getMostSimilarData(dataset, depth);
            archive[depth].entries.set(i, new ArchiveEntry(dataset, branch));
        }
    }

    public int getMostSimilarData(ASPDataset dataset, int depth) {
        int bestEntry = 0;
        int bestScore = Integer.MAX_VALUE;
        List<ArchiveEntry> entries = archive[depth].entries;

        for (int i = 0; i < entries.size(); i++) {
            int score = computeDifference(entries.get(i).data, dataset).a;
            if (score < bestScore) {
                bestEntry = i;
                bestScore = score;
            }
        }
        return bestEntry;
    }


}
