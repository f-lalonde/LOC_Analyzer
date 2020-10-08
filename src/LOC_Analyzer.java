import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* PAR RAPPORT À LA COMPLEXITÉ CYCLOMATIQUE :
 *
 * Raisonnement : Tout d'abord, un programme qui n'aurait pas de noeud prédicat aura une CC de 1.
 * Ensuite, pour chaque présence des expressions suivantes, on va augmenter le nombre de 1 :
 * if et else if (également sous la forme X? Y:Z) - for - forEach - while - case (mais pas default) - catch - (assert?)
 *
 *   HYPOTHÈSE 1 :  Le code est bien formatté et fonctionnel.
 *
 *   HYPOTHÈSE 2 :  Étant donné qu'une exception dans un bloc try { ... } catch { ... } ne peut être levé qu'une seule
 *                  fois par bloc, on va monter le nombre de nœuds prédicats de 1 pour chaque bloc catch.
 *
 *  Addendum Michalis Famelis sur hypothèse 2
 *  (7 septembre, 13h56, channel Question sur Slack) :
 *              "Il serait bien sur interessant de voir si une telle hypothèse revele des cas plus interessants dans
 *               JFreechart. Peut-être vous auriez une option de configuration qui l'active et le désactive, et puis
 *               vous comparez?"  todo : <-- faire ça.
 */

public class LOC_Analyzer {
    private final ArrayList<Classe> listClasses = new ArrayList<>();

    private final ArrayList<String> fileLines;
    private int javadocLines = 0;
    private int commentOutsideOfAClass = 0;
    private int LOCOutsideOfAClass = 0;
    private String currentMethod = " ";

    // Patrons regex

    private final Pattern classPattern = Pattern.compile(
            "((public|protected|default|private)?\\s+)?" + "(abstract\\s+)?" + "(class|interface)\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s+" +
                    "((extends|implements)\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s*)?\\{", Pattern.CASE_INSENSITIVE);
    // à partir d'ici il faut compter les {} pour arriver à quelque chose d'équilibré.

    // ce Pattern est dégueulasse, je sais. Il y a surement moyen de faire mieux.
    // J'ai tenté de le rendre "lisible", autant que possible...
    private final Pattern methodPattern = Pattern.compile(
            "^(?!\\s*(catch|while|if|for|switch))\\s*" +
                    "((public|protected|default|private)\\s+)?" +
                    "(static\\s+)?" +
                    "([a-zA-Z_$][a-zA-Z0-9_$]*\\s*" +
                    "(<\\s*([a-zA-Z_$]+[a-zA-Z0-9_$]*\\s*,\\s*)*[a-zA-Z_$]+[a-zA-Z0-9_$]*\\s*>\\s+)?" +
                    "((\\[])*\\s+)?)?" +
                    "(?<methodName>[a-zA-Z_$][a-zA-Z0-9_$]*)\\s*" +
                    "\\((?<signature>(\\s*(?<type1>[a-zA-Z_$][a-zA-Z0-9_$]*" +
                    "(<\\s*([a-zA-Z_$]+[a-zA-Z0-9_$]*\\s*,\\s*)*[a-zA-Z_$]+[a-zA-Z0-9_$]*\\s*>\\s+)?" +
                    "(\\[])*)\\s+" +
                    "[a-zA-Z_$][a-zA-Z0-9_$]*\\s*,)*\\s*" +
                    "((?<type2>[a-zA-Z_$][a-zA-Z0-9_$]*" +
                    "(<\\s*([a-zA-Z_$]+[a-zA-Z0-9_$]*\\s*,\\s*)*[a-zA-Z_$]+[a-zA-Z0-9_$]*\\s*>\\s+)?" +
                    "((\\[])*\\s+)?)" +
                    "[a-zA-Z_$][a-zA-Z0-9_$]*))?\\s*\\)" +
                    "(\\s+throws\\s+[a-zA-Z_$][a-zA-Z0-9_$]*)?\\s*\\{", Pattern.CASE_INSENSITIVE);
    // à partir d'ici, il faut compter les {} pour arriver à quelque chose d'équilibré.

