package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconView;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class GridAdapter extends BaseAdapter {

    private Context ccontext;
    private List<Kviz> list = new ArrayList<>();
    private Kviz dodjKviz = new Kviz("Dodaj Kviz", null, new Kategorija("Svi", "671"));

    public GridAdapter(Context context, List<Kviz> objects) {
        this.ccontext = context;
        this.list = objects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(ccontext).inflate(R.layout.grid_item, parent, false);

        Kviz current = list.get(position);

        TextView naziv = (TextView) convertView.findViewById(R.id.naziv);
        TextView brPitanja = (TextView) convertView.findViewById(R.id.brPitanja);

        naziv.setText(current.getNaziv());

        if (!current.equals(dodjKviz)) {
            if (current.getPitanja() != null) {
                String pitanja = String.valueOf(current.getPitanja().size());
                brPitanja.setText(pitanja);
            }
        }

        IconView ikona = (IconView) convertView.findViewById(R.id.icon);
        ImageView slika = (ImageView) convertView.findViewById(R.id.slika);

        String id = current.getKategorija().getId();
        if (current.equals(dodjKviz)) {
            ikona.setIcon(-1);
            slika.setImageResource(R.drawable.icon_plus_color);
        } else {
            slika.setImageDrawable(null);
            ikona.setIcon(Integer.parseInt(id));
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
