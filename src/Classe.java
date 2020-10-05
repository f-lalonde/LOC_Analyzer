import java.util.HashMap;

public class Classe extends Methode {

    private final HashMap<String, Methode> class_methods= new HashMap<>();

    public Classe(String name, int start, int end, int javadocLines){
        super(name, start, end, javadocLines);
    }

    public void addMethod(String methodName, Methode methode){
        class_methods.put(methodName, methode);
    }

    public HashMap<String, Methode> getClass_methods() {
        return class_methods;
    }

    public Methode getClassMethod(String name) {
        return class_methods.get(name);
    }
}