    private final Pattern methodNameExtracter = Pattern.compile("(?<methodName>[a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(([a-zA-Z_$][a-zA-Z0-9_$\\s,<>\\[\\]]*)?\\)");

    private final Pattern patternMultiLineCommentonOneLine = Pattern.compile("/\\*.*\\*/");

    private final Pattern patternNoeudPredicat = Pattern.compile("(" +
            "(?<if>\\s*if(\\s|\\Q(\\E))|" + "(?<ifShortHand>.*\\s*?\\s*.*\\s*:\\s*.*)|" + "(?<for>\\s*for(\\s|\\Q(\\E))|" +
            "(?<forEach>\\.forEach(\\s|\\Q(\\E))|" + "(?<while>\\s*while(\\s|\\Q(\\E))|" +
            "(?<case>\\s*case\\s)" + ")", Pattern.CASE_INSENSITIVE);

    private final Pattern patternNoeudPredicatWithCatch = Pattern.compile("(" +
            "(?<if>\\s*if(\\s|\\Q(\\E))|" + "(?<ifShortHand>.*\\s*?\\s*.*\\s*:\\s*.*)|" + "(?<for>\\s*for(\\s|\\Q(\\E))|" +
            "(?<forEach>\\.forEach(\\s|\\Q(\\E))|" + "(?<while>\\s*while(\\s|\\Q(\\E))|" +
            "(?<case>\\s*case\\s)|" + "(?<catch>(\\s*|})catch(\\s|\\Q(\\E))" +
            ")", Pattern.CASE_INSENSITIVE);

    public LOC_Analyzer(ArrayList<String> fileLines) {
        this.fileLines = fileLines;
        analyzer();
    }

