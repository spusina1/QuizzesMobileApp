package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;

public class MySpinnerAdapter extends ArrayAdapter<Kategorija> {

    int resource;
    private Context context;
    private ArrayList<Kategorija> list = new ArrayList<>();

    public MySpinnerAdapter(Context context, int _resource, ArrayList<Kategorija> kategorije) {
        super(context, _resource, kategorije);
        this.context = context;
        list = kategorije;
        resource = _resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.spinner, parent, false);

        }

        TextView name = (TextView) listItem.findViewById(R.id.title);

        if (position == -1) name.setText(R.string.spiner);
        else name.setText(list.get(position).getNaziv());

        return listItem;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;
        view = View.inflate(context, R.layout.spinner_dropdown, null);
        final TextView textView = (TextView) view.findViewById(R.id.spinnertitle);
        textView.setText(list.get(position).getNaziv());
        return view;
    }
}
