import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindFiles {

    // Patron pour extension de fichier
    private final Pattern patternJavaFile = Pattern.compile(".java$", Pattern.CASE_INSENSITIVE);

    private ArrayList<File> fileList = new ArrayList<>();

    public FindFiles(){
        fileList = ListJavaFilesInDirectory(fileList);
    }

    /**
     * Cherche tous les fichiers qui se terminent par ".java" dans le dossier où le programme est exécuté ainsi que ses.
     * sous-dossiers, qui sont recherchés récursivement.
     * @return un ArrayList de tous les fichiers répondant au critrère ci-haut.
     */
    private ArrayList<File> ListJavaFilesInDirectory(ArrayList<File> fileList) {
        File directory = new File(System.getProperty("user.dir"));
        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
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
