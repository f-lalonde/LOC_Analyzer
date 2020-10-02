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

    // Patron(s) pour fichier(s)
    private final Pattern patternJavaFile = Pattern.compile(".java$", Pattern.CASE_INSENSITIVE);

    // Patrons pour commentaires et lignes vides. Assume une lecture ligne par ligne du fichier.

        // détecte /* avec quoi que ce soit avant.
    private final Pattern patternMultiLineCommentStart = Pattern.compile("(^.*/\\*|^/\\*)");

        // détecte */ avec quoi que ce soit avant.
    private final Pattern patternMultiLineEnd = Pattern.compile(".*\\*/");

        // détecte \** avec quoi que ce soit avant. (utise le même "End" que MultiLine)
    private final Pattern patternJavaDocStart = Pattern.compile("(^.*/\\*\\*|^/\\*\\*)");

        // détecte // avec quoi que ce soit avant.
    private final Pattern patternSingleLineComment = Pattern.compile("^.*//|^//");

        // détecte un string composé d'espace vide et se terminant soit par un espace, soit par un caractère de retour
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

    private void FindBalancedCurlyBracket(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        String currentWord;
        char currentChar;
        while(scanner.hasNext()){
            currentWord = scanner.next();
            for(int i = 0; i<currentWord.length(); ++i){
                currentChar = currentWord.charAt(i);
                if(MatchChar(currentChar, '{')){
                    //do something
                }
            }
        }

    }

    private boolean MatchChar(char currentChar, char charToMatch){
        return charToMatch == currentChar;
    }




}
