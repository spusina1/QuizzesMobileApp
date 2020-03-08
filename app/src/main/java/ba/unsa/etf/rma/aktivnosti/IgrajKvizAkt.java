package ba.unsa.etf.rma.aktivnosti;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import ba.unsa.etf.rma.OnRangTaskCompleted;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.database.DatabaseHandler;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.Rang;

public class IgrajKvizAkt extends AppCompatActivity implements PitanjeFrag.OnItemClick, OnRangTaskCompleted {
    private ArrayList<Pitanje> pitanja;
    private InformacijeFrag firstFragment;
    private PitanjeFrag secondFragment;
    private RangLista rangListaFrag;
    private int indeksPitanja = 0;
    private String naziv;
    private int brTacnih = 0;
    private int brPreostalih = 0;
    private int ukupno = 0;
    private double procenat = 0;
    private boolean odobrenPocetakKviza = true;
    private long razlika = 0;
    private Kviz kviz = new Kviz();

    private String imeIprezime;
    private ArrayList<Rang> rang = new ArrayList<>();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        kviz = (Kviz) getIntent().getSerializableExtra("kviz");
        pitanja = new ArrayList<>(kviz.getPitanja());
        Collections.shuffle(pitanja);
        brPreostalih = kviz.getPitanja().size() - 1;
        ukupno = kviz.getPitanja().size();
        if(ukupno%2 != 0) ukupno++;
        naziv = kviz.getNaziv();


