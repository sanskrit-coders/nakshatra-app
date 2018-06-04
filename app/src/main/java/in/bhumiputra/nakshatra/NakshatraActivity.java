package in.bhumiputra.nakshatra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;

import in.bhumiputra.nakshatra.nighantu.Style;
import in.bhumiputra.nakshatra.nighantu.anga.Bhasha;
import in.bhumiputra.nakshatra.nighantu.anga.STuple;

import static android.app.SearchManager.SUGGEST_COLUMN_TEXT_1;
import static in.bhumiputra.nakshatra.NighantuProvider.AKARADISTR;
import static in.bhumiputra.nakshatra.PreferancesActivity.ClV;
import static in.bhumiputra.nakshatra.PreferancesActivity.DAV;
import static in.bhumiputra.nakshatra.PreferancesActivity.LIV;

public class NakshatraActivity extends AppCompatActivity {

    private String tag= "Nakshatra";

    DrawerLayout navigationD;
    LinearLayout navigationL;
    LinearLayout treeL;

    //ActionBarDrawerToggle abdt;

    ListView tree;
    TreeAdapter treeAdapter;

    RelativeLayout descrBlock;
    RelativeLayout descrNavigation;
    ObjectAnimator animator;
    JaalaView descrView;
    AutoCompleteTextView suggestionsView;
    SearchView searchView;
    int svWidth;
    MenuItem menu_lm;

    final boolean transliterate = false;
    boolean isTtsOn= false;
    boolean shouldShowSuggestions = true;

    HashSet<String> bookmarks;
    BHTask bookmarksTask;
    BHTask historyTask;

    HashSet<String> session_history; //this session only recorded history.
    STuple<LinkedHashMap<String, ArrayList<STuple<String>>>> domTree;
    STuple<ArrayList<String>> dictionaryNames;

    TextToSpeech tts;

    boolean pref_auto_toggle_transliterate = true;
    boolean pref_prev_next = true;
    String pref_display = PreferancesActivity.ClV;

