package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Implement {@link EditCostModel} interface. Use the query corpus to learn a model
 * of errors that occur in our dataset of queries, and use this to compute P(R|Q).
 */
public class EmpiricalCostModel implements EditCostModel {
	private static final long serialVersionUID = 1L;
	
	HashMap<String, Integer> del_count = new HashMap<String, Integer>();		//del_x_y - 	xy typed as x
	HashMap<String, Integer> ins_count = new HashMap<String, Integer>();		//ins_x_y - 	x typed as xy
	HashMap<String, Integer> sub_count = new HashMap<String, Integer>();		//sub_x_y - 	y typed as x
	HashMap<String, Integer> trans_count = new HashMap<String, Integer>();		//trans_x_y - 	xy typed as yx
	HashMap<String, Integer> unigram_count = new HashMap<String, Integer>();
	HashMap<String, Integer> bigram_count = new HashMap<String, Integer>();

	
  public EmpiricalCostModel(String editsFile) throws IOException {
    BufferedReader input = new BufferedReader(new FileReader(editsFile));
    System.out.println("Constructing edit distance map...");
    String line = null;
    while ((line = input.readLine()) != null) {
      Scanner lineSc = new Scanner(line);
      lineSc.useDelimiter("\t");
      String noisy = lineSc.next();
      String clean = lineSc.next();
      /*
       * TODO: Your code here
       */
      
      //Count unigrams and bigrams
      char prev = '#';
      for(char c : clean.toCharArray()) {
    	  incr_unigram_count(c);
    	  incr_bigram_count(prev + String.valueOf(c));
    	  prev = c;
      }      
      incr_bigram_count(String.valueOf(prev) + '#');
      
      //Build confusion matrix
      String edit_type = identify_edit(noisy, clean); //<type>$x$y

      if(edit_type.startsWith("del")) {
    	  incr_confusion_matrix(del_count, edit_type);
      } else if (edit_type.startsWith("ins")) {
    	  incr_confusion_matrix(ins_count, edit_type);
      } else if (edit_type.startsWith("sub")) {
    	  incr_confusion_matrix(sub_count, edit_type);
      } else if (edit_type.startsWith("trans")) {
    	  incr_confusion_matrix(trans_count, edit_type);
      } else {
    	  //No edits
      }

    }
    
    input.close();
    System.out.println("Done.");
  }

  
  private void incr_confusion_matrix(HashMap<String, Integer> edit_count_matrix, String edit_key) {
	  if(edit_count_matrix.containsKey(edit_key))
		  edit_count_matrix.put(edit_key, edit_count_matrix.get(edit_key) + 1);
	  else
		  edit_count_matrix.put(edit_key, 1);
  }
    
  private void incr_unigram_count(char c) {
	  String unigram = String.valueOf(c);
	  
	  if(unigram_count.containsKey(unigram))
		  unigram_count.put(unigram, unigram_count.get(unigram) + 1);
	  else
		  unigram_count.put(unigram, 1);
  }
  
  private void incr_bigram_count(String bigram) {
	  if(bigram_count.containsKey(bigram))
		  bigram_count.put(bigram, bigram_count.get(bigram) + 1);
	  else
		  bigram_count.put(bigram, 1);
  }
  
