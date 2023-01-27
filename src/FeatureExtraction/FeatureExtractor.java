package FeatureExtraction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

// Not going to lie, you do not want to read this class.
public class FeatureExtractor {

    private Graph<Integer> vcg;
    private Graph<Integer> vg;
    private Graph<Integer> cg;
    private int totalClause;
    private int totalVar;
    private double[] features;
    private Clause[] clauses;

    // variable[0] does not exist btw for the sake of reducing bugs and readability.
    private Variable[] variables;

    private List<Clause>[] negativeLiteralBins;

    public FeatureExtractor() {
        this.features = new double[44];
        this.vcg = new Graph<>();
        this.vg = new Graph<>();
        this.cg = new Graph<>();
        totalClause = 0;
        totalVar = 0;
        negativeLiteralBins = new List[1];
        negativeLiteralBins[0] = new ArrayList<Clause>();
    }

    public void satReader(File file) {
        boolean isProblem;
        boolean isClause = false;
        boolean isVar = false;
        int clauseCount = 0;
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Clause clause = new Clause();
                isProblem = false;
                for (String s : data.split(" ")) {
                    if (isProblem) {
                        if (!isInteger(s))
                            continue;
                        // Add total variables of the SAT instance
                        if (totalVar == 0) {
                            totalVar = Integer.parseInt(s);
                            variables = new Variable[totalVar+1];
                            for (int i = 1; i <= totalVar; i++) {
                                variables[i] = new Variable((i));
                            }
                            negativeLiteralBins = new List[totalVar+1];
                            for (int i = 0; i < negativeLiteralBins.length; i++) {
                                negativeLiteralBins[i] = new ArrayList<Clause>();
                            }
                            continue;
                        }
                        // Add total clauses of the SAT instance
                        if (totalClause == 0) {
                            totalClause = Integer.parseInt(s);
                            clauses = new Clause[totalClause];
                            break;
                        }

                    }
                    // Check clause line
                    if (isVar) {
                        // Add to the clause graphs when iterating the first time through this line.
                        if (isClause) {
                            clauseCount++;
                            vcg.addVertex(clauseCount + totalVar);
                            cg.addVertex(clauseCount);
                            clause.setId(clauseCount);
                            isClause = false;
                        }

                        int v = Integer.parseInt(s);
                        // If literal is 0, it means the end of the clause line.
                        if (v == 0) {
                            isVar = false;
                            for (int i = 0; i < clause.getSize(); i++) {
                                for (int j = i; j < clause.getSize(); j++) {
                                    if (i != j) {
                                        int x = clause.getVariable(i);
                                        int y = clause.getVariable(j);
                                        if (!vg.hasEdge(x, y)) {
                                            vg.addEdge(x,y);
                                        }
                                    }
                                }
                            }
                            clauses[clauseCount-1] = clause;
                            break;
                        }

                        // Add the literal
                        clause.addLiteral(v);
                        variables[Math.abs(v)].addOccurrence(v);

                        // Add clause to the negative bins if the literal is negative
                        if (isNegative(v)) {
                            negativeLiteralBins[Math.abs(v)].add(clause);
                        }

                        // Add variable to the graphs and connect the edge.
                        v = Math.abs(v);
                        if (!vcg.hasVertex(v)) {
                            vcg.addVertex(v);
                        }
                        if (!vg.hasVertex(v)) {
                            vg.addVertex(v);
                        }
                        if (!vcg.hasEdge(v, clauseCount + totalVar)) {
                            vcg.addEdge(v, clauseCount + totalVar);
                        }

                    }
                    else {
                        if (s.charAt(0) == 'c')
                            break;
                        if (s.equals("p")) {
                            isProblem = true;
                            continue;
                        }
                        if (isInteger(s)) {
                            isClause = true;
                            isVar = true;
                        }
                    }
                }
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void generateFeatures() {

        // Calculate problem size features.
        features[0] = totalClause;
        features[1] = totalVar;
        double ratio = (double) totalClause/totalVar;
        features[2] = ratio;
        features[3] = Math.pow(ratio, 2);
        features[4] = Math.pow(ratio, 3);
        double ratioR = (double) totalVar/totalClause;
        features[5] = ratioR;
        features[6] = Math.pow(ratioR, 2);
        features[7] = Math.pow(ratioR, 3);

        // Calculate Variable Clause Graph Features.
        int totalVarEdges = 0;
        int totalClauseEdges = 0;
        int maxVEdge = 0;
        int maxCEdge = 0;
        int minVEdge = Integer.MAX_VALUE;
        int minCEdge = Integer.MAX_VALUE;
        double sdVar = 0;
        double sdClause = 0;

        Map<Integer, List<Integer>> map = vcg.getMap();
        for (int v : map.keySet()) {
            int nEdge = map.get(v).size();
            if (v > totalVar) {
                totalClauseEdges += nEdge;
                if (nEdge > maxCEdge) {
                    maxCEdge = nEdge;
                }
                if (nEdge < minCEdge) {
                    minCEdge = nEdge;
                }
                continue;
            }
            totalVarEdges += nEdge;
            if (nEdge > maxVEdge) {
                maxVEdge = nEdge;
            }
            if (nEdge < minVEdge) {
                minVEdge = nEdge;
            }
        }
        int[] varCounts = new int[maxVEdge + 1];
        int[] clauseCounts = new int[maxCEdge + 1];
        double meanVarEdge = (double) totalVarEdges/totalVar;
        //System.out.println(totalVar);
        double meanClauseEdge = (double) totalClauseEdges/totalClause;
        for (int i = 1; i <= totalVar; i++) {
            if (vcg.hasVertex(i)) {
                sdVar += Math.pow((map.get(i).size() - meanVarEdge), 2);
                varCounts[map.get(i).size()]++;
            }
            else {
                sdVar += Math.pow(meanVarEdge, 2);
            }
        }
        for (int i = totalVar+1; i <= totalVar+totalClause; i++) {
            if (vcg.hasVertex(i)) {
                sdClause += Math.pow((map.get(i).size() - meanClauseEdge), 2);
                clauseCounts[map.get(i).size()]++;
            }
            else {
                sdClause += Math.pow(meanClauseEdge, 2);
            }
        }

        features[8] = meanVarEdge;
        features[9] = calcCV(calcSD(sdVar, totalVar),meanVarEdge);
        features[10] = minVEdge;
        features[11] = maxVEdge;
        features[12] = calcEntropy(varCounts, totalVar);
        features[13] = meanClauseEdge;
        features[14] = calcCV(calcSD(sdClause, totalClause),meanClauseEdge);
        features[15] = minCEdge;
        features[16] = maxCEdge;
        features[17] = calcEntropy(clauseCounts, totalClause);

        // Clear up memory space.
        vcg = null;

        // Calculate Variable Graph features.
        double[] res = calcGraphStatistics(vg, totalVar);
        features[18] = res[0];
        features[19] = res[1];
        features[20] = res[2];
        features[21] = res[3];

        // Clear up memory space.
        vg = null;

        // Calculate Clause Graph features.
        connectCG();
        res = calcGraphStatistics(cg, totalClause);
        features[22] = res[0];
        features[23] = res[1];
        features[24] = res[2];
        features[25] = res[3];
        features[26] = res[4];

        // Clear up memory space.
        cg = null;
        negativeLiteralBins = null;

        // Balance features.
        // Ratio of positive and negative literals in each clause.
        double meanBalance;
        double totalBalance = 0d;
        double minBalance = Double.MAX_VALUE;
        double maxBalance = 0d;
        double sdBalance = 0d;
        double unary = 0d;
        double binary = 0d;
        double ternary = 0d;

        for (Clause clause : clauses) {
            double balance = clause.getBalanceRatio();
            totalBalance += balance;
            if (minBalance > balance)
                minBalance = balance;
            if (maxBalance < balance)
                maxBalance = balance;
            int size = clause.getSize();
            if (size == 1) {
                unary++;
                continue;
            }
            if (size == 2) {
                binary++;
                continue;
            }
            if (size == 3) {
                ternary++;
            }
        }
        meanBalance = totalBalance / totalClause;
        for (Clause clause : clauses) {
            sdBalance += Math.pow((clause.getBalanceRatio() - meanBalance),2);
        }
        features[27] = meanBalance;
        features[28] = calcCV(calcSD(sdBalance, totalClause), meanBalance);
        features[29] = minBalance;
        features[30] = maxBalance;

        // Ratio of positive and negative occurrence of each variable
        totalBalance = 0d;
        minBalance = Double.MAX_VALUE;
        maxBalance = 0d;
        sdBalance = 0d;

        for (int i = 1; i < variables.length; i++) {
            double balance = variables[i].getBalanceRatio();
            totalBalance += balance;
            if (minBalance > balance)
                minBalance = balance;
            if (maxBalance < balance)
                maxBalance = balance;
        }
        meanBalance = totalBalance / totalVar;
        for (int i = 1; i < variables.length; i++) {
            sdBalance += Math.pow((variables[i].getBalanceRatio() - meanBalance),2);
        }
        features[31] = meanBalance;
        features[32] = calcCV(calcSD(sdBalance, totalVar), meanBalance);
        features[33] = minBalance;
        features[34] = maxBalance;

        // unary, binary and tenary clauses. Already calculated above^
        features[35] = unary/totalClause;
        features[36] = binary/totalClause;
        features[37] = ternary/totalClause;

        // Calculate horn features.
        double hornRatio;
        double totalHorns = 0d;
        for (Clause clause : clauses) {
            totalHorns += clause.isHorn() ? 1 : 0;
        }
        hornRatio = totalHorns / totalClause;
        features[38] = hornRatio;

        int[] hornOccurrences = new int[variables.length];
        for (Clause clause : clauses) {
            if (clause.isHorn()) {
                List<Integer> literals = clause.getLiterals();
                for (int x : literals) {
                    int v = Math.abs(x);
                    hornOccurrences[v]++;
                }
            }
        }
        double hornMean;
        totalHorns = 0;
        double minHorn = Double.MAX_VALUE;
        double maxHorn = 0d;

        for (int i = 1; i <= totalVar; i++) {
            int horns = hornOccurrences[i];
            totalHorns += horns;
            if (minHorn > horns)
                minHorn = horns;
            if (maxHorn < horns)
                maxHorn = horns;

        }
        hornMean = totalHorns / totalVar;
        double sdHorn = 0;
        for (int i = 1; i <= totalVar; i++) {
            sdHorn += Math.pow((hornOccurrences[i] - hornMean),2);
        }
        features[39] = hornMean;
        features[40] = calcCV(calcSD(sdHorn, totalVar), hornMean);
        features[41] = minHorn;
        features[42] = maxHorn;
        features[43] = calcEntropy(hornOccurrences,totalVar);
    }

    private double[] calcGraphStatistics(Graph<Integer> g, int total) {
        double[] res = new double[5];
        Map<Integer, List<Integer>> map = g.getMap();
        int minEdge = Integer.MAX_VALUE;
        int maxEdge = 0;
        int totalEdges = 0;
        for (int v : map.keySet()) {
            int nEdge = map.get(v).size();
            totalEdges += nEdge;
            if (nEdge > maxEdge) {
                maxEdge = nEdge;
            }
            if (nEdge < minEdge) {
                minEdge = nEdge;
            }
        }
        double meanEdge = (double) totalEdges/total;
        double sd = 0d;
        for (int i = 1; i <= total; i++) {
            if (g.hasVertex(i)) {
                sd += Math.pow((map.get(i).size() - meanEdge), 2);
            }
            else {
                sd += Math.pow(meanEdge, 2);
            }
        }

        int[] counts = new int[maxEdge+1];
        for (int v : map.keySet()) {
            counts[map.get(v).size()]++;
        }

        res[0] = meanEdge;
        res[1] = calcCV(calcSD(sd, total),meanEdge);
        res[2] = minEdge;
        res[3] = maxEdge;
        res[4] = calcEntropy(counts, total);
        return res;
    }

    private double calcEntropy(int[] counts, int total) {
        double entropy = 0;
        for (int count : counts) {
            if(count == 0) {
                continue;
            }
            double p = (double) count / total;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    public double calcSD(double n, int size) {
        return Math.sqrt((n/size));
    }

    public double calcCV(double sd, double mean) {
        if (mean == 0d) {
            return Double.MAX_VALUE;
        }
        return sd/mean;
    }

    private boolean isNegative(int i) {
        return i < 0;
    }

    // Add edges to the Clause graph
    private void connectCG() {
        for (int i = 1; i < negativeLiteralBins.length; i++) {
            if (negativeLiteralBins[i].size() == 0) {
                continue;
            }
            for (int j = 0; j < negativeLiteralBins[i].size(); j++) {
                for (int k = j + 1; k < negativeLiteralBins[i].size(); k++) {
                    int clause1 = negativeLiteralBins[i].get(j).id;
                    int clause2 = negativeLiteralBins[i].get(k).id;
                    if (!cg.hasEdge(clause1,clause2)) {
                        cg.addEdge(clause1,clause2);
                    }
                }
            }
        }
    }

    public double[] getFeatures() {
        return features;
    }

    public void reader(File file) {
            boolean isAlg;
            try {
                Scanner myReader = new Scanner(file);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    isAlg = false;
                    List<String> features = new ArrayList<>();
                    for (String s : data.split("\t")) {
                        if (!isAlg) {
                            isAlg = true;
                            continue;
                        }
                        for (String a : s.split(" ")) {
                            System.out.println(a);
                            break;
                        }
                    }
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
    }

    public void algoReduce(File file) {
        boolean isAlg;
        List<String> algs = new ArrayList<>();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                isAlg = false;
                boolean solution = false;
                StringBuilder times = new StringBuilder();
                for (String s : data.split("\t")) {
                    if (!isAlg) {
                        isAlg = true;
                        String r = s.substring(s.indexOf("/") + 1);
                        times.append(r.substring(0, r.lastIndexOf(".")));
                        continue;
                    }
                    for (String a : s.split(" ")) {
                        times.append(" ").append(a);
                        if (!solution && (int) Double.parseDouble(a) != 3600) {
                            solution = true;
                        }
                        break;
                    }
                }
                if (solution) {
                    algs.add(times.toString());
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try{
            File toWrite = new File("Files/SAT/algorithms_reduced.txt");
            FileWriter writer = new FileWriter(toWrite);
            for (String s : algs) {
                writer.write(s + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }
    }

    public void writeFeaturesToFile(File file) {
        StringBuilder s = new StringBuilder(file.getName() + " ");
        try{
            for (double i : features) {
                s.append(" ").append(i);
            }
            BufferedWriter output;
            output = new BufferedWriter(new FileWriter("Files/SAT/DatasetSAT.txt", true));
            output.append(s.toString());
            output.newLine();

            output.close();
            System.out.println("Succesfully wrote to the result file: " + file.getName());
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }
    }


}
