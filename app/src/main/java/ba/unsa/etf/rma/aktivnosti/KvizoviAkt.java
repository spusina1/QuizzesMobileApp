package ba.unsa.etf.rma.aktivnosti;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ba.unsa.etf.rma.NetworkStateReceiver;
import ba.unsa.etf.rma.OnKategorijeTaskCompleted;
import ba.unsa.etf.rma.OnKvizoviTaskCompleted;
import ba.unsa.etf.rma.OnSviKvizoviTaskCompleted;
import ba.unsa.etf.rma.OnTaskCompleted;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.MyListAdapter;
import ba.unsa.etf.rma.adapteri.MySpinnerAdapter;
import ba.unsa.etf.rma.database.DatabaseHandler;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.Rang;


public class KvizoviAkt extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener, AdapterView.OnItemSelectedListener, ListaFrag.OnItemClick, OnKvizoviTaskCompleted, OnKategorijeTaskCompleted, OnSviKvizoviTaskCompleted {

    //liste
    private static ArrayList<Kategorija> kategorije = new ArrayList<>();
    private static ArrayList<Kviz> kvizovi = new ArrayList<>();
    private static ArrayList<Kviz> listaOdabranihKvizova = new ArrayList<Kviz>();
    private static ArrayList<Pitanje> pitanja = new ArrayList<>();
    private static ArrayList<Pitanje> odabranaPitanja = new ArrayList<>();

    private static Kviz dodjKviz = new Kviz("Dodaj Kviz", new ArrayList<Pitanje>(), new Kategorija("Svi", "671"));
    private static Kategorija svi = new Kategorija("Svi", "671");
    private static Kviz odabraniKviz;
    private static Kategorija trenutnaKategorija;
    ProgressDialog progressDialog;

    private Map<String, ArrayList<Rang>> mojaMapa = new HashMap<>();

    private NetworkStateReceiver networkStateReceiver;

    //widgeti
    private Spinner spiner;
    private ListView listaKvizova;

    //adapteri
    private MyListAdapter adapterListeKvizova;
    private MySpinnerAdapter adapterSpinner;

