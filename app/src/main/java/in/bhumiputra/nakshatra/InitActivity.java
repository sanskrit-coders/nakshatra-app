package in.bhumiputra.nakshatra;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static in.bhumiputra.nakshatra.NighantuProvider.AKARADISTR;

public class InitActivity extends AppCompatActivity {
    static final String[] PERMISSIONS = {WRITE_EXTERNAL_STORAGE};
    static final int REQ_CODE1= 123;
    private static final String tag= "modalu";
    private Uri homeUri = Uri.parse("file:///android_asset/Sahayam.html");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferances, false);

        Intent intent= getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(action.equals(Intent.ACTION_SEND) && (type!= null)) {
            if(type.equals("text/plain")) {
                String tPadam= intent.getStringExtra(Intent.EXTRA_TEXT);
                if(tPadam!= null) {
                    this.homeUri = Uri.parse(NighantuProvider.AKARADISTR+ "/nighantu/"+ tPadam);
                }
            }
        }

        step1Permissions();
        //setContentView(R.layout.activity_modalu);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void step1Permissions() {
        boolean sare= hasPermissions(PERMISSIONS);
        if(sare) {
            Log.i(tag, "permissions granted");
            step2Indices();
            return;
        }

        else {
            Log.i(tag, "permissions not avialable.");
            step1_1AskPermissions();
            return;
        }
    }

    public boolean hasPermissions(String[] kAnumatulu) { //kAnumatulu: kavalsina anumatulu.
        boolean sare= true;
        for(String kAnumati: kAnumatulu) {
            if(!hasPermission(kAnumati)) {
                Log.i(tag, kAnumati+ " permission not avialable");
                sare= false;
                break;
            }
        }
        return sare;
    }

    public boolean hasPermission(String anumati) {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            if (checkSelfPermission(anumati)== PackageManager.PERMISSION_GRANTED) {
                //Log.i(tag, anumati +" permission granted");
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }


    public void step1_1AskPermissions() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            Log.i(tag, "asking permissions");
            requestPermissions(netPermissions(PERMISSIONS), REQ_CODE1);
            return;
        }
    }

    public String[] netPermissions(String[] allPermissions) {
        ArrayList<String> sPermissions= new ArrayList<>();
        for(String permission: allPermissions) {
            if(!hasPermission(permission)) {
                sPermissions.add(permission);
            }
        }
        return (sPermissions.toArray(new String[sPermissions.size()]));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        //Log.i(tag, "entered onRequestPermissionResult()");
        switch(requestCode) {

            case REQ_CODE1: {
                boolean sare = true;
                if ((results == null) || (results.length == 0)) {
                    sare = false;
                }
                else {
                    for(int i : results) {
                        if(i== PackageManager.PERMISSION_DENIED) {
                            sare = false;
                            break;
                        }
                    }
                }

                if(sare) {
                    //Log.i(tag, "anumatulu labhinchinavi: onRequestPermissionResult()");
                    step2Indices();
                }
                else {
                    //finish();
                }
                break;
            }

        }

    }


    public void step2Indices() {
        startActivityForResult(
                new Intent(Intent.ACTION_DEFAULT, null, this.getApplicationContext(), IndexerActivity.class ),
                125
        );
        overridePendingTransition(0, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode== 125) {
            if(resultCode== IndexerActivity.RESULT_CODE_NO_DICTIONARIES) {
                this.finish();
                return;
            }
            lastStep();
        }
    }

    public void lastStep() {
        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferanceCentral.pref_aw_first_time, true)) {
            Log.i(tag, "app widget first time setup: true.");
            AlarmManager am= (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent kottaRpI= new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, Uri.parse("RPRA"), this.getApplicationContext(), WordOfTheDay.class);
            kottaRpI.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {0, 1});
            PendingIntent kottaRpPI= PendingIntent.getBroadcast(this.getApplicationContext(), 1 , kottaRpI, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 5);
            long nundi= (long) (calendar.getTimeInMillis());
            try {
                am.setRepeating(AlarmManager.RTC, nundi, AlarmManager.INTERVAL_DAY, kottaRpPI);
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PreferanceCentral.pref_aw_first_time, false).apply();
            }
            catch(NullPointerException npe) {
                Log.e(tag, "error in setting repeating alarm.", npe);
            }
        }
        else {
            //Log.i(tag, "pp widget first time setup: false.");
        }
        //getContentResolver().update(Uri.parse(AKARADISTR+ "/update/db/"), null, null, null);
        Intent intent= new Intent(Intent.ACTION_SEARCH, homeUri,this, NakshatraActivity.class);
        //intent.putExtra(SearchManager.QUERY, padam);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }



}
