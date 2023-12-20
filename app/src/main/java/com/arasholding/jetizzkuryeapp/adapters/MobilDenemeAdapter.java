package com.arasholding.jetizzkuryeapp.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.MobilDenemeRequest;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;

import java.util.ArrayList;
import java.util.List;

public class MobilDenemeAdapter extends RecyclerView.Adapter<MobilDenemeViewHolder>{
    private List<MobilDenemeRequest> listBarcode = new ArrayList<>();
    private Context context;

    public MobilDenemeAdapter(List<MobilDenemeRequest> barcodeList, Context context) {
        this.listBarcode = barcodeList;
        this.context = context;
    }
    @NonNull
    @Override
    public MobilDenemeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.list_row, viewGroup, false);

        return new MobilDenemeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MobilDenemeViewHolder holder, int position) {
        holder.txtDeviceToken.setVisibility(View.VISIBLE);

        holder.txtBarkod.setText(Html.fromHtml("<font color='#D50000'>"+ String.valueOf(listBarcode.get(position).getBarkod())+" </font>" ));
        holder.txtDeviceToken.setText(Html.fromHtml("<b> Device Token: </b> " + String.valueOf(listBarcode.get(position).getDeviceToken())));
        holder.txtKonum.setText(Html.fromHtml("<b> Koordinat: </b> " + String.valueOf(listBarcode.get(position).getKonum())));
        holder.imgCount.setText(String.valueOf(position+1));
    }


    @Override
    public int getItemCount() { return listBarcode.size();}


}

