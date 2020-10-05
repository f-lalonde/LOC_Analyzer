import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//todo : est-ce qu'on préférerait que la méthode soit statique plutôt qu'un objet construit? (je crois que oui, mais à réfléchir).

public class FindFiles {

    // Patron pour extension de fichier
    private final Pattern patternJavaFile = Pattern.compile(".java$", Pattern.CASE_INSENSITIVE);

    private ArrayList<File> fileList = new ArrayList<>();

    public FindFiles(){
        File currentDirectory = new File(System.getProperty("user.dir"));
        fileList = ListJavaFilesInDirectory(currentDirectory, fileList);
    }

    /**
     * Cherche tous les fichiers qui se terminent par ".java" dans le dossier spécifié, ainsi
     * que dans ses sous-dossiers, et les ajoute dans le ArrayList fourni en argument.
     * @param dir dossier dans lequel faire la recherche.
     * @param fileList liste des fichiers déjà trouvés.
     * @return le ArrayList mis à jour avec les nouvelles entrées.
     */
    private ArrayList<File> ListJavaFilesInDirectory(File dir, ArrayList<File> fileList) {

        for(File file : Objects.requireNonNull(dir.listFiles())){
            if(file.isDirectory()){
                fileList = ListJavaFilesInDirectory(file, fileList);
            } else {
                Matcher matcher = patternJavaFile.matcher(file.getName());
                if (matcher.find()) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    public ArrayList<File> getFileList() {
        return fileList;
    }

}
