package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjaAdapter extends ArrayAdapter<Pitanje> {
    int plus;
    private Context mContext;
    private List<Pitanje> list = new ArrayList<>();

    public PitanjaAdapter(Context context, List<Pitanje> objects, int pl) {
        super(context, R.layout.list_rowcell, objects);
        mContext = context;
        list = objects;
        plus = pl;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_rowcell, parent, false);

        Pitanje current = list.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.discription);
        name.setText(current.getNaziv());

        ImageView slika = (ImageView) listItem.findViewById(R.id.slika);
        if (current.getNaziv().equals("Dodaj pitanje"))
            slika.setImageResource(R.drawable.icon_plus_color);
        else if (plus == 1) slika.setImageResource(R.drawable.icon_plus_color);
        else slika.setImageResource(R.drawable.icon_circle_color);

        return listItem;
    }
}
