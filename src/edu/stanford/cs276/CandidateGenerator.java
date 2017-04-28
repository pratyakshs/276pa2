package edu.stanford.cs276;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CandidateGenerator implements Serializable {

	private static final long serialVersionUID = 1L;
	private static CandidateGenerator cg_;

  /**
  * Constructor
  * IMPORTANT NOTE: As in the NoisyChannelModel and LanguageModel classes,
  * we want this class to use the Singleton design pattern.  Therefore,
  * under normal circumstances, you should not change this constructor to
  * 'public', and you should not call it from anywhere outside this class.
  * You can get a handle to a CandidateGenerator object using the static
  * 'get' method below.
  */
  private CandidateGenerator() {}

  public static CandidateGenerator get() throws Exception {
    if (cg_ == null) {
      cg_ = new CandidateGenerator();
    }
    return cg_;
  }

  public static final Character[] alphabet = { 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
      'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', ' ', ',' };

  // Generate all candidates for the target query
  public Set<String> getCandidates(String query, LanguageModel lm) throws Exception {
    Set<String> candidates = new HashSet<String>();
    /*
     * Your code here
     */
    // call getCandidatesWord for all tokens in query
    // take cartesian product...

    String[] tokens = query.split("\\s+");

    // Same query
    candidates.add(query);

    // 1 edit distance
    for(int i = 0; i < tokens.length; i++){
    	String original_word = tokens[i];

    	Set<String> word_candidates = getCandidatesWord(tokens[i], lm);

    	for(String word_cand : word_candidates){
    		tokens[i] = word_cand;
    		candidates.add(str_arr_to_str(tokens));
    	}

    	tokens[i] = original_word;
    }

    // 2 edit distance
    for(int i = 0; i < tokens.length; i++){

    	String original_i_word = tokens[i];
		Set<String> i_word_candidates = getCandidatesWord(tokens[i], lm);

		for(String i_word_cand : i_word_candidates){
			tokens[i] = i_word_cand;

			for(int j = 0; j < tokens.length; j++) {
				if (i == j)
					continue;

	    		String original_j_word = tokens[j];
	    		Set<String> j_word_candidates = getCandidatesWord(tokens[j], lm);

	    		for(String j_word_cand : j_word_candidates){
	    			tokens[j] = j_word_cand;
	            	candidates.add(str_arr_to_str(tokens));
	    		}

	    		tokens[j] = original_j_word;
			}
		}

		tokens[i] = original_i_word;
    }


    return candidates;
  }

  public String str_arr_to_str(String[] tokens){
	  StringBuilder sb = new StringBuilder();
	  	for(String s : tokens){
	  		sb.append(s);
	  	}

	  	return sb.toString();
  }

  boolean isTransposition(String word1, String word2) {
      if (word1.length() != word2.length()) {
          return false;
      }
      int i;
      for (i = 0; i < word1.length(); i++) {
          if (word1.charAt(i) != word2.charAt(i)) {
              break;
          }
      }
      if (i+1 >= word2.length()) {
          return false;
      }
      // check transposition
      if (!((word1.charAt(i+1) == word2.charAt(i)) && (word1.charAt(i) == word2.charAt(i+1)))) {
          return false;
      }

      // compare the rest of the string
      String substr1 = word1.substring(i+2), substr2 = word2.substring(i+2);
      if (substr1.equals(substr2)) {
          return true;
      } else {
          return false;
      }
  }

  public Set<String> getCandidatesWord(String word, LanguageModel lm) throws Exception {
      Set<String> candidates = new HashSet<String>();
      /*
       * Your code here
       */
      // case 1: compare query word with unmodified dictionary word
      // candidate is at zero edit distance from query word
      //    -> do not add to candidates set

      // case 2: compare modified query word with unmodified dictionary word
      for (int k = 0; k < word.length(); k++) {
          StringBuilder sb = new StringBuilder(word);
          sb.deleteCharAt(k);
          String modWord = sb.toString();
          if (lm.unigram.count(modWord) > 0) {
              candidates.add(modWord);
          }

          // case 4: compare modified query word with modified dictionary word
          if (lm.unigramDeletes.containsKey(modWord)) {
              HashSet<String> hs = lm.unigramDeletes.get(modWord);
              for (String candidate : hs) {
                  if (isTransposition(candidate, word)) {
                      candidates.add(candidate);
                  }
              }
          }
      }
      // case 3: compare unmodified query word with modified dictionary word
      if (lm.unigramDeletes.containsKey(word)) {
          HashSet<String> hs = lm.unigramDeletes.get(word);
          candidates.addAll(hs);
      }
      return candidates;
    }
}
