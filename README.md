# LOC_Analyzer
TP1 - Métrique
Dans le cadre du cours IFT-3913
Professeur : Michalis Famelis

## Pour faire fonctionner :
**Depuis IntelliJ** : 
  1.  Cloner le Git
  2.  S'assurer que les dépendances soient bien vues par Maven (il se peut que vous deviez exécuter le goal "install" dans le menu de Maven. Cela va télécharger et appliquer les dépendances)
  3.  Exécuter la configuration :
    1. "Main" pour exécuter le logiciel, ce qui compilera les informations pour le code lui-même
    2. "Tests" pour rouler les tests unitaires
    3. "Rebuild JAR" afin de générer un .jar exécutable.
  
**Depuis le .jar exécutable** : 

Il suffit de déplacer le fichier Analyzer-1.0-SNAPSHOT.jar dans le dossier source du projet à analyser. Ensuite vous avez trois choix :
  1. Double cliquer sur le fichier pour l'exécuter. Il faudra faire attention, car il n'y a rien pour vous dire quand le travail est terminer. Il faudra regarder les processus.
  2. Démarrer le fichier depuis l'invite de commande à l'aide de la commande suivante : 
      `java -jar ./Analyzer-1.0-SNAPSHOT.jar`
      (Cela requiert d'avoir java dans le PATH)
  3. En utilisant l'utilitaire run.bat, que vous trouverez dans le dossier source de ce projet-ci. Il doit être placé dans le même dossier que Analyzer-1.0-SNAPSHOT.jar. Cet utilitaire ouvrira une invite de commande qui vous laissera savoir quand le travail est terminé. 

L'exécution du fichier produira deux fichiers dans le dossier source du projet analysé : 
  1. classes.csv   - Il contient les informations relatives à toutes les classes découvertes dans le projet
  2. methodes.csv  - Il contient les informations relatives à toutes les méthodes découvertes dans le projet


Pour toute question, veuiller contacter francis.lalonde@umontrea.ca

Merci de votre intérêt :)

