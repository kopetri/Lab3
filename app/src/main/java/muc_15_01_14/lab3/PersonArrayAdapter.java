package muc_15_01_14.lab3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by David on 26.05.2015.
 */
public class PersonArrayAdapter extends ArrayAdapter<Person>{


    private List<Person> persons;

    public PersonArrayAdapter(Context context, int resource, List<Person> list) {
        super(context, resource, list);
        this.persons = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View itemView = convertView;
        if(itemView == null){
            Context ctx = parent.getContext();
            LayoutInflater inflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflator.inflate(R.layout.person_item, parent,false);
        }

        LinearLayout space = (LinearLayout) itemView.findViewById(R.id.color_bar);
        TextView text = (TextView) itemView.findViewById(R.id.txt_user);
        TextView time = (TextView) itemView.findViewById(R.id.txt_time);

        space.setBackgroundColor(persons.get(position).getColor());
        text.setText(persons.get(position).getUser());


        time.setText("Position uploaded " +  persons.get(position).getAge()/60 +" minutes ago");



        return itemView;
    }
}
