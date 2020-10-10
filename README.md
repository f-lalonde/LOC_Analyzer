# LOC_Analyzer
TP1 - Métrique  
Dans le cadre du cours IFT-3913  
Professeur : Michalis Famelis  
Remis le 9 octobre 2020  

Note quant au devoir : La version Maven avait été démarrée en test, puis développée comme source principale. Vous trouverez l'historique des commits ici : https://github.com/pticrix/Analyzer

## Description
Ce programme analyse tous les fichiers java se trouvant dans un dossier ainsi que ces sous-dossiers, et en extrait des informations à partir des classes et des méthodes s'y trouvant. 

Les informations recueillies et calculées seront exportées dans des fichiers .csv, pouvant ensuite être importées dans le tabulateur de votre choix. 

### Les informations sur les classes sont les suivantes : 
- Chemin d'accès du fichier dans lequel se trouve la classe
- Nom de la classe
- classe_LOC : nombre de lignes de code d’une classe
- classe_CLOC : nombre de lignes de code d’une classe qui contiennent des commentaires
- classe_DC : densité de commentaires pour une classe : classe_DC = classe_CLOC / classe_LOC
- WMC : « Weighted Methods per Class », pour chaque classe. i.e. la somme pondérée des complexités des méthodes d'une classe. 
- classe_BC : degré selon lequel une classe est bien commentée classe_BC = classe_DC / WMC

### Les informations sur les méthodes sont les suivantes :
- methode_LOC : nombre de lignes de code d’une méthode
- methode_CLOC : nombre de lignes de code d’une méthode qui contiennent des commentaires
- methode_DC : densité de commentaires pour une méthode : methode_DC = methode_CLOC / methode_LOC
- CC : complexité cyclomatique de McCabe pour chaque méthode
- methode_BC : degré selon lequel une méthode est bien commentée methode_BC = methode_DC / CC

## Pour faire fonctionner le programme :
**Depuis IntelliJ** : 
1.  Cloner le Git
2.  S'assurer que les dépendances soient bien vues par Maven (il se peut que vous deviez exécuter le goal "install" dans le menu de Maven. Cela va télécharger et appliquer les dépendances)
3.  Exécuter la configuration :
    - "Main" pour exécuter le logiciel, ce qui compilera les informations pour le code lui-même
    - "Tests" pour rouler les tests unitaires
    - "Rebuild JAR" afin de générer un .jar exécutable.
  
**Depuis le .jar exécutable** : 

Il suffit de déplacer le fichier Analyzer-1.0-SNAPSHOT.jar dans le dossier source du projet à analyser. Ensuite vous avez trois choix :
  1. Double cliquer sur le fichier pour l'exécuter. Il faudra faire attention, car il n'y a rien pour vous dire quand le travail est terminé. Il faudra regarder les processus.
  2. Démarrer le fichier depuis l'invite de commande à l'aide de la commande suivante :  
      `java -jar ./Analyzer-1.0-SNAPSHOT.jar`  
      (Cela requiert d'avoir java dans le PATH)
  3. En utilisant l'utilitaire run.bat, que vous trouverez dans le dossier source de ce projet-ci. Il doit être placé dans le même dossier que Analyzer-1.0-SNAPSHOT.jar. Cet utilitaire ouvrira une invite de commande qui vous laissera savoir quand le travail est terminé. 

Le .jar exécutable et l'utilitaire run.bat se trouvent dans le dossier "util"

L'exécution du fichier produira deux fichiers dans le dossier source du projet analysé : 
  1. classes.csv   - Il contient les informations relatives à toutes les classes découvertes dans le projet
  2. methodes.csv  - Il contient les informations relatives à toutes les méthodes découvertes dans le projet


Pour toute question, veuillez contacter francis.lalonde@umontreal.ca

Merci de votre intérêt :)

