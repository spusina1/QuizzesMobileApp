package ba.unsa.etf.rma;

import java.util.ArrayList;
import java.util.Map;

import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Rang;

public interface OnSviKvizoviTaskCompleted {
    void onSviKvizoviTaskCompleted(ArrayList<Kviz> kvizovi, Map<String, ArrayList<Rang>> mapaRangova);
}
