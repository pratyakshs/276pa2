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
    	  System.out.println("unidentified edit type");
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
	  
	  return edit_type + "$" + x + "$" + y;
  }
  
  // You need to add code for this interface method to calculate the proper empirical cost.
  @Override
  public double editProbability(String original, String R, int distance) {
    /*
     * TODO: Your code here
     */

	  char[] xy = new char[2];
	  int edit_type = edit_type(R, original, xy);
	  double prob = 0.0;
	  
	  char x = xy[0];
	  char y = xy[1];
	  
	  switch(edit_type) {
		  case 0: //del
			  prob = (float) del[x][y] / bigram_count[x][y];
			  break;
		  case 1: //ins
			  prob = (float) ins[x][y] / char_count[x];
			  break;
		  case 2: //sub
			  prob = (float) sub[x][y] / char_count[y];
			  break;
		  case 3: //trans
			  prob = (float) trans[x][y] / bigram_count[x][y];
			  break;
		  default:
			  System.out.println("doens't fit any of the edit types");
	  }
	  
	  return prob;
  }
  
  private int edit_type(String clean, String noisy, char[] xy) {
	  
	  //need to fill in xy
	  
	  if(clean.length() > noisy.length()) {
		  return 0; //del
	  } else if(clean.length() < noisy.length()) {
		  return 1; //ins
	  } else {
		  for(int i = 0; i < clean.length(); i++) {
			  if(clean.charAt(i) == noisy.charAt(i))
				  continue;
			  
			  if(i == clean.length() - 1 || clean.charAt(i + 1) == noisy.charAt(i + 1))
				  return 2; //sub
			  else
				  return 3; //trans
		  }
	  }
	  
	  return -1;
  }
}
