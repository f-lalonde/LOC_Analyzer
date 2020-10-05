import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// deux façons de faire que je peux voir :
// 1. Lire ligne par ligne, et anticiper les possibles whitespace dans les regex (option actuellement implémenté)
// 2. Éliminer le whitespace, tout mettre en un gros bloc mais en préservant les \n et \r, utiliser ceux-ci pour compter les lignes de codes.
//    Plus besoin d'anticiper le whitespace.

public class LOC_Analyzer {
    private final ArrayList<Classe> listClasses = new ArrayList<>();

    // Métriques
    private int classe_LOC = 0;
    private int methode_LOC = 0;
    private int classe_CLOC = 0;
    private int methode_CLOC = 0;
    private double classe_DC;
    private double methode_DC;

    private ArrayList<String> fileLines;
    private int javadocLines = 0;
    private int importLines = 0;
    // Misc
    private int import_lines_in_file = 0;

    // Patrons regex
    private final Pattern javaNamingConvention = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*");

    Pattern classPattern = Pattern.compile(
            "((public|protected|default|private)?\\s+)?" + "class\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s+" +
                    "((extends|implements)\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s+)?\\{", Pattern.CASE_INSENSITIVE);
    // à partir d'ici il faut compter les {} pour arriver à quelque chose d'équilibré.

    Pattern methodPattern = Pattern.compile(
            "((public|protected|default|private)\\s+)?" +
                    "(static\\s+)?" + "[a-zA-Z_$][a-zA-Z0-9_$]*\\s+" +  "[a-zA-Z_$][a-zA-Z0-9_$]*\\s*" +
                    "\\((([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*,)*\\s*" +
                    "([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*))?\\)\\{", Pattern.CASE_INSENSITIVE);
    // à partir d'ici, il faut compter les {} pour arriver à quelque chose d'équilibré.

    Pattern methodNameExtracter = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*\\s*\\(([a-zA-Z_$][a-zA-Z0-9_$\\s,<>\\[\\]]*)?\\)");

    public LOC_Analyzer(ArrayList<String> fileLines) {
        this.fileLines = fileLines;
        analyzer();
    }

    /**
     * Méthode principale de la classe. Effectue ou appelle toutes les opération d'analyse des lignes.
     */
    private void analyzer(){
        for(int i = 0; i<fileLines.size(); ++i){

            /* Stratégie générale : on vérifie s'il y a un type de commentaire dans la ligne, on enlève tout
               ce qu'il contient, puis on vérifie ce qu'il reste. */

            String line = fileLines.get(i);

            // Détection des classes et des méthodes

            classMatcher(line, i);

            methodMatcher(line, i);

            // Détection des commentaires

            //todo: sans barrière, ici on va avoir un problème ; À chaque boucle, on va resetter les bool.
            // Bye bye comptage des commentaires multilignes!
            boolean mlCommentFound = false;
            boolean javaDocfound = false;
            boolean singleCommentFound = false;

            Pattern patternMultiLineCommentonOneLine = Pattern.compile("/\\*.*\\*/");

            // on vérifie que "//" n'est pas imbriqué dans un commentaire /* ... */ ou /* ...
            // juste des String.contains pourrait peut-être être suffisant ici?
            if(line.contains("//") &&
                    line.replaceAll(patternMultiLineCommentonOneLine.pattern(), "").
                            replaceAll("/\\*", "").contains("//")) {

                // on enlève la partie commentée et on continue l'analyse sur ce qu'il reste.
                line = line.replaceAll("//.*", "");
                singleCommentFound = true;
            }

            Matcher mlCommentOneLine = patternMultiLineCommentonOneLine.matcher(line);
            if(line.contains("/*")){
                if(mlCommentOneLine.find()){
                    singleCommentFound = true;
                    line = line.replaceAll("/\\*.*\\*/", "");
                } else {
                    if(line.contains("/**")){
                        javaDocfound = true;
                    } else {
                        mlCommentFound = true;
                    }
                    line = line.replaceAll("/\\*.*", "");
                }
            }

            // Détection des importations. On va ajouter ces lignes à toutes les classes présentes dans le fichier.

            // todo : on pourrait laisser faire et juste dire que tout code ou commentaire qui ne se trouve pas dans
            //  une classe en particulier appartient à toutes les classes se trouvant dans le fichier (surtout que
            //  généralement, on retrouve une seule classe par fichier, du haut de mon peu d'expérience). Donc, on doit
            //  détecter si on est dans une classe, et si non, garder en mémoire le compte de LOC / CLOC, et l'ajouter
            //  à toutes les classes du fichier à la fin.
            Pattern importPattern = Pattern.compile("\\s.*import\\s[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$][a-zA-Z0-9_$]*"
                    + "|^import\\s[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$][a-zA-Z0-9_$]*");
            Matcher importMatch = importPattern.matcher(line);

        }
    }

