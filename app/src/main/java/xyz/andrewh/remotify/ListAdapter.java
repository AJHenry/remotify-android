package xyz.andrewh.remotify;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends ArrayAdapter<ApplicationModel> {

    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(List<ApplicationModel> items, Context context) {
        super(context, R.layout.application_list_view, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.application_list_view, null);
        }

        final ApplicationModel p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.application_name);
            TextView tt2 = (TextView) v.findViewById(R.id.application_type);
            Switch tt3 = (Switch) v.findViewById(R.id.switch_toggle);

            if (tt1 != null) {
                tt1.setText(p.getName());
            }

            if (tt2 != null) {
                tt2.setText(p.getPackageName());
            }

            if (tt3 != null) {
                tt3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        p.setChecked(isChecked);
                    }
                });
                tt3.setChecked(p.isChecked());

            }
        }

        return v;
    }

}