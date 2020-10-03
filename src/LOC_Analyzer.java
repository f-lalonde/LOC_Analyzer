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

    // Métriques
    private int classe_LOC = 0;
    private int methode_LOC = 0;
    private int classe_CLOC = 0;
    private int methode_CLOC = 0;
    private double classe_DC;
    private double methode_DC;

    // Misc
    private int import_lines_in_file = 0;

    // Liste de patterns regex

    // juste des String.contains pourrait peut-être être suffisant ici?
    private final Pattern patternMultiLineCommentonOneLine = Pattern.compile("/\\*.*\\*/");
    private final Pattern patternMultiLineCommentStart = Pattern.compile("/\\*");
    private final Pattern patternMultiLineEnd = Pattern.compile("\\*/");
    private final Pattern patternJavaDocStart = Pattern.compile("/\\*\\*"); // même "End" que MultiLine
    private final Pattern patternSingleLineComment = Pattern.compile("//");

    // Patrons pour classes et méthodes
    private final Pattern classPattern = Pattern.compile(
            "((public|protected|default|private)?\\s+)?" +
            "class\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s+((extends|implements)\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s+)?\\{", Pattern.CASE_INSENSITIVE);
            // à partir d'ici il faut compter les {} pour arriver à quelque chose d'équilibré.

    private final Pattern methodPattern = Pattern.compile(
            "((public|protected|default|private)\\s+)?" +
            "(static\\s+)?" +
            "[a-zA-Z_$][a-zA-Z0-9_$]*\\s+" +
            "[a-zA-Z_$][a-zA-Z0-9_$]*\\s*" +
            "\\((([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*,)*\\s*" +
            "([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*))?\\)\\{", Pattern.CASE_INSENSITIVE);
            // à partir d'ici, il faut compter les {} pour arriver à quelque chose d'équilibré.

    // Patrons misc
    private final Pattern javaNamingConvention = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*");

    private final Pattern importPattern = Pattern.compile("" +
            "\\s.*import\\s[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$][a-zA-Z0-9_$]*" +
            "|^import\\s[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$][a-zA-Z0-9_$]*");

    public LOC_Analyzer(ArrayList<String> fileLines){
        boolean lineIsInMultiLinecomment = false;
        for(int i = 0; i<fileLines.size(); ++i){

            /* Stratégie générale : on vérifie s'il y a un type de commentaire dans la ligne, on enlève tout
               ce qu'il contient, puis on vérifie ce qu'il reste. */

            String line = fileLines.get(i);

            Matcher classMatcher = classPattern.matcher(line);
            if(classMatcher.find()){
                Classe classe = new Classe(classMatcher.group().replaceAll(".*class\\s+", "").split(" ")[0]);
            }

            Matcher importMatch = importPattern.matcher(line);
            Matcher mlCommentOneLine = patternMultiLineCommentonOneLine.matcher(line);

            boolean mlCommentFound = false;
            boolean javaDocfound = false;
            boolean singleCommentFound = false;

            if(line.contains("//") &&
                    // on vérifie que "//" n'est pas imbriqué dans un commentaire /* ... */ ou /* ...
                    line.replaceAll(patternMultiLineCommentonOneLine.pattern(), "").
                    replaceAll("/\\*", "").contains("//")) {

                line = line.replaceAll("//.*", "");
                singleCommentFound = true;
            }

            if(line.contains("/*")){
                if(mlCommentOneLine.find()){
                    singleCommentFound = true;
                    line = line.replaceAll("/\\*.*\\*/", "");
                } else {
                    if(line.contains("/**")){
                        javaDocfound = true;
                    } else {
                        lineIsInMultiLinecomment = true;
                    }
                    line = line.replaceAll("/\\*.*", "");
                }
            }

        }
    }

    private void findBalancedCurlyBracket() {

    }

    private void  isMultiLineClosed(String[] strings){
        boolean multiLineisClosed = true;

        for(String string:strings){

        }
    }


}
