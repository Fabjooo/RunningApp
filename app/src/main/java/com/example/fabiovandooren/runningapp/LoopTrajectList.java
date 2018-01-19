package com.example.fabiovandooren.runningapp;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by fabiovandooren on 19/10/17.
 */

public class LoopTrajectList extends ArrayAdapter<LoopTraject> {

    private Activity context;
    private List<LoopTraject> loopTrajectList;

    public LoopTrajectList(Activity context, List<LoopTraject> loopTrajectList){
        super(context,R.layout.looptrajecten_list, loopTrajectList);
        this.context = context;
        this.loopTrajectList = loopTrajectList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.looptrajecten_list, null, true);

        TextView textViewDatum = (TextView) listViewItem.findViewById(R.id.textViewDatum);
        TextView textViewKms = (TextView) listViewItem.findViewById(R.id.textViewKms);
        TextView textViewID = (TextView) listViewItem.findViewById(R.id.textViewID);

        LoopTraject loopTraject = loopTrajectList.get(position);

        textViewDatum.setText(loopTraject.getLoopTrajectDatum());
        textViewKms.setText(loopTraject.getLoopTrajectKms());
        textViewID.setText(loopTraject.getLoopTrajectId());

        return listViewItem;

    }
}
