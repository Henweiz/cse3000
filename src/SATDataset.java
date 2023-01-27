import java.io.*;
import java.util.*;

public class SATDataset {

    private String[] features;

    private HashMap<String, Integer> instances;

    private int instanceSize;

    // size = 365.
    public SATDataset(int size){
        this.instanceSize = size;
        this.features = new String[size];
        this.instances = new HashMap<>();
    }

    public void generateFeatureFile(File algorithms, File target) {
        List<String> dataset = new ArrayList<>();
        try {
            Scanner myReader = new Scanner(algorithms);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                boolean isInstance = false;
                String instance = "";
                StringBuilder algs = new StringBuilder();
                for (String s : data.split(" ")) {
                    if (!isInstance) {
                        isInstance = true;
                        instance = s;
                        continue;
                    }
                    algs.append(s).append(" ");
                }
                if (instances.containsKey(instance)) {
                    int row = instances.get(instance);
                    algs.append("f ").append(this.features[row]);
                    dataset.add(algs.toString());
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        try{
            FileWriter writer = new FileWriter(target);
            for (String s : dataset) {
                writer.write(s + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }

    }

    public void generateFeatureFile2(File algorithms, File target) {
        List<String> dataset = new ArrayList<>();
        StringBuilder algs = new StringBuilder();
        String instance = "";
        try {
            Scanner myReader = new Scanner(algorithms);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                boolean isAlgo = true;
                boolean gotInstance = false;
                for (String s : data.split("\t")) {
                    if (!gotInstance) {
                        if (!instance.equals(s)) {
                            if (instances.containsKey(instance)) {
                                int row = instances.get(instance);
                                algs.append("f ").append(this.features[row]);
                                String add = algs.toString();
                                dataset.add(add);
                            }
                            instance = s;
                            algs = new StringBuilder();
                        }
                        gotInstance = true;
                        continue;
                    }
                    if (isAlgo) {
                        isAlgo = false;
                        continue;
                    }
                    algs.append(s).append(" ");
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try{
            FileWriter writer = new FileWriter(target);
            for (String s : dataset) {
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
                StringBuilder features = new StringBuilder();
                for (String s : data.split("\t")) {
                    if (!isInstance) {
                        isInstance = true;
                        instance = s;
                        continue;
                    }
                    features.append(s).append(" ");
                }
                if (!instance.equals("")) {
                    instances.put(instance, countInstance);
                    this.features[countInstance] = features.toString();
                    countInstance++;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void scanBinarizedFile(File file) {
        int countInstance = 0;
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                StringBuilder features = new StringBuilder();
                for (String s : data.split(" ")) {
                    features.append(s).append(" ");
                }
                this.features[countInstance] = features.toString();
                countInstance++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void convertToFloat(File file) {
        List<String> floats = new ArrayList<>();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                StringBuilder features = new StringBuilder();
                for (String s : data.split(" ")) {
                    if (s.equals(""))
                        continue;
                    double d = Double.parseDouble(s);
                    float f = (float) d;
                    if (Float.isInfinite(f)) {
                        f = 0f;
                    }
                    features.append(f).append(" ");
                }
                floats.add(features.toString());
            }
            myReader.close();
            try {
                File file1 = new File("Files/SAT/converted.txt");
                FileWriter writer = new FileWriter(file1);
                for (String i : floats) {

                    writer.write(i + System.lineSeparator());
                }
                writer.close();
            } catch (IOException e) {
                System.out.println("an error occurred.");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writeOnlyFeatures(File target) {
        try {
            FileWriter writer = new FileWriter(target);
            for (String i : features) {
                writer.write(i + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }
    }
}
