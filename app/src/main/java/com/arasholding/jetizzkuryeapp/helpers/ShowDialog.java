package com.arasholding.jetizzkuryeapp.helpers;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.arasholding.jetizzkuryeapp.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

public class ShowDialog {
    public static void setSnackBar(View root, String snackTitle) {
        Snackbar.make(root, snackTitle, Snackbar.LENGTH_LONG)
                .setAction("KAPAT", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setDuration(10000)
                .setActionTextColor(root.getResources().getColor(android.R.color.holo_orange_light ))
                .show();
    }
    public static Dialog CreateDialog(Context c,String message,String title) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage(message);
        builder.setTitle(title);

        builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

//        builder.setNegativeButton("Cancel", new   DialogInterface.OnClickListener(){
//            public void onClick(DialogInterface dialog, int id){
//
//            }
//        });
        AlertDialog dialog = builder.create();
        return builder.create();
    }

    public  void vibrate(Context context)
    {
        Vibrator v = (Vibrator )context.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
//            for (int i = 0; i < 3; i++) {
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    playBeepSound();
//
//                }
//
//            }, 1000);
//        }
            playBeepSound(context);
        } else {
            //deprecated in API 26
            v.vibrate(1000);
            playBeepSound(context);
        }
    }

    public MediaPlayer playBeepSound(Context context) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.stop();
                mp.release();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
//                Log.w(TAG, "Failed to beep " + what + ", " + extra);
                // possibly media player error, so release and recreate
                mp.stop();
                mp.release();
                return true;
            }
        });
        try {
            AssetFileDescriptor file = context.getResources().openRawResourceFd(R.raw.system_fault);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            } finally {
                file.close();
            }

            mediaPlayer.prepare();
            mediaPlayer.start();
            return mediaPlayer;
        } catch (IOException ioe) {
//            Log.w(TAG, ioe);
            mediaPlayer.release();
            return null;
        }
    }

}