    /*
    methods dealing with initialisation...
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleDisplay();
        super.onCreate(savedInstanceState);
        //khatiMarchu();
        setContentView(R.layout.activity_nakshatra_navigation);

        tts= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                NakshatraActivity.this.onTtsInit(status);
            }
        });

        navigationD = findViewById(R.id.nakshatra_navigation_drawer);
        navigationL = navigationD.findViewById(R.id.navigation_drawer_left);
        treeL = navigationD.findViewById(R.id.dom_tree_outer_box);

        Toolbar actionBarToolBar= findViewById(R.id.actionBar);
        initToolBar(actionBarToolBar);

        //initActionBarDrawerToggle();

        descrView = findViewById(R.id.descr);
        WebBackForwardList mvjb= descrView.restoreState(savedInstanceState); //NOTE...
        initJaalaView(descrView);
        dumpBookmarks();
        session_history = new HashSet<>();

        tree = (ListView) findViewById(R.id.dom_tree);
        initTree();

        descrBlock = (RelativeLayout) findViewById(R.id.descr_box);
        initDescrNavigation();

        initPreferances(); //NOTE!!!

        Intent intent = getIntent();
        String action= intent.getAction();
        String type= intent.getType();
        if (action.equals(Intent.ACTION_SEARCH) && (mvjb== null)) {
            Uri uri= intent.getData();
            if(uri!= null) {
                String url= uri.toString();
                if(url.startsWith(AKARADISTR+ "/nighantu/")) {
                    descrView.loadUrl(url);
                }
                else {
                    descrView.loadUrl(url); //NOTE...
                }
            }
            else {
                String padam = intent.getStringExtra(SearchManager.QUERY);
                search(padam);
            }
        }

        showHelpDialog();
        showRateDialog();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //abdt.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration nConfig) {
        super.onConfigurationChanged(nConfig);
        DisplayMetrics dm= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        svWidth= dm.widthPixels;

        if(searchView != null) {
            searchView.setMaxWidth((int) Math.floor(svWidth * 0.78));
        }

        //abdt.onConfigurationChanged(nConfig);
    }

    void handleDisplay() {
        String theme= PreferenceManager.getDefaultSharedPreferences(this).getString(PreferanceCentral.pref_theme, ClV);
        pref_display = theme;
        if(theme.equalsIgnoreCase(ClV)) {
            setTheme(R.style.AppTheme_Classic);
            getContentResolver().update(Uri.parse(AKARADISTR+ "/update/style/"+ Style.CLASSIC), null, null, null);
        }
        else if(theme.equalsIgnoreCase(LIV)) {
            setTheme(R.style.AppTheme_Light);
            getContentResolver().update(Uri.parse(AKARADISTR+ "/update/style/"+ Style.LIGHT), null, null, null);
        }
        else if(theme.equalsIgnoreCase(DAV)) {
            setTheme(R.style.AppTheme_Dark);
            getContentResolver().update(Uri.parse(AKARADISTR+ "/update/style/"+ Style.DARK), null, null, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRestart() {
         //Log.i(tag, "onRetart() called.");
        super.onRestart();
        String tPrtn_pradarshana= PreferenceManager.getDefaultSharedPreferences(this).getString(PreferanceCentral.pref_theme, ClV);
        if(!tPrtn_pradarshana.equalsIgnoreCase(pref_display)) {
            this.recreate();
            return;
        }
        bookmarks = null;
        dumpBookmarks();
        initPreferances();
        /*if(descrView!= null) {
            String padam= descrView.getTitle();
            manageBookmark(padam);
        }*/ //this is done in AsyncTask itself.
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        descrView.saveState(bundle);
    }

    public boolean initToolBar(final Toolbar tb) {
        View bv = tb.getChildAt(0); // bv:bomma(logo)View.
        bv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(navigationD.isDrawerOpen(navigationL)) {
                    navigationD.closeDrawer(navigationL);
                }
                else {
                    navigationD.openDrawer(navigationL);
                }
            }
        });

        setSupportActionBar(tb);

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    /*private void initActionBarDrawerToggle() {
        abdt= new ActionBarDrawerToggle(this,
                navigationD,
                R.string.drawer_opened,
                R.string.drawer_closed
                );

        navigationD.addDrawerListener(abdt);

    }*/ ///TODO!!!

    public boolean initJaalaView(final JaalaView vv) {

        vv.getSettings().setJavaScriptEnabled(true);

        vv.addJavascriptInterface(new Js(this), "Js");

        vv.setFocusable(true);
        vv.setFocusableInTouchMode(true);

        vv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /*if(suggestionsView!= null) {
                    suggestionsView.setText(Uri.parse(url).getLastPathSegment());
                }*/
                if((url.startsWith("file://")) || (url.startsWith(AKARADISTR))) {
                    return false;
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView wv, String url, Bitmap fi) {
                super.onPageStarted(wv, url, fi);
            }

            @Override
            public void onPageFinished(WebView wv, String url) {
                //Log.i(tag, wv.getTitle()+" loaded");

                ((JaalaView)wv).mAnimate();
                super.onPageFinished(wv, url);

                if(suggestionsView != null) {
                    NakshatraActivity.this.updateSuggestionsView();
                }

                if(isTtsOn && (tts.isSpeaking())) {
                    tts.stop();
                }

                String padam= wv.getTitle();
                manageBookmark(padam);
                if(url.startsWith(AKARADISTR+ "/nighantu/")) {
                    insertHistory(padam);
                    hideDescrNavigationLayout(pref_prev_next);
                }

                domTree = new STuple<>(new LinkedHashMap<String, ArrayList<STuple<String>>>(), new LinkedHashMap<String, ArrayList<STuple<String>>>());
                dictionaryNames = new STuple<>(new ArrayList<String>(), new ArrayList<String>());
                wv.evaluateJavascript("chettu();", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        NakshatraActivity.this.onJsResult(value);
                    }
                });

            }
        });

        return true;
    }

    public void initDescrNavigation() {
        descrNavigation = (RelativeLayout) findViewById(R.id.descr_nav_box);

        ((ImageView)findViewById(R.id.descr_nav_prev)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descrView.toPreviousOrNextWord(JaalaView.previous);
            }
        });
        ((ImageView)findViewById(R.id.descr_nav_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descrView.toPreviousOrNextWord(JaalaView.next);
            }
        });

        animator= ObjectAnimator.ofFloat(descrNavigation, "alpha", 1f, 0f);
        animator.setDuration(5000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                descrNavigation.setVisibility(View.GONE);
                descrNavigation.setAlpha(1f);
                super.onAnimationEnd(animation);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                descrNavigation.setVisibility(View.GONE);
                descrNavigation.setAlpha(1f);
                super.onAnimationCancel(animation);
            }
        });
    }

    public void hideDescrNavigationLayout(boolean dachu) {
        if(animator.isRunning()) {
            animator.cancel();
        }
        descrNavigation.setVisibility(View.VISIBLE);
        if(dachu) {
            animator.start();
        }
    }

    public void onJsResult(String phalitam) {
        buildTree();
    }

    public boolean initTree() {
        tree.setAdapter((treeAdapter = new TreeAdapter(domTree, dictionaryNames)));
        return true;
    }

    public void buildTree() {
        treeAdapter.swapTree(domTree, dictionaryNames);
        if(dictionaryNames.first.size()+ dictionaryNames.second.size() >= 2) {
            showDrawerSimulateDialog();
        }
    }


    public boolean onTtsInit(int sthiti) {
        if(sthiti== TextToSpeech.SUCCESS) {
            isTtsOn= true;
            tts.setLanguage(Locale.US);
            return true;
        }
        else {
            isTtsOn= false;
            return false;
        }
    }

    void initPreferances() {
        pref_auto_toggle_transliterate = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferanceCentral.pref_auto_reset_transliteration_mode, true);
        pref_prev_next = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferanceCentral.pref_auto_hide_prev_next, true);
    }

    void showHelpDialog() {
        int openedTimes= PreferenceManager.getDefaultSharedPreferences(this).getInt(PreferanceCentral.pref_nakshatra_opened_times, 0);
        if(openedTimes== 0) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prevFragment = getFragmentManager().findFragmentByTag("dialog");
            if (prevFragment != null) {
                ft.remove(prevFragment);
            }

            DialogFragment helpDialog= new HelpDialog();
            helpDialog.show(ft, "dialog");
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(PreferanceCentral.pref_nakshatra_opened_times, openedTimes+ 1).apply();
    }

    void showRateDialog() {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);

        int vyavadhi= pref.getInt(PreferanceCentral.pref_rate_dialog_period, 0);
        pref.edit().putInt(PreferanceCentral.pref_rate_dialog_period, ++vyavadhi).apply();

        String sthiti= pref.getString(PreferanceCentral.pref_rate_status, RateDialog.askNext);
        if(sthiti.equalsIgnoreCase(RateDialog.askNext) && (vyavadhi>= 15)) {

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prevFragment = getFragmentManager().findFragmentByTag("dialog");
            if (prevFragment != null) {
                ft.remove(prevFragment);
            }
            DialogFragment rateDialog= new RateDialog();
            rateDialog.show(ft, "dialog");
        }
    }

    private void showDrawerSimulateDialog() {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        boolean should= pref.getBoolean(PreferanceCentral.pref_should_simulate_drawer_help_dialog, true);

        if(!should) {
            return;
        }
        navigationD.openDrawer(treeL);
        navigationD.openDrawer(navigationL);

        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prevFragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prevFragment != null) {
            ft.remove(prevFragment);
        }
        android.support.v4.app.DialogFragment drawerSimulateDialog= new DrawerSimulateDialog();
        drawerSimulateDialog.show(ft, "dialog");
        pref.edit().putBoolean(PreferanceCentral.pref_should_simulate_drawer_help_dialog, false).apply();
    }

    @Override
    public void onDestroy() {
        tts.shutdown();
        if(bookmarksTask != null) {
            bookmarksTask.cancel(true);
            bookmarksTask = null;
        }
        if(historyTask != null) {
            historyTask.cancel(true);
            historyTask = null;
        }
        super.onDestroy();
    }


    @Override
    public void onNewIntent(Intent intent) {
        dumpBookmarks();
        String action= intent.getAction();
        String type= intent.getType();
        if (action.equals(Intent.ACTION_SEARCH)) {
            Uri uri= intent.getData();
            if(uri!= null) {
                String url= uri.toString();
                if(url.startsWith(AKARADISTR+ "/nighantu/")) {
                    descrView.loadUrl(url);
                }
            }
            else {
                String padam = intent.getStringExtra(SearchManager.QUERY);
                search(padam);
            }
        }
        try {
            getSupportActionBar().hide();
            getSupportActionBar().show();
        }
        catch(NullPointerException npe) {
            Log.e(tag, "error in toggling actionBar.", npe);
        }
    }


    /*
    methods dealing with main options menu...
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_nakshatra, menu);

        menu_lm= menu.findItem(R.id.nks_menu_transliterate);

        DisplayMetrics dm= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        svWidth= dm.widthPixels;

        searchView = (SearchView) menu.findItem(R.id.nks_menu_search).getActionView();
        searchView.setMaxWidth((int) Math.floor(svWidth*0.78));
        initSearchView(searchView);

        suggestionsView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        initAutoCompleteTextView(suggestionsView);

        ImageView goButton= searchView.findViewById(android.support.v7.appcompat.R.id.search_go_btn);
        if(goButton!= null) {
            goButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    search(suggestionsView.getText().toString());
                }
            });
        }

        return true;
    }

    public boolean initSearchView(SearchView vv) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        // Assumes current activity is the searchable activity
        vv.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        vv.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        //vv.setSubmitButtonEnabled(true);
        vv.setOnQueryTextListener(onQuery);
        vv.setOnSuggestionListener(onSuggest);
        vv.setQueryRefinementEnabled(true);
        return true;
    }

    public boolean initAutoCompleteTextView(AutoCompleteTextView sv) {
        sv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search(v.getText().toString());
                return true;
            }
        });
        sv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    NakshatraActivity.this.hideIme(v);
                    /*searchView.setMaxWidth((int)Math.floor(svWidth*0.78));
                    menu_lm.setShowAsAction(SHOW_AS_ACTION_NEVER);
                    if(pref_auto_toggle_transliterate) {
                        //transliterate = false; TODO!!!
                        menu_lm.setIcon(R.drawable.ic_lm1ol4_achetana);
                        menu_lm.setChecked(false);
                    }
                    menu_lm.setVisible(false);*/
                }
                else {
                    /*searchView.setMaxWidth(svWidth);
                    menu_lm.setShowAsAction(SHOW_AS_ACTION_ALWAYS);
                    menu_lm.setVisible(true);*/ //TODO
                    if(navigationD.isDrawerOpen(navigationL)) {
                        navigationD.closeDrawer(navigationL);
                    }
                    if(navigationD.isDrawerOpen(treeL)) {
                        navigationD.closeDrawer(treeL);
                    }
                }
            }
        });
        //sv.setTypeface(Typeface.createFromAsset(getAssets(), "font/NotoSansTamil-Bold.ttf"));

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*if (abdt.onOptionsItemSelected(item)) {
            return true;
        }*/
        switch(item.getItemId()) {
            case R.id.nks_menu_recents: {
                String charitra= recentHistory();
                descrView.loadUrl(AKARADISTR + "/internal/" + "recents");
                return true;
            }
            case R.id.nks_menu_transliterate: {
                /*transliterate= !transliterate;
                item.setChecked(transliterate);
                if(transliterate) {
                    item.setIcon(R.drawable.ic_lm1ol4_chetana);
                }
                else {
                    item.setIcon(R.drawable.ic_lm1ol4_achetana);
                }*/ //TODO!!!
                return true;
            }
            case R.id.nks_menu_home: {
                descrView.loadUrl("file:///android_asset/Sahayam.html");
                return true;
            }
            case R.id.nks_menu_bookmarks: {
                Intent intent= new Intent(Intent.ACTION_DEFAULT,null, this, BookmarksActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.nks_menu_history: {
                Intent intent= new Intent(Intent.ACTION_DEFAULT,null, this, HistoryActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.nks_menu_preferances: {
                Intent intent= new Intent(Intent.ACTION_DEFAULT,null, this, PreferancesActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.nks_menu_help: {
                descrView.loadUrl("file:///android_asset/Sahayam.html");
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }




    /*
    methods of to interacting with webview...
     */
    public void search(String padam) {

        if((padam!= null) && (!padam.equals(""))) {
            //String uri = AKARADISTR + "/nighantu/" + Uri.encode(padam.toLowerCase(), null);
            String uri = AKARADISTR + "/nighantu/" + Uri.encode(padam, null); //NOTE!!!
            descrView.loadUrl(uri);
        }
    }

    public void hideIme(View v) {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        catch(NullPointerException npe) {
            Log.e(tag, "cannot hide IME", npe);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && descrView.canGoBack()) {
            descrView.goBack();
            /*if(suggestionsView!= null) {
                String padam= Uri.decode(Uri.parse(descrView.getOriginalUrl()).getLastPathSegment());
                suggestionsView.setText(padam);
            }*/ //no need. onPageFinished callback will handle this.
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    public String recentHistory() {
        return Helper.backForwardPage(descrView, this);
    }



    /*
    implementinhg SearchView callbacks...
     */

    SearchView.OnQueryTextListener onQuery= new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return NakshatraActivity.this.onQueryTextSubmition(query);
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return NakshatraActivity.this.onQueryChange(newText);
        }
    };

    public boolean onQueryTextSubmition(String padam) {
        search(padam);
        return true;
    }

    public boolean onQueryChange(String padam) {
        //TODO
        return false;
    }



   SearchView.OnSuggestionListener onSuggest= new SearchView.OnSuggestionListener() {
       @Override
       public boolean onSuggestionSelect(int position) {
           return false;
       }

       @Override
       public boolean onSuggestionClick(int position) {
           return NakshatraActivity.this.onSuggestion(position);
       }
   };

   public boolean onSuggestion(int position) {
       Cursor c= searchView.getSuggestionsAdapter().getCursor();
       if (c == null) {
           return true;
       }
       if (c.moveToPosition(position)) {
           // Get the new query from the suggestion.
           String padam= c.getString(c.getColumnIndex(SUGGEST_COLUMN_TEXT_1));
           search(padam);
       }

       return true;
   }

   public String updateSuggestionsView() {
       String padam= descrView.getTitle();
       if(suggestionsView != null) {
           shouldShowSuggestions = false; //will be resetted in inQueryChange callback.
           suggestionsView.setText(padam);
           suggestionsView.dismissDropDown();
           hideIme(suggestionsView);
           descrView.requestFocus();
           descrView.requestFocusFromTouch();
       }
       return padam;
   }

    /*
    listeners for navigationL onClicks...
     */
    //onClick for bookmark button.
    public void toggleBookmark(View v) {
        String padam= descrView.getTitle();
        String pani= null;
        if(bookmarks.contains(padam.toLowerCase())) {
            pani= BHTask.delete;
        }
        else {
            pani= BHTask.insert;
        }
        if(bookmarksTask == null) {
            bookmarksTask = new BHTask();
            bookmarksTask.execute(BHTask.bmTable, pani, padam);
        }
    }

    //onClick for share button.
    public void showShareDialog(View v) {
        navigationD.closeDrawer(navigationL);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prevFragment = getFragmentManager().findFragmentByTag("dialog");
        if (prevFragment != null) {
            ft.remove(prevFragment);
        }
        DialogFragment shareDF= new ShareDialog();
        shareDF.show(ft, "dialog");
    }

    //onClick for pronounce button.
    public void speakTitle(View v) {
        String padam= descrView.getTitle();
        speak(padam, 0);
    }

    //onClick for random.
    public void showRandomWord(View v) {
        String padam= getContentResolver().insert(Uri.parse(AKARADISTR+ "/response/random/00"), null).toString();
        navigationD.closeDrawer(navigationL);
        descrView.loadUrl(AKARADISTR + "/nighantu/" + Uri.encode(padam));
    }

    //onClick for WOD.
    public void showWordOfTheDay(View v) {
        navigationD.closeDrawer(navigationL);
        descrView.loadUrl(AKARADISTR+ "/internal/"+ "wordoftheday");
    }

    //onClick for exit.
    public void exit(View v) {
        this.finish();
    }

   /*
   from now on inner classes...
    */
   private class Js {
       private Activity activity;

       public Js(Activity activity) {
           this.activity= activity;
       }

       @JavascriptInterface
       public void speak(String padam, int vidham) { //vidham is for some future options..
           NakshatraActivity.this.speak(padam, vidham);
       }

       @JavascriptInterface
       public void copy(String pathyam) {
           NakshatraActivity.this.copy(pathyam);
       }

       @JavascriptInterface
       public void share(String pathyam) {
           NakshatraActivity.this.share(pathyam);
       }

       @JavascriptInterface
       public void addDictionaryToTree(int vibhagaSankhya, int va, String nighId, String nShirshika) { //nShirshika: nighShirshika.
           LinkedHashMap<String, ArrayList<STuple<String>>> vibhagam= null;
           ArrayList<String> nighPerlu= null;

           vibhagam= domTree.nth0(vibhagaSankhya);
           nighPerlu= dictionaryNames.nth0(vibhagaSankhya);

           if((vibhagam!= null) && (nighPerlu!= null)) {
               vibhagam.put(nShirshika, new ArrayList<STuple<String>>());
               nighPerlu.add(nShirshika);
           }
       }

       @JavascriptInterface
       public void addEntryToDictionary(int vibhagaSankhya, String nighId, String nShirshika, int va, String aropaId, String aShirshika) {
           LinkedHashMap<String, ArrayList<STuple<String>>> vibhagam= null;
           ArrayList<String> nighPerlu= null;

           vibhagam= domTree.nth0(vibhagaSankhya);
           nighPerlu= dictionaryNames.nth0(vibhagaSankhya);

           if((vibhagam!= null) && (nighPerlu!= null)) {
               if(!(vibhagam.containsKey(nShirshika)) && (nighPerlu.contains(nShirshika))) {
                   vibhagam.put(nShirshika, new ArrayList<STuple<String>>());
                   nighPerlu.add(nShirshika);
               }
               ArrayList<STuple<String>> nv= vibhagam.get(nShirshika);
               //nv.add(new STuple<String>(aropaId, aShirshika));
               nv.add(new STuple<String>(aropaId, aShirshika.replaceFirst("_([0-9][0-9]*)$", " $1").replaceAll("_([0-9])", "$1")));
           }
       }


       @JavascriptInterface
       public void logWebView(String log) {
           Log.i("Js", log);
       }

   }

   public void speak(String padam, int vidham) {
       if(isTtsOn) {
           tts.speak(padam, TextToSpeech.QUEUE_ADD, null, padam);
           if(!Bhasha.bhasha(padam).equalsIgnoreCase(Bhasha.EN)) {
               Toast.makeText(this, getString(R.string.tts_not_supported), Toast.LENGTH_SHORT).show();
           }
       }
    }

    public void copy(String pathyam) {
       if(pathyam== null) {
           return;
       }
        ClipboardManager cbm = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mukka = ClipData.newPlainText("pathyam", pathyam);
        try {
            cbm.setPrimaryClip(mukka);
            Toast.makeText(NakshatraActivity.this, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show();
        }
        catch(NullPointerException npe) {
            Log.e(tag, "error in copying text.");
        }
    }

    public void share(String pathyam) {
        Intent intent= new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, pathyam);
        intent.setType("text/plain");
        startActivity(intent);
    }

    class BHTask extends AsyncTask< String, Void, String[]> {

        static final String bmTable = "bookmarks";
        static final String hTable = "history";

        static final String dump = "SELECT_ALL";
        static final String insert = "INSERT";
        static final String delete = "DELETE";

        static final String saphalam= "true";
        static final String viphalam= "false";

        @Override
        protected String[] doInBackground(String... args) {
            String table= args[0];
            Helper.initBHHelper(NakshatraActivity.this.getApplicationContext());
            SQLiteDatabase db = Helper.bhDbHelper.getWritableDatabase();

            if(table.equalsIgnoreCase(bmTable)) {
                String riti = args[1]; // riti: mode.

                if (riti.equalsIgnoreCase(dump)) {
                    Cursor cursor = db.rawQuery("select padam from bookmarks where exist='true'", null);
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        bookmarks.add(cursor.getString(0).toLowerCase());
                    }
                    cursor.close();
                    return new String[]{table, riti, null, saphalam};
                }

                else if (riti.equalsIgnoreCase(insert)) {
                    String padam = args[2];
                    ContentValues cv = new ContentValues();
                    cv.put("padam", padam); //toLowerCase() call removed.
                    cv.put("time", Calendar.getInstance().getTimeInMillis());
                    cv.put("exist", "true"); //TODO!! should be removed, and let it choose default.
                    long saphalata = db.insert("bookmarks", null, cv);
                    if (saphalata >= 0) {
                        bookmarks.add(padam.toLowerCase());
                        return new String[]{table, riti, padam, saphalam};
                    }
                    else {
                        return new String[]{table, riti, padam, viphalam};
                    }
                }

                else if (riti.equalsIgnoreCase(delete)) {
                    String padam = args[2];
                    int saphalata = db.delete("bookmarks", "padam= ?", new String[]{padam});
                    if (saphalata > 0) {
                        bookmarks.remove(padam.toLowerCase());
                        return new String[]{table, riti, padam, saphalam};
                    }
                    else {
                        return new String[]{table, riti, padam, viphalam};
                    }
                }

                return new String[]{table, riti, null, viphalam};
            }

            else if(table.equalsIgnoreCase(hTable)) {
                String riti= args[1];
                if(riti.equalsIgnoreCase(insert)) {
                    String padam= args[2];
                    padam= padam.replaceAll("'", "''"); //toLowerCase() call removed.
                    db.execSQL("insert into history (padam, time, frequency) values ('"+ padam+ "', "+ Calendar.getInstance().getTimeInMillis()+ ",\n" +
                            "(case\n" +
                            "when (select frequency from history where padam='"+ padam+ "') is null\n" +
                            "then 1\n" +
                            "else\n" +
                            "(select frequency from history where padam='"+ padam+ "')+ 1\n" +
                            "end)\n" +
                            ");");
                    session_history.add(padam.toLowerCase().replaceAll("''", "'"));
                    return new String[] {table, riti, padam, saphalam};
                }
            }

            return new String[]{table, null, null, viphalam};
        }

        @Override
        protected void onPostExecute(String[] phalitam) {
            String pattika = phalitam[0];

            if (pattika.equalsIgnoreCase(bmTable)) {
                String riti = phalitam[1];
                String padam = phalitam[2];
                String saphalata = phalitam[3];

                if (riti.equalsIgnoreCase(insert) && saphalata.equalsIgnoreCase(saphalam)) {
                    NakshatraActivity.this.toggleBookmarkIcon(true);
                } else if (riti.equalsIgnoreCase(delete) && saphalata.equalsIgnoreCase(saphalam)) {
                    NakshatraActivity.this.toggleBookmarkIcon(false);
                } else if (riti.equalsIgnoreCase(dump) && saphalata.equalsIgnoreCase(saphalam)) {
                    if(descrView != null) {
                        String shirshika= descrView.getTitle();
                        if(shirshika!= null) {
                            manageBookmark(shirshika);
                        }
                    }
                }
                NakshatraActivity.this.bookmarksTask = null;
            }

            else if(pattika.equalsIgnoreCase(hTable)) {
                NakshatraActivity.this.historyTask = null;
            }
        }

    }


    public boolean dumpBookmarks() {

        if(bookmarks == null) {
            bookmarks = new HashSet<>();
        }
        if(bookmarks.size()== 0) {
            if(bookmarksTask != null) {
                bookmarksTask.cancel(true);
            }
            bookmarksTask = new BHTask();
            bookmarksTask.execute(BHTask.bmTable, BHTask.dump, null);
        }
        return true;
    }

    public boolean manageBookmark(String padam) {
        if(bookmarks.contains(padam.toLowerCase())) {
            return toggleBookmarkIcon(true);
        }
        else {
            return toggleBookmarkIcon(false);
        }
    }

    public boolean toggleBookmarkIcon(boolean pettu) {
       ImageView mb= findViewById(R.id.nda_bookmark);
        if(pettu) {
            mb.setImageResource(R.drawable.bookmark_filled);
            //mb.setColorFilter(R.color.grey);
            return true;
        }
        else {
            mb.setImageResource(R.drawable.bookmark_no_fill);
            //mb.setColorFilter(R.color.colorPrimaryDark);
            return false;
        }
    }


    public boolean insertHistory(String padam) {
       if(session_history == null) {
           session_history = new HashSet<>();
       }
       if(!session_history.contains(padam.toLowerCase())) {
           historyTask = new BHTask();
           historyTask.execute(BHTask.hTable, BHTask.insert, padam);
           return true;
       }
       else {
           return false;
       }
    }



    class EntryHolder {
        final LinearLayout entry;
        final TextView name;

        public EntryHolder(View v) {
            this.entry = (LinearLayout) v.findViewById(R.id.aropam_namuna);
            this.name = (TextView) v.findViewById(R.id.aropam_namuna_peru);
        }

        public void idMarchu(String id) {
            if(name == null) {
                return;
            }
            name.setTag(id);
        }
    }

    class TreeAdapter extends BaseAdapter {

        STuple<LinkedHashMap<String, ArrayList<STuple<String>>>> _dTree;
        STuple<ArrayList<String>> _dictionaryNames;

        public TreeAdapter(STuple<LinkedHashMap<String, ArrayList<STuple<String>>>> _domChettu, STuple<ArrayList<String>> _dictionaryNames) {
            super();
            this._dTree = _domChettu;
            this._dictionaryNames = _dictionaryNames;
        }

        @Override
        public int getCount() {
            if((_dTree == null) || (_dictionaryNames == null)) {
                return 0;
            }
            int lekka= 0;
            for(int i= 0; i< 2; i++) {
                LinkedHashMap<String, ArrayList<STuple<String>>> vc= _dTree.nth0(i);
                if((vc!= null) && (!vc.isEmpty())) {
                    lekka++;
                    ArrayList<String> vp= _dictionaryNames.nth0(i);
                    for(int j= 0; j< vp.size(); j++) {
                        ArrayList<STuple<String>> nc= vc.get(vp.get(j));
                        if((nc!= null) && (!nc.isEmpty())) {
                            lekka++;
                            lekka+= nc.size();
                        }
                    }
                }
            }
            return lekka;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return(3);
        }

        @Override
        public int getItemViewType(int position) {
            if((_dTree == null) || (_dictionaryNames == null)) {
                return 0;
            }
            int rakam= amshaVivaram(position).rakam;
            if(rakam>= 0) {
                return rakam;
            }
            return 0;
        }

        public AmshaVivaram amshaVivaram(int sth) {
            int count= 0;
            for(int i= 0; i< 2; i++) {
                LinkedHashMap<String, ArrayList<STuple<String>>> vc= _dTree.nth0(i);
                if((vc!= null) && (!vc.isEmpty())) {
                    count++;
                    if((sth+ 1)== count) {
                        return new AmshaVivaram(0, i, -1, -1);
                    }
                    ArrayList<String> vp= _dictionaryNames.nth0(i);
                    for(int j= 0; j< vp.size(); j++) {
                        ArrayList<STuple<String>> nc= vc.get(vp.get(j));
                        if((nc!= null) && (!nc.isEmpty())) {
                            count++;
                            if((sth+ 1)== count) {
                                return new AmshaVivaram(1, i, j, -1);
                            }
                            for(int k= 0; k< nc.size(); k++) {
                                count++;
                                if((sth+ 1)== count) {
                                    return new AmshaVivaram(2, i, j, k);
                                }
                            }
                        }
                    }
                }
            }
            return new AmshaVivaram(-1, -1, -1, -1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if((_dTree == null) || (_dictionaryNames == null)) {
                return null;
            }
            AmshaVivaram vivaram= amshaVivaram(position);

            if(vivaram.rakam== 0) {
                View row= convertView;
                if(row== null) {
                    LayoutInflater inflater= (LayoutInflater) NakshatraActivity.this
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row= inflater.inflate(R.layout.catogory_amsham_tree, null);
                }
                HistoryActivity.Holder holder= (HistoryActivity.Holder) row.getTag();

                if(holder== null) {
                    holder= new HistoryActivity.Holder(row);
                    row.setTag(holder);
                }
                String vibhagam= "";
                switch(vivaram.v) {
                    case 0: {
                        vibhagam= getString(R.string.headers);
                        break;
                    }
                    case 1: {
                        vibhagam= getString(R.string.related_words);
                        break;
                    }
                }
                holder.group.setText(vibhagam);
                return row;
            }
            else if(vivaram.rakam== 1) {
                View row= convertView;
                if(row== null) {
                    LayoutInflater inflater= (LayoutInflater) NakshatraActivity.this
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //loVarusa= inflater.inflate(android.R.layout.simple_list_item_multiple_choice, null);
                    row= inflater.inflate(R.layout.dictionary_amsham_tree, null);
                }
                HistoryActivity.Holder holder= (HistoryActivity.Holder) row.getTag();

                if(holder== null) {
                    holder= new HistoryActivity.Holder(row);
                    row.setTag(holder);
                }
                String nigh= "";
                nigh= _dictionaryNames.nth0(vivaram.v).get(vivaram.n);

                holder.group.setText(nigh);
                return row;
            }
            else if(vivaram.rakam== 2) {
                STuple<String> aropam= _dTree.nth0(vivaram.v).get(_dictionaryNames.nth0(vivaram.v).get(vivaram.n)).get(vivaram.a);

                View innerRow= convertView;
                if(innerRow== null) {
                    LayoutInflater inflater= (LayoutInflater) NakshatraActivity.this
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //loVarusa= inflater.inflate(android.R.layout.simple_list_item_multiple_choice, null);
                    innerRow= inflater.inflate(R.layout.tree_entry, null);
                }
                EntryHolder inHolder= (EntryHolder) innerRow.getTag();

                if(inHolder== null) {
                    inHolder= new EntryHolder(innerRow);
                    innerRow.setTag(inHolder);
                }

                inHolder.name.setText(aropam.second);
                inHolder.name.setTag(aropam.first);
                inHolder.name.setOnClickListener(entrySelectionListener);
                return innerRow;
            }
            return null;
        }

        public View.OnClickListener entrySelectionListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id= (String) v.getTag();
                if(id!= null) {
                    navigationD.closeDrawer(treeL);
                    NakshatraActivity.this.descrView.evaluateJavascript("chupu('"+ id+ "')", null);
                }
            }
        };

        public void swapTree(STuple<LinkedHashMap<String, ArrayList<STuple<String>>>> _domChettu, STuple<ArrayList<String>> _nighantuvulu) {
            this._dTree = _domChettu;
            this._dictionaryNames = _nighantuvulu;
            notifyDataSetChanged();
        }

    }

    class AmshaVivaram {
       int rakam;
       int v;
       int n;
       int a;
       public AmshaVivaram(int rakam, int v, int n, int a) {
           this.rakam= rakam;
           this.v= v;
           this.n= n;
           this.a= a;
       }
    }

}
