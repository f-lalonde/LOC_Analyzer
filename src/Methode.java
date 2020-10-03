public class Methode {
    private String name;
    private int LOC;
    private int CLOC;
    private double DC;

    public Methode(String name){
        this.name = name;
        this.LOC = 0;
        this.CLOC = 0;
        this.DC = 0;
    }

    public void incrementLOC(){
        LOC++;
    }

    public void incrementCLOC(){
        CLOC++;
    }

    public void computeDC(){
        if(LOC == 0){
            DC = 0;
        } else {
            DC = (double)CLOC / LOC;
        }
    }

    public String getName() {
        return name;
    }

    public int getLOC() {
        return LOC;
    }

    public int getCLOC() {
        return CLOC;
    }

    public double getDC() {
        return DC;
    }

}
