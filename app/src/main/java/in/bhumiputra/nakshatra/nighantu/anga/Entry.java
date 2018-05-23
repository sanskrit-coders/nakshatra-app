package in.bhumiputra.nakshatra.nighantu.anga;

/**
 * model for a dictionary entry. wraps 'entry' and 'description'.
 */
public class Entry implements Comparable<Entry> {
    
    public final String entry;
    public final String description;
    
    public Entry(String entry, String description) {
        this.entry = entry;
        this.description = description;
    }
    
    public final String entry() {
        return this.entry;
    }
    
    public final String description() {
        return this.description;
    }
    
    @Override
    public final int compareTo(Entry entry2) {
        return this.entry.compareTo(entry2.entry);
    }
    
    public final boolean equals(Entry entry2) {
        return (this.entry.equals(entry2.entry) && this.description.equals(entry2.description));
    }
    
    public final int hashCode() {
        return (this.entry.hashCode() ^ this.description.hashCode());
    }
    
    public String toString() {
        String str= this.entry + ":-- "+ this.description + "\n";
        return str;
    }
    
}
    
    