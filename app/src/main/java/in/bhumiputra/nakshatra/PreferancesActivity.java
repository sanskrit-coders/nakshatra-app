package in.bhumiputra.nakshatra;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

import static in.bhumiputra.nakshatra.IndexerActivity.ACTION_REINDEX;
import static in.bhumiputra.nakshatra.NighantuProvider.AKARADISTR;

public class PreferancesActivity extends AppCompatActivity {

    //talalu...
    public static final String ClV = "classic";
    public static final String LIV = "light";
    public static final String DAV = "dark";

    //perlu...
    public static final String CLT = "Classic";
    public static final String LIT = "Light";
    public static final String DAT = "Dark";

    static final String[] pradarshana_viluvalu= new String[] {ClV, LIV, DAV};
    static final String[] pradarshana_perlu= new String[] {CLT, LIT, DAT};


    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleDisplay(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferances);

        Toolbar actionBarToolBar= findViewById(R.id.pref_actionbar);
        setSupportActionBar(actionBarToolBar);
        actionBar= getSupportActionBar();
        initActionBar();

        if (getFragmentManager().findFragmentById(R.id.pref_pref_box)==null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.pref_pref_box, new PrefFragment())
                    .commit();
        }
    }

    static void handleDisplay(Activity activity) {
        String pradarshana= PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferanceCentral.pref_theme, ClV);
        if(pradarshana.equalsIgnoreCase(ClV)) {
            activity.setTheme(R.style.AppTheme_Classic);
            //activity.getContentResolver().update(Uri.parse(AKARADISTR+ "/marchu/shaili/"+ Shaili.CLASSIC), null, null, null);
        }
        else if(pradarshana.equalsIgnoreCase(LIV)) {
            activity.setTheme(R.style.AppTheme_Light);
            //activity.getContentResolver().update(Uri.parse(AKARADISTR+ "/marchu/shaili/"+ Shaili.LIGHT), null, null, null);
        }
        else if(pradarshana.equalsIgnoreCase(DAV)) {
            activity.setTheme(R.style.AppTheme_Dark);
            //activity.getContentResolver().update(Uri.parse(AKARADISTR+ "/marchu/shaili/"+ Shaili.DARK), null, null, null);
        }
    }

    void initActionBar() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(R.drawable.preferances);
    }



    public static class PrefFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            addPreferencesFromResource(R.xml.preferances);
        }

        @Override
        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);
            setReindexIntent(view);
            handleThemeSummary();
            handleModeSummary();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        void setReindexIntent(View v) {
            Preference rip= getPreferenceScreen().findPreference(PreferanceCentral.pref_reindex_all_dictionaries);
            rip.setIntent(new Intent(ACTION_REINDEX, null, getActivity(), IndexerActivity.class));
        }

        void handleThemeSummary() {
            String pradarshita= getPreferenceScreen().getSharedPreferences().getString(PreferanceCentral.pref_theme, ClV);
            ArrayList<String> vj= new ArrayList<>();
            Collections.addAll(vj, pradarshana_viluvalu);
            int va= vj.indexOf(pradarshita);
            String pradarshita_peru= pradarshana_perlu[va];
            Preference pr= getPreferenceScreen().findPreference(PreferanceCentral.pref_theme);
            pr.setSummary(getString(R.string.pref_theme__summary_part)+ ": "+ pradarshita_peru);
        }

        void handleModeSummary() {
            int vidham= Integer.valueOf(getPreferenceScreen().getSharedPreferences().getString(PreferanceCentral.pref_indices_loading_mode, "1"));
            Preference pr= getPreferenceScreen().findPreference(PreferanceCentral.pref_indices_loading_mode);
            pr.setSummary(getString(R.string.pref_indices_loading_mode__summary)+ vidham);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equalsIgnoreCase(PreferanceCentral.pref_theme)) {
                handleThemeSummary();
                getActivity().recreate();
                //Toast.makeText(getActivity(), getString(R.string.pradarshana_marchu_gamanika), Toast.LENGTH_SHORT).show();
            }
            else if(key.equalsIgnoreCase(PreferanceCentral.pref_indices_loading_mode)) {
                handleModeSummary();
                getActivity().getContentResolver().update(Uri.parse(AKARADISTR+ "/update/mode/"), null, null, null);
            }
        }
    }
}
