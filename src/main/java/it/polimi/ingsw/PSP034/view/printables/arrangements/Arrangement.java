package it.polimi.ingsw.PSP034.view.printables.arrangements;

import it.polimi.ingsw.PSP034.view.printables.PrintableObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Arrangement extends PrintableObject {
    private ArrayList<PrintableObject> objects;
    private int alignment;
    private int border;

    public Arrangement(){
        super();
        this.objects = new ArrayList<>();
        alignment = 0;
        border = 1;
        super.setObjectSize(0);
    }

    public void addObjects(PrintableObject...objects){
        this.objects.addAll(Arrays.asList(objects));
        updateAlignment();
    }

    public void removeObjects(PrintableObject...objects){
        this.objects.removeAll(Arrays.asList(objects));
        updateAlignment();
    }

    public void setBorder(int border){
        this.border = border;
        updateAlignment();
    }

    public void updateAlignment(){};

    ArrayList<PrintableObject> getObjects() {
        return objects;
    }

    int getAlignment() {
        return alignment;
    }

    int getBorder() {
        return border;
    }

    void setObjects(ArrayList<PrintableObject> objects) {
        this.objects = objects;
    }

    void setAlignment(int alignment) {
        this.alignment = alignment;
    }
}