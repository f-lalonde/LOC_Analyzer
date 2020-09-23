import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LOC_Analyzer {
    private int classe_LOC = 0;
    private int methode_LOC = 0;
    private int classe_CLOC = 0;
    private int methode_CLOC = 0;
    private double classe_DC;
    private double methode_DC;

    public static void main(String[] args) {

    }

    /**
     * Cherche tous les fichiers qui se termine par ".java" dans le dossier où le programme est exécuté.
     * @return un ArrayList de tous les fichiers répondant au critrère ci-haut.
     */
    private ArrayList<File> ListJavaFilesInDirectory() {
        File directory = new File(System.getProperty("user.dir"));
        ArrayList<File> fileList = new ArrayList<>();
        for(File file : Objects.requireNonNull(directory.listFiles())){
            Pattern pattern = Pattern.compile(".java$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(file.getName());
            if(matcher.find()){
                fileList.add(file);
            }
        }
        return fileList;
    }

    private void ReadFile(File file){

    }




}
