package com.arasholding.jetizzkuryeapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.ZimmetListModel;
import com.arasholding.jetizzkuryeapp.ui.CameraBarcodeReaderActivity;
import com.arasholding.jetizzkuryeapp.ui.DeliveryActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ZimmetAdapter extends RecyclerView.Adapter<ZimmetViewHolder> implements Filterable {
    private List<ZimmetListModel> listBarcode = new ArrayList<>();
    private List<ZimmetListModel> filteredData  = new ArrayList<>();
    private Context context;
    private ItemFilter mFilter = new ItemFilter();

    public ZimmetAdapter(List<ZimmetListModel> barcodeList, Context context) {
        this.listBarcode = barcodeList;
        this.filteredData  = filteredData;
        this.context = context;
    }

    @NonNull
    @Override
    public ZimmetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.list_row, viewGroup, false);

        return new ZimmetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ZimmetViewHolder barcodeViewHolder, int i) {
        barcodeViewHolder.txtAliciAdi.setText(Html.fromHtml("<font color='#D50000'>" + String.valueOf(listBarcode.get(i).getAlici()) + " </font>"));
        barcodeViewHolder.txtTakipNo.setText(Html.fromHtml("<b> Takip No: </b> " + String.valueOf(listBarcode.get(i).getAtfNo())));
        barcodeViewHolder.txtAdres.setText(Html.fromHtml("<b> Adres: </b> " + String.valueOf(listBarcode.get(i).getAliciAdres())));
        barcodeViewHolder.txtDesi.setText(Html.fromHtml("<b> Desi:</b> " + listBarcode.get(i).getDesi()));
        barcodeViewHolder.txtGonAdi.setText(Html.fromHtml("<b> Mağaza:</b> " + listBarcode.get(i).getMagaza()));
        barcodeViewHolder.txtZimmetAlan.setText(Html.fromHtml("<b> Zimmet Alan Kurye:</b> " + listBarcode.get(i).getKurye()));
        barcodeViewHolder.txtToplamParca.setText(Html.fromHtml("<b> Parça Sayısı:</b> " + listBarcode.get(i).getToplamParca()));
        barcodeViewHolder.imgCount.setText(String.valueOf(i+1));
        if (listBarcode.get(i).getMusteriTakipNo() != null) {
            barcodeViewHolder.txtmTakipNo.setVisibility(View.VISIBLE);
            barcodeViewHolder.txtmTakipNo.setText(Html.fromHtml("<b> MTakipNo:</b> " + listBarcode.get(i).getMusteriTakipNo()));
        }

        barcodeViewHolder.txtHizmetNo.setText(Html.fromHtml("<b> Hizmet:</b> " + listBarcode.get(i).getHizmet()));
        if (listBarcode.get(i).getGondericiNotu() != null) {
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
                String tempHizmet = listBarcode.get(position).getHizmet();
                String tempZimmetAlan = listBarcode.get(position).getKurye();
                String tempDesi = listBarcode.get(position).getDesi();
                String tempSonIslem = listBarcode.get(position).getSonIslem();
                int tempAtfId = listBarcode.get(position).getAtfId();
                int tempMusteriId = listBarcode.get(position).getMusteriId();
                String tempAliciTelefon = listBarcode.get(position).getAliciTelefon();
                String tempTahTip = listBarcode.get(position).getTahsilatTipi();
                Double tempTahTur = listBarcode.get(position).getTahsilatTutari();

                Intent i = new Intent(context, DeliveryActivity.class);
                i.putExtra("tempAlici", tempAlici);
                i.putExtra("tempAtfNo", tempAtfNo);
                i.putExtra("tempGonderici", tempGonderici);
                i.putExtra("tempHizmet", tempHizmet);
                i.putExtra("tempZimmetAlan", tempZimmetAlan);
                i.putExtra("tempDesi", tempDesi);
                i.putExtra("tempSonIslem", tempSonIslem);
                i.putExtra("tempAtfId", tempAtfId);
                i.putExtra("tempMusteriId", tempMusteriId);
                i.putExtra("tempAliciTelefon", tempAliciTelefon);
                i.putExtra("tempTahTip", tempTahTip);
                i.putExtra("tempTahTur", tempTahTur);
                context.startActivity(i);

            }
        });
    }

    @Override
    public int getItemCount() {
        return listBarcode.size();
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<ZimmetListModel> list = listBarcode;

            int count = list.size();
            final ArrayList<ZimmetListModel> nlist = new ArrayList<ZimmetListModel>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getAlici();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(list.get(i));
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<ZimmetListModel>) results.values;
            listBarcode = filteredData;
            notifyDataSetChanged();
        }
//        public void updateReceiptsList(List<ZimmetListModel> newlist) {
//            listBarcode = newlist;
//            this.no();
//        }
    }
}
