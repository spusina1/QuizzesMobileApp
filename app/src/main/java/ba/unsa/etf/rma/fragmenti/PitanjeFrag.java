package ba.unsa.etf.rma.fragmenti;

import android.content.Context;
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
import ba.unsa.etf.rma.adapteri.AdapterIgre;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.klase.Pitanje;


public class PitanjeFrag extends Fragment {
    private TextView tekstPitanja;
    private ListView listaOdgovora;
    private Pitanje pitanje;
    private AdapterIgre adapterListe;
    private ArrayList<String> odgovori;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pitanje, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        tekstPitanja = (TextView) view.findViewById(R.id.tekstPitanja);
        listaOdgovora = (ListView) view.findViewById(R.id.odgovoriPitanja);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments().containsKey("pitanja")) {
            pitanje = (Pitanje) getArguments().getSerializable("pitanja");
            odgovori = pitanje.dajRandomOdgovore();
            tekstPitanja.setText(pitanje.getNaziv());
            adapterListe = new AdapterIgre(this.getActivity(), odgovori, pitanje.getTacan());
            listaOdgovora.setAdapter(adapterListe);
        }

        final IgrajKvizAkt aktivnost = (IgrajKvizAkt) getActivity();

        listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String odabrani = odgovori.get(position);
                boolean tacan = odabrani.equals(pitanje.getTacan());

                if (tacan) {
                    view.setBackgroundColor(getResources().getColor(R.color.zelena));
                } else {
                    view.setBackgroundColor(getResources().getColor(R.color.crvena));
                    listaOdgovora.getChildAt(pitanje.getIndeksTacnog()).setBackgroundColor(getResources().getColor(R.color.zelena));

                }

                listaOdgovora.setEnabled(false);
                aktivnost.onAnswerClick(tacan);
            }
        });
    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public interface OnItemClick {
        public void onAnswerClick(boolean tacan);
    }
}
