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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;
import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

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

import ba.unsa.etf.rma.OnKategorijeTaskCompleted;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, OnKategorijeTaskCompleted {

    ProgressDialog progressDialog;
    private EditText nazivKategorije;
    private EditText ikona;
    private Button dodajIkonu;
    private Button spasiKategoriju;
    private Icon[] selectedIcons;
    //za vratit
    private String nazivKat;
    private String idKat;
    private ArrayList<Kategorija> sveKategorije = new ArrayList<>();

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
    public void onKategorijeTaskCompleted(ArrayList<Kategorija> kategorije) {

        for (int i = 0; i < kategorije.size(); i++) {
            sveKategorije.add(kategorije.get(i));
        }


        nazivKat = nazivKategorije.getText().toString();
        nazivKat = nazivKat.replaceAll("\\s", "");

        idKat = ikona.getText().toString();
        Kategorija novaKategorija = new Kategorija(nazivKat, idKat);

        progressDialog.dismiss();

        if (sveKategorije.contains(novaKategorija)) {
            new AlertDialog.Builder(this)
                    .setTitle("Greška")
                    .setMessage("Unesena kategorija već postoji!")
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else {
            Intent intent = new Intent();
            intent.putExtra("MESSAGE", novaKategorija);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        final IconDialog iconDialog = new IconDialog();

        nazivKategorije = (EditText) findViewById(R.id.etNaziv);
        ikona = (EditText) findViewById(R.id.etIkona);
        dodajIkonu = (Button) findViewById(R.id.btnDodajIkonu);
        spasiKategoriju = (Button) findViewById(R.id.btnDodajKategoriju);


        nazivKategorije.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if (!nazivKategorije.getText().toString().equals("")) {
                    nazivKategorije.setBackgroundColor(Color.WHITE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });


        ikona.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                if (!ikona.getText().toString().equals("")) {
                    ikona.setBackgroundColor(Color.WHITE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ikona.setEnabled(false);
        dodajIkonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                iconDialog.setSelectedIcons(selectedIcons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        spasiKategoriju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isInternetOn()) {
                    if (validirajPolja()) {

                        progressDialog.show();
                        new KreirajDokumentTask((OnKategorijeTaskCompleted) DodajKategorijuAkt.this).execute("ucitajKategorije");


                    }
                }
                else{
                    new AlertDialog.Builder(DodajKategorijuAkt.this)
                            .setTitle("Greška")
                            .setMessage("Nema konekcije, ne može se dodati kategorija!")
                            .setPositiveButton(android.R.string.yes, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            }
        });
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        ikona.setText(String.valueOf(selectedIcons[0].getId()));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();//finishing activity
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean validirajPolja() {
        boolean tacno = true;
        if (nazivKategorije.getText().toString().equals("")) {
            nazivKategorije.setBackgroundColor(Color.parseColor("#FFCDD2"));
            tacno = false;
        } else
            nazivKategorije.setBackgroundColor(Color.WHITE);

        if (ikona.getText().toString().equals("")) {
            ikona.setBackgroundColor(Color.parseColor("#FFCDD2"));
            tacno = false;
        } else
            ikona.setBackgroundColor(Color.WHITE);

        return tacno;
    }

    public class KreirajDokumentTask extends AsyncTask<Object, Integer, Void> {

        public String TOKEN = "";
        private ArrayList<Kategorija> kategorijas = new ArrayList<>();
        private OnKategorijeTaskCompleted listener;

        public KreirajDokumentTask(OnKategorijeTaskCompleted listener) {
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

                if (akcija.equals("ucitajKategorije")) {
                    kategorijas = ucitajKategorije();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            if (listener != null) listener.onKategorijeTaskCompleted(kategorijas);
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

