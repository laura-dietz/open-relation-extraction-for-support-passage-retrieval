import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.mpii.clausie.ClausIE;
import de.mpii.clausie.Clause;
import de.mpii.clausie.Proposition;
import de.mpii.clausie.parser.StanfordDepNNParser;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


public class ExampleNNFile {
	
	 private static String serializedClassifier = "/Users/AminaKadry/Downloads/stanford-ner-2015-12-09/classifiers/english.muc.7class.distsim.crf.ser.gz";
	 public static void getClausesAndPropositionsPerFile (String entityid) throws IOException, ClassCastException, ClassNotFoundException
	 {
		 String queryID = entityid.split("_")[0];
		 String allSentencesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allSentences-SIDs.txt";
		 File allSentences = new File(allSentencesFilePath);
		 String allPropositionsFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allPropositions.txt";
		 File allPropositions = new File(allPropositionsFilePath);
		 String allClausesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allClauses.txt";
		 File allClauses = new File(allClausesFilePath);
		 FileInputStream in = new FileInputStream(allSentences);
		 BufferedReader br = new BufferedReader (new InputStreamReader(in));
		 String line = "";
		 PrintWriter cWriter = new PrintWriter(new FileWriter (allClauses, true));
		 PrintWriter pWriter = new PrintWriter(new FileWriter (allPropositions, true));
		 while ((line=br.readLine())!=null)
		 {
			 cWriter = new PrintWriter(new FileWriter (allClauses, true));
			 pWriter = new PrintWriter(new FileWriter (allPropositions, true));
			 String id = line.split("\t")[0];
			 System.out.println("sent\\" + queryID + "_" + entityid + "\\S\\" + id);
			 String sentence = line.split("\t")[1];
			
		        
		    	// Initialize the parser
		        StanfordDepNNParser parser = new StanfordDepNNParser();
		        parser.initParser();
		        
		        // Initialize Stanford CoreNLP NER
		        AbstractSequenceClassifier<CoreLabel> classifier = null;
		        classifier = CRFClassifier.getClassifier(serializedClassifier);
		        
		        
		        // Initialize ClausIE object
		        ClausIE clausIE = new ClausIE();
		        clausIE.getOptions().print(System.out, "# ");
		        
		        // Parse the sentence and measure the parsing time
		        
		        long start = System.currentTimeMillis();
		        clausIE.setSemanticGraph(parser.parse(sentence));
		        long end = System.currentTimeMillis();

		        
		        // Clause detection + propositions generation
		        clausIE.detectClauses();
		        clausIE.generatePropositions();

		        cWriter.write(id + "\t");
		        for (int i=0;i<clausIE.getClauses().size();i++)
		        {
		        	Clause clause = clausIE.getClauses().get(i);
		        	if (i!=(clausIE.getClauses().size()-1))
		        	{
		        		cWriter.write(clause.toString(clausIE.getOptions()) + "\t");
		        	}
		        	else
		        	{
		        		cWriter.write(clause.toString(clausIE.getOptions()));
		        	}	
		        	
		        }
		        cWriter.write("\n");
		        cWriter.close();
		       
		        pWriter.write(id + "\t");
		        for (int i=0;i<clausIE.getPropositions().size();i++)
		        {
		        	Proposition prop = clausIE.getPropositions().get(i);
		        	if (i!=(clausIE.getPropositions().size()-1))
		        	{
		        		pWriter.write(prop.toString() + "\t");
		        	}
		        	else
		        	{
		        		pWriter.write(prop.toString());
		        	}	
		        	
		        }
		        pWriter.write("\n");
		        pWriter.close();
		 }
		 
		 cWriter.close();
		 pWriter.close();	
		
	 }
	 
	 public static void getClausesAndPropositionsPerQuery (String queryid) throws IOException, ClassCastException, ClassNotFoundException
	 {
		 String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
			File f = new File(ROOT_FILE_PATH);
	        File[] allSubFiles=f.listFiles();
	        for (File file : allSubFiles) {
	            if(file.isDirectory())
	            {
	            	String [] absolutePath_split = file.getAbsolutePath().split("/");
	            	String entity_id = absolutePath_split[absolutePath_split.length-1];
	            	System.out.println(entity_id);
	            	getClausesAndPropositionsPerFile(entity_id);
	            }
	        }
	 }
	 
	 public static void main(String[] args) throws IOException, ClassCastException, ClassNotFoundException 
	 {
//		 System.out.println("-----Started query201-----");
//		 getClausesAndPropositionsPerQuery("query201"); 
//		 System.out.println("-----Finished query201-----");
//		 System.out.println("-----Started query204-----");
//		 getClausesAndPropositionsPerQuery("query204"); 
//		 System.out.println("-----Finished query204-----");
//		 System.out.println("-----Started query206-----");
//		 getClausesAndPropositionsPerQuery("query206"); 
//		 System.out.println("-----Finished query206-----");
//		 System.out.println("-----Started query216-----");
//		 getClausesAndPropositionsPerQuery("query216"); 
//		 System.out.println("-----Finished query216-----");
//		 System.out.println("-----Started query201-----");
//		 getClausesAndPropositionsPerQuery("query220"); 
//		 System.out.println("-----Finished query220-----");
//		 System.out.println("-----Started query201-----");
//		 getClausesAndPropositionsPerQuery("query224"); 
//		 System.out.println("-----Finished query224-----");
//		 System.out.println("-----Started query231-----");
//		 getClausesAndPropositionsPerQuery("query231"); 
//		 System.out.println("-----Finished query231-----");
//		 System.out.println("-----Started query234-----");
//		 getClausesAndPropositionsPerQuery("query234"); 
//		 System.out.println("-----Finished query234-----");
//		 System.out.println("-----Started query281-----");
//		 getClausesAndPropositionsPerQuery("query281"); 
//		 System.out.println("-----Finished query281-----");
//		 System.out.println("-----Started query300-----");
//		 getClausesAndPropositionsPerQuery("query300"); 
//		 System.out.println("-----Finished query300-----");
		 
//		 getClausesAndPropositionsPerFile("query300_7");
//		 getClausesAndPropositionsPerFile("query300_8");
//		 getClausesAndPropositionsPerFile("query300_9");
//		 getClausesAndPropositionsPerFile("query220_5");
//		 getClausesAndPropositionsPerFile("query220_6");
		 
		 System.out.println(">>>>>DONE<<<<<");

		 
		 
	 }

	       
}
