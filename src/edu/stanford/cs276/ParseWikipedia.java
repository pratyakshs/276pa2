package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ParseWikipedia {

	HashMap<String, Integer> del_count = new HashMap<String, Integer>();		//del_x_y - 	xy typed as x
	HashMap<String, Integer> ins_count = new HashMap<String, Integer>();		//ins_x_y - 	x typed as xy
	HashMap<String, Integer> sub_count = new HashMap<String, Integer>();		//sub_x_y - 	y typed as x
	HashMap<String, Integer> trans_count = new HashMap<String, Integer>();		//trans_x_y - 	xy typed as yx
	HashMap<String, Integer> unigram_count = new HashMap<String, Integer>();
	HashMap<String, Integer> bigram_count = new HashMap<String, Integer>();
	
	public ParseWikipedia(String editsFile) throws IOException {
		
	    BufferedReader input = new BufferedReader(new FileReader(editsFile));
	    String line = null;
	    
    	while ((line = input.readLine()) != null) {
    		line = line.replaceAll("\\s+", "");
    		String[] tokens = line.split(":");
			String clean = tokens[0];
			String[] noisy_arr = tokens[1].split(",");
			
			//Count unigrams and bigrams
			char prev = '~';
		    for(char c : clean.toCharArray()) {
		    	incr_unigram_count(c);
		    	incr_bigram_count(prev + String.valueOf(c));
		    	prev = c;
		    }
		    incr_bigram_count(String.valueOf(prev) + '~');
			
		    //Build confusion matrix
			for(String noisy : noisy_arr) {

				ArrayList<String> edit_types = get_all_edits(clean, noisy);
				
				for(String edit_type : edit_types) {
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
			}
    	}
	    
	    input.close();
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
	  
	  private void incr_confusion_matrix(HashMap<String, Integer> edit_count_matrix, String edit_key) {
		  if(edit_count_matrix.containsKey(edit_key))
			  edit_count_matrix.put(edit_key, edit_count_matrix.get(edit_key) + 1);
		  else
			  edit_count_matrix.put(edit_key, 1);
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
					  x = '~';
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
					  x = '~';
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
				  
				  if (i < noisy.length() - 1 && noisy.charAt(i) == clean.charAt(i + 1) && clean.charAt(i) == noisy.charAt(i + 1)) { //trans - xy typed as yx
					  edit_type = "trans";
					  x = clean_c;
					  y = clean.charAt(i + 1);
					  break;
				  } else { //sub - y typed as x 
					  edit_type = "sub";
					  x = noisy_c;
					  y = clean_c;
					  break;
				  }
			  }
		  }
		  
		  if (!edit_type.equals("")) {
			  return edit_type + "@" + x + "@" + y;
		  }
		  else
			  return "no_edit";
	  }
	  
	  private ArrayList<String> get_all_edits(String clean, String noisy) {
		  ArrayList<String> edit_list = new ArrayList<String>();

		  for(int i = 0; i < clean.length() && i < noisy.length(); i++) {
			  
			  if(clean.charAt(i) == noisy.charAt(i) && i == Math.min(clean.length(), noisy.length()) - 1 && noisy.length() != clean.length()) {
				  //has to be del or ins
				  edit_list.add(identify_edit(noisy.substring(i), clean.substring(i)));				  
				  break;
			  }
			  
			  if(clean.charAt(i) == noisy.charAt(i))
				  continue;
			  
			  String edit = identify_edit(noisy, clean); //<type>@x@y
			  
			  if(edit.equals("no_edit"))
				  break;
			  
			  edit_list.add(edit);
			  
			  //Prepare next edit
			  if (edit.startsWith("del")) {
				  clean = clean.substring(Math.min(i + 1, clean.length()));
				  noisy = noisy.substring(i);
				  i = 0;
			  } else if (edit.startsWith("ins")) {
				  clean = clean.substring(i);
				  noisy = noisy.substring(Math.min(i + 1, noisy.length()));
				  i = 0;
			  } else if(edit.startsWith("sub")) {
				  clean = clean.substring(Math.min(i + 1, clean.length()));
				  noisy = noisy.substring(Math.min(i + 1, noisy.length()));
				  i = 0;
			  } else if (edit.startsWith("trans")) {
				  clean = clean.substring(Math.min(i + 2, clean.length()));
				  noisy = noisy.substring(Math.min(i + 2, noisy.length()));
				  i = 0;
			  } else {
			  }
			  
			  if(clean.length() == 0 || noisy.length() == 0 || clean == null || noisy == null)
				  break;
		  }
		  
		  return edit_list;
	  }
	  
	  public HashMap<String, Integer> get_ins_count() {
		  return ins_count;
	  }
	  
	  public HashMap<String, Integer> get_del_count() {
		  return del_count;
	  }
	  
	  public HashMap<String, Integer> get_sub_count() {
		  return sub_count;
	  }
	  
	  public HashMap<String, Integer> get_trans_count() {
		  return trans_count;
	  }
	  
	  public HashMap<String, Integer> get_unigram_count() {
		  return unigram_count;
	  }
	  
	  public HashMap<String, Integer> get_bigram_count() {
		  return bigram_count;
	  }
}
