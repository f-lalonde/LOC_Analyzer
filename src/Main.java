import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        ArrayList<File> javaFileList = FindFiles.getFilesList();
        javaFileList.forEach(file -> {
            try {
                ArrayList<String> linesCoded = LinesExtractor.fileContentLineByLine(file);
                LOC_Analyzer analyzer = new LOC_Analyzer(linesCoded);
                // Envoyer vers la création de rapport.
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        });

    }
}
