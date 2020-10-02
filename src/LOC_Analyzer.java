import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// deux façons de faire que je peux voir :
// 1. Lire ligne par ligne, et anticiper les possibles whitespace dans les regex (option actuellement implémenté)
// 2. Éliminer le whitespace, tout mettre en un gros bloc mais en préservant les \n et \r, utiliser ceux-ci pour compter les lignes de codes.
//    Plus besoin d'anticiper le whitespace.

public class LOC_Analyzer {
    private int classe_LOC = 0;
    private int methode_LOC = 0;
    private int classe_CLOC = 0;
    private int methode_CLOC = 0;
    private double classe_DC;
    private double methode_DC;

    // Liste de patterns regex

    private final Pattern patternMultiLineCommentStart = Pattern.compile("(^.*/\\*|^/\\*)");
    private final Pattern patternMultiLineEnd = Pattern.compile(".*\\*/");
    private final Pattern patternJavaDocStart = Pattern.compile("(^.*/\\*\\*|^/\\*\\*)"); // même "End" que MultiLine

    private final Pattern patternSingleLineComment = Pattern.compile("^.*//|^//");

    //todo : incertain de ce regex
    private final Pattern blankLine = Pattern.compile("^\\s*$|(^(\\r\\n|\\r|\\n))|((\\r\\n|\\r|\\n)$)");

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

    private ArrayList<String> FileContentLineByLine(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        ArrayList<String> javaFileContent = new ArrayList<>();
        while(scanner.hasNext()){
            javaFileContent.add(scanner.nextLine());
        }
        return javaFileContent;
    }



    private void FindBalancedCurlyBracket() {

    }


    public static void main(String[] args) {

    }

}
