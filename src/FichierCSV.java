//Classe pour créer un fichier CSV

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class FichierCSV {

    public static void main (String[] args) throws Exception {

        /**Code pour écrire un fichier CSV pris sur
         * http://java.mesexemples.com/fichiersrepertoires/java-creer-un-fichier-csv/
         */
        CSVWriter writer = new CSVWriter(new FileWriter("classes.csv"), ',');
        String[][] entree = {{"chemin", "class", "classe_LOC", "classe_CLOC", "classe_DC"}};
        for(String elem[]:entree)
            writer.writeNext(elem);
        writer.close();

    }

}