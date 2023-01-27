import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileMerger {

    private List<String> features;

    private List<String> algorithms;

    public FileMerger() {
        this.features = new ArrayList<>();
        this.algorithms = new ArrayList<>();
    }

    public void merge(File f, File a, File target) {
        try {
            Scanner myReader = new Scanner(a);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                    StringBuilder add = new StringBuilder();
                    for (String s : data.split(" ")) {
                        if (s.equals("f")) {
                            add.append("f");
                            algorithms.add(add.toString());
                            break;
                        } else {
                            add.append(s).append(" ");
                        }
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            Scanner myReader = new Scanner(f);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                features.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try{
            FileWriter writer = new FileWriter(target);
            for (int i = 0; i < algorithms.size(); i++) {
                writer.write(algorithms.get(i) + " " + features.get(i) + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("an error occurred.");
            e.printStackTrace();
        }
    }

}
