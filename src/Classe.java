import java.util.ArrayList;

public class Classe extends Methode {

    private ArrayList<Methode> class_methods;

    public Classe(String name){
        super(name);
        this.class_methods = new ArrayList<>();
    }

    public void addMethod(Methode methode){
        class_methods.add(methode);
    }

    public ArrayList<Methode> getClass_methods() {
        return class_methods;
    }
}
