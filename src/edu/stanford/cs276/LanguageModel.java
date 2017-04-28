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
import edu.stanford.cs276.util.Pair;

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
//  Dictionary bigram = new Dictionary();
  HashMap<Pair<Integer, Integer>, Integer> bigram = new HashMap<Pair<Integer, Integer>, Integer>();
  HashMap<Integer, HashSet<Integer>> unigramDeletes = new HashMap<Integer, HashSet<Integer>>();
  HashMap<String, Integer> termDict = new HashMap<String, Integer>();
  HashMap<Integer, String> revTermDict = new HashMap<Integer, String>();
  HashMap<String, Integer> delDict = new HashMap<String, Integer>();
//  HashMap<Integer, String> revDelDict = new HashMap<Integer, String>();

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
    int termCtr = 0, delCtr = 0;
    for (File file : dir.listFiles()) {
//        System.gc();
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

          for (int j = 0; j < tokens.length; j++) {
              int termId = -1;
              if (termDict.containsKey(tokens[j])) {
                  termId = termDict.get(tokens[j]);
              } else {
                  termId = termCtr;
                  termCtr++;
                  termDict.put(tokens[j], termId);
                  revTermDict.put(termId, tokens[j]);
              }

              unigram.add(termId);
              for (int k = 0; k < tokens[j].length(); k++) {
                  StringBuilder deleted = new StringBuilder(tokens[j]);
                  deleted.deleteCharAt(k);
                  String newToken = deleted.toString();
                  int newTermId = -1;
                  if (delDict.containsKey(newToken)) {
                      newTermId = delDict.get(newToken);
                  } else {
                      newTermId = delCtr;
                      delCtr++;
                      delDict.put(newToken, newTermId);
//                      revDelDict.put(newTermId, newToken);
                  }

                  if (unigramDeletes.containsKey(newTermId)) {
                      unigramDeletes.get(newTermId).add(termId);
                  } else {
                      HashSet<Integer> hs = new HashSet<Integer>();
                      hs.add(termId);
                      unigramDeletes.put(newTermId, hs);
                  }
              }
          }
          for (int j = 0; j < tokens.length-1; j++) {
              int termId1 = termDict.get(tokens[j]);
              int termId2 = termDict.get(tokens[j+1]);
              Pair<Integer, Integer> pair = new Pair<Integer, Integer>(termId1, termId2);
              if (bigram.containsKey(pair)) {
                  bigram.put(pair, bigram.get(pair)+1);
              } else {
                  bigram.put(pair, 1);
              }
          }
      }
      System.out.println("unigram termCount = " + unigram.termCount());
      input.close();
    }
    System.out.println("unigrams: " + termDict.size());
    System.out.println("bigrams: " + bigram.size());
    System.out.println("delDict: " + delDict.size());
    System.out.println("Done.");
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
