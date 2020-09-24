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

    // Liste de patterns regex

    // Patron(s) pour fichier(s)
    private final Pattern patternJavaFile = Pattern.compile(".java$", Pattern.CASE_INSENSITIVE);

    // Patrons pour commentaires et lignes vides
    private final Pattern patternMultiLineComment = Pattern.compile("(^.*/\\*|^/\\*)(.*|.*(\\r\\n|\\r|\\n).*)\\*/.*");
    private final Pattern patternJavaDoc = Pattern.compile("(^.*/\\*\\*|^/\\*\\*)(.*|.*\n.*)\\*/.*");
    private final Pattern patternSingleLineComment = Pattern.compile("(^.*//|^//).*\n");
    private final Pattern blankLine = Pattern.compile("^\\s*$|(^(\\r\\n|\\r|\\n))|((\\r\\n|\\r|\\n)$)", Pattern.MULTILINE);

    // Patrons pour classes et méthodes
    private final Pattern classPattern = Pattern.compile(
            "\\s*((public|protected|default|private)?\\s+)?" +
            "class\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s*\\{", Pattern.CASE_INSENSITIVE);
            // à partir d'ici il faut compter les {} pour arriver à quelque chose d'équilibré.

    private final Pattern methodPattern = Pattern.compile(
            "\\s*((public|protected|default|private)\\s+)?" +
            "(static\\s+)?" +
            "[a-zA-Z_$][a-zA-Z0-9_$]*\\s+" +
            "[a-zA-Z_$][a-zA-Z0-9_$]*\\s*" +
            "\\((([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*,)*\\s*" +
            "([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*))?\\)\\{");
            // à partir d'ici, il faut compter les {} pour arriver à quelque chose d'équilibré.

    // Patrons misc
    private final Pattern javaNamingConvention = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*");

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
            Matcher matcher = patternJavaFile.matcher(file.getName());
            if(matcher.find()){
                fileList.add(file);
            }
        }
        return fileList;
    }

    private void ReadFile(File file){

    }




}
