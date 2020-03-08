package ba.unsa.etf.rma.aktivnosti;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ba.unsa.etf.rma.OnTaskCompleted;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.OdgovoriAdapter;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajPitanjeAkt extends AppCompatActivity implements OnTaskCompleted {

    ProgressDialog progressDialog;
    //widgeti
    private EditText nazivPitanja;
    private EditText odgovorPitanja;
    private ListView listaOdgovora;
    private Button dodajOdgovor;
    private Button dodajTacan;
    private Button dodajPitanje;
    //kolekcije
    private ArrayList<String> odgovori = new ArrayList<>();
    private ArrayList<Pitanje> svaPitanja = new ArrayList<>();
    //adapteri
    private OdgovoriAdapter adapterListe;
    //za vratit
    private String tacanOdgovor = "";
    private String naziv;

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
    public void onTaskCompleted(ArrayList<Pitanje> pitanjes) {


        for (int i = 0; i < pitanjes.size(); i++) {
            svaPitanja.add(pitanjes.get(i));
        }
        progressDialog.dismiss();

        naziv = nazivPitanja.getText().toString();
        naziv = naziv.replaceAll("\\s", "");

        Pitanje novoPitanje = new Pitanje(naziv, naziv, odgovori, tacanOdgovor);
        if (svaPitanja.contains(novoPitanje)) {
            new AlertDialog.Builder(this)
                    .setTitle("Greška")
                    .setMessage("Uneseno pitanje već postoji!")
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else {
            Intent intent = new Intent();
            intent.putExtra("MESSAGE", novoPitanje);
            setResult(RESULT_OK, intent);
            finish();//finishing activity
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        nazivPitanja = (EditText) findViewById(R.id.etNaziv);
        odgovorPitanja = (EditText) findViewById(R.id.etOdgovor);
        listaOdgovora = (ListView) findViewById(R.id.lvOdgovori);
        dodajOdgovor = (Button) findViewById(R.id.btnDodajOdgovor);
        dodajTacan = (Button) findViewById(R.id.btnDodajTacan);
        dodajPitanje = (Button) findViewById(R.id.btnDodajPitanje);

        adapterListe = new OdgovoriAdapter(this, odgovori, tacanOdgovor);
        listaOdgovora.setAdapter(adapterListe);


        nazivPitanja.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if (!nazivPitanja.getText().toString().equals("")) {
                    nazivPitanja.setBackgroundColor(Color.WHITE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        odgovorPitanja.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if (!odgovorPitanja.getText().toString().equals("")) {
                    odgovorPitanja.setBackgroundColor(Color.WHITE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        dodajOdgovor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pokupiPitanje = odgovorPitanja.getText().toString();
                if (!pokupiPitanje.equals("")) {
                    if (!odgovori.contains(pokupiPitanje))
                        odgovori.add(pokupiPitanje);
                    adapterListe.notifyDataSetChanged();
                    odgovorPitanja.setText("");
                }
            }
        });

        dodajTacan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pokupiPitanje = odgovorPitanja.getText().toString();
                if (!pokupiPitanje.equals("")) {
                    if (tacanOdgovor.equals("")) {
                        tacanOdgovor = odgovorPitanja.getText().toString();
                        if (!odgovori.contains(tacanOdgovor))
                            odgovori.add(tacanOdgovor);

                        adapterListe.setTacanOdgovor(tacanOdgovor);
                        adapterListe.notifyDataSetChanged();
                        odgovorPitanja.setText("");
                    }
                }

            }

        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        dodajPitanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetOn()) {
                    if (validirajPolja()) {
                        progressDialog.show();
                        new KreirajDokumentTask((OnTaskCompleted) DodajPitanjeAkt.this).execute("ucitajPitanja");
                    }
                }
                else{
                    new AlertDialog.Builder(DodajPitanjeAkt.this)
                            .setTitle("Greška")
                            .setMessage("Nema konekcije, ne može se dodati pitanje!")
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            }
        });

        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (odgovori.get(position).equals(tacanOdgovor)) tacanOdgovor = "";
                odgovori.remove(position);
                adapterListe.notifyDataSetChanged();

            }
        });

    }

    private boolean validirajPolja() {
        boolean tacno = true;
        if (nazivPitanja.getText().toString().equals("")) {
            nazivPitanja.setBackgroundColor(Color.parseColor("#FFCDD2"));
            tacno = false;
        } else
            nazivPitanja.setBackgroundColor(Color.WHITE);


        if (tacanOdgovor.equals("")) {
            odgovorPitanja.setBackgroundColor(Color.parseColor("#FFCDD2"));
            tacno = false;
        } else odgovorPitanja.setBackgroundColor(Color.WHITE);


        return tacno;

    }

    public class KreirajDokumentTask extends AsyncTask<Object, Integer, Void> {

        public String TOKEN = "";
        private ArrayList<Pitanje> pitanja = new ArrayList<>();
        private OnTaskCompleted listener;

        public KreirajDokumentTask(OnTaskCompleted listener) {
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

                if (akcija.equals("ucitajPitanja")) {
                    pitanja = ucitajPitanja();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            if (listener != null) listener.onTaskCompleted(pitanja);
        }

        private ArrayList<Pitanje> ucitajPitanja() {

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
                    mogucaPitanja.add(novoPitanje);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mogucaPitanja;
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
