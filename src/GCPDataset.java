import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GCPDataset {

    private String[] features;

    private HashMap<String, Integer> instances;

    private int instanceSize;

    // size = 569.
    public GCPDataset(int size){
        this.instanceSize = size;
        this.features = new String[size];
        this.instances = new HashMap<>();
    }

    public void generateFeatureFile(File file) {
        List<String> data = new ArrayList<>();

        File dynamic = new File("Files/GCP/dynamic_online.txt");
        File uniform = new File("Files/GCP/uniform.txt");
        File pcost = new File("Files/GCP/low_knowledge.txt");
        File oracle = new File("Files/GCP/oracle.txt");

        int[] dynamicp = new int[instanceSize];
        Arrays.fill(dynamicp, 3600);
        int[] uniformp = new int[instanceSize];
        Arrays.fill(uniformp, 3600);
        int[] pcostp = new int[instanceSize];
        Arrays.fill(pcostp, 3600);
        int[] oraclep = new int[instanceSize];
        Arrays.fill(oraclep, 3600);

        dynamicp = readAlgo(dynamic, "dynamic_online", dynamicp);
        uniformp = readAlgo(uniform, "uniform_portfolio", uniformp);
        pcostp = readAlgo(pcost, "PCOST", pcostp);
        oraclep = readAlgo(oracle, "oracle", oraclep);

        for (int i = 0; i < instanceSize; i++) {
            if (dynamicp[i] == 3600 && uniformp[i] == 3600 && pcostp[i] == 3600 && oraclep[i] == 3600) {
                continue;
            }
            data.add(dynamicp[i] + " " + uniformp[i] + " " + pcostp[i] + " " + oraclep[i] + " f" + features[i]);
        }

        try{
            FileWriter writer = new FileWriter(file);
            for (String s : data) {
                writer.write(s + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }


    }

    public void scanFeatures(File file) {
        boolean isInstance;
        int countInstance = 0;
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                isInstance = false;
                String instance = "";
                String features = "";
                for (String s : data.split("\t")) {
                    if (!isInstance) {
                        isInstance = true;
                        instance = s;
                        continue;
                    }
                    features = String.join(" ", features, s);
                }
                System.out.println(features);
                if (!instance.equals("")) {
                    instances.put(instance, countInstance);
                    this.features[countInstance] = features;
                    countInstance++;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public int[] readAlgo(File file, String alg, int[] p) {
        List<List<String>> dataset = new ArrayList<>();
        boolean isAlg;
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                isAlg = false;
                String instance = "";
                int time = -1;
                List<String> features = new ArrayList<>();
                for (String s : data.split("\t")) {
                    if (!isAlg) {
                        if (s.equals(alg)) {
                            isAlg = true;
                            continue;
                        }
                        break;
                    }
                    if (instance.equals("")) {
                        instance = s;
                        continue;
                    }
                    if (isInteger(s)) {
                        if (time == -1) {
                            time = Integer.parseInt(s);
                        }
                    }
                    else {
                        if (s.equals("TRUE")) {
                            int i = instances.get(instance);
                            if (time < p[i])
                                p[i] = time;
                        }
                    }

                }
                Collections.addAll(features, data.split("\t"));
                dataset.add(features);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return p;
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }



}
