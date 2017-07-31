import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class New_AnnotationsReader {
	
	//returns entity wikititle of a given file name
	public static String getWikiTitle (String file_name) throws IOException 
	{
		
		String entity_id = file_name.split("-")[0];
		File allWikiTitles = new File("/Users/AminaKadry/Desktop/wikititles.tsv");
		String wikiTitle = "";
		FileInputStream in = new FileInputStream(allWikiTitles);
		BufferedReader br = new BufferedReader (new InputStreamReader(in));
		String line="";
		while ((line=br.readLine())!=null)
		{
			String [] line_split = line.split("\t");
			String wiki_entity_id = line_split[1];
			if (entity_id.equals(wiki_entity_id))
			{
				wikiTitle = line_split[2];
				break;
			}
		}
		return wikiTitle;
	}
	
	//returns a map containing the sentence id as the key and the corresponding label as the value
	public static Map<String, String> getQuesAnnotationsMap (File in_f) throws IOException
	{
		Map<String, String> map = new TreeMap<String,String>();
		FileInputStream in = new FileInputStream(in_f);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		String sent_id = "";
		String label = "";
		while ((line=br.readLine())!=null)
		{
			String[] line_split = line.split("\t") ;
			if (line_split.length==2)
			{
				sent_id = line_split[0];
				label = line_split[1];
				map.put(sent_id,label);			
			}
				
		}
		return map;
	}
	
	//returns a map containing all different entity mentions of an entity
	public static Map<String, String[]> getAllEntityMentionsMap(File entityMentions) throws IOException
	{
		Map<String, String[]> map = new TreeMap<String, String[]>();
		FileInputStream in = new FileInputStream(entityMentions);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		while ((line=br.readLine())!=null)
		{
			
			String [] split = line.split("\t");
			int arrayLength = split.length-1;
			String [] eMentions = new String[arrayLength];
			String entityID = split[0];
			for (int i=0;i<arrayLength;i++)
			{
				eMentions[i] = split[i+1];
			}
			map.put(entityID, eMentions);
			
		}

		return map;
	}
	
	//returns a map containing the sentence id as the key and the corresponding label as the value for ques6
	public static Map<String, String> getAnnotationsMapQ6(File ques6) throws IOException
	{
		Map<String, String> map = new HashMap<String, String>();
		FileInputStream in = new FileInputStream(ques6);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		while ((line=br.readLine())!=null)
		{
			String entityID = line.split("\t")[0];
			String label = line.split("\t")[1];
			map.put(entityID, label);
		}
		return map;
	}
	//returns all entities that are annotated as relevant
	public static void getRelevantEntities(File ques1, File ques6, File out) throws IOException
	{
		FileInputStream in = new FileInputStream(ques1);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		Map<String, String> map = getAnnotationsMapQ6(ques6);
		String line = "";
		PrintWriter writer = new PrintWriter(out, "UTF-8");
		while ((line=br.readLine())!=null)
		{
			
			String entity1ID = line.split("\t")[0].split("\\\\")[1];
			for (Map.Entry<String, String> m:map.entrySet())
			{
				String entity2ID = m.getKey();
				String label2 = m.getValue();
				if (entity1ID.equals(entity2ID))
				{
					if (label2.equals("1"))
					{
						writer.write(line + "\n");
					}
					break;
				}
			}
		}
		writer.close();
	}
	
	//returns a file containing the sentences which include at least one entity mention of a given entity
	public static double getSentencesContainingEntityName(String entityID, File allSentences, File out) throws IOException
	{
		//File allStatistics = new File("/Users/AminaKadry/Desktop/Association_Analysis/Statistics/AllStatistics(New).txt");
		//PrintWriter all = new PrintWriter(new FileWriter(allStatistics, true));
		File entityMentions = new File("/Users/AminaKadry/Desktop/Association_Analysis/EntityMentions.txt");
		Map<String, String []> allEntityMentions = getAllEntityMentionsMap(entityMentions);
		FileInputStream in = new FileInputStream(allSentences);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		boolean found = false;
		String entityTitle = getWikiTitle(allSentences.getName());
		String queryID = allSentences.getName().split("-")[0].split("_")[0];
		PrintWriter writer = new PrintWriter(out, "UTF-8");
		double fraction = 0.0;
		double n1 = 0.0;
		double n2 = 0.0;
		System.out.println(entityID);
		while ((line=br.readLine())!=null)
		{
			n1++;
		}
		System.out.println("Total number of sentences: " + n1);
		
		in.getChannel().position(0);
		br = new BufferedReader(new InputStreamReader(in));
		line = "";
		
		while ((line=br.readLine())!=null)
		{
			found = false;
			String [] split = line.split("\t");
			String sentence = split[1];
			String id = split[0];
			
			for (Map.Entry<String, String[]> m:allEntityMentions.entrySet())
			{
				String mEntityID = m.getKey();
				if (mEntityID.equals(entityID))
				{
					String [] em = m.getValue();
					for (int i=0;i<em.length;i++)
					{
						if (sentence.toLowerCase().contains(em[i].toLowerCase()))
						{
							
							found = true;
							break;
						}
					}
					if (found)
					{
						n2++;
						String sID = "sent\\" + queryID + "_" + entityTitle + "\\S\\" + id;
						writer.write(sID + "\n");
					}
					break;
					
				}
			}
			
		}
		System.out.println("Number of sentences containing relevant entity's name: " + n2);
		fraction = n2/n1;
		System.out.println("fraction is: " + fraction);
		writer.close();
		//all.write(entityID + "\t" + n1 + "\t" + n2 + "\t" + fraction + "%" + "\n");
		//all.close();
		return fraction;
	}
	
	//same as getSentencesContainingEntityName but for all 100 entities
	public static Map<String, Double> getAllSentencesContainingEntityName(String queryid) throws IOException
	{
		Map <String, Double> map = new TreeMap<String, Double>();
		File allSentences = new File("");
		File allSentencesContainingRelevantEntity = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String allSentencesFileName = entity_id + "-allSentences-SIDs.txt";
                String out_FileName = entity_id + "_sContainingRelevantEntity.txt";
                File x = new File(file.getAbsolutePath());
                File [] allXFiles = x.listFiles();
                for (File xFile :  allXFiles)
                {
               	 if (xFile.getName().equals(allSentencesFileName))
                	{
                		allSentences = new File(xFile.getAbsolutePath());
                		
                	}
               	 if (xFile.getName().equals(out_FileName))
                	{
               		 	allSentencesContainingRelevantEntity = new File(xFile.getAbsolutePath());
                		
                	}
                }
                double fraction =  getSentencesContainingEntityName(entity_id, allSentences, allSentencesContainingRelevantEntity);
                map.put(entity_id, fraction);
            }
        }
        
        return map;
	}
	
	public static Map<String, String> getAllProcessedQueryTitles () throws IOException
	{
		File processedQueryTitles = new File("/Users/AminaKadry/Desktop/processed_query_titles_tsv.txt");
		Map<String, String> map = new TreeMap<String, String>();
		FileInputStream in = new FileInputStream(processedQueryTitles);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		while ((line=br.readLine())!=null)
		{
			String [] split = line.split("\t");
			String qID = split[0];
			String qTitle = split[1];
			map.put(qID, qTitle);
		}
		return map;
	}
	
	public static String getProcessedQueryTitle (String qID) throws IOException
	{
		String pQTitle = "";
		Map<String, String> map = getAllProcessedQueryTitles();
		for (Map.Entry<String, String> m:map.entrySet())
		{
			if (qID.equals(m.getKey()))
			{
				pQTitle = m.getValue();
				break;
			}
		}
		return pQTitle;
	}
	
	public static double getSentencesContainingQueryTerm (String entityID, File allSentences, File out) throws IOException
	{
		System.out.println(entityID);
		String queryID = allSentences.getName().split("-")[0].split("_")[0];
		String entityTitle = getWikiTitle(allSentences.getName());
		String pQTitle = getProcessedQueryTitle(queryID);
		String [] allQTerms = pQTitle.split(" ");
		FileInputStream in = new FileInputStream(allSentences);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		boolean found = false;
		String line = "";
		PrintWriter writer = new PrintWriter(out, "UTF-8");
		double n1 = 0.0;
		double n2 = 0.0;
		double fraction = 0.0;
		while ((line=br.readLine())!=null)
		{	
			n1++;
			found = false;
			String id = line.split("\t")[0];
			String s = line.split("\t")[1];
			String[] s_split = s.split(" ");
			for (int i=0;i<s_split.length;i++)
			{
				for (int j=0;j<allQTerms.length;j++)
				{
					if (s_split[i].toLowerCase().equals(allQTerms[j].toLowerCase()))
					{
						found = true;
						n2++;
						break;
					}
				}
				if (found)
				{
					String sID = "sent\\" + queryID + "_" + entityTitle+ "\\S\\" + id;
					writer.write(sID +"\n");
					break;
				}
			}
		}
		writer.close();
		System.out.println("Total number of sentences: " + n1);
		System.out.println("Total number of sentences containing query term: " + n2);
		fraction = n2/n1;
		System.out.println("Fraction: " + fraction);
		return fraction;
	}
	
	public static Map<String, Double> getAllSentencesContainingQueryTerm (String queryid) throws IOException
	{
		Map<String, Double> map = new TreeMap<String, Double>();
		File allSentences = new File("");
		File sContainingQTerm = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	//System.out.println(file.getAbsolutePath());
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String allSentences_FileName = file.getAbsolutePath() + "/" + entity_id + "-allSentences-SIDs.txt";
                allSentences = new File(allSentences_FileName);
                String out_FileName = file.getAbsolutePath() + "/" + entity_id + "_sContainingQueryTerm.txt";
                sContainingQTerm = new File(out_FileName);
                double fraction =  getSentencesContainingQueryTerm(entity_id, allSentences, sContainingQTerm);
                map.put(entity_id, fraction);
            }
        }
		return map;
		
	}
	//returns all sentences containing a relevant relation for a given entity
			public static void getSentencesContainingRelation2 (File out) throws IOException
			{
				File ques3 = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques3-filtered.txt");
				Map<String, String> map = getQuesAnnotationsMap(ques3);
				String entityTitle = getWikiTitle(out.getName());
				String queryID = out.getName().split("-")[0].split("_")[0];
				//System.out.println(queryID);
				entityTitle = queryID + "_" + entityTitle;
				//System.out.println(entityTitle);
				Map<Integer, String> ma = new HashMap<Integer, String>();
				PrintWriter writer = new PrintWriter(out, "UTF-8");
				for (Map.Entry<String, String> m:map.entrySet())
				{
					//System.out.println("entityTitle: " + entityTitle);
					String sID = m.getKey();
					int id = Integer.parseInt(sID.split("\\\\")[3]);
					String label = m.getValue();
					String eID = sID.split("\\\\")[1];
					//System.out.println("eID: " + eID);
					sID = "sent\\" + sID.split("\\\\",2)[1];
					if (eID.equals(entityTitle))
					{
						if (label.equals("1"))
						{
							ma.put(id, label);
						}
						
					}
				}
				
				for (Map.Entry<Integer, String> ma1:ma.entrySet())
				{
					String newID = "sent\\" + entityTitle + "\\S\\" + ma1.getKey();
					writer.write(newID + "\n");
				}
				writer.close();
				
			}
			
	//same as getSentencesContainingRelevantRelation but for a given query
	public static Map<String, Double> getAllSentencesContainingRelation2 (String queryid) throws IOException
	{
			Map<String, Double> map = new TreeMap<String, Double>();
			File allSentencesContainingRelation = new File("");
			File allSentences = new File("");
			String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
			File f = new File(ROOT_FILE_PATH);
		    File[] allSubFiles=f.listFiles();
		    for (File file : allSubFiles) {
		    	if(file.isDirectory())
		        {
		            	String [] absolutePath_split = file.getAbsolutePath().split("/");
		                String entity_id = absolutePath_split[absolutePath_split.length-1];
		                String allSentences_FileName = file.getAbsolutePath() + "/" + entity_id + "-allSentences-SIDs.txt";
		                allSentences = new File(allSentences_FileName);
		                String out_FileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelation(2).txt";
		                allSentencesContainingRelation = new File (out_FileName); 
		                //System.out.println(allSentencesContainingRelation.getAbsolutePath());
		                getSentencesContainingRelation2(allSentencesContainingRelation);
		                FileInputStream in1 = new FileInputStream(allSentences);
		                FileInputStream in2 = new FileInputStream(allSentencesContainingRelation);
		                BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		                BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		                String line1 = "";
		                String line2 = "";
		                double n1 = 0.0;
		                double n2 = 0.0;
		                while ((line1=br1.readLine())!=null)
		                {
		                	n1++;
		                }
		                //System.out.println("Total number of sentences: " + n1);
		                while ((line2=br2.readLine())!=null)
		                {
		                	n2++;
		                }
		                double fraction = n2/n1;
		                //System.out.println("Total number of sentences containing relation: " + n2);
		                System.out.println(n2);
		                //System.out.println("Fraction: " + fraction);
		                map.put(entity_id, fraction);
		        }
		    } 
		    
		    for (Map.Entry<String, Double> m:map.entrySet())
		    {
		    	System.out.println(m.getValue());
		    }
		    
		    return map;
		        
	}
	
	//returns all sentences containing a relevant relation for a given entity
		public static void getSentencesContainingRelevantRelation3(File out) throws IOException
		{
			File ques2 = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques2-filtered.txt");
			Map<String, String> map = getQuesAnnotationsMap(ques2);
			String entityTitle = getWikiTitle(out.getName());
			String queryID = out.getName().split("-")[0].split("_")[0];
			//System.out.println(queryID);
			entityTitle = queryID + "_" + entityTitle;
			//System.out.println(entityTitle);
			Map<Integer, String> ma = new HashMap<Integer, String>();
			PrintWriter writer = new PrintWriter(out, "UTF-8");
			for (Map.Entry<String, String> m:map.entrySet())
			{
				//System.out.println("entityTitle: " + entityTitle);
				String sID = m.getKey();
				int id = Integer.parseInt(sID.split("\\\\")[3]);
				String label = m.getValue();
				String eID = sID.split("\\\\")[1];
				//System.out.println("eID: " + eID);
				sID = "sent\\" + sID.split("\\\\",2)[1];
				if (eID.equals(entityTitle))
				{
					
					if (label.equals("1"))
					{
						ma.put(id, label);
					}
					
				}
			}
			
			for (Map.Entry<Integer, String> ma1:ma.entrySet())
			{
				String newID = "sent\\" + entityTitle + "\\S\\" + ma1.getKey();
				writer.write(newID + "\n");
			}
			writer.close();
			
		}
		
		//same as getSentencesContainingRelevantRelation3 but for a given query + returns fraction of relevant relations out of all relations
		public static Map<String, Double> getAllSentencesContainingRelevantRelation3 (String queryid) throws IOException
		{
			Map<String, Double> map = new TreeMap<String, Double>();
			File allSentencesContainingRelevantRelation2 = new File("");
			File sContainingRelation = new File("");
			//File sContainingRelevantRelation3 = new File("");
			
			String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
			File f = new File(ROOT_FILE_PATH);
	        File[] allSubFiles=f.listFiles();
	        for (File file : allSubFiles) {
	        	
	            if(file.isDirectory())
	            {
	            	String [] absolutePath_split = file.getAbsolutePath().split("/");
	                String entity_id = absolutePath_split[absolutePath_split.length-1];
	                String out_FileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantRelation(2).txt";
	                allSentencesContainingRelevantRelation2 = new File (out_FileName); 
	                String sContainingRelation_fileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelation(2).txt";
	                sContainingRelation = new File(sContainingRelation_fileName);
	                String sContainingRelevantRelation3_fileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantRelation(3).txt";
	                File sContainingRelevantRelation3 = new File(sContainingRelevantRelation3_fileName);
	                PrintWriter writer = new PrintWriter(sContainingRelevantRelation3, "UTF-8");
	                getSentencesContainingRelevantRelation3(allSentencesContainingRelevantRelation2);
	                FileInputStream in1 = new FileInputStream(sContainingRelation);
	                FileInputStream in2 = new FileInputStream(allSentencesContainingRelevantRelation2);
	                BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
	                BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
	                String line1 = "";
	                String line2 = "";
	                double n1 = 0.0;
	                double n2 = 0.0;
	                double fraction = 0.0;
	                while ((line1=br1.readLine())!=null)
	                {
	                	n1++;
	                }
	                //System.out.println("Total number of sentences containing relation: " + n1);
	                
	                while ((line2=br2.readLine())!=null)
	                {
	                	while ((line1=br1.readLine())!=null)
	                	{
	                		if (line2.equals(line1))
	                		{
	                			n2++;
	                			writer.write(line2 + "\n");
	                			break;
	                		}
	                	}
	                	in1.getChannel().position(0);
	                	br1 = new BufferedReader(new InputStreamReader(in1));
	                	line1="";
	                }
	                
	               // System.out.println("Total number of sentences containing relevant relation: " + n2);
	                System.out.println(n2);
	                fraction = n2/n1;
	                //System.out.println("Fraction: " + fraction);
	                map.put(entity_id, fraction);
	                writer.close();
	            }
	        }
	        
	       
	        for (Map.Entry<String, Double> m:map.entrySet())
	        {
	        	System.out.println(m.getValue());
	        }
	        return map;
		}
		//returns all sentences containing a ClausIE extraction for a given entity
		public static void getSentencesContainingClausieExtraction(File out) throws IOException
		{
			File ques4 = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques4-filtered.txt");
			Map<String, String> map = getQuesAnnotationsMap(ques4);
			String entityTitle = getWikiTitle(out.getName());
			String queryID = out.getName().split("-")[0].split("_")[0];
			//System.out.println(queryID);
			entityTitle = queryID + "_" + entityTitle;
			//System.out.println(entityTitle);
			Map<Integer, String> ma = new HashMap<Integer, String>();
			PrintWriter writer = new PrintWriter(out, "UTF-8");
			for (Map.Entry<String, String> m:map.entrySet())
			{
				//System.out.println("entityTitle: " + entityTitle);
				String sID = m.getKey();
				int id = Integer.parseInt(sID.split("\\\\")[3].split("-")[0]);
				String label = m.getValue();
				String eID = sID.split("\\\\")[1];
				//System.out.println("eID: " + eID);
				sID = "sent\\" + sID.split("\\\\",2)[1];
				if (eID.equals(entityTitle))
				{
					if (label.equals("1"))
					{
						ma.put(id, label);
					}
					
				}
			}
			
			for (Map.Entry<Integer, String> ma1:ma.entrySet())
			{
				String newID = "sent\\" + entityTitle + "\\S\\" + ma1.getKey();
				writer.write(newID + "\n");
			}
			writer.close();
		}
		
		//same as getSentencesContainingClausieExtraction but for a given query
	public static Map<String, Double> getAllSentencesContainingClausieExtraction (String queryid) throws IOException
	{
		Map<String, Double> map = new TreeMap<String, Double> ();
		File allSentencesContainingClausieExtraction = new File("");
		File allSentences = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
		File[] allSubFiles=f.listFiles();
		for (File file : allSubFiles) {
			if(file.isDirectory())
			 {
			     String [] absolutePath_split = file.getAbsolutePath().split("/");
			     String entity_id = absolutePath_split[absolutePath_split.length-1];
			           String out_FileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingClausieExtraction.txt";
			           allSentencesContainingClausieExtraction = new File (out_FileName); 
			           String allSentences_fileName = file.getAbsolutePath() + "/" + entity_id + "-allSentences-SIDs.txt";
			           allSentences = new File(allSentences_fileName);
			           getSentencesContainingClausieExtraction(allSentencesContainingClausieExtraction);
			           FileInputStream in1 = new FileInputStream(allSentences);
		                FileInputStream in2 = new FileInputStream(allSentencesContainingClausieExtraction);
		                BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		                BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		                String line1 = "";
		                String line2 = "";
		                double n1 = 0.0;
		                double n2 = 0.0;
		                while ((line1=br1.readLine())!=null)
		                {
		                	n1++;
		                }
		                //System.out.println("Total number of sentences: " + n1);
		                while ((line2=br2.readLine())!=null)
		                {
		                	n2++;
		                }
		                double fraction = n2/n1;
		                //System.out.println("Total number of sentences containing clausie extraction: " + n2);
		               System.out.println(n2);
		                //System.out.println("Fraction: " + fraction);
		                map.put(entity_id, fraction);
			       }
			}
			
			for (Map.Entry<String, Double> m:map.entrySet())
			{
				System.out.println(m.getValue());
			}
			return map;
		}		
		
		//returns all sentences containing a ClausIE extraction for a given entity
		public static void getSentencesContainingRelevantClausieExtraction(File out) throws IOException
		{
			File ques5 = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques5-filtered.txt");
			Map<String, String> map = getQuesAnnotationsMap(ques5);
					String entityTitle = getWikiTitle(out.getName());
					String queryID = out.getName().split("-")[0].split("_")[0];
					//System.out.println(queryID);
					entityTitle = queryID + "_" + entityTitle;
					//System.out.println(entityTitle);
					Map<Integer, String> ma = new HashMap<Integer, String>();
					PrintWriter writer = new PrintWriter(out, "UTF-8");
					for (Map.Entry<String, String> m:map.entrySet())
					{
						//System.out.println("entityTitle: " + entityTitle);
						String sID = m.getKey();
						int id = Integer.parseInt(sID.split("\\\\")[3].split("-")[0]);
						String label = m.getValue();
						String eID = sID.split("\\\\")[1];
						//System.out.println("eID: " + eID);
						sID = "sent\\" + sID.split("\\\\",2)[1];
						if (eID.equals(entityTitle))
						{
							if (label.equals("1"))
							{
								ma.put(id, label);
							}
							
						}
					}
					
					for (Map.Entry<Integer, String> ma1:ma.entrySet())
					{
						String newID = "sent\\" + entityTitle + "\\S\\" + ma1.getKey();
						writer.write(newID + "\n");
					}
					writer.close();
		}
				
		//same as getSentencesContainingRelevantClausieExtraction but for a given query
	public static Map<String, Double> getAllSentencesContainingRelevantClausieExtraction (String queryid) throws IOException
	{
					Map<String, Double> map = new TreeMap<String, Double>();
					File allSentencesContainingRelevantClausieExtraction = new File("");
					File sContainingClausieExtraction = new File("");
					String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
					File f = new File(ROOT_FILE_PATH);
			        File[] allSubFiles=f.listFiles();
			        for (File file : allSubFiles) {
			            if(file.isDirectory())
			            {
			            	String [] absolutePath_split = file.getAbsolutePath().split("/");
			                String entity_id = absolutePath_split[absolutePath_split.length-1];
			                String out_FileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantClausieExtraction.txt";
			                allSentencesContainingRelevantClausieExtraction = new File (out_FileName); 
			                String sContainingClausieExtraction_fileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingClausieExtraction.txt";
			                sContainingClausieExtraction = new File(sContainingClausieExtraction_fileName);
			                getSentencesContainingRelevantClausieExtraction(allSentencesContainingRelevantClausieExtraction);
			                String sContainingRelevantClausieExtraction2_fileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantClausieExtraction(2).txt";
			                File sContainingRelevantClausieExtraction2 = new File(sContainingRelevantClausieExtraction2_fileName);
			                PrintWriter writer = new PrintWriter(sContainingRelevantClausieExtraction2, "UTF-8");
			                FileInputStream in1 = new FileInputStream(sContainingClausieExtraction);
			                FileInputStream in2 = new FileInputStream(allSentencesContainingRelevantClausieExtraction);
			                BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
			                BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			                String line1 = "";
			                String line2 = "";
			                double n1 = 0.0;
			                double n2 = 0.0;
			                double fraction = 0.0;
			                while ((line1=br1.readLine())!=null)
			                {
			                	n1++;
			                }
			               // System.out.println("Total number of sentences containing clausie extraction: " + n1);
			                
			                while ((line2=br2.readLine())!=null)
			                {
			                	while ((line1=br1.readLine())!=null)
			                	{
			                		if (line2.equals(line1))
			                		{
			                			n2++;
			                			writer.write(line2 + "\n");
			                			break;
			                		}
			                	}
			                	in1.getChannel().position(0);
			                	br1 = new BufferedReader(new InputStreamReader(in1));
			                	line1="";
			                }
			                writer.close();
			               //System.out.println("Total number of sentences containing relevant clausie extraction: " + n2);
			                System.out.println(n2);
			                fraction = n2/n1;
			                //System.out.println("Fraction: " + fraction);
			                map.put(entity_id, fraction);
			            }
			 }  
			        
			        for (Map.Entry<String, Double> m:map.entrySet())
			        {
			        	System.out.println(m.getValue());
			        }
			        return map;
		}
				
		//returns the fraction of sentences with relevant clausie extractions out of sentences containing relevant relation
		public static double getFractionOfSentencesWithRelevantClausieExtractions (File sContainingRelevantRelation3, File sContainingRelevantClausieExtraction, File out) throws IOException
		{
			String queryid = out.getName().split("-")[0].split("_")[0];
			String entityTitle = getWikiTitle(out.getName());
			entityTitle = queryid + "_" + entityTitle;
			System.out.println(entityTitle);
			double fraction = 0.0;
			FileInputStream in1 = new FileInputStream(sContainingRelevantClausieExtraction);
			FileInputStream in2 = new FileInputStream(sContainingRelevantRelation3);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
			BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
			String line1 = "";
			String line2 = "";
			double n1 = 0.0;
			double n2 = 0.0;
			PrintWriter writer = new PrintWriter(out, "UTF-8");
			while ((line2=br2.readLine())!=null)
			{
				n2++;
			}
			System.out.println("Total number of sentences containing relevant relation: " + n2);
			in2.getChannel().position(0);
			br2 = new BufferedReader(new InputStreamReader(in2));
			line2 = "";
			
			while ((line1=br1.readLine())!=null)
			{
				while ((line2=br2.readLine())!=null)
				{
					if (line1.equals(line2))
					{
						
						n1++;
						writer.write(line1 + "\n");
						break;
					}
				}
				in2.getChannel().position(0);
				br2 = new BufferedReader(new InputStreamReader(in2));
				line2 = "";
			}
			writer.close();
			System.out.println("Total number of sentences with relevant ClausIE extraction: " + n1);
			fraction = n1/n2;
			System.out.println("Fraction: " + fraction);
			
			return fraction;
		}
			
	
	//returns a file containing all sentences which explain the entity relevance of a given entity
	public static double getSentencesExplainingEntityRelevance2 (String entityID, File allSentences, File out) throws IOException
	{
		//System.out.println(entityID);
		double fraction = 0.0;
		File ques1 = new File ("/Users/AminaKadry/Desktop/Association_Analysis/ques1-filtered.txt");
		FileInputStream in = new FileInputStream(allSentences);
		BufferedReader br = new BufferedReader (new InputStreamReader(in));
		String line = "";
		double n1 = 0.0;
		double n2= 0.0;
		String entityTitle = getWikiTitle(allSentences.getName());
		String queryID = allSentences.getName().split("-")[0].split("_")[0];
		String sID = "";
		PrintWriter writer = new PrintWriter(out, "UTF-8");
		while ((line=br.readLine())!=null)
		{
			n1++;
		}
		
		//System.out.println("Total number of sentences: " + n1);
		in.getChannel().position(0);
		br = new BufferedReader (new InputStreamReader(in));
		line = "";
		
		Map<String, String> map = getQuesAnnotationsMap(ques1);
		while ((line=br.readLine())!=null)
		{
			String [] split = line.split("\t");
			String id = split[0];
			sID = "sent\\" + queryID + "_" + entityTitle + "\\S\\" + id;
			for (Map.Entry<String, String> m:map.entrySet())
			{
				String sentenceID = m.getKey();
				String label = m.getValue();
				if (sID.equals(sentenceID))
				{
					if (label.equals("1"))
					{
						writer.write(m.getKey() + "\n");
						n2++;
						break;
					}
				}
			}
			
		}
		
		writer.close();
		
		//System.out.println("Number of sentences explaining entity relevance: " + n2);
		System.out.println(n2);
		fraction = (n2/n1);
		//System.out.println("Fraction: " + fraction);
		return fraction;
	}
	
	//same as getSentencesExplainingEntityRelevance2 but for all 100 entities
	public static Map<String, Double> getAllSentencesExplainingEntityRelevance2(String queryid) throws IOException
	{
		Map<String, Double> map = new TreeMap<String, Double>();
		File allSentencesExplainingEntityRelevance = new File("");
		File allSentences = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	//System.out.println(file.getAbsolutePath());
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String allSentences_FileName = entity_id + "-allSentences-SIDs.txt";
                String out_FileName = file.getAbsolutePath() + "/" + entity_id + "_sExpEntityRelevance(2).txt";
                //System.out.println(out_FileName);
                allSentencesExplainingEntityRelevance = new File (out_FileName);
                File x = new File(file.getAbsolutePath());
                File [] allXFiles = x.listFiles();
                for (File xFile :  allXFiles)
                {
               	 
               	 if (xFile.getName().equals(allSentences_FileName))
                	{
               		 allSentences = new File(xFile.getAbsolutePath());
                		
                	}

                }
                
                double fraction =  getSentencesExplainingEntityRelevance2(entity_id, allSentences, allSentencesExplainingEntityRelevance);
                
                map.put(entity_id, fraction);
            }
        }
        System.out.println("----");
        for (Map.Entry<String, Double> m:map.entrySet())
        {
        	System.out.println(m.getValue());
        }
		return map;
	}
	
	

	
	
	
	public static Map<String, Double> getAllFractionOfSentencesWithRelevantClausieExtractions (String queryid) throws IOException
	{
		Map<String, Double> map = new TreeMap<String, Double>();
		File allSentencesContainingRelevantClausieExtraction = new File("");
		File allSentencesContainingRelevantRelation = new File("");
		File out = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String out_FileName = file.getAbsolutePath() + "/" + entity_id + "-fractionRelevantClausieExtraction.txt";
                out = new File (out_FileName); 
                String sRelevantClausieExtraction_fileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantClausieExtraction.txt";
                allSentencesContainingRelevantClausieExtraction  = new File(sRelevantClausieExtraction_fileName);
                String sRelevantRelation_fileName = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantRelation(3).txt";
                allSentencesContainingRelevantRelation = new File(sRelevantRelation_fileName);
                getFractionOfSentencesWithRelevantClausieExtractions(allSentencesContainingRelevantRelation, allSentencesContainingRelevantClausieExtraction, out);          
            }
        }    
		return map;
	}
	
	//returns fraction of ground truth out of all predicted
	public static double getPrecision (File predicted, File groundTruth) throws IOException
	{
		String entityID = predicted.getName().split("_")[0] + "_" +  predicted.getName().split("_")[1];
		entityID = entityID.split("-")[0];
		System.out.println(entityID);
		double precision = 0.0;
		FileInputStream in1 = new FileInputStream(predicted);
		FileInputStream in2 = new FileInputStream(groundTruth);
		BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		String line1 = "";
		String line2 = "";
		double TP = 0.0;
		double FP = 0.0;
		boolean found = false;
		while ((line1=br1.readLine())!=null)
		{
			found = false;
			while ((line2=br2.readLine())!=null)
			{
				if (line1.equals(line2))
				{
					found = true;
					break;
				}
			}
			if (found)
			{
				TP++;
			}
			else
			{
				FP++;
			}
			in2.getChannel().position(0);
			br2 = new BufferedReader(new InputStreamReader(in2));
			line2 = "";
		}
		//System.out.println("TP: " + TP);
		//System.out.println("FP: " + FP);
		double den = TP + FP;
		precision = TP/den;
		System.out.println("Precision = " + TP + "/" + den + "= " +  precision);
		return precision;	
	}
	
	//returns fraction of precited out of all ground truth
	public static double getRecall (File predicted, File groundTruth) throws IOException
	{
		String entityTitle = predicted.getName().split("_")[0] + "_" +  predicted.getName().split("_")[1];
		//System.out.println(entityTitle);
		double recall = 0.0;
		FileInputStream in1 = new FileInputStream(predicted);
		FileInputStream in2 = new FileInputStream(groundTruth);
		BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		String line1 = "";
		String line2 = "";
		double TP = 0.0;
		double FN = 0.0;
		boolean found = false;
		while ((line2=br2.readLine())!=null)
		{
			found = false;
			while ((line1=br1.readLine())!=null)
			{
				if (line1.equals(line2))
				{
					found = true;
					break;
				}
			}
			if (found)
			{
				TP++;
			}
			else
			{
				FN++;
			}
			
			in1.getChannel().position(0);
			br1 = new BufferedReader(new InputStreamReader(in1));
			line1 = "";
		}
		//System.out.println("TP: " + TP);
		//System.out.println("FN: " + FN);
		double den = TP + FN;
		recall = TP/den;
		System.out.println("Recall = " + TP + "/" + den + "= " +  recall);
		return recall;	
	}
	
	//predicts s explaining relevance (predicted) when s expresses relation (ground truth)
	public static Map<String, double []> getPRRelationExpRelevance (String queryid) throws IOException
	{
		Map<String, double[]> map = new TreeMap<String, double[]>();
		double [] values = new double [2];
		File allSentencesExpEntityRelevance = new File("");
		File allSentencesExpressingRelation = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String sExpEntityRelevance_Filename = file.getAbsolutePath() + "/" + entity_id + "_sExpEntityRelevance(2).txt";
                allSentencesExpEntityRelevance = new File(sExpEntityRelevance_Filename);
                String sContainingRelation_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelation(2).txt";
                allSentencesExpressingRelation = new File(sContainingRelation_Filename);
                double precision = getPrecision(allSentencesExpEntityRelevance, allSentencesExpressingRelation);
                double recall = getRecall(allSentencesExpEntityRelevance, allSentencesExpressingRelation);
                values[0] = precision;
                values[1] = recall;
                map.put(entity_id, values);
            }
        }
		return map;
	}
	
	//predicts s explaining relevance (predicted) when s expresses relevant relation (ground truth)
	public static Map<String, double[]> getPRRelevantRelationExpRelevance(String queryid) throws IOException
	{
		Map<String, double[]> map = new TreeMap<String, double[]>();
		double [] values = new double [2];
		File allSentencesExpEntityRelevance = new File("");
		File allSentencesContainingRelevantRelation = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String sExpEntityRelevance_Filename = file.getAbsolutePath() + "/" + entity_id + "_sExpEntityRelevance(2).txt";
                allSentencesExpEntityRelevance = new File(sExpEntityRelevance_Filename);
                String sContainingRelevantRelation_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantRelation(3).txt";
                allSentencesContainingRelevantRelation = new File(sContainingRelevantRelation_Filename);
                double precision = getPrecision(allSentencesExpEntityRelevance, allSentencesContainingRelevantRelation);
                double recall = getRecall(allSentencesExpEntityRelevance, allSentencesContainingRelevantRelation);
                values[0] = precision;
                values[1] = recall;
                map.put(entity_id, values);
            }
        }
        
		return map;
	}	
	
	//measures precision and recall of clausie extractions (predicted) out of s expressing relation (ground truth)
	public static Map<String, double []> getPRClausieRelation (String queryid) throws IOException
	{
		Map<String, double[]> map = new TreeMap<String, double[]>();
		double [] values = new double [2];
		File allSentencesContainingRelation = new File("");
		File allSentencesContainingClausie = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String sContainingRelation_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelation(2).txt";
                allSentencesContainingRelation = new File(sContainingRelation_Filename);
                String sContainingClausie_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingClausieExtraction.txt";
                allSentencesContainingClausie = new File(sContainingClausie_Filename);
                double precision = getPrecision(allSentencesContainingClausie, allSentencesContainingRelation);
                double recall = getRecall(allSentencesContainingClausie, allSentencesContainingRelation);
                values[0] = precision;
                values[1] = recall;
                map.put(entity_id, values);
            }
        }
        
		return map;
	}
	
	//measures precision and recall of relevant clausie extractions (predicted) out of s expressing relevant relation (ground truth)
		public static Map<String, double []> getPRRelevantClausieRelevantRelation (String queryid) throws IOException
		{
			Map<String, double[]> map = new TreeMap<String, double[]>();
			double [] values = new double [2];
			File allSentencesContainingRelevantRelation = new File("");
			File allSentencesContainingRelevantClausie = new File("");
			String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
			File f = new File(ROOT_FILE_PATH);
	        File[] allSubFiles=f.listFiles();
	        for (File file : allSubFiles) {
	            if(file.isDirectory())
	            {
	            	String [] absolutePath_split = file.getAbsolutePath().split("/");
	                String entity_id = absolutePath_split[absolutePath_split.length-1];
	                String sContainingRelevantRelation_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantRelation(3).txt";
	                allSentencesContainingRelevantRelation = new File(sContainingRelevantRelation_Filename);
	                String sContainingRelevantClausie_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantClausieExtraction(2).txt";
	                allSentencesContainingRelevantClausie = new File(sContainingRelevantClausie_Filename);
	                double precision = getPrecision(allSentencesContainingRelevantClausie, allSentencesContainingRelevantRelation);
	                double recall = getRecall(allSentencesContainingRelevantClausie, allSentencesContainingRelevantRelation);
	                values[0] = precision;
	                values[1] = recall;
	                map.put(entity_id, values);
	            }
	        }
	        
			return map;
		}
	
	//predicts s explaining relevance (predicted) when s contains clausie extraction (ground truth)
	public static Map<String, double[]> getPRClausieExpRelevance(String queryid) throws IOException
	{
		Map<String, double[]> map = new TreeMap<String, double[]>();
		double [] values = new double [2];
		File allSentencesExpEntityRelevance = new File("");
		File allSentencesContainingClausie = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String sExpEntityRelevance_Filename = file.getAbsolutePath() + "/" + entity_id + "_sExpEntityRelevance(2).txt";
                allSentencesExpEntityRelevance = new File(sExpEntityRelevance_Filename);
                String sContainingClausie_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingClausieExtraction.txt";
                allSentencesContainingClausie = new File(sContainingClausie_Filename);
                double precision = getPrecision(allSentencesExpEntityRelevance, allSentencesContainingClausie);
                double recall = getRecall(allSentencesExpEntityRelevance, allSentencesContainingClausie);
                values[0] = precision;
                values[1] = recall;
                map.put(entity_id, values);
            }
        }
        
		return map;
	}
	
	//predicts s explaining relevance (predicted) when s expresses relevant clausie extraction (ground truth)
	public static Map<String, double[]> getPRRelevantClausieExpRelevance(String queryid) throws IOException
	{
		Map<String, double[]> map = new TreeMap<String, double[]>();
		double [] values = new double [2];
		File allSentencesExpEntityRelevance = new File("");
		File allSentencesContainingRelevantClausie = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String sExpEntityRelevance_Filename = file.getAbsolutePath() + "/" + entity_id + "_sExpEntityRelevance(2).txt";
                allSentencesExpEntityRelevance = new File(sExpEntityRelevance_Filename);
                String sContainingClausie_Filename = file.getAbsolutePath() + "/" + entity_id + "-sContainingRelevantClausieExtraction(2).txt";
                allSentencesContainingRelevantClausie = new File(sContainingClausie_Filename);
                double precision = getPrecision(allSentencesExpEntityRelevance, allSentencesContainingRelevantClausie);
                double recall = getRecall(allSentencesExpEntityRelevance, allSentencesContainingRelevantClausie);
                values[0] = precision;
                values[1] = recall;
                map.put(entity_id, values);
            }
        }
        
		return map;
	}
	
	public static Map<String, double[]> getPRContainEntityExpRelevance(String queryid) throws IOException
	{
		Map<String, double[]> map = new TreeMap<String, double[]>();
		double [] values = new double [2];
		File allSentencesExpEntityRelevance = new File("");
		File allSentencesContainingEntity = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String sExpEntityRelevance_Filename = file.getAbsolutePath() + "/" + entity_id + "_sExpEntityRelevance(2).txt";
                allSentencesExpEntityRelevance = new File(sExpEntityRelevance_Filename);
                String sContainingEntity_Filename = file.getAbsolutePath() + "/" + entity_id + "_sContainingRelevantEntity.txt";
                allSentencesContainingEntity = new File(sContainingEntity_Filename);
                double precision = getPrecision(allSentencesExpEntityRelevance, allSentencesContainingEntity);
                double recall = getRecall(allSentencesExpEntityRelevance, allSentencesContainingEntity);
                values[0] = precision;
                values[1] = recall;
                map.put(entity_id, values);
            }
        }
		return map;
	}
	public static Map<String, double[]> getPRContainQTermExpRelevance(String queryid) throws IOException
	{
		Map<String, double[]> map = new TreeMap<String, double[]>();
		double [] values = new double [2];
		File allSentencesExpEntityRelevance = new File("");
		File allSentencesContainingQTerm = new File("");
		String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
		File f = new File(ROOT_FILE_PATH);
        File[] allSubFiles=f.listFiles();
        for (File file : allSubFiles) {
            if(file.isDirectory())
            {
            	String [] absolutePath_split = file.getAbsolutePath().split("/");
                String entity_id = absolutePath_split[absolutePath_split.length-1];
                String sExpEntityRelevance_Filename = file.getAbsolutePath() + "/" + entity_id + "_sExpEntityRelevance(2).txt";
                allSentencesExpEntityRelevance = new File(sExpEntityRelevance_Filename);
                String sContainingQTerm_Filename = file.getAbsolutePath() + "/" + entity_id + "_sContainingQueryTerm.txt";
                allSentencesContainingQTerm = new File(sContainingQTerm_Filename);
                double precision = getPrecision(allSentencesExpEntityRelevance, allSentencesContainingQTerm);
                double recall = getRecall(allSentencesExpEntityRelevance, allSentencesContainingQTerm);
                values[0] = precision;
                values[1] = recall;
                map.put(entity_id, values);
            }
        }
		return map;
	}
	
	
	public static void main (String [] args) throws IOException
	{
		File ques1_filtered = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques1-filtered.txt");
		File ques3_filtered = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques3-filtered.txt");
		File ques2_filtered = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques2-filtered.txt");
		File ques4_filtered = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques4-filtered.txt");
		File ques5_filtered = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques5-filtered.txt");
		
		File query201_1_sExpEntityRelevance2 = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query201/query201_1/query201_1_sExpEntityRelevance(2).txt");
		File query201_1_sContainingRelevantEntity = new File ("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query201/query201_1/query201_1_sContainingRelevantEntity.txt");
//		getPrecision(query201_1_sExpEntityRelevance2, query201_1_sContainingRelevantEntity);
//		getRecall(query201_1_sExpEntityRelevance2, query201_1_sContainingRelevantEntity);
		
		File query201_1_sContainingRelevantRelation3 = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query201/query201_1/query201_1-sContainingRelevantRelation(3).txt");
		//getSentencesContainingRelevantRelation3(query201_1_sContainingRelevantRelation3);
		//getPrecision(query201_1_sExpEntityRelevance2, query201_1_sContainingRelevantRelation3);
		//getRecall(query201_1_sExpEntityRelevance2, query201_1_sContainingRelevantRelation3);
				
		File query201_1_sContainingRelevantClausieExtraction = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query201/query201_1/query201_1-sContainingRelevantClausieExtraction.txt");
		//getSentencesContainingRelevantClausieExtraction(query201_1_sContainingRelevantClausieExtraction);
//		getPrecision(query201_1_sExpEntityRelevance2, query201_1_sContainingRelevantClausieExtraction);
//		getRecall(query201_1_sExpEntityRelevance2, query201_1_sContainingRelevantClausieExtraction);
				
		File query204_1_sExpEntityRelevance2 = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query204/query204_1/query204_1_sExpEntityRelevance(2).txt");
		File query204_1_sContainingRelevantEntity = new File ("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query204/query204_1/query204_1_sContainingRelevantEntity.txt");
		//getPrecision(query204_1_sExpEntityRelevance2, query204_1_sContainingRelevantEntity);
		//getRecall(query204_1_sExpEntityRelevance2, query204_1_sContainingRelevantEntity);
		
		File query204_1_sContainingRelevantRelation3 = new File ("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query204/query204_1/query204_1-sContainingRelevantRelation(3).txt");
		File query204_1_sContainingRelevantClausieExtraction = new File ("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query204/query204_1/query204_1-sContainingRelevantClausieExtraction.txt");
		File query204_1_fractionRelevantClausieExtraction = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query204/query204_1/query204_1-fractionRelevantClausieExtraction.txt");
		//getFractionOfSentencesWithRelevantClausieExtractions(query204_1_sContainingRelevantRelation3, query204_1_sContainingRelevantClausieExtraction, query204_1_fractionRelevantClausieExtraction);
		
		File allSentencesExplainingRelevance = new File("/Users/AminaKadry/Desktop/Association_Analysis/allSentencesExplainingRelevance.txt");
		
		File query201_1_allSentences = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query201/query201_1/query201_1-allSentences-SIDs.txt");
		File query201_1_allSentencesContainingQTerm = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query201/query201_1/query201_1_sContainingQueryTerm.txt");
		//getSentencesContainingQueryTerm ("query201_1", query201_1_allSentences, query201_1_allSentencesContainingQTerm);

		File query201_1_sContainingRelation2 = new File("/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/query201/query201_1/query201_1-sContainingRelation(2).txt");
		//getSentencesContainingRelation2(query201_1_sContainingRelation2);
		
//		System.out.println("-----Starting query 201-----");
//		getPRContainEntityExpRelevance("query201");
//		System.out.println("-----Finished query 201-----");
//		System.out.println();
//		System.out.println("-----Starting query 204-----");
//		getPRContainEntityExpRelevance("query204");
//		System.out.println("-----Finished query 204-----");
//		System.out.println();
//		System.out.println("-----Starting query 206-----");
//		getPRContainEntityExpRelevance("query206");
//		System.out.println("-----Finished query 206-----");
//		System.out.println();
//		System.out.println("-----Starting query 216-----");
//		getPRContainEntityExpRelevance("query216");
//		System.out.println("-----Finished query 216-----");
//		System.out.println();
//		System.out.println("-----Starting query 220-----");
//		getPRContainEntityExpRelevance("query220");
//		System.out.println("-----Finished query 220-----");
//		System.out.println();
//		System.out.println("-----Starting query 224-----");
//		getPRContainEntityExpRelevance("query224");
//		System.out.println("-----Finished query 224-----");
//		System.out.println();
//		System.out.println("-----Starting query 231-----");
//		getPRContainEntityExpRelevance("query231");
//		System.out.println("-----Finished query 231-----");
//		System.out.println();
//		System.out.println("-----Starting query 234-----");
//		getPRContainEntityExpRelevance("query234");
//		System.out.println("-----Finished query 234-----");
//		System.out.println();
//		System.out.println("-----Starting query 281-----");
//		getPRContainEntityExpRelevance("query281");
//		System.out.println("-----Finished query 281-----");
//		System.out.println();
//		System.out.println("-----Starting query 300-----");
//		getPRContainEntityExpRelevance("query300");
//		System.out.println("-----Finished query 300-----");
		System.out.println(">>>>DONE<<<<");
	}
}
