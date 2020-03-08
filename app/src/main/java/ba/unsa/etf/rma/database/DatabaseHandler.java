package ba.unsa.etf.rma.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.Rang;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static int id = 0;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "baza";

    //tabele
    private static final String TABLE_KVIZ = "kvizovi";
    private static final String TABLE_KATEGORIJA = "kategorije";
    private static final String TABLE_PITANJE = "pitanja";
    private static final String TABLE_ODGOVOR = "odgovori";
    private static final String TABLE_KVIZ_PITANJE = "kvizPitanje";
    private static final String TABLE_RANG = "rangListe";

    //tabela_kviz
    private static final String KEY_NAZIV_KVIZA = "nazivKviza";
    private static final String  ID_KATEGORIJE = "idKategorije";

    //tabela_pitanje
    private  static  final  String KEY_NAZIV_PITANJA = "nazivPitanja";
    private  static  final  String TEKST_PITANJA = "tekstPitanja";
    private  static  final  String TACAN_ODGOVOR = "tacanOdgovori";

    //tabela_kategorja
    private static final String KEY_NAZIV_KATEGORIJE = "nazivKategorije";
    private static final String  ID_IKONICE = "idIkonice";

    //tabela_odgovor
    private static final String KEY_ID_ODGOVOR = "idOdgovora";
    private static final String ODGOVOR = "odgovor";
    private static final String  FKEY_NAZIV_PITANJA = "nazivPitanja";

    //tabela_kvizPitanje
    private static final String KEY_ID_KVIZPITANJE = "id";
    private static final String  FKEY_KVIZ = "nazivKviza";
    private static final String FKEY_PITANJE = "nazivPitanja";

    //tabela rang
    private static final String KEY_ID_RANG = "id";
    private static  final  String NAZIV_IGRACA = "nazivIgraca";
    private static final String OSVOJENI_BODOVI = "osvojeniBodovi";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {



        String CREATE_KVIZ_TABLE = "CREATE TABLE " + TABLE_KVIZ + "("
                + KEY_NAZIV_KVIZA + " TEXT PRIMARY KEY,"
                + ID_KATEGORIJE + " TEXT" + ")";
        db.execSQL(CREATE_KVIZ_TABLE);

        String CREATE_PITENJE_TABLE = "CREATE TABLE " + TABLE_PITANJE + "("
                + KEY_NAZIV_PITANJA + " TEXT PRIMARY KEY,"
                + TEKST_PITANJA + " TEXT,"
                + TACAN_ODGOVOR + " TEXT" + ")";
        db.execSQL(CREATE_PITENJE_TABLE);

        String CREATE_KATEGORIJA_TABLE = "CREATE TABLE " + TABLE_KATEGORIJA + "("
                + KEY_NAZIV_KATEGORIJE + " TEXT PRIMARY KEY,"
                + ID_IKONICE + " TEXT" + ")";
        db.execSQL(CREATE_KATEGORIJA_TABLE);

        String CREATE_ODGOVOR_TABLE = "CREATE TABLE " + TABLE_ODGOVOR + "("
                + KEY_ID_ODGOVOR + " INTEGER PRIMARY KEY,"
                + ODGOVOR + " TEXT,"
                + FKEY_NAZIV_PITANJA + " TEXT" + ")";
        db.execSQL(CREATE_ODGOVOR_TABLE);

        String CREATE_KVIZ_PITENJE_TABLE = "CREATE TABLE " + TABLE_KVIZ_PITANJE + "("
                + KEY_ID_KVIZPITANJE + " INTEGER PRIMARY KEY,"
                + FKEY_KVIZ + " TEXT,"
                + FKEY_PITANJE + " TEXT" + ")";
        db.execSQL(CREATE_KVIZ_PITENJE_TABLE);


        String CREATE_RANG_TABLE = "CREATE TABLE " + TABLE_RANG + "("
                + KEY_ID_RANG + " INTEGER PRIMARY KEY,"
                + FKEY_KVIZ + " TEXT,"
                + NAZIV_IGRACA + " TEXT,"
                + OSVOJENI_BODOVI + " REAL" + ")";
        db.execSQL(CREATE_RANG_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KVIZ);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PITANJE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KATEGORIJA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ODGOVOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KVIZ_PITANJE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RANG);

        // Create tables again
        onCreate(db);
    }

    //TODO: Dodavanje u bazu

    public void addKviz(Kviz kviz) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAZIV_KVIZA, kviz.getNaziv());
        values.put(ID_KATEGORIJE, kviz.getKategorija().getNaziv());

        // Inserting Row
        if(!daLiPostojiKviz(kviz)){
            addKategorija(kviz.getKategorija());
            for(int i=0; i<kviz.getPitanja().size(); i++){
                addPitanje(kviz.getPitanja().get(i));
            }
            stvoriNovuVezuSPitanjima(kviz);

        db.insert(TABLE_KVIZ, null, values);}

    }

    public void addKategorija(Kategorija kategorija) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAZIV_KATEGORIJE, kategorija.getNaziv());
        values.put(ID_IKONICE, kategorija.getId());

        // Dodajemo novu kategoriju samo ako ne postoji vec u bazi
        if(!daLiPostojiKategorija(kategorija)) {
            db.insert(TABLE_KATEGORIJA, null, values);
        }

    }

    public void addPitanje(Pitanje pitanje){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAZIV_PITANJA, pitanje.getNaziv());
        values.put(TEKST_PITANJA, pitanje.getTekstPitanja());
        values.put(TACAN_ODGOVOR, pitanje.getTacan());

        // Inserting Row
        if(!daLiPostojiPitanje(pitanje)){
            for(int i=0; i<pitanje.getOdgovori().size(); i++){
                addOdgovor(pitanje.getOdgovori().get(i), pitanje.getNaziv());
            }
            db.insert(TABLE_PITANJE, null, values);
        }


    }

    public void addOdgovor (String odgovor, String nazivPitanja){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_ODGOVOR;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Integer brojRedova = cursor.getCount();
        brojRedova++;

        ContentValues values = new ContentValues();
        values.put(KEY_ID_ODGOVOR, brojRedova);
        values.put(ODGOVOR, odgovor);
        values.put(FKEY_NAZIV_PITANJA, nazivPitanja);

        // Inserting Row
        db.insert(TABLE_ODGOVOR, null, values);
        //db.close(); // Closing database connection
    }

    public void addKvizPitanje (String nazivKviza, String nazivPitanja){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_KVIZ_PITANJE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Integer brojRedova = cursor.getCount();
        brojRedova++;

        ContentValues values = new ContentValues();
        values.put(KEY_ID_KVIZPITANJE, brojRedova);
        values.put(FKEY_KVIZ, nazivKviza);
        values.put(FKEY_PITANJE, nazivPitanja);

        // Inserting Row
        db.insert(TABLE_KVIZ_PITANJE, null, values);
    }

    public void addRang (Rang rang , String nazivKviza){

        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_RANG;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Integer brojRedova = cursor.getCount();
        brojRedova++;

        ContentValues values = new ContentValues();
        values.put(KEY_ID_RANG, brojRedova);
        values.put(FKEY_KVIZ, nazivKviza);
        values.put(NAZIV_IGRACA, rang.getImePrezimeIgraca());
        values.put(OSVOJENI_BODOVI, rang.getRezulata());

        // Inserting Row
        db.insert(TABLE_RANG, null, values);
    }

    //TODO: UPDATE BAZE

    public int updateKviz(Kviz straiKviz, Kviz noviKviz) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAZIV_KVIZA, noviKviz.getNaziv());
        values.put(ID_KATEGORIJE, noviKviz.getKategorija().getNaziv());

        obrisiStaruVezuSPitanjima(straiKviz);

        stvoriNovuVezuSPitanjima(noviKviz);

        dodajPitanjaiOdgovore(noviKviz);


        // updating row
        return db.update(TABLE_KVIZ, values, KEY_NAZIV_KVIZA + " = ?",
                new String[] { straiKviz.getNaziv() });

    }


    public void obrisiStaruVezuSPitanjima (Kviz stariKviz){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_KVIZ_PITANJE, FKEY_KVIZ + " = ?",
                new String[] { stariKviz.getNaziv() });
        //db.close();

    }

    public  void stvoriNovuVezuSPitanjima(Kviz noviKviz){
        SQLiteDatabase db = this.getWritableDatabase();

        for (int i=0; i<noviKviz.getPitanja().size(); i++) {

            String selectQuery = "SELECT  * FROM " + TABLE_KVIZ_PITANJE;
            Cursor cursor = db.rawQuery(selectQuery, null);
            Integer brojRedova = cursor.getCount();
            brojRedova++;

            id += 3;

            ContentValues values = new ContentValues();
            values.put(KEY_ID_KVIZPITANJE, id);
            values.put(FKEY_KVIZ, noviKviz.getNaziv());
            values.put(FKEY_PITANJE, noviKviz.getPitanja().get(i).getNaziv());

            // Inserting Row
            db.insert(TABLE_KVIZ_PITANJE, null, values);
        }
        // db.close(); // Closing database connection

    }

    public void dodajPitanjaiOdgovore(Kviz noviKviz){

        SQLiteDatabase db = this.getWritableDatabase();

        for(int i=0; i<noviKviz.getPitanja().size(); i++) {

            addPitanje(noviKviz.getPitanja().get(i));

        }

    }


    public ArrayList<Kviz> dohvatiSveKvizove(){
        ArrayList<Kviz> kvizovi = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_KVIZ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Kviz kviz = new Kviz();
                kviz.setNaziv(cursor.getString(0));

                Kategorija kategorija = pronadjiKategoriju(cursor.getString(1));
                kviz.setKategorija(kategorija);

                ArrayList<Pitanje> pitanja = pronadjiPitanja(cursor.getString(0));
                kviz.setPitanja(pitanja);
                // Adding contact to list
                kvizovi.add(kviz);
            } while (cursor.moveToNext());
        }

        return kvizovi;
    }

    public ArrayList<Kviz> dohvatiOdabraneKvizove(Kategorija kat){
        ArrayList<Kviz> kvizovi = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_KVIZ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Kviz kviz = new Kviz();
                kviz.setNaziv(cursor.getString(0));

                Kategorija kategorija = pronadjiKategoriju(cursor.getString(1));
                kviz.setKategorija(kategorija);

                ArrayList<Pitanje> pitanja = pronadjiPitanja(cursor.getString(0));
                kviz.setPitanja(pitanja);
                // Adding contact to list
                if(cursor.getString(1).equals(kat.getNaziv()) || kat.getNaziv().equals("Svi") )
                kvizovi.add(kviz);
            } while (cursor.moveToNext());
        }

        return kvizovi;
    }

    public ArrayList<Pitanje> pronadjiPitanja(String nazivKviza){
        ArrayList<Pitanje> pitanja = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_KVIZ_PITANJE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
              if(cursor.getString(1).equals(nazivKviza)){
                  Pitanje pitanje = new Pitanje();
                  pitanje = pronadjiJednoPitanje(cursor.getString(2));
                  pitanja.add(pitanje);
              }
            } while (cursor.moveToNext());
        }

        return pitanja;
    }

    public Pitanje pronadjiJednoPitanje(String nazivPitanja){
        Pitanje pitanje = new Pitanje();
        String selectQuery = "SELECT  * FROM " + TABLE_PITANJE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getString(0).equals(nazivPitanja)){
                    pitanje.setNaziv(cursor.getString(0));
                    pitanje.setTekstPitanja(cursor.getString(1));
                    pitanje.setTacan(cursor.getString(2));
                    ArrayList<String> odgovori = pronadjiOdgovore(nazivPitanja);
                    pitanje.setOdgovori(odgovori);
                }

            } while (cursor.moveToNext());
        }
        return pitanje;
    }

    public ArrayList<String> pronadjiOdgovore(String nazivPitanja){
        ArrayList<String> odgovori = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_ODGOVOR;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                if(cursor.getString(2).equals(nazivPitanja)){
                    odgovori.add(cursor.getString(1));
                }
            } while (cursor.moveToNext());
        }
        return odgovori;
    }

    public Kategorija pronadjiKategoriju(String nazivKategorije){

        Kategorija kategorija = new Kategorija();

        String selectQuery = "SELECT  * FROM " + TABLE_KATEGORIJA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                 if(cursor.getString(0).equals(nazivKategorije)){
                kategorija.setNaziv(cursor.getString(0));
                kategorija.setId(cursor.getString(1));}

            } while (cursor.moveToNext());
        }

        return kategorija;
    }

    public ArrayList<Kategorija> dohvatiSveKategorije(){
        ArrayList<Kategorija> kategorije = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_KATEGORIJA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Kategorija kategorija = new Kategorija();
                kategorija.setNaziv(cursor.getString(0));
                kategorija.setId(cursor.getString(1));
                kategorije.add(kategorija);
            } while (cursor.moveToNext());
        }

        return kategorije;
    }

    public ArrayList<Pitanje> dohvatiSvaPitanja(){
        ArrayList<Pitanje> pitanja = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_PITANJE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Pitanje pitanje = new Pitanje();
                pitanje.setNaziv(cursor.getString(0));
                pitanje.setTekstPitanja(cursor.getString(1));
                pitanje.setTacan(cursor.getString(2));
                ArrayList<String> odgovori = pronadjiOdgovore(cursor.getString(0));
                pitanje.setOdgovori(odgovori);

                pitanja.add(pitanje);
            } while (cursor.moveToNext());
        }

        return pitanja;
    }

    public ArrayList<Rang> dohvatiRangListuZaKviz(String kviz){
        ArrayList<Rang> rangLista = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_RANG;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
               if(cursor.getString(1).equals(kviz)){
                   Rang clan = new Rang();
                   clan.setImePrezimeIgraca(cursor.getString(2));
                   clan.setRezulata(cursor.getDouble(3));

                   rangLista.add(clan);
               }
            } while (cursor.moveToNext());
        }
        return rangLista;
    }

    public  Map<String, ArrayList<Rang>> dohvatiRangove(){
        Map<String, ArrayList<Rang>> rangovi = new HashMap<>();

        String selectQuery = "SELECT  * FROM " + TABLE_KVIZ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
             ArrayList<Rang> rangs = new ArrayList<>();
             rangs = dohvatiRangListuZaKviz(cursor.getString(0));
             if(rangs.size()!=0)
             rangovi.put(cursor.getString(0), rangs);
            } while (cursor.moveToNext());
        }

        return  rangovi;
    }

