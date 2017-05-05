package edu.stanford.cs276;

import java.util.HashMap;

public class BuildModels {

  public static double MU = .05;
  public static LanguageModel languageModel;
  public static NoisyChannelModel noisyChannelModel;

  public static void main(String[] args) throws Exception {
	  
    String trainingCorpus = null;
    String editsFile = null;
    String extra = null;
    if (args.length == 2 || args.length == 3) {
      trainingCorpus = args[0];
      editsFile = args[1];
      if (args.length == 3) extra = args[2];
    } 
    else {
      System.err.println(
          "Invalid arguments.  Argument count must 2 or 3 \n" 
          + "./buildmodels <training corpus dir> <training edit1s file> \n"
          + "./buildmodels <training corpus dir> <training edit1s file> <extra> \n"
          + "SAMPLE: ./buildmodels data/corpus data/edit1s.txt \n"
          + "SAMPLE: ./buildmodels data/corpus data/edit1s.txt extra \n");
      return;
    }
    System.out.println("training corpus: " + args[0]);
    
    languageModel = LanguageModel.create(trainingCorpus);
    noisyChannelModel = NoisyChannelModel.create(editsFile);
    
    if ("extra".equals(extra)) {
        /*
         * If you want to experiment with some form of extra credit in the 
         * model-building process, you can add code to this block.  You should 
         * also feel free to move this block to any other location you feel is 
         * appropriate.  The two things to verify are: 
         * 
         * 1. When you run the assignment scripts WITHOUT the 'extra' parameter, 
         * your basic implementations run correctly, and without any of your 
         * extra credit code.  
         * 
         * 2. When you run the scripts WITH the 'extra' parameter, your extra 
         * credit code runs as expected. 
         */
      	
  		  System.out.println("Constructing edit distance map with wikipedia...");
          ParseWikipedia pw = new ParseWikipedia("spell-errors.txt");
          HashMap<String, Integer> del_count = pw.get_del_count();
          HashMap<String, Integer> ins_count = pw.get_ins_count();
          HashMap<String, Integer> sub_count = pw.get_sub_count();
          HashMap<String, Integer> trans_count = pw.get_trans_count();
          HashMap<String, Integer> unigram_count = pw.get_unigram_count();
          HashMap<String, Integer> bigram_count = pw.get_bigram_count();
          
          System.out.println("del_count size = " + del_count.size());
          System.out.println("ins_count size = " + ins_count.size());
          System.out.println("sub_count size = " + sub_count.size());
          System.out.println("trans_count size = " + trans_count.size());
          System.out.println("unigram_count size = " + unigram_count.size());
          System.out.println("bigram_count_size = " + bigram_count.size());
          
          System.out.println("Merging into empirical counts matricies");
          
          noisyChannelModel.merge_in_wikipedia(del_count, ins_count, sub_count, trans_count, unigram_count, bigram_count);
          
          System.out.println("Done merging.");
      }    
    
    // Save the models to disk
    System.out.println("saving to disk");
    noisyChannelModel.save();
    languageModel.save();
  }
}
