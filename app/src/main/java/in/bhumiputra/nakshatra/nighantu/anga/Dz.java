package in.bhumiputra.nakshatra.nighantu.anga;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

import in.bhumiputra.nakshatra.nighantu.tools.InflaterInputStreamI;
import in.bhumiputra.nakshatra.nighantu.tools.RandomAccessInputStream;

/**
 *implementation pf {@link DictInterface} interface for modelling compressed .dict.dz files.
 *dictzip is just a gzip format with extra field setted in gzip header. in extra field header dictzip stores compressed chunk lengths, so that we can gain advantage with pseudo random access.
 * see RFC1952 for gzip format specification, and dictzip linux man page for dictzip specific header specification.
 * all dictzip specific header computation is delegated to {@link DzHeader} class.
 *
 * we need not to decompress entire big file, but just at maximum one or two very very small chunks.
 * first with chunk info stored in DzHeader, we randomly seek to intended chunk, and then we inflate needed chunk with {@link InflaterInputStreamI} class, which is modified version of standard library's {@link java.util.zip.InflaterInputStream} class.
 *
 */

public class Dz implements DictInterface {


    public final Id id;
    public final File nighF;
    public  final Ifo ifo;

    private RandomAccessInputStream randDz;
    private InflaterInputStreamI dzStream;

    private DzHeader dzHeader;

    public static final boolean isCompressed= true;
    public boolean isValid= true;
    public static final String tag= "Dz";

    public Dz(final Id id, final File nighF, final  Ifo ifo) throws IOException {
        this.id= id;
        this.nighF= nighF;
        this.ifo= ifo;

        this.randDz = new RandomAccessInputStream(this.nighF, "r");
        this.dzHeader = new DzHeader(randDz);
        this.dzStream = new InflaterInputStreamI(randDz, new Inflater(true));
    }



    @Override
    public Id getId() {
        return id;
    }

    @Override
    public boolean isCompressed() {
        return isCompressed;
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public int chunkLength() {
        return dzHeader.chunkLength();
    }







    @Override
    public Entry rawEntryAt(Address address) throws IOException {
        if(address == null) {
            return null;
        }
        int startBlock= address.offset/ dzHeader.chunkLength();
        int spanBlock= (address.offset+ address.len- 1)/ dzHeader.chunkLength();

        int bufSize= (spanBlock- startBlock+ 1)* dzHeader.chunkLength();
        long startBlockAddr= dzHeader.dind().get(startBlock).first;

        randDz.seek(startBlockAddr);
        dzStream.setBufferSize(2*bufSize);
        byte[] buf= new byte[bufSize];
        dzStream.read(buf,0,buf.length);
        int start= address.offset% dzHeader.chunkLength();
        int len= address.len;

        String descr= new String(buf, start, len, "UTF-8");
        Entry entry= new Entry(address.padam, descr);
        return entry;
    }

    @Override
    public Entry entryAt(Address chirunama) throws IOException {
        return rawEntryAt(chirunama);
    }

    @Override
    public ArrayList<Entry> entriesAt(ArrayList<Address> addresses) throws IOException {
        ArrayList<Entry> entries= new ArrayList<>();
        if(addresses == null) {
            return entries;
        }

        for(Address chirunama: addresses) {
            entries.add(entryAt(chirunama));
        }
        return entries;
    }

    @Override
    public STuple<ArrayList<Entry>> entriesTuple(STuple<ArrayList<Address>> adrTuple) throws IOException {
        ArrayList<Entry> shiraB= entriesAt(adrTuple.first);
        ArrayList<Entry> paryayaB= entriesAt(adrTuple.second);
        STuple<ArrayList<Entry>> dvayam= new STuple<>(shiraB, paryayaB);
        return dvayam;
    }
}
