package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Rang;

public class AdapterRangListe extends ArrayAdapter<Rang> {

    private Context mContext;
    private List<Rang> list = new ArrayList<>();


    public AdapterRangListe(Context context, List<Rang> objects) {
        super(context, R.layout.list_rowcell, objects);
        mContext = context;
        list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.rang_rowcell, parent, false);

        Rang current = list.get(position);

        TextView pozicija = (TextView) listItem.findViewById(R.id.pozicija);
        TextView imeIgraca = (TextView) listItem.findViewById(R.id.imeIgraca);
        TextView rezulatat = (TextView) listItem.findViewById(R.id.rezultat);

        pozicija.setText(String.valueOf(position + 1));
        imeIgraca.setText(current.getImePrezimeIgraca());
        rezulatat.setText(String.valueOf(current.getRezulata()));

        return listItem;
    }
}