    private void dodajPoPraviluKviz(Kviz kviz) {
        if (listaOdabranihKvizova.isEmpty()) listaOdabranihKvizova.add(kviz);
        else listaOdabranihKvizova.set(listaOdabranihKvizova.size() - 1, kviz);
        if (!listaOdabranihKvizova.contains(dodjKviz))
            listaOdabranihKvizova.add(dodjKviz);
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

    @Override
    public void onKategorijeTaskCompleted(ArrayList<Kategorija> kat) {

        kategorije.clear();

        for (int i = 0; i < kat.size(); i++) {
            kategorije.add(kat.get(i));
        }

        if (!kategorije.contains(svi))
            kategorije.add(svi);

        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            ListaFrag listFragment = new ListaFrag();
            Bundle argument = new Bundle();
            argument.putSerializable("kategorije", kategorije);
            listFragment.setArguments(argument);
            getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, listFragment).commit();

        } else {
            spiner.setOnItemSelectedListener(this);
            adapterSpinner = new MySpinnerAdapter(this, R.layout.spinner, kategorije);
            adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown);
            spiner.setAdapter(adapterSpinner);
            spiner.setSelection(kategorije.indexOf(trenutnaKategorija));
        }

        //TODO: Ako ima internet konekcije pri kreiranju aktivnosti sve kategorije iz FireDatabase unosim i u lokalnu bazu
        if(isInternetOn()){
            for(int i=0; i<kategorije.size(); i++)
                new DatabaseHandler(this).addKategorija(kategorije.get(i));

        }

    }

    public void nastaviAktivnost(){

    }

    //TODO: Iz FireDatabaze ucitavam kvizove i rangListe u lokalnu bazu
    @Override
    public void onSviKvizoviTaskCompleted(ArrayList<Kviz> kvizovi, Map<String, ArrayList<Rang>> mapa) {
        for(int i=0; i<kvizovi.size(); i++){
            new DatabaseHandler(this).addKviz(kvizovi.get(i));
            //new KreirajDokumentTask().execute("azurirajRangListe", kvizovi.get(i));
        }

        mojaMapa = mapa;
        new RangTask().execute("azurirajRangListe");

    }


    @Override
    public void onKvizoviTaskCompleted(ArrayList<Kviz> kvizovii) {

        listaOdabranihKvizova.clear();

        for (int i = 0; i < kvizovii.size(); i++) {
            dodajPoPraviluKviz(kvizovii.get(i));
        }

        if (!listaOdabranihKvizova.contains(dodjKviz)) listaOdabranihKvizova.add(dodjKviz);
        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Bundle argument1 = new Bundle();
            argument1.putSerializable("lista", listaOdabranihKvizova);
            argument1.putSerializable("kategorije", kategorije);
            DetailFrag detailFrag = new DetailFrag();
            detailFrag.setArguments(argument1);
            getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, detailFrag).commit();

        } else {
            adapterListeKvizova = new MyListAdapter(this, listaOdabranihKvizova);
            listaKvizova.setAdapter(adapterListeKvizova);
        }
        progressDialog.dismiss();
    }

    @Override
    public void networkAvailable() {


          new KategorijeTask((OnKategorijeTaskCompleted) this).execute("ucitajKategorije");
          new KvizoviTask((OnSviKvizoviTaskCompleted) this).execute("ucitajSveKvizove"); //ucitavaju se i rang liste uz kvizove
    }

    @Override
    public void networkUnavailable() {

        kategorije = new DatabaseHandler(this).dohvatiSveKategorije();
        trenutnaKategorija  = svi;


        spiner.setOnItemSelectedListener(this);
        adapterSpinner = new MySpinnerAdapter(this, R.layout.spinner, kategorije);
        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown);
        spiner.setAdapter(adapterSpinner);
        spiner.setSelection(kategorije.indexOf(trenutnaKategorija));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);


        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener((NetworkStateReceiver.NetworkStateReceiverListener) this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        DatabaseHandler db = new DatabaseHandler(this);
        SQLiteDatabase baza = db.getWritableDatabase();



         db.obrisiSveIzBaze();


       if(isInternetOn()) {


        }
        trenutnaKategorija = svi;

        spiner = (Spinner) findViewById(R.id.spPostojeceKategorije);
        listaKvizova = (ListView) findViewById(R.id.lvKvizovi);



        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        if(isInternetOn()) {

            //Prvo ucitati kategorije u spiner
            new KategorijeTask((OnKategorijeTaskCompleted) this).execute("ucitajKategorije");
            new KvizoviTask((OnSviKvizoviTaskCompleted) this).execute("ucitajSveKvizove"); //ucitavaju se i rang liste uz kvizove
        }
        else {

            kategorije = new DatabaseHandler(this).dohvatiSveKategorije();
            trenutnaKategorija  = svi;


            spiner.setOnItemSelectedListener(this);
            adapterSpinner = new MySpinnerAdapter(this, R.layout.spinner, kategorije);
            adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown);
            spiner.setAdapter(adapterSpinner);
            spiner.setSelection(kategorije.indexOf(trenutnaKategorija));

        }

        Configuration config = getResources().getConfiguration();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            new KreirajDokumentTask((OnKvizoviTaskCompleted) this).execute("ucitajKvizove");
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, final int position, long id) {

        progressDialog.show();

//        trenutnaKategorija = kategorije.get(position);
//        new KreirajDokumentTask((OnKvizoviTaskCompleted) this).execute("ucitajKvizove");
        trenutnaKategorija = kategorije.get(position);

        if(isInternetOn()){

            new KreirajDokumentTask((OnKvizoviTaskCompleted) this).execute("ucitajKvizove");
        }

        else{
            Log.d("Trenztna kat", trenutnaKategorija.getNaziv());
            listaOdabranihKvizova.clear();
            listaOdabranihKvizova = new DatabaseHandler(this).dohvatiOdabraneKvizove(trenutnaKategorija);
            listaOdabranihKvizova.add(dodjKviz);
            adapterListeKvizova = new MyListAdapter(this, listaOdabranihKvizova);
            listaKvizova.setAdapter(adapterListeKvizova);
            progressDialog.dismiss();
        }

        listaKvizova.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                odabraniKviz = listaOdabranihKvizova.get(position);
                if (odabraniKviz.getNaziv().equals(dodjKviz.getNaziv())) {

                    if(isInternetOn()) {

                        KreirajDokumentTask asyncTask = new KreirajDokumentTask(new OnTaskCompleted() {

                            @Override
                            public void onTaskCompleted(ArrayList<Pitanje> pitanjes) {
                                Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);

                                intent.putExtra("kviz", (Serializable) odabraniKviz);
                                intent.putExtra("kategorije", kategorije);
                                intent.putExtra("trenutnaKategorija", trenutnaKategorija);
                                intent.putExtra("moguca", pitanjes);
                                startActivityForResult(intent, 1);

                            }

                        });
                        asyncTask.execute("ucitajPitanja", odabraniKviz);
                    }
                    else{
                        new AlertDialog.Builder(KvizoviAkt.this)
                                .setTitle("Greška")
                                .setMessage("Nema konekcije, ne može se dodati kviz!")
                                .setPositiveButton(android.R.string.yes, null)
                                .setNegativeButton(android.R.string.no, null)
                                .show();
                    }

                } else {
                    Intent intent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
                    intent.putExtra("kviz", (Serializable) odabraniKviz);
                    startActivity(intent);
                    Log.d("Naziv kviza", odabraniKviz.getNaziv());
                    for(int i=0; i<odabraniKviz.getPitanja().size(); i++){
                        Log.d("Naziv pitanja", odabraniKviz.getPitanja().get(i).getNaziv());
                        for(int j=0; j<odabraniKviz.getPitanja().get(i).getOdgovori().size(); j++){
                            Log.d("Odgovor", odabraniKviz.getPitanja().get(i).getOdgovori().get(j));
                        }
                    }
                }
            }
        });


        listaKvizova.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //zabrana editovanja ako nema konekcije
                if(isInternetOn()) {

                    Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                    odabraniKviz = listaOdabranihKvizova.get(position);

                    KreirajDokumentTask asyncTask = new KreirajDokumentTask(new OnTaskCompleted() {

                        @Override
                        public void onTaskCompleted(ArrayList<Pitanje> pitanjes) {
                            Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);

                            intent.putExtra("kviz", (Serializable) odabraniKviz);
                            intent.putExtra("kategorije", kategorije);
                            intent.putExtra("trenutnaKategorija", trenutnaKategorija);
                            intent.putExtra("moguca", pitanjes);
                            startActivityForResult(intent, 1);

                        }

                    });
                    asyncTask.execute("ucitajPitanja", odabraniKviz);
                }

                else{
                    new AlertDialog.Builder(KvizoviAkt.this)
                            .setTitle("Greška")
                            .setMessage("Nema konekcije, ne može se editovati kviz!")
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
                return true;


            }
        });

    }

    public void PostaviOdabrani(Kviz kviz) {
        odabraniKviz = kviz;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Configuration config = getResources().getConfiguration();

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

                Kviz kviz = (Kviz) data.getSerializableExtra("MESSAGE");
                trenutnaKategorija = kviz.getKategorija();

                odabranaPitanja.clear();

                for (int i = 0; i < kviz.getPitanja().size(); i++) {
                    if (!pitanja.contains(kviz.getPitanja().get(i))) {
                        pitanja.add(kviz.getPitanja().get(i));
                        odabranaPitanja.add(kviz.getPitanja().get(i));
                    }
                }

                if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (!kategorije.contains(kviz.getKategorija())) {

                        ///AZURIRANJE BAZE ZA KATEGORIJE
                        new KreirajDokumentTask().execute("addKategorija", kviz.getKategorija());

                        if (kategorije.isEmpty()) kategorije.add(kviz.getKategorija());
                        else kategorije.set(kategorije.size() - 1, kviz.getKategorija());
                        if (!kategorije.contains(svi)) kategorije.add(svi);

                        ListaFrag listFragment = new ListaFrag();
                        Bundle argument = new Bundle();
                        argument.putSerializable("kategorije", kategorije);
                        listFragment.setArguments(argument);
                        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, listFragment).commit();

                    }


                    if (odabraniKviz.equals(dodjKviz) && !kvizovi.contains(kviz)) {
                        if (!kviz.getNaziv().equals("")) {
                            kvizovi.add(kviz);

                            //DODAVANJE NOVOG KVIZA U BAZU
                            new KreirajDokumentTask().execute("add", odabranaPitanja, kviz);

                        }
                    } else {
                        //AZURIRANJE KVIZA U BAZI
                        String nazivKvizaZaBrisanje = odabraniKviz.getNaziv();
                        new KreirajDokumentTask().execute("edit", odabranaPitanja, kviz, nazivKvizaZaBrisanje);

                    }

                    progressDialog.show();
                    new KreirajDokumentTask((OnKvizoviTaskCompleted) this).execute("ucitajKvizove");


                } else {

                    //dodavanje nove kategorije u spinner
                    if (!kategorije.contains(kviz.getKategorija())) {

                        //AZURIRANJE BAZE ZA KATEGORIJE
                        new KreirajDokumentTask().execute("addKategorija", kviz.getKategorija());

                        //TODO: Azuriranje lokalne baze za kategorije //ADD
                        new DatabaseHandler(this).addKategorija(kviz.getKategorija());

                        if (kategorije.isEmpty()) kategorije.add(kviz.getKategorija());
                        else kategorije.set(kategorije.size() - 1, kviz.getKategorija());
                        if (!kategorije.contains(svi)) kategorije.add(svi);
                        adapterSpinner.notifyDataSetChanged();

                    }

                    if (odabraniKviz.equals(dodjKviz) && !kvizovi.contains(kviz)) {
                        if (!kviz.getNaziv().equals("")) {
                            kvizovi.add(kviz);

                            //DODAVANJE NOVOG KVIZA U BAZU
                            new KreirajDokumentTask().execute("add", odabranaPitanja, kviz);

                            //TODO: Azuriranje lokalne baze za kviz //ADD
                            new DatabaseHandler(this).addKviz(kviz);

                        }

                    } else {
                        //AZURIRANJE KVIZA U BAZI
                        String nazivKvizaZaBrisanje = odabraniKviz.getNaziv();
                        new KreirajDokumentTask().execute("edit", odabranaPitanja, kviz, nazivKvizaZaBrisanje);

                        //TODO: Azuriranje lokalne baze za kviz //UPDATE
                        Log.d("Naziv starog", odabraniKviz.getNaziv());
                        Log.d("Naziv novog", kviz.getNaziv());
                        new DatabaseHandler(this).updateKviz(odabraniKviz, kviz);

                    }

                    //postavljanje trenutne kategorije spinnera

                    spiner.setSelection(kategorije.indexOf(trenutnaKategorija));
                    adapterSpinner.notifyDataSetChanged();

                    progressDialog.show();
                    new KreirajDokumentTask((OnKvizoviTaskCompleted) this).execute("ucitajKvizove");

                }
            } else {
                Kviz kviz = (Kviz) data.getSerializableExtra("MESSAGE");

                if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (!kategorije.contains(kviz.getKategorija())) {

                        //AZURIRANJE BAZE ZA KATEGORIJE
                        new KreirajDokumentTask().execute("addKategorija", kviz.getKategorija());

                        if (kategorije.isEmpty()) kategorije.add(kviz.getKategorija());
                        else kategorije.set(kategorije.size() - 1, kviz.getKategorija());
                        if (!kategorije.contains(svi)) kategorije.add(svi);

                        ListaFrag listFragment = new ListaFrag();
                        Bundle argument = new Bundle();
                        argument.putSerializable("kategorije", kategorije);
                        listFragment.setArguments(argument);
                        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, listFragment).commit();

                    }
                } else {

                    if(isInternetOn()) {
                        //dodavanje nove kategorije u spinner
                        if (!kategorije.contains(kviz.getKategorija())) {

                            //AZURIRANJE BAZE ZA KATEGORIJE
                            new KreirajDokumentTask().execute("addKategorija", kviz.getKategorija());

                            //TODO: Azuriranje lokalne baze za kategorije //ADD
                            new DatabaseHandler(this).addKategorija(kviz.getKategorija());

                            if (kategorije.isEmpty()) kategorije.add(kviz.getKategorija());
                            else kategorije.set(kategorije.size() - 1, kviz.getKategorija());
                            if (!kategorije.contains(svi)) kategorije.add(svi);
                            adapterSpinner.notifyDataSetChanged();
                        }

                        //postavljanje trenutne kategorije spinnera
                        trenutnaKategorija = kviz.getKategorija();
                        spiner.setSelection(kategorije.indexOf(trenutnaKategorija));
                        adapterSpinner.notifyDataSetChanged();
                    }
                    progressDialog.show();
                    new KreirajDokumentTask((OnKvizoviTaskCompleted) this).execute("ucitajKvizove");
                }
            }
        }

    }

    @Override
    public void onAnswerClick(Kategorija kategorija) {
        Bundle argument = new Bundle();
        trenutnaKategorija = kategorija;
        progressDialog.show();
        new KreirajDokumentTask((OnKvizoviTaskCompleted) this).execute("ucitajKvizove");

    }




    public class KvizoviTask extends AsyncTask<Object, Integer, Void> {
        public String TOKEN = "";
        private OnSviKvizoviTaskCompleted listener;

        private   Map<String, ArrayList<Rang>> mapa = new HashMap<>();
        private ArrayList<Rang> rangovi = new ArrayList<>();
        public KvizoviTask(OnSviKvizoviTaskCompleted listener) {
            this.listener = listener;
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
                if(akcija.equals("ucitajSveKvizove")){
                    kvizovi=ucitajSveKvizove();
                    mapa=ucitajSveRangove();

                    //Log.d("Broj ucitanih", String.valueOf(kvizovi.size()));
                }


            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (listener != null)
                listener.onSviKvizoviTaskCompleted(kvizovi, mapa);
        }


        private  Map<String, ArrayList<Rang>> ucitajSveRangove(){
            Map<String, ArrayList<Rang>> mapaRangova = new HashMap<>();

            try {
                String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/Rangliste?access_token=";
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
                    JSONObject rangovii = documents.getJSONObject(i);

                    JSONObject jsonFields = rangovii.getJSONObject("fields");

                    JSONObject jsonNaziv = jsonFields.getJSONObject("nazivKviza");
                    String naziv = jsonNaziv.getString("stringValue");

                    JSONObject jsonLista = jsonFields.getJSONObject("lista");
                    JSONObject jsonMapValue1 = jsonLista.getJSONObject("mapValue");
                    JSONObject jsonFields2 = jsonMapValue1.getJSONObject("fields");

                    boolean ponavljajUcitavanje = true;
                    int brojac = 1;

                    ArrayList<Rang> rangovi = new ArrayList<>();

                    while (ponavljajUcitavanje) {

                        Rang jedanRezultat = null;

                        try {
                            JSONObject jsonRedniBrojKviza = jsonFields2.getJSONObject(String.valueOf(brojac));
                            JSONObject jsonMapValue2 = jsonRedniBrojKviza.getJSONObject("mapValue");
                            JSONObject jsonFields3 = jsonMapValue2.getJSONObject("fields");

                            String stringFields3 = jsonFields3.toString();
                            String[] uzmiIme = stringFields3.split("\"");
                            String username = uzmiIme[1];

                            JSONObject jsonUsername = jsonFields3.getJSONObject(username);
                            double ostverniRezultat = jsonUsername.getDouble("doubleValue");

                            jedanRezultat = new Rang(username, ostverniRezultat);
                            rangovi.add(jedanRezultat);

                            brojac++;
                        } catch (JSONException e) {
                            ponavljajUcitavanje = false;
                        }
                    }
                    mapaRangova.put(naziv, rangovi);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {

            }
            return mapaRangova;

        }


        private ArrayList<Kviz> ucitajSveKvizove() {

            ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();

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

                    odabraniKvizovi.add(novi);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {

                e.printStackTrace();
                Kviz novi = new Kviz(nazivKviza, new ArrayList<Pitanje>(), kat);
                odabraniKvizovi.add(novi);

            }

            return odabraniKvizovi;
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
    }

    public class RangTask extends AsyncTask<Object, Integer, Void> {
        public String TOKEN = "";
        private OnKategorijeTaskCompleted listener;



        @Override
        protected Void doInBackground(Object... objects) {

            GoogleCredential credentials = null;

            try {

                InputStream is = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                TOKEN = credentials.getAccessToken();

                String akcija = (String) objects[0];
                    if(akcija.equals("azurirajRangListe")){
                    azurirajRangListe();
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
         KvizoviAkt.this.nastaviAktivnost();
        }

        private void azurirajRangListe(){

            ArrayList<Rang> listaRangovaZaKviz = new ArrayList<>();
          //  Kviz kviz =  (Kviz)object;
         //   String nazivKviza =  kviz.getNaziv();

            Map<String, ArrayList<Rang>> mapa = new HashMap<>();
            mapa = new DatabaseHandler(KvizoviAkt.this).dohvatiRangove();

            String korijenskiUrl = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/";

            for(Map.Entry<String, ArrayList<Rang>> entry : mapa.entrySet()) {

                String nazivKviza = entry.getKey();
                listaRangovaZaKviz = entry.getValue();

                //prvo obrisem ranglistu za kviz
                try {

                    //     String korijenskiUrl = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/";
                    String url = "Rangliste/" + nazivKviza + "?access_token=";

                    URL urlObj = new URL(korijenskiUrl + url + URLEncoder.encode(TOKEN, "UTF-8"));
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                    conn.setRequestMethod("DELETE");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "aplication/json");
                    conn.connect();
                    System.out.println(conn.getResponseMessage());

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.print("Izuzetak, dokument nije obrisan");

                }

                //listaRangovaZaKviz = new DatabaseHandler(KvizoviAkt.this).dohvatiRangListuZaKviz(kviz);

                try {

                    String url = "Rangliste/?documentId=" + nazivKviza + "&access_token=";
                    URL urlObj = new URL(korijenskiUrl + url + URLEncoder.encode(TOKEN, "UTF-8"));
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "aplication/json");


                    StringBuilder dokuemntBilder = new StringBuilder("{ \"fields\": " +
                            "{ \"nazivKviza\": { \"stringValue\": \"" + nazivKviza + "\"}, " +
                            " \"lista\" : { \"mapValue\" : { \"fields\" : { ");
                    int pozicija = 1;

                    for (int i = 0; i < listaRangovaZaKviz.size(); i++) {
                        dokuemntBilder.append(" \"" + pozicija + "\" : { \"mapValue\" : { \"fields\" : { " +
                                " \"" + listaRangovaZaKviz.get(i).getImePrezimeIgraca() + "\" : { \"doubleValue\" : \"" + listaRangovaZaKviz.get(i).getRezulata() + "\"}}}}");

                        if (i != listaRangovaZaKviz.size() - 1)
                            dokuemntBilder.append(", ");

                        pozicija++;
                    }
                    dokuemntBilder.append(" }}}}}");


                    String dokument = dokuemntBilder.toString();


                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = dokument.getBytes("utf-8");
                        os.write(input, 0, input.length);

                    }
                    int codde = conn.getResponseCode();


                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.print("Izuzetak, dokument nije dodan");

                }
            }

        }

    }


    public class KategorijeTask extends AsyncTask<Object, Integer, Void> {
        public String TOKEN = "";
        private OnKategorijeTaskCompleted listener;
        private ArrayList<Kategorija> kat;
        private ArrayList<Kviz> sviKvizovi;
   //     private Map<String, >


        public KategorijeTask(OnKategorijeTaskCompleted listener) {
            this.listener = listener;
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
                if (akcija.equals("ucitajKategorije")) {
                    kat = ucitajKategorije();
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (listener != null)
                listener.onKategorijeTaskCompleted(kat);
        }

        private ArrayList<Kategorija> ucitajKategorije() {
            ArrayList<Kategorija> sveKategorije = new ArrayList<>();
            try {
                String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/Kategorije?access_token=";
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
                    JSONObject kategorije = documents.getJSONObject(i);
                    JSONObject fields = kategorije.getJSONObject("fields");
                    JSONObject naziv = fields.getJSONObject("naziv");
                    String nazivKategorije = naziv.getString("stringValue");
                    JSONObject idIkon = fields.getJSONObject("idIkonice");
                    String idIkonice = String.valueOf(idIkon.getInt("integerValue"));

                    Kategorija kategorija = new Kategorija(nazivKategorije, idIkonice);
                    sveKategorije.add(kategorija);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {

            }

            return sveKategorije;
        }
    }

    public class KreirajDokumentTask extends AsyncTask<Object, Integer, Void> {

        public String TOKEN = "";
        private OnTaskCompleted listener;
        private OnKvizoviTaskCompleted listener2;
        private ArrayList<Kviz> kvizovii = new ArrayList<>();

        public KreirajDokumentTask(OnTaskCompleted listener) {
            this.listener = listener;

        }

        public KreirajDokumentTask(OnKvizoviTaskCompleted listener2) {
            this.listener2 = listener2;

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
                if (akcija.equals("add")) addKviz(objects[2], objects[1]);  // novi + pitanja
                else if (akcija.equals("edit")) {
                    deleteKviz(objects[3]); //stari
                    addKviz(objects[2], objects[1]); //novi + pitanja
                } else if (akcija.equals("addKategorija")) addKategorija(objects[1]);

                else if (akcija.equals("ucitajPitanja")) {

                    ArrayList<Pitanje> pitanjes = new ArrayList<>();
                    pitanjes = ucitajPitanja(objects[1]);
                    listener.onTaskCompleted(pitanjes);

                } else if (akcija.equals("ucitajKvizove")) {
                    kvizovii = ucitajKvizove();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

//TODO: Azuriraanje ranga
        @Override
        protected void onPostExecute(Void v) {
            if (listener2 != null)
                listener2.onKvizoviTaskCompleted(kvizovii);
        }




        private ArrayList<Kviz> ucitajKvizove() {

            ArrayList<Kviz> odabraniKvizovi = new ArrayList<>();

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

                    if (novi.getKategorija().equals(trenutnaKategorija) || trenutnaKategorija.equals(svi))
                        odabraniKvizovi.add(novi);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {

                e.printStackTrace();
                Kviz novi = new Kviz(nazivKviza, new ArrayList<Pitanje>(), kat);
                if (novi.getKategorija().equals(trenutnaKategorija) || trenutnaKategorija.equals(svi))
                    odabraniKvizovi.add(novi);

            }

            return odabraniKvizovi;
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


        private ArrayList<Pitanje> ucitajPitanja(Object... objects) {

            Kviz kviz = (Kviz) objects[0];
            ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();

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
                    JSONObject pitanje = documents.getJSONObject(i);
                    JSONObject fields = pitanje.getJSONObject("fields");
                    JSONObject naziv = fields.getJSONObject("naziv");
                    String nazivPitanja = naziv.getString("stringValue");
                    JSONObject index = fields.getJSONObject("indexTacnog");
                    int indexTacnog = index.getInt("integerValue");

                    JSONObject odgovori = fields.getJSONObject("odgovori");
                    JSONObject arrayValue = odgovori.getJSONObject("arrayValue");

                    ArrayList<String> odgovoriPitanja = new ArrayList<>();

                    if (arrayValue.has("values")) {
                        JSONArray values = arrayValue.getJSONArray("values");
                        for (int j = 0; j < values.length(); j++) {
                            JSONObject odgovor = values.getJSONObject(j);
                            String odgovorPitanja = odgovor.getString("stringValue");
                            odgovoriPitanja.add(odgovorPitanja);
                        }
                    }
                    Pitanje novoPitanje = new Pitanje(nazivPitanja, nazivPitanja, odgovoriPitanja, odgovoriPitanja.get(indexTacnog));
                    if (!kviz.getPitanja().contains(novoPitanje)) mogucaPitanja.add(novoPitanje);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mogucaPitanja;
        }


        private void addKategorija(Object... objects) {
            Kategorija novaKategorija = (Kategorija) objects[0];

            try {
                String korijenskiUrl = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/";
                String url = "Kategorije?documentId=" + novaKategorija.getNaziv() + "&access_token=";
                URL urlObj = new URL(korijenskiUrl + url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "aplication/json");

                String dokument = "{\"fields\" : {\"naziv\" : {\"stringValue\" : \"" + novaKategorija.getNaziv() + "\"}, \"idIkonice\" : {\"integerValue\" : \"" + Integer.parseInt(novaKategorija.getId()) + "\"}}}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = dokument.getBytes("utf-8");
                    os.write(input, 0, input.length);

                }
                int codde = conn.getResponseCode();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        private void deleteKviz(Object... objects) {

            String naziv = (String) objects[0];

            try {

                String korijenskiUrl = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/";
                String url = "Kvizovi/" + naziv + "?access_token=";

                URL urlObj = new URL(korijenskiUrl + url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                conn.setRequestMethod("DELETE");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "aplication/json");
                conn.connect();
                System.out.println(conn.getResponseMessage());

            } catch (IOException e) {
                e.printStackTrace();
                System.out.print("Izuzetak, dokument nije obrisan");

            }
        }

        private void addKviz(Object... objects) {

            Kviz noviKviz = (Kviz) objects[0];

            ArrayList<Pitanje> preuzetaPitanja = new ArrayList<>();
            preuzetaPitanja = (ArrayList<Pitanje>) objects[1];

            try {
                String korijenskiUrl = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/";
                String url = "Kvizovi?documentId=" + noviKviz.getNaziv() + "&access_token=";

                URL urlObj = new URL(korijenskiUrl + url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "aplication/json");

                String dokument = "{\"fields\" : {\"naziv\" : {\"stringValue\" : \"" + noviKviz.getNaziv() + "\"}, \"idKategorije\" : {\"stringValue\" : \"" + noviKviz.getKategorija().getId() + "\"}, ";
                dokument += "\"pitanja\" : {\"arrayValue\" : {\"values\" : [";
                for (int i = 0; i < noviKviz.getPitanja().size(); i++) {
                    dokument += "{\"stringValue\" : \"" + noviKviz.getPitanja().get(i).getNaziv() + "\"}";
                    if (i != noviKviz.getPitanja().size() - 1) dokument += ",";
                }
                dokument += "]}}}}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = dokument.getBytes("utf-8");
                    os.write(input, 0, input.length);

                }
                int codde = conn.getResponseCode();

                //AŽURIRANJE DOKUMENTA PITANJA
                for (int j = 0; j < preuzetaPitanja.size(); j++) {

                    korijenskiUrl = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/";
                    url = "Pitanja?documentId=" + preuzetaPitanja.get(j).getNaziv() + "&access_token=";


                    urlObj = new URL(korijenskiUrl + url + URLEncoder.encode(TOKEN, "UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();

                    System.out.print(conn.toString());

                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "aplication/json");


                    dokument = "{\"fields\" : {\"naziv\" : {\"stringValue\" : \"" + preuzetaPitanja.get(j).getNaziv() + "\"}, \"indexTacnog\" : {\"integerValue\" : \"" + preuzetaPitanja.get(j).getIndeksTacnog() + "\"}, ";
                    dokument += "\"odgovori\" : {\"arrayValue\" : {\"values\" : [";
                    for (int i = 0; i < preuzetaPitanja.get(j).getOdgovori().size(); i++) {
                        dokument += "{\"stringValue\" : \"" + preuzetaPitanja.get(j).getOdgovori().get(i) + "\"}";
                        if (i != preuzetaPitanja.get(j).getOdgovori().size() - 1) dokument += ",";
                    }
                    dokument += "]}}}}";

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = dokument.getBytes("utf-8");
                        os.write(input, 0, input.length);

                    }

                    codde = conn.getResponseCode();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.print("Izuzetak, dokument nije dodan");

            }

        }

    }


}