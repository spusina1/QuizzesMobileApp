package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.KategorijeAdapter;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;


public class ListaFrag extends Fragment {

    private TextView tekst;
    private ListView listaKategorija;
    private ArrayList<Kategorija> kategorije;
    private KategorijeAdapter kategorijeAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        tekst = (TextView) view.findViewById(R.id.tekstPitanja);
        listaKategorija = (ListView) view.findViewById(R.id.listaKategorija);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments().containsKey("kategorije")) {
            kategorije = (ArrayList<Kategorija>) getArguments().getSerializable("kategorije");
            kategorijeAdapter = new KategorijeAdapter(this.getActivity(), kategorije);
            listaKategorija.setAdapter(kategorijeAdapter);
        }
        final KvizoviAkt aktivnost = (KvizoviAkt) getActivity();

        listaKategorija.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                aktivnost.onAnswerClick(kategorije.get(position));
            }
        });

    }

    public interface OnItemClick {
        public void onAnswerClick(Kategorija kategorija);
    }
}