        getDataFromEventTable();

    }

    @Override
    public void onAnswerClick(boolean tacan) {

        if (tacan) {
            brTacnih++;
        }
        brPreostalih--;
        Bundle argument = new Bundle();
        procenat = 100 * (double) brTacnih / ukupno;
        argument.putString("naziv", naziv);
        argument.putInt("brTacnih", brTacnih);
        if (brPreostalih < 0) brPreostalih = 0;
        argument.putInt("brPreostalih", brPreostalih);
        argument.putDouble("procenat", procenat);
        InformacijeFrag informacijeFrag = new InformacijeFrag();
        informacijeFrag.setArguments(argument);
        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, informacijeFrag).commit();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ucitajPitanje();
            }
        }, 2000);


    }

    private void setAlarm(int minute){

        int trenutnoSati = Calendar.getInstance().getTime().getHours();
        int trenutnoMinuta = Calendar.getInstance().getTime().getMinutes();

        Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
        i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        i.putExtra(AlarmClock.EXTRA_MESSAGE, "Početak igranja kviza!");
        i.putExtra(AlarmClock.EXTRA_HOUR, trenutnoSati);
        i.putExtra(AlarmClock.EXTRA_MINUTES, trenutnoMinuta + minute);
        startActivity(i);

    }

    public void getDataFromEventTable() {



            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 10);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10) {

            Log.d("Tu sam", "Tuuuuuuuu");
            for (int i = 0; i < permissions.length; i++) {

                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.READ_CALENDAR)) {

                    if (grantResult == PackageManager.PERMISSION_GRANTED) {

                        Log.d("Tu sam", "I ovdje");

                        Cursor  kursor = null;
                        ContentResolver contentResolver = getContentResolver();

                        String[] mProjection =
                                {
                                        CalendarContract.Events.DTSTART,
                                };

                        Uri uri = CalendarContract.Events.CONTENT_URI;

                        String selection = CalendarContract.Events.DTSTART + " > ? ";
                        String[] selectionArgs = new String[]{String.valueOf(Calendar.getInstance().getTimeInMillis())};

                        kursor = contentResolver.query(uri, mProjection, selection, selectionArgs, null);

                        if(kursor!=null && kursor.getCount()>=1) {

                        ArrayList<Long> lista = new ArrayList<>();
                        while (kursor.moveToNext()) {
                           Long pocetak = kursor.getLong(kursor.getColumnIndex(CalendarContract.Events.DTSTART));
                           lista.add(pocetak);
                           // if(pocetak<minVrijeme) minVrijeme = pocetak;
                        }

                        Long minVrijeme = lista.get(0);
                            for (int j = 0; j < lista.size(); j++) {
                                if ( lista.get(j) < minVrijeme) {
                                    minVrijeme = lista.get(j);
                                }
                            }

                        Date date = new Date(minVrijeme);
                        Log.d("Ispisi vrijeme", String.valueOf(date));

                        razlika = minVrijeme - Calendar.getInstance().getTimeInMillis();

                            Log.d("Razlika je", String.valueOf(razlika));
                            Log.d("Za kviz", String.valueOf(60000*ukupno/2));

                        if(razlika < (60000*ukupno/2)) odobrenPocetakKviza = false;


                        }
                        prikazAktivnosti();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 10);
                    }
                }
            }
        }
    }

    private void prikazAktivnosti(){

        if(odobrenPocetakKviza) {

                //TODO : ALARM
                if(ukupno!=0)
                setAlarm(ukupno / 2);

                FragmentTransaction ft;
                ft = getSupportFragmentManager().beginTransaction();
                firstFragment = new InformacijeFrag();
                ft.replace(R.id.informacijePlace, firstFragment);
                Bundle argument = new Bundle();

                argument.putString("naziv", naziv);
                argument.putInt("brTacnih", brTacnih);
                if (brPreostalih < 0) brPreostalih = 0;
                argument.putInt("brPreostalih", brPreostalih);
                argument.putDouble("procenat", procenat);
                firstFragment.setArguments(argument);


                secondFragment = new PitanjeFrag();
                Bundle pitanje = new Bundle();
                if (indeksPitanja < pitanja.size()) {
                    pitanje.putSerializable("pitanja", pitanja.get(indeksPitanja));
                    indeksPitanja++;
                } else {
                    pitanje.putSerializable("pitanja", new Pitanje("Kviz je završen!", "", new ArrayList<String>(), ""));

                }
                secondFragment.setArguments(pitanje);
                ft.replace(R.id.pitanjePlace, secondFragment);
                ft.commit();


        } else{
            new AlertDialog.Builder(this)
                    .setTitle("Greška")
                    .setMessage("Imate dogadjaj u kalendaru za " + razlika/600000 + " minuta!")
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }


    public void ucitajPitanje() {

        secondFragment = new PitanjeFrag();

        final Bundle pitanje = new Bundle();
        if (indeksPitanja < pitanja.size()) {
            pitanje.putSerializable("pitanja", pitanja.get(indeksPitanja));
            indeksPitanja++;
        } else {
            pitanje.putSerializable("pitanja", new Pitanje("Kviz je završen!", "", new ArrayList<String>(), ""));

            final EditText edittext = new EditText(this);

            final RangTask rangTask = new RangTask((OnRangTaskCompleted) this);

            new AlertDialog.Builder(this)
                    .setTitle("Kviz zavrsen")
                    .setMessage("Unesite ime i prezime!")
                    .setView(edittext)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            rangListaFrag = new RangLista();

                            imeIprezime = edittext.getText().toString();

                            Rang clan = new Rang();
                            clan.setImePrezimeIgraca(imeIprezime);
                            clan.setRezulata(procenat);


                            if (imeIprezime.equals("")) imeIprezime = "anonimus";
                            clan.setImePrezimeIgraca(imeIprezime);

                            //dodavanje ranga u lokalnu bazu
                            DatabaseHandler db = new DatabaseHandler(IgrajKvizAkt.this);
                            db.addRang(clan, naziv);

                            if(isInternetOn()){
                            rangTask.execute("addClan", clan, naziv);}
                            else{
                                ArrayList<Rang> rangIzLokalne = new DatabaseHandler(IgrajKvizAkt.this).dohvatiRangListuZaKviz(kviz.getNaziv());
                                sort(rangIzLokalne);
                                final Bundle lista = new Bundle();
                                lista.putSerializable("rang", rangIzLokalne);
                                rangListaFrag.setArguments(lista);
                                getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rangListaFrag).commit();
                            }

                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();

        }

        secondFragment.setArguments(pitanje);
        getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, secondFragment).commit();
    }

    @Override
    public void onRangTaskCompleted(ArrayList<Rang> rangLista) {
        final Bundle lista = new Bundle();
        lista.putSerializable("rang", rangLista);
        rangListaFrag.setArguments(lista);
        getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, rangListaFrag).commit();
    }

    private void sort(ArrayList<Rang> novaRangLista) {
        for (int i = 0; i < novaRangLista.size(); i++) {
            int indeksMinimanlnog = i;

            for (int j = i + 1; j < novaRangLista.size(); j++) {
                if (novaRangLista.get(indeksMinimanlnog).getRezulata() < novaRangLista.get(j).getRezulata()) {
                    indeksMinimanlnog = j;
                }
            }

            Collections.swap(novaRangLista, indeksMinimanlnog, i);
        }
    }

    public class RangTask extends AsyncTask<Object, Integer, Void> {

        public String TOKEN = "";
        OnRangTaskCompleted listener;
        private ArrayList<Rang> rangs = new ArrayList<>();

        public RangTask(OnRangTaskCompleted onRangTaskCompleted) {
            this.listener = onRangTaskCompleted;
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
                if (akcija.equals("addClan")) {
                    rangs = azurirajRangListu(objects[1], objects[2]);
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(Void v) {
            listener.onRangTaskCompleted(rangs);
        }




        private boolean daLiPostojiRangLista(String nazivKviza) {

            try {
                String upit = "{\n" +
                        "\"structuredQuery\": {\n" +
                        "\"where\": {\n" +
                        "\"fieldFilter\": {\n" +
                        "\"field\": {\"fieldPath\": \"nazivKviza\"}, \n" +
                        "\"op\": \"EQUAL\",\n" +
                        "\"value\": {\"stringValue\": \"" + nazivKviza + "\"}\n" +
                        "}\n" +
                        "},\n" +
                        "\"select\": {\"fields\": [ {\"fieldPath\": \"nazivKviza\"}, {\"fieldPath\": \"lista\"} ] }, \n" +
                        "\"from\": [{\"collectionId\" : \"Rangliste\"}], \n" +
                        "\"limit\" : 1000\n" +
                        "}\n" +
                        "}";

                String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents:runQuery";

                URL urlObjekat = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObjekat.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "appliction/json");
                conn.setRequestProperty("Accept", "appliation/json");
                conn.setRequestProperty("Authorization", "Bearer " + TOKEN);

                OutputStream outputStream = conn.getOutputStream();
                byte[] unos = upit.getBytes();
                outputStream.write(unos, 0, unos.length);

                InputStream odgovor = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(odgovor, "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = bufferedReader.readLine()) != null)
                    response.append(responseLine.trim());

                JSONArray rezultat = new JSONArray(response.toString());

                try {
                    JSONObject jsonJedanRezultat = rezultat.getJSONObject(0);
                    JSONObject jsonDokument = jsonJedanRezultat.getJSONObject("document");
                    return true;
                } catch (JSONException e) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;

        }

        private ArrayList<Rang> azurirajRangListu(Object... objects) {

            ArrayList<Rang> novaRangLista = new ArrayList<>();

            String nazivKviza = (String) objects[1];
            Rang noviClan = (Rang) objects[0];


            HttpURLConnection conn = null;

            //KADA NE RANGLISTE ZA KVIZ VEC SE DODAJE PO PRVI PUT
            try {
                String korijenskiUrl = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/";

                if (daLiPostojiRangLista(nazivKviza)) {

                    try {
                        String query = "{\n" +
                                "\"structuredQuery\": {\n" +
                                "\"where\": {\n" +
                                "\"fieldFilter\": {\n" +
                                "\"field\": {\"fieldPath\": \"nazivKviza\"}, \n" +
                                "\"op\": \"EQUAL\",\n" +
                                "\"value\": {\"stringValue\": \"" + nazivKviza + "\"}\n" +
                                "}\n" +
                                "},\n" +
                                "\"select\": {\"fields\": [ {\"fieldPath\": \"nazivKviza\"}, {\"fieldPath\": \"lista\"} ] }, \n" +
                                "\"from\": [{\"collectionId\" : \"Rangliste\"}], \n" +
                                "\"limit\" : 1000\n" +
                                "}\n" +
                                "}";

                        String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents:runQuery";

                        URL urlObjekat = new URL(url);

                        HttpURLConnection con = (HttpURLConnection) urlObjekat.openConnection();

                        con.setRequestProperty("Authorization", "Bearer " + TOKEN);
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "appliction/json");
                        con.setRequestProperty("Accept", "appliation/json");


                        OutputStream outputStream = con.getOutputStream();
                        byte[] unos = query.getBytes();
                        outputStream.write(unos, 0, unos.length);

                        InputStream odgovor = con.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(odgovor, "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = bufferedReader.readLine()) != null)
                            response.append(responseLine.trim());

                        JSONArray rezultatiKverija = new JSONArray(response.toString());
                        try {

                            JSONObject jedanRezultatKverijaIzuzetak = rezultatiKverija.getJSONObject(0);
                            JSONObject documentsIzuzetak = jedanRezultatKverijaIzuzetak.getJSONObject("document");


                            for (int i = 0; i < rezultatiKverija.length(); i++) {
                                JSONObject jedanRezultatKverija = rezultatiKverija.getJSONObject(i);
                                JSONObject document = jedanRezultatKverija.getJSONObject("document");

                                JSONObject jsonFields = document.getJSONObject("fields");

                                JSONObject jsonNaziv = jsonFields.getJSONObject("nazivKviza");
                                String naziv = jsonNaziv.getString("stringValue");

                                JSONObject jsonLista = jsonFields.getJSONObject("lista");
                                JSONObject jsonMapValue1 = jsonLista.getJSONObject("mapValue");
                                JSONObject jsonFields2 = jsonMapValue1.getJSONObject("fields");

                                boolean ponavljajUcitavanje = true;
                                int brojac = 1;

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
                                        novaRangLista.add(jedanRezultat);

                                        brojac++;
                                    } catch (JSONException e) {
                                        ponavljajUcitavanje = false;
                                    }
                                }
                            }
                        } catch (JSONException e) {

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    novaRangLista.add(noviClan);

                    sort(novaRangLista);


                    try {
                        String spojeno = URLEncoder.encode(nazivKviza, "utf-8");

                        String url = "https://firestore.googleapis.com/v1/projects/rma19-933f0/databases/(default)/documents/Rangliste/" + spojeno + "?currentDocument.exists=true" + "&access_token=";

                        URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();


                        con.setRequestProperty("Authorization", "Bearer " + TOKEN);
                        con.setRequestMethod("PATCH");
                        con.setRequestProperty("Content-Type", "appliction/json");
                        con.setRequestProperty("Accept", "appliation/json");


                        StringBuilder dokuemntBilder = new StringBuilder("{ \"fields\": " +
                                "{ \"nazivKviza\": { \"stringValue\": \"" + nazivKviza + "\"}, " +
                                " \"lista\" : { \"mapValue\" : { \"fields\" : { ");
                        int pozicija = 1;

                        for (int i = 0; i < novaRangLista.size(); i++) {
                            dokuemntBilder.append(" \"" + pozicija + "\" : { \"mapValue\" : { \"fields\" : { " +
                                    " \"" + novaRangLista.get(i).getImePrezimeIgraca() + "\" : { \"doubleValue\" : \"" + novaRangLista.get(i).getRezulata() + "\"}}}}");

                            if (i != novaRangLista.size() - 1)
                                dokuemntBilder.append(", ");

                            pozicija++;
                        }
                        dokuemntBilder.append(" }}}}}");


                        String dokument = dokuemntBilder.toString();

                        OutputStream outputStream = con.getOutputStream();
                        byte[] unos = dokument.getBytes();
                        outputStream.write(unos, 0, unos.length);

                        InputStream odgovor = con.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(odgovor, "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = bufferedReader.readLine()) != null)
                            response.append(responseLine.trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    String url = "Rangliste/?documentId=" + nazivKviza + "&access_token=";
                    URL urlObj = new URL(korijenskiUrl + url + URLEncoder.encode(TOKEN, "UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();

                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "aplication/json");
                    conn.setRequestProperty("Accept", "appliation/json");

                    String dokument = "{\"fields\" : {\"nazivKviza\" : {\"stringValue\" : \"" + nazivKviza + "\"}, \"lista\" : {\"mapValue\" : { \"fields\" : { " +
                            "\"1\" : { \"mapValue\" : { \"fields\" : { " +
                            " \"" + noviClan.getImePrezimeIgraca() + "\" : { \"doubleValue\" : \"" + noviClan.getRezulata() + "\"}}}}" + " }}}}}";


                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = dokument.getBytes("utf-8");
                        os.write(input, 0, input.length);

                    }
                    int codde = conn.getResponseCode();
                    novaRangLista.add(noviClan);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            return novaRangLista;
        }
    }

}