    /**
     * Méthode principale de la classe. Effectue ou appelle toutes les opération d'analyse des lignes.
     */
    private void analyzer(){

        // on va ajouter les lignes qui se trouvent à l'extérieur des classes (commentaires, imports, etc) à toutes
        // les classes se trouvant dans le fichier, à l'exception de la javaDoc plus ou moins correctement formée

        boolean outsideOfAClass = true;
        boolean outsideOfAMethod = true;
        int currentClassIndex = 0;
        Methode thisMethode = null;

        boolean mlCommentFound = false;
        boolean javaDocfound = false;
        boolean singleCommentFound;

        for(int i = 0; i<fileLines.size(); ++i) {

            singleCommentFound = false;

            /* Stratégie générale : on vérifie s'il y a un type de commentaire dans la ligne, on enlève tout
               ce qu'il contient, puis on vérifie ce qu'il reste. */

            String line = fileLines.get(i);

            // on ignore les lignes vides
            if(line.isBlank()){
                continue;
            }

            // On empêche la détection dans les strings, en s'assurant de ne pas capter une mauvaise paire de " ".
            // Ex : fin de commentaire " multi-ligne */ String oops = "on perd tout ceci";
            line = line.replaceAll("\".*\\*/", "SafeGuarded! */");
            line = line.replaceAll("\".*\"", "\"replacedString\" ");

            /* On vérifie d'abord si on est à l'intérieur d'un commentaire multi-ligne.
               Si oui, on doit ajouter une ligne de commentaire, et vérifier si il y a la fin du commentaire sur cette
               ligne-ci. Si oui, on doit vérifier pour la présence de code. Sinon, on passe à la ligne suivante.
             */
            if(mlCommentFound || javaDocfound){

                if(line.contains("*/")){
                    // on gère d'abord la possibilité d'avoir ouvert et fermé un nouveau commentaire multi-ligne pour
                    // éviter une détection trop gourmande.
                    line = line.replaceAll("/\\*.*\\*/", "");

                    // puis on enlève la partie commentée et on continue l'analyse sur ce qu'il reste.
                    line = line.replaceAll(".*\\*/", "");
                    if(javaDocfound){
                        javadocLines++;
                    } else {
                        singleCommentFound = true;
                    }

                } else {
                    if (outsideOfAClass || outsideOfAMethod) {
                        if (javaDocfound) {
                            javadocLines++;
                        } else if(outsideOfAClass){
                            commentOutsideOfAClass++;
                        }
                    } else {
                        listClasses.get(currentClassIndex).incrementCLOC();
                        thisMethode.incrementCLOC();

                    }
                    continue;
                }
            }

            // Détection des classes et des méthodes

            if (classMatcher(line, i)) {    //  <--- il y a des effets de bords ici.
                outsideOfAClass = false;
                currentClassIndex = listClasses.size() - 1;
            } else if (!listClasses.isEmpty() && i > listClasses.get(currentClassIndex).getEnd()) {
                outsideOfAClass = true;
            }

            if (methodMatcher(line, i)) {   //  <--- il y a des effets de bords ici.
                outsideOfAMethod = false;
                thisMethode = listClasses.get(currentClassIndex).getMethod(currentMethod);
            } else if (thisMethode != null && i > thisMethode.getEnd()) {
                outsideOfAMethod = true;
            }

            // Détection des commentaires
            if (findSingleComment(line)) {
                singleCommentFound = true;
                // on enlève la partie commentée et on continue l'analyse sur ce qu'il reste.
                line = line.replaceAll("//.*", "");
            }

            // tableau de 3 booléens, représentant : 0 - singleCommentFound, 1 - mlCommentFound, 2 - javaDocfound
            boolean[] values = findMultiLineComment(line);

            // mise à jour des lignes, selon ce qui fut trouvé
            if(values[0]){
                line = line.replaceAll("/\\*.*\\*/", "");
            } else if(values[1] || values[2]){
                line = line.replaceAll("/\\*.*", "");
            }

            singleCommentFound = singleCommentFound || values[0];
            mlCommentFound = values[1];
            javaDocfound = values[2];

            // si on a trouvé des commentaires, CLOC++
            if(singleCommentFound || mlCommentFound || javaDocfound) {
                if (outsideOfAClass) {
                    if (javaDocfound) {
                        javadocLines++;
                    } else {
                        commentOutsideOfAClass++;
                    }
                } else {
                    listClasses.get(currentClassIndex).incrementCLOC();
                    if (!outsideOfAMethod) {
                        assert thisMethode != null : "On a détecté que l'on est dans une méthode, mais aucune méthode n'a été chargée.";
                        thisMethode.incrementCLOC();
                    }
                }
            }
            // si après tous les retraits il reste encore des charactères non vide, LOC++
            if(!line.isBlank()){
                if(outsideOfAClass){
                    LOCOutsideOfAClass++;
                } else {
                    listClasses.get(currentClassIndex).incrementLOC();
                    if (!outsideOfAMethod) {
                        assert thisMethode != null : "On a détecté que l'on est dans une méthode, mais aucune méthode n'a été chargée.";
                        thisMethode.incrementLOC();

                        // On en profite au passage pour vérifier la présence d'un noeud prédicat :
                        Matcher matcherNoeudPredicat = patternNoeudPredicat.matcher(line);
                        while(matcherNoeudPredicat.find()) {
                            thisMethode.incrementNoeudPredicat();

                        }
                    }
                }
            }
        }

        // on ajoute les lignes de code à l'extérieur des classes à toutes les classes, puisqu'elles en dépendent toutes.
        listClasses.forEach(classe -> {
            while(LOCOutsideOfAClass > 0){
                classe.incrementLOC();
                LOCOutsideOfAClass--;
            }
            while (commentOutsideOfAClass > 0){
                classe.incrementCLOC();
                commentOutsideOfAClass--;
            }
            classe.computeDC();

            classe.getClass_methods().forEach((name,method) -> {
                method.computeDC();
                method.computeCC();
                method.computeBC();
            });
        });
    }

