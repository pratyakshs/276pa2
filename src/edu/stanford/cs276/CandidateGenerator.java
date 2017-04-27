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
  public Set<String> getCandidates(String query) throws Exception {
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
    	
    	Set<String> word_candidates = getCandidatesWord(tokens[i]);
    	
    	for(String word_cand : word_candidates){
    		tokens[i] = word_cand;
    		candidates.add(str_arr_to_str(tokens));
    	}

    	tokens[i] = original_word;
    }
    
    // 2 edit distance
    for(int i = 0; i < tokens.length; i++){
    	
    	String original_i_word = tokens[i];
		Set<String> i_word_candidates = getCandidatesWord(tokens[i]);

		for(String i_word_cand : i_word_candidates){
			tokens[i] = i_word_cand;
			
			for(int j = 0; j < tokens.length; j++) {
				if (i == j)
					continue;
				
	    		String original_j_word = tokens[j];
	    		Set<String> j_word_candidates = getCandidatesWord(tokens[j]);
	    		
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
  
  public Set<String> getCandidatesWord(String word) throws Exception {
      Set<String> candidates = new HashSet<String>();
      /*
       * Your code here
       */
      return candidates;
    }

}
