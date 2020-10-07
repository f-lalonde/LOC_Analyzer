public class Methode {
    private final String name;
    private int LOC;
    private int CLOC;
    private double DC;

    private final int start;
    private final int end;

    public Methode(String name, int start, int end, int javadocLines){
        this.name = name;
        this.LOC = 0;
        this.CLOC = javadocLines;
        this.DC = 0;
        this.start = start;
        this.end = end;
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

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

}
