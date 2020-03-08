package ba.unsa.etf.rma;

import java.util.ArrayList;
import ba.unsa.etf.rma.klase.Kviz;

public interface OnKvizoviTaskCompleted {
    void onKvizoviTaskCompleted(ArrayList<Kviz> kvizovi);
}
