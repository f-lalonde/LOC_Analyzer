import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class LinesExtractor {

    public ArrayList<String> LinesExtractor(File file) throws FileNotFoundException {
        return fileContentLineByLine(file);
    }

    private ArrayList<String> fileContentLineByLine(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        ArrayList<String> javaFileContent = new ArrayList<>();
        while(scanner.hasNext()){
            javaFileContent.add(scanner.nextLine());
        }
        return javaFileContent;
    }

}
