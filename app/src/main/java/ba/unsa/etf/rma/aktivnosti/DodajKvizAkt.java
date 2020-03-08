package ba.unsa.etf.rma.aktivnosti;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ba.unsa.etf.rma.OnKvizoviTaskCompleted;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.MySpinnerAdapter;
import ba.unsa.etf.rma.adapteri.PitanjaAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajKvizAkt extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnKvizoviTaskCompleted {

    private static Kviz dodjKviz = new Kviz("Dodaj Kviz", null, new Kategorija("Svi", "671"));
    ProgressDialog progressDialog;
    //widgeti
    private Spinner kategorija;
    private EditText tekst;
    private ListView dodanaPitanja;
    private ListView mogucaPitanja;
    private Button dodaj;
    private Button importuj;
    //kolekcije
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Pitanje> dodanaP = new ArrayList<>();
    private ArrayList<Pitanje> mogucaP = new ArrayList<>();
    private ArrayList<Kviz> sviKvizovi = new ArrayList<>();

    //adapteri
    private PitanjaAdapter adapterZaDodana;
    private PitanjaAdapter adapterZaMoguca;
    private MySpinnerAdapter adapterSpiner;

    //Vrijednosti koje vracam
    private Kviz kviz;
    private String nazivKviza;
    private Kategorija kategorijaKviza;

    //Za otvaranje novih aktivnosti
    private Pitanje dodajPitanje = new Pitanje("Dodaj pitanje", null, null, null);
    private Kategorija dodajKategoriju = new Kategorija("Dodaj kategoriju", "DK");
    private Kategorija svi = new Kategorija("Svi", "671");


    private void dodajPoPravilu(Pitanje pitanje) {
        if (dodanaP.isEmpty()) dodanaP.add(pitanje);
        else dodanaP.set(dodanaP.size() - 1, pitanje);
        if (!dodanaP.contains(dodajPitanje))
            dodanaP.add(dodajPitanje);
    }

    public boolean isInternetOn() {
        ConnectivityManager mgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();

        if (netInfo != null) {
            if (netInfo.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }


    private void dodajPoPraviluKategoriju(Kategorija kategorija) {
        //komentar
        kategorije.set(kategorije.size() - 1, kategorija);
        if (!kategorije.contains(dodajKategoriju)) {
            kategorije.add(dodajKategoriju);
        }
    }

    @Override
    public void onKvizoviTaskCompleted(ArrayList<Kviz> kvizovi) {

        for (int i = 0; i < kvizovi.size(); i++) {
            sviKvizovi.add(kvizovi.get(i));
        }
        nazivKviza = tekst.getText().toString();

        nazivKviza = nazivKviza.replaceAll("\\s", "");

        kategorijaKviza = (Kategorija) kategorija.getSelectedItem();
        dodanaP.remove(dodajPitanje);
        Kviz novi = new Kviz(nazivKviza, dodanaP, kategorijaKviza);

        progressDialog.dismiss();

        if (!sviKvizovi.contains(novi) || kviz.equals(novi)) {
            Intent intent = new Intent();
            intent.putExtra("MESSAGE", (Serializable) novi);
            setResult(RESULT_OK, intent);
            finish();//finishing activity

        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Greška")
                    .setMessage("Uneseni kviz već postoji!")
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        kategorija = (Spinner) findViewById(R.id.spKategorije);
        tekst = (EditText) findViewById(R.id.etNaziv);
        dodanaPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        mogucaPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dodaj = (Button) findViewById(R.id.btnDodajKviz);
        importuj = (Button) findViewById(R.id.btnImportKviz);

        kviz = (Kviz) getIntent().getSerializableExtra("kviz");
        kategorije = (ArrayList<Kategorija>) getIntent().getSerializableExtra("kategorije");
        mogucaP = (ArrayList<Pitanje>) getIntent().getSerializableExtra("moguca");


        adapterSpiner = new MySpinnerAdapter(this, R.layout.spinner, kategorije);
        adapterSpiner.setDropDownViewResource(R.layout.spinner_dropdown);
        kategorija.setAdapter(adapterSpiner);

        if (!kategorije.contains(dodajKategoriju)) {
            kategorije.add(dodajKategoriju);
        }


        kategorija.setOnItemSelectedListener(this);

        Kategorija trenutna = (Kategorija) getIntent().getSerializableExtra("trenutnaKategorija");
        for (int i = 0; i < adapterSpiner.getCount(); i++) {
            if (trenutna.getNaziv().equals(adapterSpiner.getItem(i).getNaziv())) {
                kategorija.setSelection(i);
                adapterSpiner.notifyDataSetChanged();
                break;
            }
        }

        if (!kviz.getNaziv().equals("Dodaj Kviz")) {

            tekst.setText(kviz.getNaziv());

            String compareValue = kviz.getKategorija().getNaziv();
            if (compareValue != null) {
                for (int i = 0; i < adapterSpiner.getCount(); i++) {
                    if (compareValue.equals(adapterSpiner.getItem(i).getNaziv())) {
                        kategorija.setSelection(i);
                        adapterSpiner.notifyDataSetChanged();
                        break;
                    }
                }
            }

            dodanaP = kviz.getPitanja();
        }

        if (!dodanaP.contains(dodajPitanje)) dodanaP.add(dodajPitanje);

        adapterZaDodana = new PitanjaAdapter(this, dodanaP, 0);
        dodanaPitanja.setAdapter(adapterZaDodana);

        adapterZaMoguca = new PitanjaAdapter(this, mogucaP, 1);
        mogucaPitanja.setAdapter(adapterZaMoguca);

        tekst.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if (!tekst.getText().toString().equals("")) {
                    tekst.setBackgroundColor(Color.WHITE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        dodanaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position2, long id) {

                if (dodanaP.get(position2).equals(dodajPitanje)) {
                    Intent otvoriNovi = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                    startActivityForResult(otvoriNovi, 2);
                }

                Pitanje pitanje = dodanaP.get(position2);
                if (!pitanje.getNaziv().equals("Dodaj pitanje")) {
                    dodanaP.remove(position2);
                    mogucaP.add(pitanje);
                }
                adapterZaDodana.notifyDataSetChanged();
                adapterZaMoguca.notifyDataSetChanged();

            }
        });

        mogucaPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position2, long id) {


                Pitanje pitanje = mogucaP.get(position2);
                mogucaP.remove(position2);
                dodajPoPravilu(pitanje);
                //dodanaP.add(pitanje);
                adapterZaDodana.notifyDataSetChanged();
                adapterZaMoguca.notifyDataSetChanged();

            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        dodaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isInternetOn()) {
                    if (validirajPolja()) {

                        progressDialog.show();
                        new KreirajDokumentTask((OnKvizoviTaskCompleted) DodajKvizAkt.this).execute("ucitajSveKvizove");
                    }
                }

                else {
                    new AlertDialog.Builder(DodajKvizAkt.this)
                            .setTitle("Greška")
                            .setMessage("Nema konekcije, ne može se dodati kviz!")
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            }
        });

        importuj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (kviz.equals(dodjKviz)) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    sendIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    sendIntent.setType("text/plain");
                    // Provjera da li postoji aplikacija koja može obaviti navedenu akciju
                    if (sendIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(sendIntent, 5);
                    }
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, final int position, long id) {


        if (kategorije.get(position).equals(dodajKategoriju)) {
            Intent otvoriNovi = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
            startActivityForResult(otvoriNovi, 3);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

                Pitanje pitanje = (Pitanje) data.getSerializableExtra("MESSAGE");
                //vratili pitanje iz prosle aktivnosti i sad ga treba dodati u listu
                if (!dodanaP.contains(pitanje))
                    dodajPoPravilu(pitanje);
            }
            adapterZaDodana.notifyDataSetChanged();
        } else if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                Kategorija kat = (Kategorija) data.getSerializableExtra("MESSAGE");
                if (!kategorije.contains(kat)) {
                    dodajPoPraviluKategoriju(kat);
                }
                kategorija.setSelection(kategorije.indexOf(kat));
                adapterSpiner.notifyDataSetChanged();
            } else {
                kategorija.setSelection(kategorije.size() - 2);
            }
        } else if (requestCode == 5 && resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    readTextFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            kategorijaKviza = (Kategorija) kategorija.getSelectedItem();
            Kviz novi = new Kviz("", dodanaP, kategorijaKviza);
            Intent intent = new Intent();
            intent.putExtra("MESSAGE", (Serializable) novi);

            setResult(RESULT_CANCELED, intent);
            finish();//finishing activity
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean validirajPolja() {
        boolean tacno = true;
        if (tekst.getText().toString().equals("")) {
            tekst.setBackgroundColor(Color.parseColor("#FFCDD2"));
            tacno = false;
        } else
            tekst.setBackgroundColor(Color.WHITE);

        return tacno;
    }

    private void readTextFromUri(Uri uri) throws IOException {

        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String string = "";
        String line;
        ArrayList<String> redovi = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            redovi.add(line);
        }
        inputStream.close();
        reader.close();

        boolean kontrola = true;

        Kviz imortovaniKviz;
        String kviz = redovi.get(0);
        String[] dioKviza = kviz.split(",", 3);
        String nazivImportovanog = dioKviza[0];
        String kategImportovanog = dioKviza[1];
        int brojPitanja = Integer.parseInt(dioKviza[2].trim());

        for (int i = 0; i < sviKvizovi.size(); i++) {
            //alter za naziv
            if (sviKvizovi.get(i).getNaziv().equals(nazivImportovanog) && kontrola) {
                new AlertDialog.Builder(this)
                        .setTitle("Greška")
                        .setMessage("Kviz kojeg importujete već postoji!")
                        .setPositiveButton(android.R.string.yes, null)
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                kontrola = false;
            }
        }

        //alter za broj pitanja
        if (brojPitanja != redovi.size() - 1 && kontrola) {
            new AlertDialog.Builder(this)
                    .setTitle("Greška")
                    .setMessage("Kviz kojeg imporujete ima neispravan broj pitanja!")
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            kontrola = false;
        }

        ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();

        for (int i = 1; i < redovi.size(); i++) {
            String pitanje = redovi.get(i);
            String[] dioPitanja = pitanje.split(",");
            String nazivPitanja = dioPitanja[0];

            if (dioPitanja.length <= 3 && kontrola) {
                new AlertDialog.Builder(this)
                        .setTitle("Greška")
                        .setMessage("Datoteka kviza kojeg importujete nema ispravan format!")
                        .setPositiveButton(android.R.string.yes, null)
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                kontrola = false;
            } else {

                int brojOdgovora = Integer.parseInt(dioPitanja[1].trim());
                int tacanOdgovor = Integer.parseInt(dioPitanja[2].trim());

                //provjera za broj odgovora

                if (dioPitanja.length - 3 != brojOdgovora && kontrola) {
                    //  Toast.makeText(this, String.valueOf(dioPitanja.length), Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(this)
                            .setTitle("Greška")
                            .setMessage("Kviz kojeg importujete ima neispravan broj odgovora!")
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                    kontrola = false;
                }

                if (tacanOdgovor < 0 || tacanOdgovor > brojOdgovora && kontrola) {
                    //  Toast.makeText(this, String.valueOf(dioPitanja.length), Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(this)
                            .setTitle("Greška")
                            .setMessage("Kviz kojeg importujete ima neispravan index tačnog odgovora!")
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();

                    kontrola = false;
                }

                ArrayList<String> odgovori = new ArrayList<>();
                for (int j = 3; j < dioPitanja.length; j++) {
                    odgovori.add(dioPitanja[j]);
                }

                Pitanje novoPitanje;
                if (kontrola) {
                    novoPitanje = new Pitanje(nazivPitanja, nazivPitanja, odgovori, odgovori.get(tacanOdgovor));
                    for (int j = 0; j < pitanjaKviza.size(); j++) {
                        if (pitanjaKviza.get(j).getNaziv().equals(novoPitanje.getNaziv()) && kontrola) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Greška")
                                    .setMessage("Kviz nije ispravan postoje dva pitanja sa istim nazivom!")
                                    .setPositiveButton(android.R.string.yes, null)
                                    .setNegativeButton(android.R.string.no, null)
                                    .show();

                            kontrola = false;
                        }
                    }
                    pitanjaKviza.add(novoPitanje);
                }
            }
        }

        if (brojPitanja != pitanjaKviza.size() && kontrola) {
            new AlertDialog.Builder(this)
                    .setTitle("Greška")
                    .setMessage("Datoteka kviza kojeg importujete nema ispravan format!")
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            kontrola = false;
        }

        //ispravan kviz

        if (kontrola) {

            imortovaniKviz = new Kviz(nazivImportovanog, pitanjaKviza, new Kategorija(kategImportovanog, "1027"));
            if (!kategorije.contains(imortovaniKviz.getKategorija())) {
                dodajPoPraviluKategoriju(imortovaniKviz.getKategorija());
                adapterSpiner.notifyDataSetChanged();
            }
            for (int i = 0; i < adapterSpiner.getCount(); i++) {
                if (kategorije.get(i).equals(imortovaniKviz.getKategorija())) {
                    kategorija.setSelection(i);
                    adapterSpiner.notifyDataSetChanged();
                    break;
                }
            }

            for (int i = 0; i < pitanjaKviza.size(); i++) {
                dodajPoPravilu(pitanjaKviza.get(i));
            }
            adapterZaDodana.notifyDataSetChanged();

            tekst.setText(imortovaniKviz.getNaziv());
        }

    }

    public class KreirajDokumentTask extends AsyncTask<Object, Integer, Void> {

        public String TOKEN = "";
        private ArrayList<Kviz> kvizovi = new ArrayList<>();
        private OnKvizoviTaskCompleted listener;

        public KreirajDokumentTask(OnKvizoviTaskCompleted listener) {
            this.listener = listener;

        }

        public KreirajDokumentTask() {
        }


        @Override
        protected Void doInBackground(Object... objects) {

            GoogleCredential credentials = null;

            try {

                InputStream is = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                TOKEN = credentials.getAccessToken();

                String akcija = (String) objects[0];

                if (akcija.equals("ucitajSveKvizove")) {
                    kvizovi = ucitajKvizove();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            if (listener != null) listener.onKvizoviTaskCompleted(kvizovi);
        }

        private ArrayList<Kviz> ucitajKvizove() {
            ArrayList<Kviz> novaLista = new ArrayList<>();

            String nazivKviza = "";
            Kategorija kat = new Kategorija();

            try {
                String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/Kvizovi?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "aplication/json");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String rezulat = convertStreamToString(in);

                JSONObject jo = new JSONObject(rezulat);
                JSONArray documents = jo.getJSONArray("documents");

                //treba proci kroz listu svih dokumenata

                for (int i = 0; i < documents.length(); i++) {
                    JSONObject kvizovi = documents.getJSONObject(i);
                    JSONObject fields = kvizovi.getJSONObject("fields");
                    JSONObject naziv = fields.getJSONObject("naziv");
                    nazivKviza = naziv.getString("stringValue");
                    JSONObject idKat = fields.getJSONObject("idKategorije");
                    String idKategorije = idKat.getString("stringValue");

                    JSONObject pitanja = fields.getJSONObject("pitanja");
                    JSONObject arrayValue = pitanja.getJSONObject("arrayValue");

                    ArrayList<Pitanje> listaPitanja = new ArrayList<>();

                    if (arrayValue.has("values")) {
                        JSONArray values = arrayValue.getJSONArray("values");

                        ArrayList<String> pitanjaKviza = new ArrayList<>();

                        for (int j = 0; j < values.length(); j++) {
                            JSONObject pitanje = values.getJSONObject(j);
                            String p = pitanje.getString("stringValue");
                            if (!p.equals(""))
                                pitanjaKviza.add(p);
                        }

                        for (int k = 0; k < pitanjaKviza.size(); k++) {

                            listaPitanja.add(pronadjiPitanje(pitanjaKviza.get(k)));
                        }
                    }

                    kat = pronadjiKategoriju(idKategorije);
                    Kviz novi = new Kviz(nazivKviza, listaPitanja, kat);

                    novaLista.add(novi);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {

                e.printStackTrace();
                Kviz novi = new Kviz(nazivKviza, new ArrayList<Pitanje>(), kat);
                novaLista.add(novi);
            }
            return novaLista;
        }

        private Kategorija pronadjiKategoriju(String id) {
            Kategorija kategorija = new Kategorija();

            try {
                String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/Kategorije?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "aplication/json");
                System.out.println(conn.getResponseMessage());
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String rezulat = convertStreamToString(in);

                JSONObject jo = new JSONObject(rezulat);
                JSONArray documents = jo.getJSONArray("documents");

                for (int i = 0; i < documents.length(); i++) {
                    JSONObject kategorije = documents.getJSONObject(i);
                    JSONObject fields = kategorije.getJSONObject("fields");
                    JSONObject naziv = fields.getJSONObject("naziv");
                    String nazivKategorije = naziv.getString("stringValue");
                    JSONObject idIkonice = fields.getJSONObject("idIkonice");
                    String idIkoniceKategorije = String.valueOf(idIkonice.getInt("integerValue"));

                    if (idIkoniceKategorije.equals(id)) {
                        kategorija.setNaziv(nazivKategorije);
                        kategorija.setId(id);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return kategorija;
        }

        private Pitanje pronadjiPitanje(String nazivPitanja) {
            Pitanje pitanje = new Pitanje();
            try {
                String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/Pitanja?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "aplication/json");
                System.out.println(conn.getResponseMessage());
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String rezulat = convertStreamToString(in);

                JSONObject jo = new JSONObject(rezulat);
                JSONArray documents = jo.getJSONArray("documents");

                for (int i = 0; i < documents.length(); i++) {
                    JSONObject pitanja = documents.getJSONObject(i);
                    JSONObject fields = pitanja.getJSONObject("fields");
                    JSONObject naziv = fields.getJSONObject("naziv");
                    String nazivP = naziv.getString("stringValue");
                    JSONObject index = fields.getJSONObject("indexTacnog");
                    int indexTacnog = index.getInt("integerValue");

                    JSONObject odgovori = fields.getJSONObject("odgovori");
                    JSONObject arrayValue = odgovori.getJSONObject("arrayValue");

                    ArrayList<String> odgovoriNaPitanje = new ArrayList<>();

                    if (arrayValue.has("values")) {

                        JSONArray values = arrayValue.getJSONArray("values");

                        for (int j = 0; j < values.length(); j++) {
                            JSONObject odgovor = values.getJSONObject(j);
                            String o = odgovor.getString("stringValue");

                            odgovoriNaPitanje.add(o);
                        }
                    }

                    if (nazivP.equals(nazivPitanja)) {
                        pitanje.setNaziv(nazivP);
                        pitanje.setTekstPitanja(nazivP);
                        pitanje.setOdgovori(odgovoriNaPitanje);
                        pitanje.setTacan(odgovoriNaPitanje.get(indexTacnog));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return pitanje;
        }

        public String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }

    }


}





