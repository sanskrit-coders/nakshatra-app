package in.bhumiputra.nakshatra.nighantu;


/**
 * css static variables to be used in result html page. as some times we may have to compute at run time it is not kept in external file.
 * TODO make it all english from translitered telugu.
 */

public class Style {
    public static final String template = "\n" +
            "body {\n" +
            "    background: @@rangu_body_nepathyam##;\n" +
            "    padding:0em;\n" +
            "    color: @@rangu_body##" +
            "}\n" +
            "\n" +
            "::selection{background:rgba(49,136,146,0.2)}" +
            "#pelalu_bottom {\n" +
            "    box-shadow: -0.05em -0.05em 0.5em black;\n" +
            "    background-color: @@rangu_pelalu_nepathyam##;\n" +
            "    position: fixed;\n" +
            "    bottom: 0em;\n" +
            "    width: 95%;\n" +
            "    display:none;\n" +
            "    z-index: 100;\n" +
            "    text-align: center;\n" +
            "    border-radius: 0.1em;\n" +
            "}\n" +
            ".pelalu_table {\n" +
            "    /*box-shadow: -0.05em -0.05em 0.5em black;\n" +
            "    background-color: @@rangu_pelalu_nepathyam##;*/\n" +
            "    align: center;\n" +
            "    /*border-radius: 0.5em;*/\n" +
            "    max-width:55.8em;\n" +
            "}\n" +
            ".pelalu_td {\n" +
            "    font-size: 1.1em;\n" +
            "    line-height: 1.5em;\n" +
            "    padding-right: 0.5em;\n" +
            "    padding-left: 0.5em;\n" +
            "    width: 50%;\n" +
            "}\n" +
            "#pelalu_bottom_td_paluku {\n" +
            "    padding-bottom: 0.4em;\n" +
            "    text-align: right;\n" +
            "\n" +
            "}\n" +
            ".pelalu_img {\n" +
            "    fill: @@rangu_nighnundi##;\n" +
            "    stroke: @@rangu_nighnundi##;\n" +
            "}\n" +
            "/* Layout specific styling */\n" +
            ".patra {\n" +
            "    padding:0em;\n" +
            "    max-width: 55.8em;\n" +
            "    margin: 0 auto 0 auto;\n" +
            "    font-family: @@patra_font-family##;\n" +
            "    font-size: @@patra_font-size##;\n" +
            "    line-height: @@patra_line-height##;\n" +
            "    word-spacing: @@patra_word-spacing##;\n" +
            "}\n" +
            "\n" +
            ".shiravibhaga {\n" +
            "    padding: 0em;\n" +
            "}\n" +
            "\n" +
            ".paryayavibhaga {\n" +
            "    padding: 0em;\n" +
            "}\n" +
            "\n" +
            ".nighantu {\n" +
            "    /*margin-top: 4em;*/\n" +
            "    margin-top: 3.5em;\n" +
            "}\n" +
            "\n" +
            ".firstnigh {\n" +
            "    margin-top: 0.5em;\n" +
            "}\n" +
            "\n" +
            ".nighnundi {\n" +
            "    margin-top: 1em; margin-bottom: 0em;\n" +
            "    border: 1px solid @@rangu_nighnundi_avadhi##;\n" +
            "    border-radius: 3px;\n" +
            "    padding: 0.5em;\n" +
            "    font-size: @@nighnundi_font-size##;\n" +
            "    font-style: italic;\n" +
            "    color: @@rangu_nighnundi##;\n" +
            "    background: @@rangu_nighnundi_nepathyam##; /*#f5f2ef*/\n" +
            "}\n" +
            "\n" +
            ".nighperu {\n" +
            "}\n" +
            "\n" +
            ".marchu_bomma {\n" +
            "    float: right;\n" +
            "    fill: @@rangu_nighnundi##;\n" +
            "    stroke: @@rangu_nighnundi##;\n" +
            "}\n" +
            "\n" +
            ".marchu_bomma::after {\n" +
            "    /*float: clear;*/\n" +
            "}\n" +
            "\n" +
            ".aropa {\n" +
            "    padding-left: 0.0em;\n" +
            "    padding-right: 0.0em;\n" +
            "    margin-top: 1.5em;\n" +
            "    margin-bottom: 0em;\n" +
            "}\n" +
            "\n" +
            ".shirshika {\n" +
            "    font-family: @@shirshika_font-family##;\n" +
            "    font-size: @@shirshika_font-size##;\n" +
            "    font-weight: 600;\n" +
            "}\n" +
            "\n" +
            ".panulupette {\n" +
            "    /*margin-left: 0.5em;*/\n" +
            "    /*position: absolute;\n" +
            "    right: 0.7em;\n*/" +
            "    float: right;\n" +
            "}\n" +
            "\n" +
            ".panulupette::after {\n" +
            "    /*float: clear;*/\n" +
            "}\n" +
            "\n" +
            ".ttspette {\n" +
            "    margin-left: 0em;\n" +
            "    margin-right: 0.5em;\n" +
            "}\n" +
            "\n" +
            ".ttsicon {\n" +
            "    fill: @@rangu_body_nepathyam##;\n" +
            "    stroke: @@rangu_body##;\n" +
            "}\n" +
            "\n" +
            ".nakalu_bomma {\n" +
            "    fill: @@rangu_body##;\n" +
            "}\n" +
            "\n" +
            ".nakalupette {\n" +
            "    /*position: absolute;\n" +
            "    right: 0.7em;\n" +
            "    //display: flex;\n" +
            "    /align-items: flex-end;*/\n" +
            "}\n" +
            "\n" +
            ".khali {\n" +
            "    height:2em;\n" +
            "}\n" +
            "\n" +
            ".peddakhali {\n" +
            "    height:15em;\n" +
            "}\n" +
            "\n" +
            ".noResult {\n" +
            "    margin-top: 1em;\n" +
            "}\n" +
            "\n" +
            ".sorryblock {\n" +
            "}\n" +
            "\n" +
            "#sorry {\n" +
            "    color: @@rangu_tlx_vyutpatti##;\n" +
            "}\n" +
            "\n" +
            ".salahalu {\n" +
            "    padding: 3em;\n" +
            "    padding-top: 3em;\n" +
            "    font-size: @@salahalu_font-size##;\n" +
            "    line-height: @@salahalu_line-height##;\n" +
            "}\n" +
            "\n" +
            ".salahaModalu {\n" +
            "    /*font-size: 16px;\n" +
            "    line-height: 19px;*/\n" +
            "    font-style: italic;\n" +
            "    color: @@rangu_salaha_modalu##;\n" +
            "    text-decoration: underline;\n" +
            "}\n" +
            "\n" +
            ".salahaBlock {\n" +
            "    padding-left: 1em;\n" +
            "}\n" +
            "\n" +
            ".salaha {\n" +
            "    text-decoration: none;\n" +
            "    color: @@rangu_salaha##;\n" +
            "}\n" +
            "\n" +
            "hr {\n" +
            "}\n" +
            "\n" +
            ".vibhagini {\n" +
            "    font-size: @@nighnundi_font-size##;\n" +
            "    background: @@rangu_vibhagini_nepathyam##;\n" +
            "    font-weight: 500;\n" +
            "    padding: 0.5em;\n" +
            "}\n" +
            "\n" +
            ".lanke {\n" +
            "    text-decoration: none;\n" +
            "    color: @@rangu_pada##;\n" +
            "}\n" +
            "\n" +
            "/*Styling for apadana...*/\n" +
            ".apadana {\n" +
            "    margin: 3em 0 1em 0;\n" +
            "    /*border: 1px dotted grey;*/\n" +
            "    border-top: 1px solid grey;\n" +
            "    /*border-radius: 3px;*/\n" +
            "    text-indent: 2em;\n" +
            "    padding: 0.5em;\n" +
            "    font-size: 0.7em;\n" +
            "    line-height: 1.35em;\n" +
            "    color: @@rangu_nighnundi##;\n" +
            "    background-color: @@rangu_apadana_nepathyam##;\n" +
            "}\n" +
            "\n" +
            ".apadana_lanke {\n" +
            "    text-decoration: none;\n" +
            "    color: @@rangu_pada##;\n" +
            "}\n" +
            "\n" +
            "pre {" +
            "    max-width: 55.8em;" +
            "    " +
            "}\n"
            ;

