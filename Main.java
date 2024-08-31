import java.io.IOException;
import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {

        long startTimePar = System.currentTimeMillis();

        System.out.println("Welcome to parallel mode!");
        System.out.print("Enter the file path: ");

        //read input file
        Scanner sc = new Scanner(System.in);
        String filepath = sc.nextLine();
        File inputFile = new File(filepath);

        //read n - for n grams
        System.out.println("Enter n (size of n grams): ");
        int n = sc.nextInt();

        if (!inputFile.exists()) {
            System.out.println("File not found. Please check the file path.");
            return;
        }

        StringBuilder text1 = new StringBuilder();
        try (Scanner sc1 = new Scanner(inputFile)) {
            while (sc1.hasNextLine()) {
                text1.append(sc1.nextLine()).append(" ");
            }
        }

        String text = text1.toString().trim()
                .toLowerCase()               // Convert to lowercase
                .replaceAll("[^a-zA-Z0-9\\s]", ""); // Remove punctuation


        int cores = Runtime.getRuntime().availableProcessors();
        int chunkSize = text.length() / cores;

        List<String> chunks = new ArrayList<>(); //split text into chunks
        for (int i = 0; i < cores; i++) {
            int start = i * chunkSize;
            int end = (i == cores - 1) ? text.length() : (i + 1) * chunkSize;
            chunks.add(text.substring(start, end));
        }


        try {
            List<NGramTask> tasks = new ArrayList<>();
            List<Thread> threads = new ArrayList<>();

            for (String chunk : chunks) {
                NGramTask task = new NGramTask(chunk, n);
                tasks.add(task);
                Thread thread = new Thread(task);
                threads.add(thread);
                thread.start();
            }

            //wait for all threads to finish
            for (Thread thread : threads) {
                thread.join();  // this could throw exception
            }

            Map<String, Integer> finalNGrams = new HashMap<>();
            Map<String, Float> finalProbabilities = new HashMap<>();

            for (NGramTask task : tasks) {
                task.getNGrams().forEach((key, value) ->
                        finalNGrams.merge(key, value, Integer::sum));
                task.getNGrams().forEach((key, value) ->
                        finalProbabilities.merge(key, Float.valueOf(value), Float::sum));
            }



            // Sort the final n-grams by frequency
            List<Map.Entry<String, Integer>> sortedNGrams = new ArrayList<>(finalNGrams.entrySet());
            sortedNGrams.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));


            // Print or process the final list of n-grams
            System.out.println("NGrams: " + finalNGrams);
            System.out.println("Number of N-Grams of size " + n + " is : " + finalNGrams.size());
            System.out.println("Probabilities: " + finalProbabilities);


        } catch (InterruptedException e) {
            throw new IOException("Thread was interrupted", e);
        }



        long stopTimePar = System.currentTimeMillis();
        System.out.println("Execution time: " + (stopTimePar - startTimePar) + " ms");



    }

}

class NGramTask implements Runnable {
    private final String chunk;
    private final int n;
    public final Map<String, Integer> nGrams = new HashMap<>();
    private final Map<String, Float> probabilities = new HashMap<>();
    private final Map<String, Float> prefixCounts = new HashMap<>();


    //constructor
    public NGramTask(String chunk, int n) {
        this.chunk = chunk;
        this.n = n;

    }

    @Override
    public void run() {
        String[] words = chunk.split("\\s+");
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(" ");
                sb.append(words[i + j]);
            }
            String nGram = sb.toString();
            nGrams.put(nGram, nGrams.getOrDefault(nGram, 0) + 1);
        }

        // Step 2: Calculate the total count for each prefix (first word of the n-gram)
        for (Map.Entry<String, Integer> entry : nGrams.entrySet()) {
            String nGram = entry.getKey();
            String[] nGramWords = nGram.split(" ");
            String prefix = nGram.split(" ")[0];
            prefixCounts.put(prefix, prefixCounts.getOrDefault(prefix, 0.0f) + entry.getValue());
        }

        // Step 3: Calculate the probability P(B|A) for each n-gram
        for (Map.Entry<String, Integer> entry : nGrams.entrySet()) {
            String nGram = entry.getKey();
            String[] nGramWords = nGram.split(" ");
            if (nGramWords.length > 1) {
                String prefix = nGram.split(" ")[0];
                Float totalPrefixCount = prefixCounts.get(prefix);
                float probability = entry.getValue() / totalPrefixCount;
                probabilities.put(nGram, probability);
            }
        }


    }

    public Map<String, Integer> getNGrams() {
        return nGrams;
    }
    // Method to get sorted n-grams by frequency
    public List<Map.Entry<String, Integer>> getSortedNGrams() {
        List<Map.Entry<String, Integer>> sortedNGrams = new ArrayList<>(nGrams.entrySet());
        sortedNGrams.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        return sortedNGrams;
    }
}