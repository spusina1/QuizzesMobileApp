package ba.unsa.etf.rma.klase;

import java.io.Serializable;

public class Rang implements Serializable {

    private String imePrezimeIgraca;
    private double rezulata;

    public Rang() {
    }

    public Rang(String imePrezimeIgraca, double rezulata) {
        this.imePrezimeIgraca = imePrezimeIgraca;
        this.rezulata = rezulata;
    }

    public String getImePrezimeIgraca() {
        return imePrezimeIgraca;
    }

    public void setImePrezimeIgraca(String imePrezimeIgraca) {
        this.imePrezimeIgraca = imePrezimeIgraca;
    }

    public double getRezulata() {
        return rezulata;
    }

    public void setRezulata(double rezulata) {
        this.rezulata = rezulata;
    }
}
