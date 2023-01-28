import FeatureExtraction.FeatureExtractor;
import Tree.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors


public class Main {
    public static void main(String[] args) {
        long start = System.nanoTime();
        File file = new File("Files/Test/anneal.txt");

        // 44 features, 15 solutions, 290 instances.
        //file = new File("Files/SAT/Solutions/SAT250.txt");

        // 48 features, 4 solutions, 105 instances.
        file = new File("Files/GCP/Solutions/ASP.txt");

        // 37 features, 532 solutions, 1004 instances.
        //file = new File("Files/MIP/Solutions/target.txt");
        ASPDataset dataset = scanASPTxt(file, 4, 48);

        //file = new File("Files/Test/test.txt");

        /*
   // Generate SAT dataset
        FeatureExtractor featureExtractor = new FeatureExtractor();


        featureExtractor.satReader(file);
        featureExtractor.generateFeatures();
        featureExtractor.writeToFile(file);

        double[] features = featureExtractor.getFeatures();
        for (int i = 0; i < features.length; i++)
            System.out.println("Feature: " + (i+1) + " " + features[i]);

        file = new File("Files/SAT/algorithms.txt");
        featureExtractor.algoReduce(file);


        file = new File("Files/SAT/DatasetSAT.txt");
        SATDataset sat = new SATDataset(365);
        sat.scanFeatures(file);
        sat.writeOnlyFeatures(new File("Files/SAT/onlyfeaturesSAT.txt"));
        sat.convertToFloat(new File("Files/SAT/onlyfeaturesSAT.txt"));
*/

        // Generate GCP dataset
/*
        file = new File("Files/GCP/features.txt");
        GCPDataset gcp = new GCPDataset(569);
        gcp.scanFeatures(file);
        File fileToWrite = new File("Files/GCP/data2.txt");
        gcp.generateFeatureFile(fileToWrite);

*/

        int depth = 2;
        int nodes = 3;
        Branch rootBranch = new Branch();
        /*
        int sum = 0;
        for (Algorithm a : dataset.getAlgorithms()) {
            sum += a.getTotalRunTime();
        }
        System.out.println(sum);
         */

        Solver solver = new Solver(dataset, 10000, depth);
        BinaryTree.Node best = solver.solveSubtree(dataset, rootBranch, depth, nodes, Integer.MAX_VALUE);

        System.out.println("Misclassification score based on runtime (* 100): " + best.getMc());
        System.out.println("number of nodes in tree " + best.getTotalNodes());
        System.out.println("cache entries: " + solver.cache.getNumEntries());


        long end = System.nanoTime();
        long duration = (end - start) / 1000000;
        System.out.println("execution took: " + duration + " ms");

        /*
        // TODO fix printing, cuz its wacky atm
        BinaryTree.Node tree = solver.constructTree(dataset, rootBranch, depth, nodes);
        tree.print2D();
        System.out.println("Total misclassifications: " + tree.computeMisclassification(dataset));
*/
    }


    // Scan dataset file.
    public static Dataset scanTxt(File file) {

        List<LabelInstances> labelInstances = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        int id = 0;

        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                boolean isLabel = true;
                int txtLabel = 0;
                ArrayList<Integer> list = new ArrayList<>();
                for (String s : data.split(" ")) {
                    if (isLabel) {
                        txtLabel = Integer.parseInt(s);
                        isLabel = false;
                    } else {
                        list.add(Integer.parseInt(s));
                    }
                }

                if (!labels.contains(txtLabel)) {
                    labels.add(txtLabel);
                    LabelInstances instances = new LabelInstances(txtLabel, new FeatureRow(list, null, id, txtLabel));
                    labelInstances.add(instances);
                    id++;
                }
                else {
                    for (LabelInstances instances : labelInstances) {
                        if (instances.getLabel() == txtLabel) {
                            instances.addList(new FeatureRow(list, null, id, txtLabel));
                            id++;
                            break;
                        }
                    }
                }
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        Dataset dataset = new Dataset();
        for (LabelInstances instances : labelInstances) {
            dataset.addInstances(instances);
        }

        return dataset;
    }

    // Scan ASP dataset file (has algorithm runtimes instead of labels).
    public static ASPDataset scanASPTxt(File file, int numOfSolutions, int numOfFeatures) {
        int skip = 0;
        numOfSolutions = numOfSolutions - skip;
        ASPDataset dataset = new ASPDataset(numOfSolutions);

        // Add number of algorithms to the dataset first.
        for (int i = 0; i < numOfSolutions; i++) {
            dataset.addAlgorithm(new Algorithm(numOfFeatures, i));
        }

        int id = 0;

        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                boolean isLabel = true;
                int txtLabel = 0;

                int skipcount = 0;
                int[] solutions = new int[numOfSolutions];
                ArrayList<Integer> list = new ArrayList<>();
                int count = 0;
                for (String s : data.split(" ")) {
                    if (s.equals("f")) {
                        isLabel = false;
                        continue;
                    }
                    // Count the algorithm runtimes.
                    if (isLabel) {
                        if (skipcount < skip) {
                            skipcount++;
                            continue;
                        }
                        solutions[count] = (int) (Double.parseDouble(s) * 100);
                        count++;
                    } else {
                        list.add(Integer.parseInt(s));
                    }
                }

                // Get the most optimal algorithm.
                int min = Integer.MAX_VALUE;
                for (int i = 0; i < numOfSolutions; i++) {
                    if (min > solutions[i]) {
                        min = solutions[i];
                        txtLabel = i;
                    }
                }
                dataset.addFeatureRow(new FeatureRow(list, solutions, id, txtLabel));
                id++;
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        return dataset;
    }

    public static void labelASPTxt(File file, int numOfSolutions, int numOfFeatures, File target) {

        ASPDataset dataset = new ASPDataset(numOfSolutions);

        // Add number of algorithms to the dataset first.
        for (int i = 0; i < numOfSolutions; i++) {
            dataset.addAlgorithm(new Algorithm(numOfFeatures, i));
        }

        int id = 0;

        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                boolean isLabel = true;
                int txtLabel = 0;
                int[] solutions = new int[numOfSolutions];
                ArrayList<Integer> list = new ArrayList<>();
                int count = 0;
                for (String s : data.split(" ")) {
                    if (s.equals("f")) {
                        isLabel = false;
                        continue;
                    }
                    // Count the algorithm runtimes.
                    if (isLabel) {
                        solutions[count] = (int) (Double.parseDouble(s) * 100);
                        count++;
                    } else {
                        list.add(Integer.parseInt(s));
                    }
                }

                // Get the most optimal algorithm.
                double min = Double.MAX_VALUE;
                for (int i = 0; i < numOfSolutions; i++) {
                    if (min > solutions[i]) {
                        min = solutions[i];
                        txtLabel = i;
                    }
                }
                dataset.addFeatureRow(new FeatureRow(list, solutions, id, txtLabel));
                id++;
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            FileWriter writer = new FileWriter(target);
            for (FeatureRow f : dataset.getDataset()) {
                writer.write(f.getOptimalLabel() + " ");
                for (int i : f.getValues()) {
                    writer.write(i + " ");
                }
                writer.write(System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }



    }

    // Write a string to a file.
    public void write(String filename, String s) {
        try{
            FileWriter writer = new FileWriter(filename);
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }
    }


}