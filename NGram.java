import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class NGram {

    // Method to generate n-grams from a list of strings
    public static Map<String, Integer> ngrams(int n, String text) {
        Map<String, Integer> nGrams = new HashMap<>();
        String[] words = text.split("\\s+"); // Split text into words

        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder nGram = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) nGram.append(" ");
                nGram.append(words[i + j]);
            }
            String nGramStr = nGram.toString().trim();
            nGrams.put(nGramStr, nGrams.getOrDefault(nGramStr, 0) + 1);
        }

        return nGrams;
    }


    public static Map<String, Float> probability(Map<String, Integer> nGrams) {
        Map<String, Float> probabilities = new HashMap<>();
        Map<String, Float> prefixCounts = new HashMap<>();

        for (Map.Entry<String, Integer> entry : nGrams.entrySet()) {
            String nGram = entry.getKey();
            String prefix = nGram.split(" ")[0];
            prefixCounts.put(prefix, prefixCounts.getOrDefault(prefix, Float.valueOf(0)) + entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : nGrams.entrySet()) {
            String nGram = entry.getKey();
            String prefix = nGram.split(" ")[0];
            Float totalPrefixCount = prefixCounts.get(prefix);
            float probability = entry.getValue() / (float) totalPrefixCount;
            probabilities.put(nGram, probability);
        }

        return probabilities;
    }


}