//TODO: Provjera trenutnog stanja u bazi

    public boolean daLiPostojiKviz(Kviz kviz){

        String selectQuery = "SELECT  * FROM " + TABLE_KVIZ;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getString(0).equals(kviz.getNaziv())) return true;
            } while (cursor.moveToNext());
        }

        return false;
    }

    public boolean daLiPostojiKategorija(Kategorija kategorija){

        String selectQuery = "SELECT  * FROM " + TABLE_KATEGORIJA;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getString(0).equals(kategorija.getNaziv())) return true;
            } while (cursor.moveToNext());
        }

        return false;
    }

    public boolean daLiPostojiPitanje(Pitanje pitanje){

        String selectQuery = "SELECT  * FROM " + TABLE_PITANJE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                if(cursor.getString(0).equals(pitanje.getNaziv())) return true;
            } while (cursor.moveToNext());
        }

        return false;
    }



//TODO: Brisanje trenutnog stanja u bazi

    public void obrisiSveIzBaze(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_KVIZ, null, null);
        db.delete(TABLE_KATEGORIJA, null, null);
        db.delete(TABLE_PITANJE, null, null);
        db.delete(TABLE_KVIZ_PITANJE, null, null);
        db.delete(TABLE_ODGOVOR, null, null);
        db.delete(TABLE_RANG, null, null);

        db.close();
    }

    public  void obrisiRangListu(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_RANG, null, null);

        db.close();
    }

}
