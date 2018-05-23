package in.bhumiputra.nakshatra.nighantu.anga;

import java.io.*;

public class Id {
    public final File path;
    public final int id;
    public final String peru;
    
    public Id(File path, String peru) {
        this.path= path;
        this.id= this.path.hashCode();
        this.peru= peru;
    }
    
    public Id(String pathString, String peru) {
        this.path= new File(pathString);
        this.id= this.path.hashCode();
        this.peru= peru;
    }
    
    public final File path() {
        return this.path;
    }
    
    public final int id() {
        return this.id;
    }
    
    public final boolean equals(Id id2) {
        return ((this.path.equals(id2.path)) && (this.id== id2.id));
    }
    
    public final int hashCode() {
        return this.id;
    }
}