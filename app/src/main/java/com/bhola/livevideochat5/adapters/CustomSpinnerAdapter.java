package com.bhola.livevideochat5.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bhola.livevideochat5.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> values;

    public CustomSpinnerAdapter(@NonNull Context context, List<String> values) {
        super(context, R.layout.spinner_item_layout, values);
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_item_layout);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.spinner_dropdown_item);
    }

    private View createViewFromResource(int position, @Nullable View convertView, @NonNull ViewGroup parent, int layoutId) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(layoutId, parent, false);
        }

        TextView text = view.findViewById(R.id.text1);
        text.setText(values.get(position));

        return view;
    }
}