  private String identify_edit(String noisy, String clean) {
	  String edit_type = "";
	  char x = 0;
	  char y = 0;
	  
	  if (noisy.length() > clean.length()) { //ins - x typed as xy
		  edit_type = "ins";
		  
		  for(int i = 0; i < clean.length(); i++) {
			  if (noisy.charAt(i) == clean.charAt(i))
				  continue;
			  
			  if (i > 0) {
				  x = noisy.charAt(i - 1);
				  y = noisy.charAt(i);
				  break;
			  } else {
				  x = '#';
				  y = noisy.charAt(0);
				  break;
			  }
		  }
		  
		  if (x == 0) {
			  x = noisy.charAt(noisy.length() - 2);
			  y = noisy.charAt(noisy.length() - 1);
		  }
	  } else if (noisy.length() < clean.length()) { //del - xy typed as x
		  edit_type = "del";
		  
		  for(int i = 0; i < noisy.length(); i++) {
			  if (noisy.charAt(i) == clean.charAt(i))
				  continue;
			  
			  if (i > 0) {
				  x = clean.charAt(i - 1);
				  y = clean.charAt(i);
				  break;
			  }
			  else {
				  x = '#';
				  y = clean.charAt(0);
				  break;
			  }
		  }

		  if(x == 0) {
			  x = clean.charAt(clean.length() - 2);
			  y = clean.charAt(clean.length() - 1);
		  }
	  } else { //sub or trans
		  for(int i = 0; i < noisy.length(); i++) {
			  char noisy_c = noisy.charAt(i);
			  char clean_c = clean.charAt(i);
			  
			  if(noisy_c == clean_c)
				  continue;
			  
			  if(i == noisy.length() - 1 || noisy.charAt(i + 1) == clean.charAt(i + 1)) { //sub - y typed as x
				  edit_type = "sub";
				  x = noisy_c;
				  y = clean_c;
				  break;
			  } else { //trans - xy typed as yx
				  edit_type = "trans";
				  x = clean_c;
				  y = clean.charAt(i + 1);
				  break;
			  }
		  }
	  }
	  
	  if (!edit_type.equals(""))
		  return edit_type + "$" + x + "$" + y;
	  else
		  return "no_edit";
  }
  
  private int get_count(HashMap<String, Integer> count_map, String key){
	  if(count_map.containsKey(key))
		  return count_map.get(key);
	  else
		  return 0;
  }
  
  // You need to add code for this interface method to calculate the proper empirical cost.
  @Override
  public double editProbability(String original, String R, int distance) {
    /*
     * TODO: Your code here
     */

	  //is R the clean one?	  
	  String clean = R;
	  String noisy = original;
	  
	  double no_edit_prob = Math.log(0.9);
	  
	  if (distance == 0) {
		  return no_edit_prob;
	  } else if (distance  == 1) {
		  return edit_log_prob(identify_edit(noisy, clean));
	  } else if (distance == 2) {
		  double prob = 0.0;
		  
		  for(int i = 0; i < clean.length() && i < noisy.length(); i++) {
			  if(clean.charAt(i) == noisy.charAt(i))
				  continue;
			  
			  //Grab first edit
			  String first_edit = identify_edit(noisy, clean);
			  prob += edit_log_prob(first_edit);
			  
			  //Grab second edit
			  String noisy_postfix = "";
			  String clean_postfix = "";
			  
			  if (first_edit.startsWith("del")) {
				  clean_postfix = clean.substring(i + 1);
				  noisy_postfix = noisy.substring(i);
			  } else if (first_edit.startsWith("ins")) {
				  clean_postfix = clean.substring(i);
				  noisy_postfix = noisy.substring(i + 1);
			  } else if(first_edit.startsWith("sub")) {
				  clean_postfix = clean.substring(i + 1);
				  noisy_postfix = noisy.substring(i + 1);
			  } else if (first_edit.startsWith("trans")) {
				  clean_postfix = clean.substring(i + 2);
				  noisy_postfix = noisy.substring(i + 2);
			  } else {
				  System.out.println("should have caught a second error here");
			  }
			  
			  prob += edit_log_prob(identify_edit(noisy_postfix, clean_postfix));
			  break;
		  }
		  
		  return prob;
	  } else {
		  System.out.println("edit distance is somehow greater than 2");
		  return 0.0;
	  }
  }
  
  private double edit_log_prob(String edit_type) {
	  int num;
	  int denom;
	  
	  String[] edit_tokens = edit_type.split("$");
	  String x = edit_tokens[1];
	  String y = edit_tokens[2];
	  
	  if (edit_type.startsWith("del")) {
		  num = get_count(del_count, edit_type);
		  denom = get_count(bigram_count, x + y);
	  } else if(edit_type.startsWith("ins")) {
		  num = get_count(ins_count, edit_type);
		  denom = get_count(unigram_count, x);
	  } else if(edit_type.startsWith("sub")) {
		  num = get_count(sub_count, edit_type);
		  denom = get_count(unigram_count, y);
	  } else if(edit_type.startsWith("trans")) {
		  num = get_count(trans_count, edit_type);
		  denom = get_count(bigram_count, x + y);
	  } else {
		  System.out.println("error");
		  return -1.0;
	  }
	  
	  return Math.log(num + 1) - Math.log(denom + 38);
  }
}
