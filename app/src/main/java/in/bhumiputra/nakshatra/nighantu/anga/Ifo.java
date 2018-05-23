package in.bhumiputra.nakshatra.nighantu.anga;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * model for an ifo file.
 * all values are stored in {@link #ifoMap} as key value pairs. and essential values like bookname are also stored in fields.
 * constructor checks weather all essential values are present, and keeps all values including all other non essentials into ifoMap.
 */

public class Ifo {

    public final Id id;
    public final File ifoF;

    public String version;
    public String bookname;
    public int wordcount;
    public int idxfilesize;

    private LinkedHashMap<String, String> ifoMap;

    public Ifo(final Id id, final File ifoF) throws IOException {
        this.id= id;
        this.ifoF= ifoF;
        this.ifoMap= new LinkedHashMap<>();
        extractIfo(ifoF);
        attributeValues();
    }

    private void extractIfo(File tF) throws IOException {
        try(BufferedReader ifoReader= new BufferedReader(new InputStreamReader(new FileInputStream(tF)))) {
            ifoReader.readLine();
            String[] versionVarusaBhagalu= ifoReader.readLine().split("=");
            if(versionVarusaBhagalu[0].equals("version")) {
                this.version= versionVarusaBhagalu[1];
            }
            else {
                throw new IOException("invalid ifo format.");
            }
            while(true) {
                String varusa= ifoReader.readLine();
                if((varusa== null) || varusa.isEmpty()) {
                    break;
                }
                String[] bhagalu= varusa.split("=");
                ifoMap.put(bhagalu[0], bhagalu[1]);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new IOException("Invalid ifo file.");
        }
    }

    private void attributeValues() throws IOException, NumberFormatException {
        this.bookname= ifoMap.get("bookname");
        if(bookname== null) {
            throw new IOException("ifo has no bookname");
        }

        String wordcountS= ifoMap.get("wordcount");
        if(wordcountS== null) {
            throw new IOException("ifo has no wordcount");
        }
        this.wordcount= Integer.valueOf(wordcountS);

        String idxfilesizeS= ifoMap.get("idxfilesize");
        if(idxfilesizeS== null) {
            throw new IOException("ifo has no idxfilesize");
        }
        this.idxfilesize= Integer.valueOf(idxfilesizeS);

        //remaining info is stored in map, and is not mandatory.
    }

    public Map<String, String> ifoMap() {
        return Collections.unmodifiableMap(ifoMap);
    }
}
