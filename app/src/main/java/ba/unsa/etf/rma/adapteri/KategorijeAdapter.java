package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconView;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;

public class KategorijeAdapter extends ArrayAdapter<Kategorija> {
    private Context mContext;
    private List<Kategorija> list = new ArrayList<>();


    public KategorijeAdapter(Context context, List<Kategorija> objects) {
        super(context, R.layout.list_rowcell, objects);
        mContext = context;
        list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_rowcell, parent, false);

        Kategorija current = list.get(position);

        TextView name = (TextView) listItem.findViewById(R.id.discription);

        name.setText(current.getNaziv());

        IconView ikona = (IconView) listItem.findViewById(R.id.icon);
        ImageView slika = (ImageView) listItem.findViewById(R.id.slika);

        String id = current.getId();


        slika.setImageDrawable(null);
        ikona.setIcon(Integer.parseInt(id));


        return listItem;
    }
}