    /*from now colors for differant themes...*/
    static final String classic_body_nepathyam= "#fefdeb";
    static final String classic_pelalu_nepathyam= "#fffde7";
    static final String classic_nighnundi_nepathyam= "#fffde7"; //fffde7;e8e7d7;F9F7C7;FDF3D0;F2F1D7
    static final String classic_nighnundi_avadhi= "grey";
    static final String classic_nighnundi= "#666";
    static final String classic_salaha_modalu= "#000000";
    static final String classic_salaha= "#595A5C";
    static final String classic_pada= "#2f4f4f";
    static final String classic_apadana_nepathyam= "#fefdeb";//#E9E3CB
    static final String classic_body= "#000000";
    static final String classic_tts= "#4b4b4b";
    static final String classic_vibhagini_nepathyam= "#f1e4b9";

    static final String light_body_nepathyam= "#fffdf6";
    static final String light_pelalu_nepathyam= "#f5f2ef";
    static final String light_nighnundi_nepathyam= "#f5f2ef";
    static final String light_nighnundi_avadhi= "grey";
    static final String light_nighnundi= "grey";
    static final String light_salaha_modalu= "#000000";
    static final String light_salaha= "#595A5C";
    static final String light_pada= "#2f4f4f";
    static final String light_apadana_nepathyam= "#fffdf6";
    static final String light_body= "#000000";
    static final String light_tts= "grey";
    static final String light_vibhagini_nepathyam= "#f1e4b9";


