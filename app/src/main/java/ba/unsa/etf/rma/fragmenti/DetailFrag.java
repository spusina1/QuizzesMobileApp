package ba.unsa.etf.rma.fragmenti;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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

import ba.unsa.etf.rma.OnTaskCompleted;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.GridAdapter;
import ba.unsa.etf.rma.aktivnosti.DodajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DetailFrag extends Fragment implements OnTaskCompleted {

    private GridView gridView;
    private GridAdapter gridAdapter;
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private Kviz dodjKviz = new Kviz("Dodaj Kviz", null, new Kategorija("Svi", "671"));
    private Kviz odabraniKviz;
    private Kategorija trenutnaKategorija;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        gridView = (GridView) view.findViewById(R.id.gridKvizovi);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments().containsKey("lista")) {
            kvizovi = (ArrayList<Kviz>) getArguments().getSerializable("lista");
        }
        if (getArguments().containsKey("kategorije")) {
            kategorije = (ArrayList<Kategorija>) getArguments().getSerializable("kategorije");
        }


        gridAdapter = new GridAdapter(this.getActivity(), kvizovi);
        gridView.setAdapter(gridAdapter);

        gridAdapter.notifyDataSetChanged();


        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                odabraniKviz = kvizovi.get(position);
                trenutnaKategorija = odabraniKviz.getKategorija();
                new KreirajDokumentTask((OnTaskCompleted) DetailFrag.this).execute("ucitajPitanja", odabraniKviz);
                return true;

            }
        });


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                odabraniKviz = kvizovi.get(position);
                trenutnaKategorija = odabraniKviz.getKategorija();

                if (odabraniKviz.getNaziv().equals(dodjKviz.getNaziv())) {
                    new KreirajDokumentTask((OnTaskCompleted) DetailFrag.this).execute("ucitajPitanja", odabraniKviz);

                } else {
                    Intent intent = new Intent(getContext(), IgrajKvizAkt.class);
                    intent.putExtra("kviz", (Serializable) odabraniKviz);
                    startActivity(intent);
                }

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final KvizoviAkt aktivnost = (KvizoviAkt) getActivity();

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Kviz kviz = (Kviz) data.getSerializableExtra("MESSAGE");
                aktivnost.PostaviOdabrani(odabraniKviz);
                aktivnost.onActivityResult(1, RESULT_OK, data);

            } else {
                aktivnost.onActivityResult(1, RESULT_CANCELED, data);
            }

        }
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onTaskCompleted(ArrayList<Pitanje> pitanjes) {

        Intent intent = new Intent(getActivity(), DodajKvizAkt.class);
        intent.putExtra("kviz", (Serializable) odabraniKviz);
        intent.putExtra("kategorije", kategorije);
        intent.putExtra("trenutnaKategorija", trenutnaKategorija);
        intent.putExtra("sviKvizovi", kvizovi);
        intent.putExtra("moguca", pitanjes);
        startActivityForResult(intent, 1);
    }

    public class KreirajDokumentTask extends AsyncTask<Object, Integer, Void> {

        public String TOKEN = "";
        ArrayList<Pitanje> pitanjes = new ArrayList<>();
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
                    pitanjes = ucitajPitanja(objects[1]);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            if (listener != null) listener.onTaskCompleted(pitanjes);
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

    }
}
