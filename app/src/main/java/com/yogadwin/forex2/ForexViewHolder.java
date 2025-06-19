package com.yogadwin.forex2;

import android.widget.TextView;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class ForexViewHolder extends RecyclerView.ViewHolder {
    TextView kodeTextView, kursTextView, namaTextView;

    public ForexViewHolder(View itemView) {
        super(itemView);

        kodeTextView = itemView.findViewById(R.id.kodeTextView);
        namaTextView = itemView.findViewById(R.id.namaTextView);
        kursTextView = itemView.findViewById(R.id.kursTextView);
    }
}