    static final String dark_body_nepathyam= "#111111"; //#24282A
    static final String dark_pelalu_nepathyam= "#24282A";
    static final String dark_nighnundi_nepathyam= "#111111"; //#33373a
    static final String dark_nighnundi_avadhi= "grey";
    static final String dark_nighnundi= "grey";
    static final String dark_salaha_modalu= "#FFFFFF";
    static final String dark_salaha= "#d69047";
    static final String dark_pada= "#d6c83f";
    static final String dark_apadana_nepathyam= "#111111";
    static final String dark_body= "#ffffff";
    static final String dark_tts= "#7f7f7f";
    static final String dark_vibhagini_nepathyam= "#222222";

    public static final int CLASSIC = 0;
    public static final int LIGHT = 1;
    public static final int DARK = 2;

    public static final int PR_SAMANYA= 0;
    public static final int[] PR= {CLASSIC, LIGHT, DARK};


    public static final int KH_SAMANYA= 0;
    public static final int[] KH= {KH_SAMANYA};



    //public static final String khUri= AKARADI+"/font/";
    public static final String khUri= "/font/";

    /*private final Context context;

    public Shaili(Context context) {
        this.context= context;
    }*/

    public static String style(int pr, int kh) {
        //pr: pradarshana, kh: khati.
        String shaili= "";
        if((pr<PR_SAMANYA) || (pr>PR[PR.length-1])) {
            pr= PR_SAMANYA;
        }
        if((kh<KH_SAMANYA) || (kh>KH[KH.length-1])) {
            kh= KH_SAMANYA;
        }
        //Log.e("shaili", "kh: "+ kh+ ", pr: "+ pr);
        switch(kh) {
            case KH_SAMANYA: {
                /*shaili= shailiForKh("\"inimai\", \"Lucida Sans Unicode\", \"sans-serif\"", "0.9em", "1.35em", "0.25em",
                        "1em",
                        "\"inimai\", \"Lucida Sans Unicode\", \"sans-serif\"", "1.3em",
                        "1em", "1.4em"
                        );*/
                /*shaili= shailiForKh("\"Lucida Sans Unicode\", \"nsr\", \"sans-serif\"", "1.1em", "1.5em", "",
                        "1.2em",
                        "\"Lucida Sans Unicode\", \"nsb\", \"sans-serif\"", "1.3em",
                        "1.2em", "1.7em"
                        );*/
                shaili= styleForFont("\"Lucida Sans Unicode\", \"nsr\", \"sans-serif\"", "0.9em", "1.5em", "",
                        "1.1em",
                        "\"Lucida Sans Unicode\", \"nsb\", \"sans-serif\"", "1.3em",
                        "1.1em", "1.4em"
                        ); //chivari...
                break;
            }

            default: {
                shaili= styleForFont("\"Lucida Sans Unicode\", \"nsr\", \"sans-serif\"", "1.1em", "1.5em", "",
                        "1.2em",
                        "\"Lucida Sans Unicode\", \"nsb\", \"sans-serif\"", "1.3em",
                        "1.2em", "1.7em"
                        );
                break;
            }

        }

        switch(pr) {
            case CLASSIC: {
                shaili= styleForDisplay(shaili,
                        classic_body_nepathyam, classic_pelalu_nepathyam,
                        classic_nighnundi_avadhi, classic_nighnundi,
                        classic_salaha_modalu, classic_salaha,
                        classic_pada,
                        classic_apadana_nepathyam,
                        classic_body, classic_tts,
                        classic_nighnundi_nepathyam,
                        classic_vibhagini_nepathyam
                        );
                break;
            }
            case LIGHT: {
                shaili= styleForDisplay(shaili,
                        light_body_nepathyam, light_pelalu_nepathyam,
                        light_nighnundi_avadhi, light_nighnundi,
                        light_salaha_modalu, light_salaha,
                        light_pada,
                        light_apadana_nepathyam,
                        light_body, light_tts,
                        light_nighnundi_nepathyam,
                        light_vibhagini_nepathyam
                );
                break;
            }
            case DARK: {
                shaili= styleForDisplay(shaili,
                        dark_body_nepathyam, dark_pelalu_nepathyam,
                        dark_nighnundi_avadhi, dark_nighnundi,
                        dark_salaha_modalu, dark_salaha,
                        dark_pada,
                        dark_apadana_nepathyam,
                        dark_body, dark_tts,
                        dark_nighnundi_nepathyam,
                        dark_vibhagini_nepathyam
                );
                break;
            }
        }

        return shaili;
    }

