package ba.unsa.etf.rma;

import java.util.ArrayList;
import ba.unsa.etf.rma.klase.Pitanje;

public interface OnTaskCompleted {
    void onTaskCompleted(ArrayList<Pitanje> pitanjes);

}
