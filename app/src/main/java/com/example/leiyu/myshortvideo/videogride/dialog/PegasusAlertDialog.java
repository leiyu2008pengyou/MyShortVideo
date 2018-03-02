package com.example.leiyu.myshortvideo.videogride.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.leiyu.myshortvideo.R;

/**
 * Created by leiyu on 2018/3/2.
 */

public class PegasusAlertDialog {
    public static void ShowAlertDialog(Context context,
                                       String message) {
        ShowAlertDialog(context, message, "OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        }, "", null);
    }

    public static void ShowAlertDialog(Context context,
                                       String message, String Propmt,
                                       DialogInterface.OnClickListener listener) {

        ShowAlertDialog(context, message, Propmt, listener, "", null);

    }

    public static void ShowAlertDialog(Context context,
                                       String message, String Positive,
                                       DialogInterface.OnClickListener positivelistener, String Negative,
                                       DialogInterface.OnClickListener negativelistener) {
        ContextThemeWrapper themedContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        } else {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Light_NoTitleBar);
        }

        View mView = LayoutInflater.from(context).inflate(R.layout.lf_alert_dialog_view, null);
        TextView messageText = (TextView) mView.findViewById(R.id.alertdialog_message);
        messageText.setText(message);

        AlertDialog.Builder alertdlg = new AlertDialog.Builder(themedContext);
        alertdlg.setView(mView);
        if (!Positive.equals("") && positivelistener != null)
            alertdlg.setPositiveButton(Positive, positivelistener);
        if (!Negative.equals("") && negativelistener != null)
            alertdlg.setNegativeButton(Negative, negativelistener);

        alertdlg.show();
    }

    public static void ShowAlertDialog(Context context,
                                       String message,
                                       String Positive,
                                       DialogInterface.OnClickListener positivelistener,
                                       String Negative,
                                       DialogInterface.OnClickListener negativelistener,
                                       DialogInterface.OnCancelListener cancelListener,
                                       DialogInterface.OnDismissListener dismissListener) {
        ContextThemeWrapper themedContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        } else {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Light_NoTitleBar);
        }

        View mView = LayoutInflater.from(context).inflate(R.layout.lf_alert_dialog_view, null);
        TextView messageText = (TextView) mView.findViewById(R.id.alertdialog_message);
        messageText.setText(message);

        AlertDialog alertdlg = new AlertDialog.Builder(themedContext).create();
        alertdlg.setView(mView);
        if (!Positive.equals("") && positivelistener != null)
            alertdlg.setButton(DialogInterface.BUTTON_POSITIVE, Positive, positivelistener);
        if (!Negative.equals("") && negativelistener != null)
            alertdlg.setButton(DialogInterface.BUTTON_NEGATIVE, Negative, negativelistener);
        alertdlg.setOnCancelListener(cancelListener);
        alertdlg.setOnDismissListener(dismissListener);
        alertdlg.show();
    }
}
