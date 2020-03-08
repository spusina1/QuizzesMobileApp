package ba.unsa.etf.rma.fragmenti;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ba.unsa.etf.rma.R;

public class InformacijeFrag extends Fragment {


    private TextView nazivKviza;
    private TextView brojTacnihPitanja;
    private TextView brojPreostalihPitanja;
    private TextView procenatTacnih;
    private Button kraj;

    private String nazivK = "";
    private int brojTacnih = 0;
    private int brojPreostalih = 0;
    private double procenat;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_informacije, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        nazivKviza = (TextView) view.findViewById(R.id.infNazivKviza);
        brojTacnihPitanja = (TextView) view.findViewById(R.id.infBrojTacnihPitanja);
        brojPreostalihPitanja = (TextView) view.findViewById(R.id.infBrojPreostalihPitanja);
        procenatTacnih = (TextView) view.findViewById(R.id.infProcenatTacnih);
        kraj = (Button) view.findViewById(R.id.btnKraj);

        nazivK = "";
        brojTacnih = 0;
        brojPreostalih = 0;
        procenat = 0;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (getArguments().containsKey("naziv")) {
            nazivK = getArguments().getString("naziv");
            nazivKviza.setText(nazivK);
        }

        if (getArguments().containsKey("brTacnih")) {
            brojTacnih = getArguments().getInt("brTacnih");
            brojTacnihPitanja.setText(String.valueOf(brojTacnih));

        }

        if (getArguments().containsKey("brPreostalih")) {
            brojPreostalih = getArguments().getInt("brPreostalih");
            brojPreostalihPitanja.setText(String.valueOf(brojPreostalih));
        }
        if (getArguments().containsKey("procenat")) {
            procenat = getArguments().getDouble("procenat");
            procenatTacnih.setText(String.format("%.2f", procenat));
        }

        kraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
