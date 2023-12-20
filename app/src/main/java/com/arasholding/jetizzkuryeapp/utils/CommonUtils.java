package com.arasholding.jetizzkuryeapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.arasholding.jetizzkuryeapp.ui.LoginActivity;

import dmax.dialog.SpotsDialog;

public class CommonUtils {
    private static  AlertDialog alertDialog;
    public static AlertDialog showLoadingDialogWithMessage(Context context, String message) {

        alertDialog = new SpotsDialog.Builder()
                .setContext(context)
                .setMessage(message)
                .setCancelable(false)
                .build();

        alertDialog.show();

        return alertDialog;
    }
    public void hideLoadingWithMessage() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    public static void showAlertWithMesseage(Context context,String messeage,String title){
        // Display dialog for user to connect, with intent to wifi settings
        AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(messeage);

        alertDialogBuilder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

//        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });

//        alertDialogBuilder.setNeutralButton("Mobile Network", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent mobileSettingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                mContext.startActivity(mobileSettingsIntent);
//                dialog.dismiss();
//            }
//        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void showTokenExpired(Context context,String messeage,String title){
        // Display dialog for user to connect, with intent to wifi settings
        AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(context)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(messeage);

        alertDialogBuilder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent= new Intent(context, LoginActivity.class);
                context.startActivity(intent);

                dialog.dismiss();
            }
        });

//        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });

//        alertDialogBuilder.setNeutralButton("Mobile Network", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent mobileSettingsIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                mContext.startActivity(mobileSettingsIntent);
//                dialog.dismiss();
//            }
//        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
