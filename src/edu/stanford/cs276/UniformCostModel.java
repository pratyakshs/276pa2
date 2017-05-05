package edu.stanford.cs276;

/**
 * Implement {@link EditCostModel} interface by assuming assuming
 * that any single edit in the Damerau-Levenshtein distance is equally likely,
 * i.e., having the same probability
 */
public class UniformCostModel implements EditCostModel {

	private static final long serialVersionUID = 1L;

  @Override
  public double editProbability(String original, String R, int distance) {
	  // TODO: Your code here
	  double edit_prob = 0.1;
	  double no_edit_prob = 0.95;

	  double ret = -1;

	  switch(distance){
		  case 0:
			  ret = no_edit_prob;
			  break;
		  case 1:
			  ret = edit_prob;
			  break;
		  case 2:
			  ret = edit_prob * edit_prob;
			  break;
		  default:
			  ret = 0;
	  }

	  return Math.log(ret);
  }
}
