package com.arasholding.jetizzkuryeapp.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arasholding.jetizzkuryeapp.R;

public class MobilDenemeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final Context context;
    TextView txtBarkod;
    TextView txtKonum;
    TextView txtDeviceToken;
    TextView imgCount;


    public MobilDenemeViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        itemView.setClickable(true);
        itemView.setOnClickListener(this);
        txtBarkod = (TextView) itemView.findViewById(R.id.txtAliciAdi);
        txtKonum = (TextView) itemView.findViewById(R.id.txtTakipNo);
        txtDeviceToken = (TextView) itemView.findViewById(R.id.txtmTakipNo);
        imgCount = (TextView) itemView.findViewById(R.id.imgCount);
    }

    @Override
    public void onClick(View view) {

    }
}
