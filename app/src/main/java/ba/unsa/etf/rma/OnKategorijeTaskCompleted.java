package ba.unsa.etf.rma;

import java.util.ArrayList;
import ba.unsa.etf.rma.klase.Kategorija;

public interface OnKategorijeTaskCompleted {
    void onKategorijeTaskCompleted(ArrayList<Kategorija> kategorije);
}
