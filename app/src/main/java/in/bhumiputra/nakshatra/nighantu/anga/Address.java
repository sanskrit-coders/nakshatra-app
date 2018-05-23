package in.bhumiputra.nakshatra.nighantu.anga;


/**
 * model for an idx entry. represents word, start offset of description in dict file, length of description.
 */
public class Address {
    
    public final String padam;
    public final int offset;
    public final int len;
    
    public Address(String padam, int offset, int len) {
        this.padam= padam;
        this.offset= offset;
        this.len= len;
    }
    
    public final String padam() {
        return this.padam;
    }
    
    public final int offset() {
        return this.offset;
    }
    
    public final int len() {
        return this.len;
    }
    
    public final boolean equals(Address chirunama2) {
        return ((this.offset== chirunama2.offset) && (this.len== chirunama2.len));
    }

    @Override
    public boolean equals(Object o1) {
        if(!(o1 instanceof Address)) {
            return false;
        }
        return (padam.equals(((Address) o1).padam) && (offset== ((Address) o1).offset) && (len== ((Address) o1).len));
    }

    @Override
    public int hashCode() {
        return (
                padam.hashCode() ^
                        offset ^
                        len
                );
    }
    
    public String toString() {
        String str= "{ padam: "+ padam+ "; offset: "+ offset+ "; len: "+ len+ " }";
        return str;
    }
    
}
    
    