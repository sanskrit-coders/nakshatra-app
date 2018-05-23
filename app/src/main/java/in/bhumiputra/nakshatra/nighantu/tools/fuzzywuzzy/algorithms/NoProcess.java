package in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.algorithms;

import in.bhumiputra.nakshatra.nighantu.tools.fuzzywuzzy.StringProcessor;

public class NoProcess implements StringProcessor {

    @Override
    public String process(String in) {
        return in;
    }

}
