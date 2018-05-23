package in.bhumiputra.nakshatra;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

/**
 * Created by damodarreddy on 3/5/18.
 */

public class RateDialog extends DialogFragment {

    public static final String rated = "rated";
    public static final String askNext = "askNext";
    public static final String dontAsk = "dontAsk";


    public Dialog onCreateDialog (Bundle savedInstanceState) {

        final SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String talk= getString(R.string.rate_me_dialog);
        builder.setMessage(talk);
        builder.setPositiveButton(R.string.rate_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=in.bhumiputra.nakshatra"));
                if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
                else {
                    String url= "http://play.google.com/store/apps/details?id=in.bhumiputra.nakshatra";
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
                pref.edit().putString(PreferanceCentral.pref_rate_status, rated).apply();
            }
        });

        builder.setNegativeButton(R.string.dont_ask_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pref.edit().putString(PreferanceCentral.pref_rate_status, dontAsk).apply();
                dismiss();
            }
        });

        builder.setNeutralButton(R.string.latter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pref.edit().putString(PreferanceCentral.pref_rate_status, askNext).putInt(PreferanceCentral.pref_rate_dialog_period, 0).apply();
                dismiss();
            }
        });

        return builder.create();
    }
}
