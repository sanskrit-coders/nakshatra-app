package in.bhumiputra.nakshatra;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Locale;

import in.bhumiputra.nakshatra.nighantu.Style;
import in.bhumiputra.nakshatra.nighantu.anga.Bhasha;

import static in.bhumiputra.nakshatra.NighantuProvider.AKARADISTR;
import static in.bhumiputra.nakshatra.PreferancesActivity.ClV;
import static in.bhumiputra.nakshatra.PreferancesActivity.DAV;
import static in.bhumiputra.nakshatra.PreferancesActivity.LIV;

public class SelectionPopUp extends Activity {

    TextToSpeech tts;
    boolean isTtsOn;
    JaalaView descrView;
    LinearLayout layout;
    ImageView closeButton;
    ImageView fullScreenButton;
    Button copyButton;
    Button replaceButton;
    RelativeLayout block;


    int wWidth;
    int wHeight;

    int width;
    int height;

    String tag= "SelectionPopUp";
    String padam;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setContentView(R.layout.activity_selection_popup);
        tts= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                SelectionPopUp.this.onTtsInit(status);
            }
        });

        layout= findViewById(R.id.spu_layout);
        adjustMetrics();
        //layout.invalidate();

        descrView = findViewById(R.id.spu_descr);
        block = findViewById(R.id.spu_box);
        closeButton = findViewById(R.id.spu_close);
        fullScreenButton = findViewById(R.id.spu_ffull_screen);

        handleDisplay();

        initLayout();
        initJaalaView(descrView);
        initCloseButton(closeButton);
        initFullScreenButton(fullScreenButton);

        /*nakalu= findViewById(R.id.empika_pelalu_charya_nakalu);
        nakaluModalu(nakalu);

        pratisthapana= findViewById(R.id.empika_pelalu_charya_pratisthapinchu);
        pratisthapanaModalu(pratisthapana);*/

        Intent intent = getIntent();
        handleIntent(intent);
    }


    private void adjustMetrics() {
        DisplayMetrics dm= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        wWidth= dm.widthPixels;
        wHeight= dm.heightPixels;

        width= (int) Math.floor(wWidth* 0.90);
        height= (int) Math.floor(wHeight* 0.62);

        layout.setLayoutParams(new FrameLayout.LayoutParams(width, height, 0x11));
    }

    private void handleIntent(Intent intent) {
        String charya= intent.getAction();
        String rakam= intent.getType();
        if((charya!= null) && charya.equalsIgnoreCase("android.intent.action.PROCESS_TEXT")) {
            padam = intent.getCharSequenceExtra("android.intent.extra.PROCESS_TEXT").toString(); //Intent.EXTRA_PROCESS_TEXT. only above M.
            String url= AKARADISTR+ "/nighantu/"+ Uri.encode(padam);
            descrView.loadUrl(url);
        }

        else if((charya!= null) && charya.equals(Intent.ACTION_SEND) && (rakam!= null)) {
            if(rakam.equals("text/plain")) {
                String tPadam= intent.getStringExtra(Intent.EXTRA_TEXT);
                if(tPadam!= null) {
                    padam= tPadam;
                    String url= AKARADISTR+ "/nighantu/"+ Uri.encode(padam);
                    descrView.loadUrl(url);
                }
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        adjustMetrics();
        handleIntent(intent);
    }


    private void handleDisplay() {

        this.getWindow().setBackgroundDrawableResource(R.color.paradarshaka);
        //block.setBackgroundResource(R.color.purti_paradarshaka);

        String theme= PreferenceManager.getDefaultSharedPreferences(this).getString(PreferanceCentral.pref_theme, ClV);
        if(theme.equalsIgnoreCase(ClV)) {
            setTheme(R.style.AppTheme_Classic);
            getContentResolver().update(Uri.parse(AKARADISTR+ "/update/style/"+ Style.CLASSIC), null, null, null);
            if(block != null) {
                block.setBackgroundResource(R.color.pata_nepathyam_bangaru);
                layout.setBackgroundResource(R.color.pata_nepathyam_bangaru);
            }
        }
        else if(theme.equalsIgnoreCase(LIV)) {
            setTheme(R.style.AppTheme_Light);
            getContentResolver().update(Uri.parse(AKARADISTR+ "/update/style/"+ Style.LIGHT), null, null, null);
            if(block != null) {
                block.setBackgroundResource(R.color.kagita_nepathyam);
                layout.setBackgroundResource(R.color.kagita_nepathyam);
            }
        }
        else if(theme.equalsIgnoreCase(DAV)) {
            setTheme(R.style.AppTheme_Dark);
            getContentResolver().update(Uri.parse(AKARADISTR+ "/update/style/"+ Style.DARK), null, null, null);
            if(block != null) {
                block.setBackgroundResource(R.color.chikati_nepathyam);
                layout.setBackgroundResource(R.color.chikati_nepathyam);
            }
        }
    }


    private boolean onTtsInit(int sthiti) {
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


    private void initLayout() {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass...
            }
        });
        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectionPopUp.this.finish();
            }
        });
    }


    private boolean initJaalaView(final JaalaView vv) {

        vv.getSettings().setJavaScriptEnabled(true);

        vv.addJavascriptInterface(new Js(this), "Js");

        vv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /*if(salahaluView!= null) {
                    salahaluView.setText(Uri.parse(url).getLastPathSegment());
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
            public void onPageFinished(WebView wv, String url) {
                //Log.i(tag, wv.getTitle()+" loaded");

                ((JaalaView)wv).mAnimate();
                /*wv.evaluateJavascript("a=document.getElementById('patra');\n" +
                        "a.style.padding= '1.4em';\n" +
                        "a.style.border-radius= '10px';\n", null);*/
                super.onPageFinished(wv, url);

                if(isTtsOn && (tts.isSpeaking())) {
                    tts.stop();
                }
            }
        });

        return true;
    }

    private void initCloseButton(ImageView m) {
        if(m== null) {
            return;
        }
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectionPopUp.this.finish();
            }
        });
    }

    private void initFullScreenButton(ImageView p) {
        if(p== null) {
            return;
        }
        p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pada= descrView.getTitle();
                Intent intent= new Intent(Intent.ACTION_SEND, null, SelectionPopUp.this, InitActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, pada);
                intent.setType("text/plain");
                startActivity(intent);
                SelectionPopUp.this.finish();
            }
        });
    }

    /*private void initCopyButton(Button n) {
        if(n== null) {
            return;
        }
        n.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(descrView== null) {
                    return;
                }
                descrView.evaluateJavascript("mottamEnchukonu('patra', 1)", null);
            }
        });
    }*/

    /*private void pratisthapanaModalu(Button p) {
        //TODO...
    }*/


    private class Js {
        private Activity activity;

        public Js(Activity activity) {
            this.activity= activity;
        }

        @JavascriptInterface
        public void speak(String padam, int vidham) { //vidham is for some future options..
            SelectionPopUp.this.speak(padam, vidham);
        }

        @JavascriptInterface
        public void copy(String pathyam) {
            ClipboardManager mukkalaBalla = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mukka = ClipData.newPlainText("pathyam",pathyam);
            try {
                mukkalaBalla.setPrimaryClip(mukka);
                Toast.makeText(activity, getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show();
            }
            catch(NullPointerException npe) {
                Log.e("SPUJS", "error in copying text.");
            }
        }

        @JavascriptInterface
        public void logWebView(String log) {
            Log.i("SPUJS", log);
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

    @Override
    public void onDestroy() {
        tts.shutdown();
        setResult(RESULT_OK);
        super.onDestroy();
    }


}
