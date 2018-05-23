package in.bhumiputra.nakshatra.nighantu.anga;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;

import in.bhumiputra.nakshatra.nighantu.tools.RandomAccessInputStream;

/**
 * class to model header part in dz file.
 * see RFC1952 for gzip format specification, and dictzip linux man page for dictzip specific header specification.
 */

public class DzHeader {

    public static final int GZIPFLAG_SIZE = 16;
    private BitSet gzipFlag = new BitSet(GZIPFLAG_SIZE);
    private long MTIME;
    private int XFL;
    private int OS;

    private int XLEN;
    private byte SI1;
    private byte SI2;
    private int SLEN;

    private int VER;
    private int CHLEN;
    private int CHCNT;

    private int[] chunkSizes;
    private List<LongTuple> dind;


    //private String ORIGINAL_FILE_NAME; //ignore.
    //private String COMMENT; //ignore.
    private int CRC16;

    private int headerLength;

    private static final int GZIP_MAGIC= 0x8b1f;
    private static final byte DZ_SI1= 0x52;
    private static final byte DZ_SI2= 0x41;

    private static final int FTEXT = 0;      // Content is ASCII text
    private static final int FHCRC = 1;      // Header CRC
    private static final int FEXTRA = 2;     // Extra field
    private static final int FNAME = 3;      // File name
    private static final int FCOMMENT = 4;  // File comment
    private static final int INT32_LEN = 4;
    private static final int EOS_LEN = 2;

    private static final int GZIP_HEADER_LEN = 10;


    public DzHeader(RandomAccessInputStream dzStream) throws IOException {
        read(dzStream);
    }

    private void read(RandomAccessInputStream dzStream) throws IOException{
        try {
            dzStream.seek(0L);

            if(readUShort(dzStream)!= GZIP_MAGIC) {
                throw new IOException("not gzip.");
            }

            if(readUByte(dzStream)!= Deflater.DEFLATED) {
                throw new IOException("Unsupported compression method.");
            }

            int flg = readUByte(dzStream);
            for (int i = 0; i < GZIPFLAG_SIZE; i++) {
                int testbit = 1 << i;
                if ((flg & testbit) == testbit) {
                    gzipFlag.set(i);
                }
            }

            this.MTIME= readUInt(dzStream);
            this.XFL=readUByte(dzStream);
            if(!((XFL== 0x02) || (XFL== 0x04))) {
                throw new IOException("corrupted header.");
            }

            this.OS= readUByte(dzStream);

            if(!gzipFlag.get(FEXTRA)) {
                throw new IOException("not dz compressed. no extra field.");
            }

            this.headerLength = GZIP_HEADER_LEN;

            this.XLEN= readUShort(dzStream);
            headerLength += XLEN+ 2;
            this.SI1= (byte) readUByte(dzStream);
            this.SI2= (byte) readUByte(dzStream);

            if(!((SI1== DZ_SI1) && (SI2== DZ_SI2))) {
                throw new IOException("not dz file. subfield ids not matched with RA");
            }

            this.SLEN= readUShort(dzStream);
            this.VER= readUShort(dzStream);
            this.CHLEN= readUShort(dzStream);
            this.CHCNT= readUShort(dzStream);

            this.chunkSizes= new int[CHCNT];

            for(int i= 0; i< CHCNT; i++) {
                chunkSizes[i]= readUShort(dzStream);
            }


            if(gzipFlag.get(FNAME)) {
                int ubyte;
                while ((ubyte= readUByte(dzStream))!= 0) {
                    headerLength++;
                    //ignored name. TODO record name too, first check what happens if file name is outof ASCII, does it stored in UTF-8? or other encoding?
                }
                headerLength++; //0.
            }

            if(gzipFlag.get(FCOMMENT)) {
                int ubyte;
                while ((ubyte= readUByte(dzStream))!= 0) {
                    headerLength++;
                    //ignored comment.
                }
                headerLength++; //0.

                if(gzipFlag.get(FHCRC)) {
                    int crc= readUShort(dzStream);
                    //TODO should implement crc check.
                    headerLength += 2;
                }
            }

            long modalu= headerLength;
            ArrayList<LongTuple> tJabita= new ArrayList<>();
            for(int podavu: chunkSizes) {
                tJabita.add(new LongTuple(modalu, podavu));
                modalu+= podavu;
            }

            this.dind = Collections.unmodifiableList(tJabita);

        } catch (IOException e) {
            Log.e("DzHeader", "DictZip header error", e);
            throw e;
        }
    }


    public static int readUByte(final InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

    public static int readUShort(final InputStream in) throws IOException { //intel byte order.
        int b = readUByte(in);
        return (readUByte(in) << 8) | b;
    }

    public static long readUInt(final InputStream in) throws IOException { //intel byte order.
        long s = readUShort(in);
        return ((long) readUShort(in) << 16) | s;
    }


    public int[] chunkSizes() {
        return this.chunkSizes;
    }

    public List<LongTuple> dind() {
        return this.dind;
    }

    public int headerLength() {
        return this.headerLength;
    }

    public int noOfChunks() {
        //noOfChunks: tunakala sankhya: chunkCount
        return CHCNT;
    }

    public int chunkLength() {
        return CHLEN;
    }


}
