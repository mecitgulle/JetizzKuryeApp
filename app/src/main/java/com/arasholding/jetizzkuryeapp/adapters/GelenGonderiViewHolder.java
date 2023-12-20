package com.arasholding.jetizzkuryeapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arasholding.jetizzkuryeapp.R;

public class GelenGonderiViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final Context context;
    TextView txtAliciAdi;
    TextView txtTakipNo;
    TextView txtmTakipNo;
    TextView txtZimmetAlan;
    TextView txtAdres;
    TextView txtHizmetNo;
    TextView txtGonAdi;
    TextView txtDesi;
    TextView txtSayi;
    TextView txtGondericiNotu;
    TextView txtToplamParca;
    TextView imgCount;

    public GelenGonderiViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        itemView.setClickable(true);
        itemView.setOnClickListener(this);
        txtAliciAdi = (TextView) itemView.findViewById(R.id.txtAliciAdi);
        txtTakipNo = (TextView) itemView.findViewById(R.id.txtTakipNo);
        txtmTakipNo = (TextView) itemView.findViewById(R.id.txtmTakipNo);
        txtZimmetAlan=(TextView)itemView.findViewById(R.id.txtZimmetAlan);
        txtAdres=(TextView)itemView.findViewById(R.id.txtAdres);
        txtHizmetNo = (TextView)itemView.findViewById(R.id.txtHizmetNo);
        txtGonAdi =(TextView)itemView.findViewById(R.id.txtGonAdi);
        txtDesi =(TextView) itemView.findViewById(R.id.txtDesi);
        txtGondericiNotu = (TextView) itemView.findViewById(R.id.txtNot);
        txtToplamParca = (TextView) itemView.findViewById(R.id.txtToplamParca);
        imgCount = (TextView) itemView.findViewById(R.id.imgCount);
//        txtSayi = (TextView) itemView.findViewById(R.id.txtsay);
    }

    @Override
    public void onClick(View v) {
        Log.e("tıklandı",String.valueOf(getAdapterPosition()));
    }
}