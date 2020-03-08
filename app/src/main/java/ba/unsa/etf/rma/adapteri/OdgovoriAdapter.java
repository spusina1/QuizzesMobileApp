package ba.unsa.etf.rma.adapteri;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;


public class OdgovoriAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> odgovori;
    private String tacanOdgovor;

    public OdgovoriAdapter(Activity context, ArrayList<String> odgovori, String tacanOdgovor) {
        super(context, R.layout.list_rowcell, odgovori);

        this.context = context;
        this.odgovori = odgovori;
        this.tacanOdgovor = tacanOdgovor;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_rowcell, null, true);

        TextView titleText = (TextView) rowView.findViewById(R.id.discription);

        titleText.setText(odgovori.get(position));
        if (odgovori.get(position).toString().equals(tacanOdgovor))
            rowView.setBackgroundColor(Color.parseColor("#CCFF90"));

        ImageView slika = (ImageView) rowView.findViewById(R.id.slika);
        slika.setImageResource(R.drawable.icon_circle_color);

        return rowView;

    }

    public void setTacanOdgovor(String tacanOdgovor) {
        this.tacanOdgovor = tacanOdgovor;
    }
}