    /**
     * Vérifie si la ligne est une déclaration de classe. Si oui, on crée un instance de Classe et on l'ajoute au
     * hashmap listClasses. Sinon, il ne se passe rien.
     * Agit par effet de bord.
     * @param line contenu de la ligne actuelle
     * @param currentLine numéro de la ligne actuelle
     */
    private void classMatcher(String line, int currentLine){
        Matcher classMatcher = classPattern.matcher(line);
        if (classMatcher.find()) {
            String className = classMatcher.group().replaceAll(".*class\\s+", "").split(" ")[0];
            int classEnd = findBalancedCurlyBracket(currentLine, fileLines);
            if(javadocLines > 0){
                listClasses.add(new Classe(className, currentLine, classEnd, javadocLines));
                javadocLines = 0; // on remet le compteur à 0 puisque javadocs assignés.
            } else {
                listClasses.add(new Classe(className, currentLine, classEnd, 0));
            }
        }
    }

    /**
     * Vérifie si la ligne est une déclaration de méthode. Si oui, on crée une instance de Methode et on l'associe à la
     * classe dans laquelle elle se trouve. Sinon, il ne se passe rien.
     * Agit par effet de bord.
     * @param line contenu de la ligne actuelle
     * @param currentLine numéro de la ligne actuelle
     */
    private void methodMatcher(String line, int currentLine){
        Matcher methodMatcher = methodPattern.matcher(line);
        if (methodMatcher.find()) {

            Matcher methodNameMatcher = methodNameExtracter.matcher(methodMatcher.group());
            String methodNameAndSign = methodNameMatcher.group();
            int methodEnd = findBalancedCurlyBracket(currentLine, fileLines);
            // on assume que la méthode trouvée se trouve dans la dernière classe trouvée.
            if(javadocLines > 0){
                listClasses.get(listClasses.size() - 1).addMethod(methodNameAndSign, new Methode(methodNameAndSign, currentLine, methodEnd, javadocLines));
                javadocLines = 0; // on remet le compteur à 0 puisque javadocs assignés.
            } else {
                listClasses.get(listClasses.size() - 1).addMethod(methodNameAndSign, new Methode(methodNameAndSign, currentLine, methodEnd, 0));
            }
        }
    }

    /**
     * Vérifie si les '{' et les '}' sont bien balancée. Si oui, renvoie la ligne à laquelle le balancement s'est
     * effectué. Sinon, renvoie -1.
     * @param startAtLine Numéro de la ligne à laquelle a débuté l'analyse
     * @param fileLines Ensemble des lignes de code du fichier.
     * @return Numéro de la ligne où l'on a trouvé le balancement.
     */
    private int findBalancedCurlyBracket(int startAtLine, ArrayList<String> fileLines) {
        int bracketCount = 0;
        for(int i = startAtLine; i<fileLines.size();++i){
            String line = fileLines.get(i);
            for(int j = 0; j<line.length(); j++){
                if(line.charAt(j) == '{'){
                    bracketCount++;
                } else if(line.charAt(j) == '}'){
                    bracketCount--;
                }
            }
            if(bracketCount == 0){
                return i;
            }
        }
        return -1;
    }

}