    public static String styleForFont(String patra_fontFamily, String patra_fontSize, String patra_lineHeight, String patra_wordSpacing,
                                      String nighnundi_fontSize,
                                      String shirshika_fontFamily, String shirshika_fontSize,
                                      String salahalu_fontSize, String salahalu_lineHeight
                                     ) {
        String shaili= template;
        shaili= shaili.replaceAll("@@patra_font-family##", patra_fontFamily).replaceAll("@@patra_font-size##", patra_fontSize).replaceAll("@@patra_line-height##", patra_lineHeight).replaceAll("@@patra_word-spacing##", patra_wordSpacing)
                .replaceAll("@@nighnundi_font-size##", nighnundi_fontSize)
                .replaceAll("@@shirshika_font-family##", shirshika_fontFamily).replaceAll("@@shirshika_font-size##", shirshika_fontSize)
                .replaceAll("@@salahalu_font-size##", salahalu_fontSize).replaceAll("@@salahalu_line-height##", salahalu_lineHeight)
                ;
        return shaili;
    }

    public static String styleForDisplay(String css,
                                         String body_nepathyam, String pelalu_nepathyam,
                                         String nighnundi_avadhi, String nighnundi,
                                         String salaha_modalu, String salaha,
                                         String pada,
                                         String apadana_nepathyam,
                                         String body, String tts,
                                         String nighnundi_nepathyam,
                                         String vibhagini_nepathyam
                                     ) {
        String cssM= css.replaceAll("@@rangu_body_nepathyam##", body_nepathyam).replaceAll("@@rangu_pelalu_nepathyam##", pelalu_nepathyam)
                .replaceAll("@@rangu_nighnundi_avadhi##", nighnundi_avadhi).replaceAll("@@rangu_nighnundi##", nighnundi)
                .replaceAll("@@rangu_salaha_modalu##", salaha_modalu).replaceAll("@@rangu_salaha##", salaha)
                .replaceAll("@@rangu_pada##", pada)
                .replaceAll("@@rangu_apadana_nepathyam##", apadana_nepathyam)
                .replaceAll("@@rangu_body##", body).replaceAll("@@rangu_tts##", tts)
                .replaceAll("@@rangu_nighnundi_nepathyam##", nighnundi_nepathyam)
                .replaceAll("@@rangu_vibhagini_nepathyam##", vibhagini_nepathyam)
                ;

        return cssM;
    }
}