    /**
     * Vérifie si la ligne est une déclaration de classe. Si oui, on crée un instance de com.francislalonde.Classe et on l'ajoute au
     * hashmap listClasses. Sinon, il ne se passe rien.
     * Agit par effet de bord.
     * @param line contenu de la ligne actuelle
     * @param currentLine numéro de la ligne actuelle
     * @return vrai si on a trouvé une classe.
     */
    private boolean classMatcher(String line, int currentLine){
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
            return true;
        }
        return false;
    }

    /**
     * Vérifie si la ligne est une déclaration de méthode. Si oui, on crée une instance de com.francislalonde.Methode et on l'associe à la
     * classe dans laquelle elle se trouve. On met également à jour la variable String "currentMethod", qui aura le nom
     * et la signature de la méthode que nous venons de détecter.
     * Sinon, il ne se passe rien.
     * Agit par effet de bord.
     * @param line contenu de la ligne actuelle
     * @param currentLine numéro de la ligne actuelle
     * @return vrai si on a trouvé une méthode
     */
    private boolean methodMatcher(String line, int currentLine){
        Matcher methodMatcher = methodPattern.matcher(line);
        if (methodMatcher.find()) {
            Matcher methodNameMatcher = methodNameExtracter.matcher(methodMatcher.group());
            methodNameMatcher.find();

            // Extrait les types de la signature et les lie par "_"
            String tempSignature;
            if(methodNameMatcher.group(2) != null) {
                tempSignature = methodNameMatcher.group(2).replaceAll("\\s*[a-zA-Z_$][a-zA-Z0-9_$]*\\s*,\\s*", "_");
                String[] temp = tempSignature.split("\\s+");
                if (temp[0].isBlank()) {
                    tempSignature = "_"+temp[1];
                } else {
                    tempSignature = "_"+temp[0];
                }
            }else {
                tempSignature = "";
            }
            currentMethod = methodNameMatcher.group("methodName")+tempSignature;
            int methodEnd = findBalancedCurlyBracket(currentLine, fileLines);
            // on assume que la méthode trouvée se trouve dans la dernière classe trouvée.
            Classe currentClass = listClasses.get(listClasses.size() - 1);
            if(javadocLines > 0){
                currentClass.addMethod(currentMethod, new Methode(currentMethod, currentLine, methodEnd, javadocLines));
                // on remet le compteur à 0 en assignant aussi les javadocs à la classe de la méthode.
                while(javadocLines > 0){
                    currentClass.incrementCLOC();
                    javadocLines--;
                }
            } else {
                listClasses.get(listClasses.size() - 1).addMethod(currentMethod, new Methode(currentMethod, currentLine, methodEnd, 0));
            }
            return true;
        }
        return false;
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
            if(line.isBlank()){
                continue;
            }
            // on évite de détecter les '{' et '}' dans les strings ou les commentaires
            line = line.replaceAll("\".*\"", "stringReplaced");
            line = line.replaceAll("//.*", " ");
            line = line.replaceAll(patternMultiLineCommentonOneLine.pattern(), " ");
            line = line.replaceAll("/\\*.*", " ");

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

    private boolean findSingleComment(String line) {
        // on vérifie que "//" n'est pas imbriqué dans un commentaire /* ... */ ou /* ... (sinon il sera compté deux fois)
        return line.contains("//") &&
                line.replaceAll(patternMultiLineCommentonOneLine.pattern(), "").
                        replaceAll("/\\*", "").contains("//");
    }

    private boolean[] findMultiLineComment(String line){
        Matcher mlCommentOneLine = patternMultiLineCommentonOneLine.matcher(line);
        boolean[] returnedValues = {false, false, false};
        if(line.contains("/*")){
            if(mlCommentOneLine.find()){
                returnedValues[0] = true;       // singleCommentFound
            } else {
                if(line.contains("/**")){
                    returnedValues[2] = true;   // javaDocfound
                } else {
                    returnedValues[1] = true;   // mlCommentFound
                }
            }
        }
        return returnedValues;
    }

    public ArrayList<Classe> getListClasses() {
        return listClasses;
    }

}