package com.arasholding.jetizzkuryeapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;
import com.arasholding.jetizzkuryeapp.ui.GelenGonderiDetayActivity;

import java.util.ArrayList;
import java.util.List;

public class GelenGonderiAdapter extends RecyclerView.Adapter<GelenGonderiViewHolder> {
    private List<ZimmetListModel> listBarcode = new ArrayList<>();
    private Context context;
    private String notBirakildi;

    public GelenGonderiAdapter(List<ZimmetListModel> barcodeList, Context context,String notBirakildi) {
        this.listBarcode = barcodeList;
        this.context = context;
        this.notBirakildi = notBirakildi;
    }
    @NonNull
    @Override
    public GelenGonderiViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.list_row, viewGroup, false);

        return new GelenGonderiViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GelenGonderiViewHolder barcodeViewHolder, int i) {
        barcodeViewHolder.txtAliciAdi.setText(Html.fromHtml("<font color='#D50000'>"+ String.valueOf(listBarcode.get(i).getAlici())+" </font>" ));
        barcodeViewHolder.txtTakipNo.setText(Html.fromHtml("<b> Takip No: </b> " + String.valueOf(listBarcode.get(i).getAtfNo())));
        barcodeViewHolder.txtAdres.setText(Html.fromHtml("<b> Adres: </b> " + String.valueOf(listBarcode.get(i).getGondericiAdres())));
        barcodeViewHolder.txtDesi.setText(Html.fromHtml("<b> Desi:</b> "+listBarcode.get(i).getDesi()));
        barcodeViewHolder.txtGonAdi.setText(Html.fromHtml("<b> Mağaza:</b> "+listBarcode.get(i).getMagaza()));
        barcodeViewHolder.txtZimmetAlan.setText( Html.fromHtml("<b> Zimmet Alan Kurye:</b> "+listBarcode.get(i).getKurye()));
        barcodeViewHolder.txtmTakipNo.setText(Html.fromHtml("<b> MTakipNo:</b> "+listBarcode.get(i).getMusteriTakipNo()));
        barcodeViewHolder.txtToplamParca.setText(Html.fromHtml("<b> Parça Sayısı:</b> " + listBarcode.get(i).getToplamParca()));
        barcodeViewHolder.imgCount.setText(String.valueOf(i+1));
        if(listBarcode.get(i).getMusteriTakipNo() != null){
            barcodeViewHolder.txtmTakipNo.setVisibility(View.VISIBLE);
            barcodeViewHolder.txtmTakipNo.setText(Html.fromHtml("<b> MTakipNo:</b> " + listBarcode.get(i).getMusteriTakipNo()));
        }

        barcodeViewHolder.txtHizmetNo.setText(Html.fromHtml("<b> Hizmet:</b> " + listBarcode.get(i).getHizmet()));
        if(listBarcode.get(i).getGondericiNotu() != null){
            barcodeViewHolder.txtGondericiNotu.setVisibility(View.VISIBLE);
            barcodeViewHolder.txtGondericiNotu.setText(Html.fromHtml("<b> Not:</b> " + listBarcode.get(i).getGondericiNotu()));
        }

        barcodeViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = barcodeViewHolder.getAdapterPosition();
                String tempAlici = listBarcode.get(position).getAlici();
                String tempAtfNo = listBarcode.get(position).getAtfNo();
                String tempGonderici = listBarcode.get(position).getMagaza();
                String tempGondericiAdres = listBarcode.get(position).getGondericiAdres();
                String tempMTakipNo = listBarcode.get(position).getMusteriTakipNo();
                String tempDeliveryEndDate = listBarcode.get(position).getOlasiTeslimTarihi();
                String tempAliciAdres = listBarcode.get(position).getAliciAdres();
                int tempAtfId = listBarcode.get(position).getAtfId();
                String tempAliciTelefon = listBarcode.get(position).getAliciTelefon();
                String tempGonderenTelefon = listBarcode.get(position).getGonderenTelefon();


                Intent i=new Intent(context, GelenGonderiDetayActivity.class);
                i.putExtra("tempAlici", tempAlici);
                i.putExtra("tempAtfNo", tempAtfNo);
                i.putExtra("tempGonderici", tempGonderici);
                i.putExtra("tempGondericiAdres", tempGondericiAdres);
                i.putExtra("tempAtfId", tempAtfId);
                i.putExtra("tempAliciTelefon", tempAliciTelefon);
                i.putExtra("tempGonderenTelefon", tempGonderenTelefon);
                i.putExtra("tempMTakipNo", tempMTakipNo);
                i.putExtra("tempAliciAdres", tempAliciAdres);
                i.putExtra("tempDeliveryEndDate", tempDeliveryEndDate);
                i.putExtra("tempNotBirakildi",notBirakildi);
                context.startActivity(i);
                ((Activity)context).finish();
            }
        });
    }

    @Override
    public int getItemCount() { return listBarcode.size();}
}
