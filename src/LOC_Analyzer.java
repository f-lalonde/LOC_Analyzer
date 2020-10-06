import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// deux façons de faire que je peux voir :
// 1. Lire ligne par ligne, et anticiper les possibles whitespace dans les regex (option actuellement implémenté)
// 2. Éliminer le whitespace, tout mettre en un gros bloc mais en préservant les \n et \r, utiliser ceux-ci pour compter les lignes de codes.
//    Plus besoin d'anticiper le whitespace.


public class LOC_Analyzer {
    private final ArrayList<Classe> listClasses = new ArrayList<>();

    private final ArrayList<String> fileLines;
    private int javadocLines = 0;
    private int commentOutsideOfAClass = 0;
    private int LOCOutsideOfAClass = 0;
    private String currentMethod = " ";

    // Patrons regex

    private final Pattern classPattern = Pattern.compile(
            "((public|protected|default|private)?\\s+)?" + "class\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s+" +
                    "((extends|implements)\\s+[a-zA-Z_$][a-zA-Z0-9_$]*\\s+)?\\{", Pattern.CASE_INSENSITIVE);
    // à partir d'ici il faut compter les {} pour arriver à quelque chose d'équilibré.

    private final Pattern methodPattern = Pattern.compile(
            "((public|protected|default|private)\\s+)?" +
                    "(static\\s+)?" + "[a-zA-Z_$][a-zA-Z0-9_$]*\\s+" +  "[a-zA-Z_$][a-zA-Z0-9_$]*\\s*" +
                    "\\((([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*,)*\\s*" +
                    "([a-zA-Z_$][a-zA-Z0-9_$]*(\\[])*\\s+[a-zA-Z_$][a-zA-Z0-9_$]*))?\\)\\{", Pattern.CASE_INSENSITIVE);
    // à partir d'ici, il faut compter les {} pour arriver à quelque chose d'équilibré.

    private final Pattern methodNameExtracter = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*\\s*\\(([a-zA-Z_$][a-zA-Z0-9_$\\s,<>\\[\\]]*)?\\)");

    private final Pattern patternMultiLineCommentonOneLine = Pattern.compile("/\\*.*\\*/");

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
        // (i.e. sur au moins 3 lignes).
        boolean outsideOfAClass = true;
        boolean outsideOfAMethod = true;
        int currentClassIndex = -1;
        Methode thisMethode = null;
        boolean mlCommentFound = false;
        boolean javaDocfound = false;
        boolean singleCommentFound;
        for(int i = 0; i<fileLines.size(); ++i) {
            singleCommentFound = false;

            /* Stratégie générale : on vérifie s'il y a un type de commentaire dans la ligne, on enlève tout
               ce qu'il contient, puis on vérifie ce qu'il reste. */

            String line = fileLines.get(i);

            /* On vérifie d'abord si on est à l'intérieur d'un commentaire multi-ligne.
               Si oui, on doit ajouter une ligne de commentaire, et vérifier si il y a la fin du commentaire sur cette
               ligne-ci. Si oui, on doit vérifier pour la présence de code. Sinon, on passe à la ligne suivante.
             */
            if(mlCommentFound || javaDocfound){
                if(line.contains("\\*/")){
                    // on gère d'abord la possibilité d'avoir ouvert et fermé un nouveau commentaire multi-ligne pour
                    // éviter une détection trop gourmande.
                    line = line.replaceAll("/\\*.*\\*/", "");

                    // puis on enlève la partie commentée et on continue l'analyse sur ce qu'il reste.
                    line = line.replaceAll(".*\\*/", "");
                    singleCommentFound = true;
                } else {
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
                    continue;
                }
            }


            // Détection des classes et des méthodes

            if (classMatcher(line, i)) {      //  <--- il y a des effets de bords ici.
                outsideOfAClass = false;
                currentClassIndex = listClasses.size() - 1;
            } else if (listClasses.get(currentClassIndex) != null && i > listClasses.get(currentClassIndex).getEnd()) {
                outsideOfAClass = true;
            }

            if (methodMatcher(line, i)) { //  <--- il y a des effets de bords ici.
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

            singleCommentFound = singleCommentFound || values[0];
            mlCommentFound = values[1];
            javaDocfound = values[2];

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
            } else {
                if(outsideOfAClass){
                    LOCOutsideOfAClass++;
                } else {
                    listClasses.get(currentClassIndex).incrementLOC();
                    if (!outsideOfAMethod) {
                        assert thisMethode != null : "On a détecté que l'on est dans une méthode, mais aucune méthode n'a été chargée.";
                        thisMethode.incrementLOC();
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
            System.out.println("La classe " + classe.getName() + " a un DC de : " + classe.computeDC());
            System.out.println("Ses méthodes sont :");
            classe.getClass_methods().forEach((name,method) -> {
                System.out.println("\t" + name + ", DC : " + method.computeDC());
            });
        });
    }

    /**
     * Vérifie si la ligne est une déclaration de classe. Si oui, on crée un instance de Classe et on l'ajoute au
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
     * Vérifie si la ligne est une déclaration de méthode. Si oui, on crée une instance de Methode et on l'associe à la
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
            currentMethod = methodNameMatcher.group();
            int methodEnd = findBalancedCurlyBracket(currentLine, fileLines);
            // on assume que la méthode trouvée se trouve dans la dernière classe trouvée.
            if(javadocLines > 0){
                listClasses.get(listClasses.size() - 1).addMethod(currentMethod, new Methode(currentMethod, currentLine, methodEnd, javadocLines));
                javadocLines = 0; // on remet le compteur à 0 puisque javadocs assignés.
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
                line = line.replaceAll("/\\*.*\\*/", "");
            } else {
                if(line.contains("/**")){
                    returnedValues[2] = true;   // javaDocfound
                } else {
                    returnedValues[1] = true;   // mlCommentFound
                }
                line = line.replaceAll("/\\*.*", "");
            }
        }
        return returnedValues;
    }
}