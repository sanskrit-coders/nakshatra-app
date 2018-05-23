package in.bhumiputra.nakshatra;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

/**
 * Created by damodarreddy on 1/22/18.
 */

public class WordOfTheDay extends AppWidgetProvider {

    private final String tag= "WordOfTheDay";
    private Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent= intent;
        Log.i(tag, "intent vishaya: "+ intent.getDataString());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context);
            if(!pref.getBoolean(PreferanceCentral.pref_aw_first_time, true)) {
                AlarmManager am= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent kottaRpI= new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, Uri.parse("RPRA"), context, WordOfTheDay.class);
                kottaRpI.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {0, 1});
                PendingIntent kottaRpPI= PendingIntent.getBroadcast(context, 1 , kottaRpI, PendingIntent.FLAG_UPDATE_CURRENT);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 5);
                long nundi= (long) (calendar.getTimeInMillis());
                try {
                    am.setRepeating(AlarmManager.RTC, nundi, AlarmManager.INTERVAL_DAY, kottaRpPI);
                }
                catch(NullPointerException npe) {
                    Log.e(tag, "error in setting repeating alarm.", npe);
                }
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] ids) {
        //Log.i(tag, "onUpdate() called.");
        ComponentName nenu= new ComponentName(context, WordOfTheDay.class);
        appWidgetManager.updateAppWidget(nenu, newWODWidget(context, ids));
    }

    public RemoteViews newWODWidget(Context context, int[] ids) {
        RemoteViews newRV= new RemoteViews(context.getPackageName(), R.layout.wod);

        String rp= "";
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        int prev_year= pref.getInt(PreferanceCentral.pref_wod_time_year, 1997);
        int year= prev_year;
        int prev_date= pref.getInt(PreferanceCentral.pref_wod_time_date, 0);
        int date= prev_date;

        boolean shouldUpdate= false;
        if(intent!= null) {
            String vishayam= intent.getDataString();

            Calendar calendar= Calendar.getInstance();
            date= calendar.get(Calendar.DAY_OF_YEAR); //kotta tedi ki navikarana.
            year= calendar.get(Calendar.YEAR);

            //Log.i(tag, "vishayam: "+ vishayam);
            if(vishayam != null && (vishayam.equalsIgnoreCase("RPM"))) {
                shouldUpdate= true;
            }
            else if(vishayam != null && (vishayam.equalsIgnoreCase("RPRA"))) {
                if((date> prev_date) || (year> prev_year)) {
                    shouldUpdate= true;
                }
            }
        }

        if(shouldUpdate) {

            boolean indexSuccess= true;

            if (indexSuccess) {
                rp = context.getContentResolver().insert(Uri.parse(NighantuProvider.AKARADISTR + "/response/random/en"), null).toString(); //rpt: rojuko padam-anglam.
            } else { //toBeTested...
                rp = "____";
            }

            pref.edit().putString(PreferanceCentral.pref_word_of_the_day, rp)
                    .putInt(PreferanceCentral.pref_wod_time_date, date).putInt(PreferanceCentral.pref_wod_time_year, year)
                    .apply();
        }

        else {
            rp= pref.getString(PreferanceCentral.pref_word_of_the_day, "___");
        }

        newRV.setTextViewText(R.id.wod_word, rp);

        Intent rpI= new Intent(Intent.ACTION_SEND, Uri.parse(NighantuProvider.AKARADISTR+ "/nighantu/"+ Uri.encode(rp)), context, InitActivity.class);
        rpI.putExtra(Intent.EXTRA_TEXT, rp).setType("text/plain");

        Intent updateRpI= new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, Uri.parse("RPM"), context, WordOfTheDay.class);
        updateRpI.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        PendingIntent rpPI= PendingIntent.getActivity(context, 1, rpI, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent kottaRpPI= PendingIntent.getBroadcast(context, 0 , updateRpI, PendingIntent.FLAG_UPDATE_CURRENT);

        newRV.setOnClickPendingIntent(R.id.wod_box, rpPI);
        newRV.setOnClickPendingIntent(R.id.wod_update, kottaRpPI);

        return newRV;
    }

    @Override
    public void onEnabled(Context context) {
        //Log.i(tag, "onEnabled() called.");
    }

    @Override
    public void onDisabled(Context context) {
        //Log.i(tag, "onDisabled() called.");
    }

}
