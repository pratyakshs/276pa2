package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.cs276.util.Dictionary;

/**
 * LanguageModel class constructs a language model from the training corpus.
 * This model will be used to score generated query candidates.
 *
 * This class uses the Singleton design pattern
 * (https://en.wikipedia.org/wiki/Singleton_pattern).
 */
public class LanguageModel implements Serializable {

	private static final long serialVersionUID = 1L;
  private static LanguageModel lm_;

  Dictionary unigram = new Dictionary();
  Dictionary bigram = new Dictionary();
  HashMap<String, HashSet<String>> unigramDeletes = new HashMap<String, HashSet<String>>();
  HashMap<String, Double> unigramProbability = new HashMap<String, Double>();
  HashMap<String, Double> bigramProbability = new HashMap<String, Double>();

  double lambda = 0.1; // parameter for interpolation

  /*
   * Feel free to add more members here (e.g., a data structure that stores bigrams)
   */

  /**
   * Constructor
   * IMPORTANT NOTE: you should NOT change the access level for this constructor to 'public',
   * and you should NOT call this constructor outside of this class.  This class is intended
   * to follow the "Singleton" design pattern, which ensures that there is only ONE object of
   * this type in existence at any time.  In most circumstances, you should get a handle to a
   * NoisyChannelModel object by using the static 'create' and 'load' methods below, which you
   * should not need to modify unless you are making substantial changes to the architecture
   * of the starter code.
   *
   * For more info about the Singleton pattern, see https://en.wikipedia.org/wiki/Singleton_pattern.
   */
  private LanguageModel(String corpusFilePath) throws Exception {
    constructDictionaries(corpusFilePath);
  }

  /**
   * This method is called by the constructor, and computes language model parameters
   * (i.e. counts of unigrams, bigrams, etc.), which are then stored in the class members
   * declared above.
   */
  public void constructDictionaries(String corpusFilePath) throws Exception {

    System.out.println("Constructing dictionaries...");
    File dir = new File(corpusFilePath);
    for (File file : dir.listFiles()) {
      if (".".equals(file.getName()) || "..".equals(file.getName())) {
        continue; // Ignore the self and parent aliases.
      }
      System.out.printf("Reading data file %s ...\n", file.getName());
      BufferedReader input = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = input.readLine()) != null) {
        /*
         * Remember: each line is a document (refer to PA2 handout)
         * TODO: Your code here
         */
          String[] tokens = line.trim().split("\\s+");
          for (int j = 0; j < tokens.length-1; j++) {
              bigram.add(tokens[j] + " " + tokens[j+1]);
          }
          for (int j = 0; j < tokens.length; j++) {
              unigram.add(tokens[j]);
              for (int k = 0; k < tokens[j].length(); k++) {
                  StringBuilder deleted = new StringBuilder(tokens[j]);
                  deleted.deleteCharAt(k);
                  String newToken = deleted.toString();
                  if (unigramDeletes.containsKey(newToken)) {
                      unigramDeletes.get(newToken).add(tokens[j]);
                  } else {
                      HashSet<String> hs = new HashSet<String>();
                      hs.add(tokens[j]);
                      unigramDeletes.put(tokens[j], hs);
                  }
              }
          }
      }
      System.out.println("unigram termCount = " + unigram.termCount());
      System.out.println("bigram termCount = " + bigram.termCount());
      input.close();
    }

    // compute unigram probability
    // log of the MLE
    double T = (double) unigram.termCount();
    Iterator<Entry<String, Integer>> it = unigram.getIterator();
    while (it.hasNext()) {
        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) it.next();
        String word = (String) pair.getKey();
        Double prob = Math.log((double) unigram.count(word) / T);
        unigramProbability.put(word, prob);
    }

    // compute bigram probability
    // log of the interpolated bigram probability
    it = bigram.getIterator();
    while (it.hasNext()) {
        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) it.next();
        String bigramStr = (String) pair.getKey();
        String[] words = bigramStr.split("\\s+");
        double num = bigram.count(bigramStr);
        double den = unigram.count(words[0]);
        double w2mle = (double) unigram.count(words[1]) / T;
        Double prob = Math.log(lambda * w2mle + (1-lambda) * (num / den));
        bigramProbability.put(bigramStr, prob);
    }
    System.out.println("Done.");
  }

  public Double getUnigramProb(String word) {
      return unigramProbability.get(word);
  }

  public Double getBigramProb(String word1, String word2) {
      String key = word1 + " " + word2;
      return bigramProbability.get(key);
  }
  /**
   * Creates a new LanguageModel object from a corpus. This method should be used to create a
   * new object rather than calling the constructor directly from outside this class
   */
  public static LanguageModel create(String corpusFilePath) throws Exception {
    if (lm_ == null) {
      lm_ = new LanguageModel(corpusFilePath);
    }
    return lm_;
  }

  /**
   * Loads the language model object (and all associated data) from disk
   */
  public static LanguageModel load() throws Exception {
    try {
      if (lm_ == null) {
        FileInputStream fiA = new FileInputStream(Config.languageModelFile);
        ObjectInputStream oisA = new ObjectInputStream(fiA);
        lm_ = (LanguageModel) oisA.readObject();
      }
    } catch (Exception e) {
      throw new Exception("Unable to load language model.  You may not have run buildmodels.sh!");
    }
    return lm_;
  }

  /**
   * Saves the object (and all associated data) to disk
   */
  public void save() throws Exception {
    FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
    ObjectOutputStream save = new ObjectOutputStream(saveFile);
    save.writeObject(this);
    save.close();
  }
}
