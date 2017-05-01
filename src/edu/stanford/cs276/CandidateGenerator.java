package edu.stanford.cs276;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

  public static boolean containsDigit(String s) {
      if (s != null && !s.isEmpty()) {
          for (char c : s.toCharArray()) {
              if (Character.isDigit(c)) {
                  return true;
              }
          }
      }
      return false;
  }

  // Generate all candidates for the target query
  public Set<String> getCandidates(String query, LanguageModel lm, HashMap<String, Integer> candidate_query_to_distance) throws Exception {
    Set<String> candidates = new HashSet<String>();
    /*
     * Your code here
     */
    // call getCandidatesWord for all tokens in query
    // take cartesian product...

    String[] tokens = query.trim().split("\\s+");

    List<Integer> typoIdx = new ArrayList<Integer>();
    for (int i = 0; i < tokens.length; i++) {
        if (!lm.termDict.containsKey(tokens[i])) {
            typoIdx.add(i);
        }
    }

    if (typoIdx.size() == 0) {
        // Same query
        candidates.add(query);
        candidate_query_to_distance.put(query, 0);

        // 1 edit distance
        for(int i = 0; i < tokens.length; i++){
        	String original_word = tokens[i];

        	Set<String> word_candidates = getCandidatesWord(tokens[i], lm);

        	for(String word_cand : word_candidates){
        		tokens[i] = word_cand;
        		String new_candidate = str_arr_to_str(tokens);
        		candidates.add(new_candidate);
        		candidate_query_to_distance.put(new_candidate, 1);
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
    	    			String new_candidate = str_arr_to_str(tokens);
    	            	candidates.add(new_candidate);
    	            	candidate_query_to_distance.put(new_candidate, 2);
    	    		}

    	    		tokens[j] = original_j_word;
    			}
    		}

    		tokens[i] = original_i_word;
        }
    } else if (typoIdx.size() == 1) {
        // 1 edit distance
        int i = typoIdx.get(0);

        String original_word = tokens[i];
        Set<String> word_candidates = getCandidatesWord(tokens[i], lm);
//        System.out.println(tokens[i] + " " + word_candidates);
        for(String word_cand : word_candidates){
            tokens[i] = word_cand;
            String new_candidate = str_arr_to_str(tokens);
            candidates.add(new_candidate);
            candidate_query_to_distance.put(new_candidate, 1);
        }
        tokens[i] = original_word;

        // 2 edit distance
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
                    String new_candidate = str_arr_to_str(tokens);
                    candidates.add(new_candidate);
                    candidate_query_to_distance.put(new_candidate, 2);
                }

                tokens[j] = original_j_word;
            }
        }
        tokens[i] = original_i_word;

    } else if (typoIdx.size() == 2) {
        int i = typoIdx.get(0), j = typoIdx.get(1);
        String original_i_word = tokens[i];
        Set<String> i_word_candidates = getCandidatesWord(tokens[i], lm);

        for(String i_word_cand : i_word_candidates){
            tokens[i] = i_word_cand;
            String original_j_word = tokens[j];
            Set<String> j_word_candidates = getCandidatesWord(tokens[j], lm);

            for(String j_word_cand : j_word_candidates){
                tokens[j] = j_word_cand;
                String new_candidate = str_arr_to_str(tokens);
                candidates.add(new_candidate);
                candidate_query_to_distance.put(new_candidate, 2);
            }
            tokens[j] = original_j_word;
        }
        tokens[i] = original_i_word;
    }
//    System.out.println(candidates);
    return candidates;
  }

  public String str_arr_to_str(String[] tokens){
	  StringBuilder sb = new StringBuilder();
	  	for(String s : tokens){
	  		sb.append(s + " ");
	  	}

	  	sb.setLength(sb.length() - 1);
	  	return sb.toString();
  }

  boolean isTransposition(String word1, String word2) {
      // transposition or substitution
      if (word1.equals(word2)) {
          return false;
      }
      if (word1.length() != word2.length()) {
          return false;
      }
      int i;
      for (i = 0; i < word1.length(); i++) {
          if (word1.charAt(i) != word2.charAt(i)) {
              break;
          }
      }
      // check if substitution
      String substr1 = word1.substring(i+1), substr2 = word2.substring(i+1);
      if (substr1.equals(substr2)) {
          return true;
      }
      if (i+1 >= word2.length()) {
          return false;
      }
      // check transposition
      if (!((word1.charAt(i+1) == word2.charAt(i)) && (word1.charAt(i) == word2.charAt(i+1)))) {
          return false;
      }

      // compare the rest of the string
      substr1 = word1.substring(i+2);
      substr2 = word2.substring(i+2);
      if (substr1.equals(substr2)) {
          return true;
      } else {
          return false;
      }
  }

  public Set<String> getCandidatesWord(String word, LanguageModel lm) throws Exception {
//      System.out.println("orig word: " + word);
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
//          System.out.println(modWord);

          if (lm.termDict.containsKey(modWord)) {
//              System.out.println("case 2 " + modWord);
              candidates.add(modWord);
          }
//          System.out.println("modWord: " + modWord);
          // case 4: compare modified query word with modified dictionary word
          if (lm.unigramDeletes.containsKey(modWord)) {
//              System.out.println("here11");
              HashSet<Integer> hs = lm.unigramDeletes.get(modWord);
              for (int cTermId : hs) {
                  String candidate = lm.revTermDict.get(cTermId);
                  if ((isTransposition(candidate, word) )) {
//                      System.out.println("case 4 " + candidate);
                      candidates.add(candidate);
                  }
              }
          }
      }
      // case 3: compare unmodified query word with modified dictionary word
      if (lm.unigramDeletes.containsKey(word)) {
          HashSet<Integer> hs = lm.unigramDeletes.get(word);
          for (int cTermId : hs) {
              String candidate = lm.revTermDict.get(cTermId);
              if (lm.termDict.containsKey(candidate)) {
//                  System.out.println("case 3 " + candidate);
                  candidates.add(candidate);
              }
          }
//          candidates.addAll(hs);
      }

      // generate all splits
      for (int k = 1; k < word.length(); k++) {
          String part1 = word.substring(0, k);
          if (lm.termDict.containsKey(part1)) {
              String part2 = word.substring(k);
              if (lm.termDict.containsKey(part2)) {
                  candidates.add(part1 + " " + part2);
              }
          }
      }
//      System.out.println("candidates: " + candidates);
      return candidates;
    }
}
