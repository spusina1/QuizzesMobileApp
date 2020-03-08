package ba.unsa.etf.rma.fragmenti;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.AdapterRangListe;
import ba.unsa.etf.rma.klase.Rang;


public class RangLista extends Fragment {

    private ListView rangLista;
    private ArrayList<Rang> rang = new ArrayList<>();
    private AdapterRangListe adapterRangListe;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_rang_lista, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        rangLista = (ListView) view.findViewById(R.id.rangLista);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments().containsKey("rang")) {
            rang = (ArrayList<Rang>) getArguments().getSerializable("rang");

            adapterRangListe = new AdapterRangListe(this.getActivity(), rang);
            rangLista.setAdapter(adapterRangListe);
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


}
