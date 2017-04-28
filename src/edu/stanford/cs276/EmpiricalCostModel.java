package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Implement {@link EditCostModel} interface. Use the query corpus to learn a model
 * of errors that occur in our dataset of queries, and use this to compute P(R|Q).
 */
public class EmpiricalCostModel implements EditCostModel {
	private static final long serialVersionUID = 1L;
	
	//way too big? todo - change to hashmap
	int[][] del = new int[256][256];
	int[][] ins = new int[256][256];
	int[][] sub = new int[256][256];
	int[][] trans = new int[256][256];
	
	int[] char_count = new int[256];
	
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
      
      //build_confusion_matrix(noisy, clean);
    }

    input.close();
    System.out.println("Done.");
  }

  private void build_confusion_matrix(String noisy, String clean){
	  
	  //inefficent, will fix later
	  for(char c : clean.toCharArray()) {
		  char_count[c]++;
	  }
	  
	  if(noisy.length() == clean.length()) { //sub or trans
		  
		  for(int i = 0; i < noisy.length(); i++) {
			  char noisy_c = noisy.charAt(i);
			  char clean_c = clean.charAt(i);
			  
			  if(noisy_c == clean_c)
				  continue;
			  
			  if(i == noisy.length() - 1 || noisy.charAt(i + 1) == clean.charAt(i + 1)) {
				  //sub
				  sub[noisy_c][clean_c]++;
				  break;
			  } else {
				  //trans
				  trans[clean_c][clean.charAt(i + 1)]++;
				  break;
			  }
		  }		  
	  } else if (noisy.length() > clean.length()){ //ins
		  for(int i = 0; i < clean.length(); i++) {
			  if (noisy.charAt(i) == clean.charAt(i))
				  continue;
			  
			  if (i > 0) {
				  ins[noisy.charAt(i - 1)][noisy.charAt(i)]++;
				  break;
			  } else {
				  ins[0][noisy.charAt(0)]++;
				  break;
			  }
		  }
		  
		  ins[noisy.charAt(noisy.length() - 2)][noisy.charAt(noisy.length() - 1)]++;
	  } else { //del
		  for(int i = 0; i < noisy.length(); i++) {
			  if (noisy.charAt(i) == clean.charAt(i))
				  continue;
			  
			  if (i > 0) {
				  del[clean.charAt(i - 1)][clean.charAt(i)]++;
				  return;
			  }
			  else {
				  del[0][clean.charAt(0)]++;
				  return;
			  }
		  }
		  
		  del[clean.charAt(clean.length() - 2)][clean.charAt(clean.length() - 1)]++;
	  }
  }
  
  // You need to add code for this interface method to calculate the proper empirical cost.
  @Override
  public double editProbability(String original, String R, int distance) {
    return 0.5;
    /*
     * TODO: Your code here
     */
  }
}
