package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;


public class RunCorrector {

  public static LanguageModel languageModel;
  public static NoisyChannelModel nsm;

  public static void main(String[] args) throws Exception {

    // Parse input arguments
    String uniformOrEmpirical = null;
    String queryFilePath = null;
    String goldFilePath = null;
    String extra = null;
    BufferedReader goldFileReader = null;

    if (args.length == 2) {
      // Default: run without extra credit code or gold data comparison
      uniformOrEmpirical = args[0];
      queryFilePath = args[1];
    }
    else if (args.length == 3) {
      uniformOrEmpirical = args[0];
      queryFilePath = args[1];
      if (args[2].equals("extra")) {
        extra = args[2];
      } else {
        goldFilePath = args[2];
      }
    }
    else if (args.length == 4) {
      uniformOrEmpirical = args[0];
      queryFilePath = args[1];
      extra = args[2];
      goldFilePath = args[3];
    }
    else {
      System.err.println(
          "Invalid arguments.  Argument count must be 2, 3 or 4 \n"
          + "./runcorrector <uniform | empirical> <query file> \n"
          + "./runcorrector <uniform | empirical> <query file> <gold file> \n"
          + "./runcorrector <uniform | empirical> <query file> <extra> \n"
          + "./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt extra \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
      return;
    }

    if (goldFilePath != null) {
      goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
    }

    // Load models from disk
    languageModel = LanguageModel.load();
    nsm = NoisyChannelModel.load();
    BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
    nsm.setProbabilityType(uniformOrEmpirical);

    String query = null;

    /*
     * Each line in the file represents one query. We loop over each query and find
     * the most likely correction
     */
    while ((query = queriesFileReader.readLine()) != null) {

      String correctedQuery = query;
      /*
       * Your code here: currently the correctQuery and original query are the same
       * Complete this implementation so that the spell corrector corrects the
       * (possibly) misspelled query
       *
       */

      CandidateGenerator cGen = CandidateGenerator.get();

      HashMap<String, Integer> candidate_query_to_distance = new HashMap<String, Integer>();
      Set<String> candidates = cGen.getCandidates(query, languageModel, candidate_query_to_distance);


      String best_candidate = null;
      double best_score = 0;
      for(String candidate : candidates) {
    	  double current_score = score(query, candidate, languageModel, nsm, candidate_query_to_distance.get(candidate));
    	  if(current_score > best_score){
    		  best_score = current_score;
    		  best_candidate = candidate;
    	  }
      }

      correctedQuery = best_candidate;

      if ("extra".equals(extra)) {
        /*
         * If you are going to implement something regarding to running the corrector,
         * you can add code here. Feel free to move this code block to wherever
         * you think is appropriate. But make sure if you add "extra" parameter,
         * it will run code for your extra credit and it will run you basic
         * implementations without the "extra" parameter.
         */
      }

      // If a gold file was provided, compare our correction to the gold correction
      // and output the running accuracy
      if (goldFileReader != null) {
        String goldQuery = goldFileReader.readLine();
        /*
         * You can do any bookkeeping you wish here - track accuracy, track where your solution
         * diverges from the gold file, what type of errors are more common etc. This might
         * help you improve your candidate generation/scoring steps
         */
      }

      /*
       * Output the corrected query.
       * IMPORTANT: In your final submission DO NOT add any additional print statements as
       * this will interfere with the autograder
       */
      System.out.println(correctedQuery);
    }
    queriesFileReader.close();
  }

  private static double P_MLE_w(String w, LanguageModel lm) {
	  double T = (double)lm.unigram.termCount();
	  double count_w = (double)lm.unigram.count(w);
	  
	  return count_w / T;
  }
  
  private static double P_MLE_w2_w1(String w2, String w1, LanguageModel lm) {
	  double count_w1_w2 = (double)lm.bigram.count(w1 + " " + w2);
	  double count_w1 = (double)lm.unigram.count(w1);
	  
	  return count_w1_w2 / count_w1;
  }
  
  private static double P_int_w2_w1(String w2, String w1, LanguageModel lm, double lambda) {
	  return lambda * P_MLE_w(w2, lm) + (1 - lambda) * P_MLE_w2_w1(w2, w1, lm);
  }
  
  //get log probs from lm and ncm, and return score as log prob
  private static double score(String R, String Q, LanguageModel lm, NoisyChannelModel ncm, int edit_distance) {
	  double u = 0.8; //tune this
	  double lambda = 0.1; //tune this
	  
	  String[] tokens = Q.split("\\s+");
	  
	  double lm_log_prob = Math.log(P_MLE_w(tokens[0], lm));
	  
	  for(int i = 1; i < tokens.length; i++)
		  lm_log_prob += Math.log(P_int_w2_w1(tokens[i], tokens[i - 1], lm, lambda));
	  
	  double ncm_log_prob = ncm.getProb(R, Q, edit_distance);
	  
	  return ncm_log_prob + u * lm_log_prob;
	  
	  /*
	  lm_log_prob = lm.getUnigramProb(tokens[0]);
	  
	  for(int i = 1; i < tokens.length; i++)
		  lm_log_prob += lm.getBigramProb(tokens[i], tokens[i - 1]);
	  
	  ncm_log_prob = ncm.getProb(R, Q, edit_distance);
	  
	  return ncm_log_prob + u * lm_log_prob;
	  */
  }
}
