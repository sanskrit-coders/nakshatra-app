package in.bhumiputra.nakshatra;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;

import static in.bhumiputra.nakshatra.NighantuProvider.AKARADISTR;

/**
 * Created by damodarreddy on 1/14/18.
 */

public class JaalaView extends WebView {

    private Context context;
    private JaalaView me;
    public String selectionWord = "";
    private String tag= "Jaala";

    static final String previous = "previous";
    static final String next = "next";

    static final int BACK_ANIMATE= 1;
    static final int FORWARD_ANIMATE= 2;
    boolean shdAnimate= false;
    int DIRN_ANIMATE= 0;


    public JaalaView(Context context) {
        super(context);
        this.context= context;
        this.me = this;
        modalu();
    }

    public JaalaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context= context;
        this.me = this;
        modalu();
    }

    public JaalaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context= context;
        this.me = this;
        modalu();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public JaalaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context= context;
        this.me = this;
        modalu();
    }


    private boolean modalu() {
        this.getSettings().setJavaScriptEnabled(true);
        this.setOnTouchListener(new Guesture(context) {
            @Override
            public boolean onSwipeRight() {
                toPreviousOrNextWord(previous);
                return true;
            }

            @Override
            public boolean onSwipeLeft() {
                toPreviousOrNextWord(next);
                return true;
            }
        });

        return true;
    }

    public boolean toPreviousOrNextWord(String dirn) {
        String url= me.getUrl();
        if(url.startsWith(AKARADISTR+ "/nighantu/")) {
            String padam= me.getTitle();
            String mtPadam= context.getContentResolver().insert(Uri.parse(AKARADISTR+ "/response/"+ dirn+ "/"+ Uri.encode(padam)), null).toString();
            shdAnimate= true;
            DIRN_ANIMATE= dirn.equalsIgnoreCase(previous)? BACK_ANIMATE : FORWARD_ANIMATE;
            me.loadUrl(AKARADISTR + "/nighantu/" + Uri.encode(mtPadam));
        }
        return true;
    }

    void mAnimate() {
        if(shdAnimate) {
            Animation anim= null;
            switch(DIRN_ANIMATE) {
                case 1: {
                    anim = AnimationUtils.loadAnimation(context,
                            R.anim.slide_in_left);
                    break;
                }
                case 2: {
                    anim = AnimationUtils.loadAnimation(context,
                            R.anim.slide_in_right);
                    break;
                }
            }
            me.startAnimation(anim);
            me.setVisibility(VISIBLE);
            shdAnimate= false;
            DIRN_ANIMATE= 0;
        }
    }



}
