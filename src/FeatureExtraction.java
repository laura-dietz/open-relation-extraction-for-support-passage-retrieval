import java.io.BufferedReader;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.TreeCoreAnnotations.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.util.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class FeatureExtraction {
	
	private static boolean isExtraction = false;
	private static boolean isSV = false;
	private static boolean isSVA = false;
	private static boolean isSVC = false;
	private static boolean isSVO = false;
	private static boolean isSVOO = false;
	private static boolean isSVOA = false;
	private static boolean isSVOC = false;
	private static boolean isEntityLinkFound = false;
	private static boolean isRelevantEntityFound = false;
	private static boolean isQTermFound = false;
	private static boolean isEntityInSubject = false;
	private static boolean isEntityInObject = false;
	private static boolean isEntityInBoth = false;
	private static boolean isEntityLinkInSubject = false;
	private static boolean isEntityLinkInObject = false;
	private static boolean isOwnEntityInSubject = false;
	private static boolean isOwnEntityInObject = false;
	private static boolean isRelevantEntityInSubject = false;
	private static boolean isRelevantEntityInObject = false;
	private static boolean isQTermInSubject = false;
	private static boolean isQTermInPredicate = false;
	private static boolean isQTermInObject = false;
	private static boolean isNEInSubject = false;
	private static boolean isNEInObject = false;
	private static boolean isNEFound = false;
	private static boolean pathContainsQTerm = false;
	private static boolean pathGoesThroughRoot = false;
	private static int pathSize = 0;
	
	public static int getSentenceLengthPerSentence (String sentence, String entityid)
	{
		String s = sentence.split("\t")[1];
		int length = s.length();
		//System.out.println(length);
		return length;
	}
	
			
	//returns the count of nouns, verbs, adjectives and adverbs in a given sentence
	public static List<Double> getPOSTagsCountsPerSentence (String sentence, String entityid)
	{
		String s = sentence.split("\t")[1];
		List<Double> posCounts = new ArrayList<Double>();
		MaxentTagger tagger = new MaxentTagger("de/mpii/clausie/resources/parser-models/english-bidirectional-distsim.tagger");
		String tagged = tagger.tagString(s);
		String [] tagged_split = tagged.split(" ");
		String [] tags = new String [tagged_split.length];
		for (int i=0;i<tagged_split.length;i++)
		{
			String tag = tagged_split[i].split("_")[1];
			tags[i] = tag;
		}	
		
		double nounsCount = 0.0;
		double verbsCount = 0.0;
		double adjsCount = 0.0;
		double advsCount = 0.0;
		for (int i=0;i<tags.length;i++)
		{
			if (tags[i].equals("NN") || tags[i].equals("NNS") || tags[i].equals("NNP") || tags[i].equals("NNPS"))
			{
				nounsCount++;
			}
			if (tags[i].equals("VB") || tags[i].equals("VBD") || tags[i].equals("VBG") || tags[i].equals("VBN") || tags[i].equals("VBP") || tags[i].equals("VBZ"))
			{
				verbsCount++;
			}
			if (tags[i].equals("JJ") || tags[i].equals("JJR") || tags[i].equals("JJS"))
			{
				adjsCount++;
			}
			if (tags[i].equals("RB") || tags[i].equals("RBR") || tags[i].equals("RBS"))
			{
				advsCount++;
			}
		} 
		
		double sLength = s.length();
		nounsCount = nounsCount/sLength;
		verbsCount = verbsCount/sLength;
		adjsCount = adjsCount/sLength;
		advsCount = advsCount/sLength;
		posCounts.add(nounsCount);
		posCounts.add(verbsCount);
		posCounts.add(adjsCount);
		posCounts.add(advsCount);
		
		return posCounts;
	}	
	
	
	
	//returns the total number of sentences in a given file
	public static double getNoOfSentences(File f) throws IOException
	{
		FileInputStream in = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		double count = 0.0;
		while ((line=br.readLine())!=null)
		{
			count++;
		}
		return count;
	}
	
	
	public static double getSentencePositionPerSentence (String sentence, String entityid) throws IOException
	{
		String queryID = entityid.split("_")[0];
		String allSentencesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allSentences-SIDs.txt";
		File allSentences = new File(allSentencesFilePath);
		double count = getNoOfSentences(allSentences);
		//System.out.println("Total number of sentences: " + count);
		String sID = sentence.split("\t")[0];
		double id = Double.parseDouble(sID)+1;
		double fraction = (id-1)/(count-1);
		double position = 1 - fraction;
		//System.out.println("Sentence position: " + position);
		return position;
	}
	
	
	
	public static List<String> getStopWords () throws IOException
	{
		File allStopWords = new File("/Users/AminaKadry/Desktop/Evaluate_L2R/Stopwords_NLP.txt");
		FileInputStream in = new FileInputStream(allStopWords);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		List<String> stopWords = new ArrayList<String>();
		while ((line=br.readLine())!=null)
		{
			stopWords.add(line);
		}
//		for (String stopWord:stopWords)
//		{
//			System.out.println(stopWord);
//		}
//		
		return stopWords;
		
	}
	
	public static double getStopWordsFractionPerSentence (String sentence, String entityid) throws IOException
	{
		String s = sentence.split("\t")[1];
		double sLength = s.length();
		double num = 0;
		List<String> stopWords= getStopWords();
		String [] s_split = s.split(" ");
		for (int i=0;i<s_split.length;i++)
		{
			String word = s_split[i];
			for (int j=0;j<stopWords.size();j++)
			{
				String stopWord = stopWords.get(j);
				if (word.toLowerCase().equals(stopWord.toLowerCase()))
				{
					num++;
					break;
				}
			}
		}
		double stopWordsFraction = num/sLength;
		return stopWordsFraction;
	}
	
	
	//return wikititle of a given filename
	public static String getWikiTitlePerFileName (String file_name) throws IOException 
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
	
	//returns surface form of wikiTitle
	public static String getWikiTitleSFPerEntityID (String entity_id) throws IOException
	{
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
				wikiTitle = line_split[3];
				break;
			}
		}
		return wikiTitle;
	}
	
	public static String getWikiTitlePerEntityID (String entity_id) throws IOException
	{
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
	
	public static List<String> getQueryTerms(String queryID) throws IOException
	{
		List<String> queryTerms = new ArrayList<String>();
		File processedQTitles = new File("/Users/AminaKadry/Desktop/processed_query_titles_tsv.txt");
		FileInputStream in = new FileInputStream(processedQTitles);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		String qTitle = "";
		while ((line=br.readLine())!=null)
		{
			String qid = line.split("\t")[0];
			if (queryID.equals(qid))
			{
				qTitle = line.split("\t")[1];
				break;
			}	
		}
		
		String[] qTitleSplit = qTitle.split(" ");
		for (String qTerm:qTitleSplit)
		{
			queryTerms.add(qTerm);
		}
		
//		for (String qTerm:queryTerms)
//		{
//			System.out.println(qTerm);
//		}
		
		return queryTerms;
	}
	
	public static double getQTermsFractionPerSentence (String sentence, String entityid) throws IOException
	{
		
		String queryID = entityid.split("_")[0];
		List<String> allQTerms = getQueryTerms(queryID);
		String s = sentence.split("\t")[1];
		double sLength = s.length();
		//System.out.println("Sentence length: " + sLength);
		double num = 0;
		String [] s_split = s.split(" ");
		for (int i=0;i<s_split.length;i++)
		{
			String word = s_split[i];
			for (int j=0;j<allQTerms.size();j++)
			{
				String qTerm = allQTerms.get(j);
				if (word.toLowerCase().equals(qTerm.toLowerCase()))
				{
					num++;
					break;
				}
			}
		}
		//System.out.println("Number of query terms in sentence: " + num);
		double qTermsFraction = num/sLength;
		//System.out.println("Fraction: " + qTermsFraction);
		return qTermsFraction;
	}
	
	//returns an arrayList containing the wiki titles of all entities annotated as relevant for a given query
	public static List<String> getRelevantEntitiesWikiTitlesPerQuery(String query_id) throws IOException
	{
		FileInputStream in = new FileInputStream("/Users/AminaKadry/Desktop/wikititles.tsv");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line="";
		List<String> relevantEntities = new ArrayList<String>();
		List<String> relevantEntitiesIDs = new ArrayList<String>();
		switch (query_id)
		{
			case("query201"):
				relevantEntitiesIDs.add(0, "query201_1");
				relevantEntitiesIDs.add(1, "query201_7");
				relevantEntitiesIDs.add(2, "query201_8");
				relevantEntitiesIDs.add(3, "query201_10");
				relevantEntitiesIDs.add(4, "query201_11");
				relevantEntitiesIDs.add(5, "query201_13");
				relevantEntitiesIDs.add(6, "query201_14");
				relevantEntitiesIDs.add(7, "query201_15");
				relevantEntitiesIDs.add(8, "query201_21");
				break;
			case("query204"):
				relevantEntitiesIDs.add(0, "query204_1");
				relevantEntitiesIDs.add(1, "query204_2");
				relevantEntitiesIDs.add(2, "query204_3");
				relevantEntitiesIDs.add(3, "query204_5");
				relevantEntitiesIDs.add(4, "query204_6");
				relevantEntitiesIDs.add(5, "query204_7");
				relevantEntitiesIDs.add(6, "query204_8");
				relevantEntitiesIDs.add(7, "query204_9");
				relevantEntitiesIDs.add(8, "query204_12");
				break;
			case("query206"):
				relevantEntitiesIDs.add(0, "query206_1");
				relevantEntitiesIDs.add(1, "query206_2");
				relevantEntitiesIDs.add(2, "query206_7");
				relevantEntitiesIDs.add(3, "query206_8");
				relevantEntitiesIDs.add(4, "query206_9");
				relevantEntitiesIDs.add(5, "query206_12");
				relevantEntitiesIDs.add(6, "query206_13");
				relevantEntitiesIDs.add(7, "query206_16");
				relevantEntitiesIDs.add(8, "query206_17");
				relevantEntitiesIDs.add(9, "query206_18");
				break;	
			case("query216"):
				relevantEntitiesIDs.add(0, "query216_2");
				relevantEntitiesIDs.add(1, "query216_3");
				relevantEntitiesIDs.add(2, "query216_4");
				relevantEntitiesIDs.add(3, "query216_7");
				relevantEntitiesIDs.add(4, "query216_8");
				relevantEntitiesIDs.add(5, "query216_9");
				relevantEntitiesIDs.add(6, "query216_13");
				relevantEntitiesIDs.add(7, "query216_19");
				relevantEntitiesIDs.add(8, "query216_23");
				relevantEntitiesIDs.add(9, "query216_29");
				break;
			case("query220"):
				relevantEntitiesIDs.add(0, "query220_1");
				relevantEntitiesIDs.add(1, "query220_2");
				relevantEntitiesIDs.add(2, "query220_3");
				relevantEntitiesIDs.add(3, "query220_4");
				relevantEntitiesIDs.add(4, "query220_5");
				relevantEntitiesIDs.add(5, "query220_6");
				relevantEntitiesIDs.add(6, "query220_8");
				relevantEntitiesIDs.add(7, "query220_9");
				relevantEntitiesIDs.add(8, "query220_11");
				break;
			case("query224"):
				relevantEntitiesIDs.add(0, "query224_1");
				relevantEntitiesIDs.add(1, "query224_5");
				relevantEntitiesIDs.add(2, "query224_17");
				relevantEntitiesIDs.add(3, "query224_18");
				relevantEntitiesIDs.add(4, "query224_21");
				relevantEntitiesIDs.add(5, "query224_22");
				relevantEntitiesIDs.add(6, "query224_23");
				relevantEntitiesIDs.add(7, "query224_24");
				relevantEntitiesIDs.add(8, "query224_37");
				break;
			case("query231"):
				relevantEntitiesIDs.add(0, "query231_1");
				relevantEntitiesIDs.add(1, "query231_5");
				relevantEntitiesIDs.add(2, "query231_7");
				relevantEntitiesIDs.add(3, "query231_9");
				relevantEntitiesIDs.add(4, "query231_10");
				relevantEntitiesIDs.add(5, "query231_11");
				relevantEntitiesIDs.add(6, "query231_12");
				relevantEntitiesIDs.add(7, "query231_13");
				relevantEntitiesIDs.add(8, "query231_16");
				relevantEntitiesIDs.add(9, "query231_24");
				break;
			case("query234"):
				relevantEntitiesIDs.add(0, "query234_1");
				relevantEntitiesIDs.add(1, "query234_2");
				relevantEntitiesIDs.add(2, "query234_4");
				relevantEntitiesIDs.add(3, "query234_5");
				relevantEntitiesIDs.add(4, "query234_6");
				relevantEntitiesIDs.add(5, "query234_7");
				relevantEntitiesIDs.add(6, "query234_11");
				relevantEntitiesIDs.add(7, "query234_13");
				relevantEntitiesIDs.add(8, "query234_22");
				break;
			case("query281"):
				relevantEntitiesIDs.add(0, "query281_1");
				relevantEntitiesIDs.add(1, "query281_3");
				relevantEntitiesIDs.add(2, "query281_4");
				relevantEntitiesIDs.add(3, "query281_5");
				relevantEntitiesIDs.add(4, "query281_17");
				relevantEntitiesIDs.add(5, "query281_20");
				relevantEntitiesIDs.add(6, "query281_26");
				relevantEntitiesIDs.add(7, "query281_34");
				relevantEntitiesIDs.add(8, "query281_47");
				relevantEntitiesIDs.add(9, "query281_48");
				break;
			case("query300"):
				relevantEntitiesIDs.add(0, "query300_1");
				relevantEntitiesIDs.add(1, "query300_2");
				relevantEntitiesIDs.add(2, "query300_3");
				relevantEntitiesIDs.add(3, "query300_5");
				relevantEntitiesIDs.add(4, "query300_6");
				relevantEntitiesIDs.add(5, "query300_7");
				relevantEntitiesIDs.add(6, "query300_8");
				relevantEntitiesIDs.add(7, "query300_9");
				relevantEntitiesIDs.add(8, "query300_12");
				relevantEntitiesIDs.add(9, "query300_13");
				break;	
		}
		
		while ((line=br.readLine())!=null)
		{
			String [] split = line.split("\t");
			String qid = split[0];
			if (query_id.equals(qid))
			{
				String entityID = split[1];
				String entityWikiID = split[2];
				for (int i=0;i<relevantEntitiesIDs.size();i++)
				{
					if (entityID.equals(relevantEntitiesIDs.get(i)))
					{
						relevantEntities.add(entityWikiID);
						break;
					}
				}
			}		
		}
		
		for (int i=0;i<relevantEntities.size();i++)
		{
			//System.out.println(relevantEntities.get(i));
		}
		
		return relevantEntities;	
	}
	
	//returns an arraylist containing the names2 of all relevantEntities returned from (getRelevantEntitiesPerQuery)
	public static List<String> getAllRelevantEntitiesNames2PerQuery(String queryID) throws IOException
	{
		String allRelevantEntitiesFilePath = "/Users/AminaKadry/Desktop/Evaluate_L2R/relevantEntities/" + queryID + "_relevantEntities.txt";
		File allRelevantEntities = new File(allRelevantEntitiesFilePath);
		List<String> allRelevantEntitiesNames = new ArrayList<String>();
		FileInputStream in = new FileInputStream(allRelevantEntities);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		while ((line=br.readLine())!=null)
		{
			String name2 = line.split("\t")[3];
			allRelevantEntitiesNames.add(name2);
		}
		
		for (String name:allRelevantEntitiesNames)
		{
			//System.out.println(name);
		}
		return allRelevantEntitiesNames;
	}
	
	public static Map<String, List<String>> getAllRelevantEntitiesPerQuery (String entityid) throws IOException
	{
		Map<String, List<String>> map = new TreeMap<String, List<String>>();
		String queryid = entityid.split("_")[0];
		String relevantEntitiesFilePath = "/Users/AminaKadry/Desktop/Evaluate_L2R/relevantEntities/" + queryid + "_relevantEntities.txt";
		File relevantEntitiesFile = new File(relevantEntitiesFilePath);
		FileInputStream in = new FileInputStream(relevantEntitiesFile);
		BufferedReader br = new BufferedReader (new InputStreamReader(in));
		List<String> relevantEntitiesList = new ArrayList<String>();
		String line = "";
		while ((line=br.readLine())!=null)
		{
			relevantEntitiesList = new ArrayList<String>();
			String [] split = line.split("\t");
			String key = split[0];
			//System.out.println("key: " + key);
			String mention = split[1];
			//System.out.println("mention: " + mention);
			String name1 = split[2];
			//System.out.println("name1: " + name1);
			String name2 = split[3];
			//System.out.println("name2: " + name2);
			relevantEntitiesList.add(mention);
			relevantEntitiesList.add(name1);
			relevantEntitiesList.add(name2);
			map.put(key, relevantEntitiesList);		
		}
		
//		for (Map.Entry<String, List<String>> m:map.entrySet())
//		{
//			System.out.println(m.getKey());
//			List<String> allRE = m.getValue();
//			for (String re:allRE)
//			{
//				System.out.println(re);
//			}
//		}
		return map;
	}
	
	
	public static Map<String, String[]> getAllEntityLinksPerEntity (String entityID) throws IOException
	{
		String queryID = entityID.split("_")[0];
		String allEntitiesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityID + "/" + entityID + "_all_entities.txt";
		File allEntities = new File(allEntitiesFilePath);
		String wikiDocumentFilePath = "/Users/AminaKadry/Desktop/wikidocuments/" + queryID + "/" + entityID;
		File wikiDocument = new File(wikiDocumentFilePath);
		Map<String, String[]> map = new TreeMap<String, String[]>();
		String [] allNames = new String [3];
		FileInputStream in1 = new FileInputStream(allEntities);
		FileInputStream in2 = new FileInputStream(wikiDocument);
		BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
		String line1 = "";
		String line2 = "";
		while ((line1=br1.readLine())!=null)
		{
			String eID1 = line1;
			while ((line2=br2.readLine())!=null)
			{
				String [] lineSplit = line2.split("\t\t");
				if (lineSplit.length==5)
				{
					
					String mention = lineSplit[0];
					String name1 = lineSplit[1];
					String name2 = lineSplit[2];
					String eID2 = lineSplit[3];
					if (eID1.equals(eID2))
					{
						allNames[0] = mention;
						allNames[1] = name1;
						allNames[2] = name2;
						map.put(eID2, allNames);
						allNames = new String[3];
						break;
					}
					
				}
			}
			in2.getChannel().position(0);
			br2 = new BufferedReader(new InputStreamReader(in2));
			line2 = "";
			
		}
		
//		for (Map.Entry<String, String[]> m:map.entrySet())
//		{
//			System.out.println("Key: " + m.getKey());
//			String [] values = m.getValue();
//			System.out.println("Mention: " + values[0]);
//			System.out.println("Name1: " + values[1]);
//			System.out.println("Name2: " + values[2]);	
//		}
		
		
		return map;
	}
	
	//returns a map containing all entityLinks+relevantEntities: key is the "wikiTitle", value is a "List<String>: mention, name1, name2"
	public static Map<String, List<String>> getAllEntitiesPerEntity (String entityid) throws IOException
	{
		Map<String, List<String>> map = new TreeMap<String, List<String>>();
		Map<String, String[]> entityLinksMap = getAllEntityLinksPerEntity(entityid);
		Map<String, List<String>> relevantEntitiesMap = getAllRelevantEntitiesPerQuery(entityid);
		for (Map.Entry<String, String[]> eLM:entityLinksMap.entrySet())
		{
			List<String> eLinksList = new ArrayList<>();
			String key = eLM.getKey();
			String [] eLinksArray = eLM.getValue();
			for (String elink:eLinksArray)
			{
				eLinksList.add(elink);
			}
			map.put(key, eLinksList);
		}
		for (Map.Entry<String, List<String>> rEM:relevantEntitiesMap.entrySet())
		{
			List<String> reLinksList = new ArrayList<>();
			String key = rEM.getKey();
			reLinksList = rEM.getValue();
			map.put(key, reLinksList);
		}
		
//		for (Map.Entry<String, List<String>> m:map.entrySet())
//		{
//			System.out.println("key: " + m.getKey());
//			List<String> values = m.getValue();
//			for (String value:values)
//			{
//				System.out.println(value);
//			}
//		}
		
		return map;
	}
	
	public static List<String> getAllEntityNames2PerEntity (String entityid) throws IOException
	{
		List<String> allEntities = new ArrayList<String>();
		String queryID = entityid.split("_")[0];
		List<String> relevantEntities = getAllRelevantEntitiesNames2PerQuery(queryID);
		Map<String, String []> entityLinksMap = getAllEntityLinksPerEntity(entityid);
		for (Map.Entry<String, String[]> map:entityLinksMap.entrySet())
		{
			String [] allEntityLinks = map.getValue();
			String entityLinkName2 = allEntityLinks[2];
			allEntities.add(entityLinkName2);
		}
		
		for (String relevantEntity:relevantEntities)
		{
			allEntities.add(relevantEntity);
		}
		
		for (String entity:allEntities)
		{
			//System.out.println(entity);
		}
		return allEntities;
		
	}
	
	public static Map<String, Double> getAllEntityNames2NoDuplicatesPerEntity (String entityid) throws IOException
	{
		Map<String, Double> map = new TreeMap<String, Double>();
		List<String> allEntities = getAllEntityNames2PerEntity(entityid);
		double i = 0.0;
		for (String entity:allEntities)
		{
			map.put(entity, i);
			i++;
		}
		for (Map.Entry<String, Double> m:map.entrySet())
		{
			//System.out.println(m.getKey());
		}
		return map;
	}
	
	public static int getNumOfEntitiesPerSentence (String sentence, String entityid) throws IOException
	{
		Map<String, Double> allEntities = getAllEntityNames2NoDuplicatesPerEntity(entityid);
		String s = sentence.split("\t")[1];
		int num = 0;
		for (Map.Entry<String, Double> m:allEntities.entrySet())
		{
				String entity = m.getKey();
				if (s.toLowerCase().contains(entity.toLowerCase()))
				{
					num++;
				}
		}
		//System.out.println("Total number of entities in sentence: " + num);
		
		return num;
	}
	
	
	public static String getQueryTitle2 (String queryID) throws IOException
	{
		String queryTitle = "";
		File allQueryTitles = new File("/Users/AminaKadry/Desktop/processed_query_titles_tsv.txt");
		FileInputStream in = new FileInputStream(allQueryTitles);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		while ((line=br.readLine())!=null)
		{
			String [] line_split = line.split("\t");
			String query_title_id = line_split[0];
			if (queryID.equals(query_title_id))
			{
				queryTitle = line_split[1];
				break;
			}
		}
		return queryTitle;
	}
	
	public static Map<String, Double> getISFPerFile (String entityid) throws IOException
	{
		Map<String, Double> map = new TreeMap<String, Double>();
		String queryID = entityid.split("_")[0];
		String allSentencesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allSentences-SIDs.txt";
		File allSentences = new File(allSentencesFilePath);
		FileInputStream in = new FileInputStream(allSentences);
		double nSentences = getNoOfSentences(allSentences);
		String entityTitle = getWikiTitlePerFileName(allSentences.getName());
		List<String> queryTerms = getQueryTerms(queryID);
		BufferedReader br = new BufferedReader (new InputStreamReader(in));
		String line = "";
		Map<String, String> annotatedSentences = getAnnotatedSentencesPerEntity(entityid);
		for (int i=0;i<queryTerms.size();i++)
		{
			line = "";
			String queryTerm = queryTerms.get(i);
			double SF = 0;
			double ISF = 0.0;
			while ((line=br.readLine())!=null)
			{
				String s = line.split("\t")[0];
					if (s.contains(queryTerm))
					{
						SF++;
					}	
			}

			double nom = nSentences+1;
			double den = 0.5 + SF;
			double fraction = nom/den;
			ISF = Math.log10(fraction);
			map.put(queryTerm, ISF);
			in.getChannel().position(0);
			br = new BufferedReader(new InputStreamReader(in));
		}
		
		for (Map.Entry<String, Double> m:map.entrySet())
		{
			//System.out.println("Query Term: " + m.getKey() + " " + "ISF: " + m.getValue());
		}
		
		return map;
	}
	
	public static List<Double> getISFSumAndAveragePerFile (String entityid) throws IOException
	{
		double ISF_sum = 0.0;
		double ISF_average = 0.0;
		List<Double> ISF_values = new ArrayList<Double>();
		Map<String, Double> map = getISFPerFile(entityid);
		double size = map.size();
		//System.out.println("no of query terms: " + size);
		for (Map.Entry<String, Double> m:map.entrySet())
		{
			double ISF = m.getValue();
			ISF_sum += ISF;
		}
		ISF_average = ISF_sum/size;
		ISF_values.add(ISF_sum);
		ISF_values.add(ISF_average);
		
		//System.out.println("ISF sum: " + ISF_values.get(0));
		//System.out.println("ISF average: " + ISF_values.get(1));
		return ISF_values;
	}
	
	public static Map<String,Double> getTFPerSentence (String sentence, String entityid) throws IOException 
	{
		Map<String, Double> map = new TreeMap<String, Double>();
		String queryID = entityid.split("_")[0];
		double tf = 0.0;
		String s = sentence.split("\t")[1];
		String [] words = s.split(" ");
		List<String> queryTerms = getQueryTerms(queryID);
		for (String queryTerm:queryTerms)
		{
			tf = 0;
			for (int i=0;i<words.length;i++)
			{
				if (words[i].toLowerCase().equals(queryTerm.toLowerCase()))
				{
					tf++;
				}
			}
			
			map.put(queryTerm, tf);
		}
		
		for (Map.Entry<String, Double> m:map.entrySet())
		{
			//System.out.println("Query term: " + m.getKey() + " - TF: " + m.getValue());
		}
 		return map;
	}
	
	
	public static double getTFISFSumPerSentence (String sentence, String entityid) throws IOException
	{
		Map<String, Double> map = new TreeMap<String, Double>();
		Map<String, Double> ISFMap = getISFPerFile(entityid);
		Map<String, Double> TFMap = getTFPerSentence(sentence, entityid);
		double TFISF = 0.0;
		double ISF = 0.0;
		double TF = 0.0;
		double TFISF_sum = 0.0;
		for (Map.Entry<String, Double> isfmap:ISFMap.entrySet())
		{
			String queryTermISF = isfmap.getKey();
			ISF = isfmap.getValue();
			
			for (Map.Entry<String, Double> tfmap:TFMap.entrySet())
			{
				String queryTermTF = tfmap.getKey();
				TF = tfmap.getValue();
				if (queryTermISF.equals(queryTermTF))
				{
					TFISF = Math.log10(2) * Math.log10(TF + 1) * ISF;
					map.put(queryTermISF, TFISF);
					break;
				}
			}
		}
		for (Map.Entry<String, Double> m:map.entrySet())
		{
			TFISF_sum += m.getValue();
			//System.out.println("Query Term: " + m.getKey() + " - TFISF: " + m.getValue());
		}
		//System.out.println(TFISF_sum);
		return TFISF_sum;
	}
	
	public static Map<String, List<String>> getAllClausesPerEntity (String entityid) throws IOException
	{
		Map<String, List<String>> allClauses = new TreeMap<String, List<String>>();
		String queryID = entityid.split("_")[0];
		String entityTitle = getWikiTitlePerEntityID(entityid);
		String allClausesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allClauses.txt";
		File allClausesFile = new File(allClausesFilePath);
		FileInputStream in = new FileInputStream(allClausesFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		List<String> allClausesList = new ArrayList<String>();
		while ((line=br.readLine())!=null)
		{
			String [] split = line.split("\t");
			String id = split[0];
			String sentenceID = "sent\\" + queryID + "_" + entityTitle+ "\\S\\" + id;
			for (int i=1; i<split.length;i++)
			{
				allClausesList.add(split[i]);
			}
			allClauses.put(sentenceID, allClausesList);
			allClausesList = new ArrayList<String>();
			
		}
		
//		for (Map.Entry<String, List<String>> m:allClauses.entrySet())
//		{
//			System.out.println(m.getKey());
//			List<String> clauses = m.getValue();
//			for (String cl:clauses)
//			{
//				System.out.println(cl);
//			}
//		}
		return allClauses;
	}
	
	public static Map<String, List<String>> getAllPropositionsPerEntity (String entityid) throws IOException
	{
		Map<String, List<String>> allPropositions = new TreeMap<String, List<String>>();
		String queryID = entityid.split("_")[0];
		String entityTitle = getWikiTitlePerEntityID(entityid);
		String allPropositionsFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allPropositions.txt";
		File allPropositionsFile = new File(allPropositionsFilePath);
		FileInputStream in = new FileInputStream(allPropositionsFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = "";
		List<String> allPropositionsList = new ArrayList<String>();
		while ((line=br.readLine())!=null)
		{
			String [] split = line.split("\t");
			String id = split[0];
			String sentenceID = "sent\\" + queryID + "_" + entityTitle+ "\\S\\" + id;
			for (int i=1; i<split.length;i++)
			{
				allPropositionsList.add(split[i]);
			}
			allPropositions.put(sentenceID, allPropositionsList);
			allPropositionsList = new ArrayList<String>();
			
		}
		
//		for (Map.Entry<String, List<String>> m:allPropositions.entrySet())
//		{
//			System.out.println(m.getKey());
//			List<String> props = m.getValue();
//			for (String p:props)
//			{
//				System.out.println(p);
//			}
//		}
		return allPropositions;
	}
	
	
	public static boolean doesClausieExtractPerSentence (String sentence) 
	{
		isExtraction = false;
		String [] split = sentence.split("\t");

		if (split.length==1)
		{
			isExtraction = false;
		}
		else
		{
			isExtraction = true;
		}
		
		return isExtraction;
	}
	
	public static List<Boolean> getClauseTypesPerSentence (String sentence)
	{
		isSV = false;
		isSVA = false;
		isSVC = false;
		isSVO = false;
		isSVOO = false;
		isSVOA = false;
		isSVOC = false;
		List <String> allClauses = new ArrayList<String>();
		List <Boolean> allClauseTypes = new ArrayList<Boolean>();
		
		String [] split = sentence.split("\t");
		String sID = split[0];
		if (split.length==1)
		{
			isSV = false;
			isSVA = false;
			isSVC = false;
			isSVO = false;
			isSVOO = false;
			isSVOA = false;
			isSVOC = false;	
		}
		else
		{
			for (int i=1;i<split.length;i++)
			{
				allClauses.add(split[i]);
			}
			for (String clause:allClauses)
			{
				String clauseType = clause.split("\\(")[0];
				switch (clauseType)
				{
				case ("SV "):
					isSV = true;
					break;
				case ("SVA "):
					isSVA = true;
					break;
				case ("SVC "):
					isSVC = true;
					break;
				case ("SVO "):
					isSVO = true;
					break;
				case ("SVOO "):
					isSVOO = true;
					break;
				case ("SVOA "):
					isSVOA = true;
					break;
				case ("SVOC "):
					isSVOC = true;
					break;
				}
			}	
		}
		
		allClauseTypes.add(isSV);
		allClauseTypes.add(isSVA);
		allClauseTypes.add(isSVC);
		allClauseTypes.add(isSVO);
		allClauseTypes.add(isSVOO);
		allClauseTypes.add(isSVOA);
		allClauseTypes.add(isSVOC);
		
		return allClauseTypes;
		
		
	}
	
	public static List<Double> getPropositionProperties (String allProp, String entityid)
	{
		List<Double> propProperties = new ArrayList<Double>();
		String [] split = allProp.split("\t");
		double sTokensNo = 0;
		double pTokensNo = 0;
		double oTokensNo = 0;
		double allTokensNo = 0;
		double averageTokensSum = 0;
		double maxTokensNo = 0;
		double averageMaxTokensNo = 0;
		List<Double> allSums = new ArrayList<Double>();
		List<Double> allMaxs = new ArrayList<Double>();
		if (split.length!=1)
		{
			for (int i=1;i<split.length;i++)
			{
				String prop = split[i];
				//System.out.println(prop);
				
				String [] propSplit = prop.split(",");
				if (propSplit.length==3)
				{
					allTokensNo = 0;
					String subject = propSplit[0];
					subject = subject.replaceAll("\"", "");
					subject = subject.replace("(", "");
					//System.out.println("subject: " + subject);
					sTokensNo = subject.split(" ").length;
					//System.out.println("sTokensNo: " + sTokensNo);
					
					String predicate = propSplit[1];
					predicate = predicate.replaceAll("\"", "");
					//System.out.println("predicate: " + predicate);
					pTokensNo = predicate.split(" ").length -1;
					//System.out.println("pTokensNo: " + pTokensNo);
					
					String object = propSplit[2];
					object = object.replaceAll("\"", "");
					object = object.replace(")", "");
					//System.out.println("object: " + object);
					oTokensNo = object.split(" ").length - 1;
					//System.out.println("oTokensNo: " + oTokensNo);
					
					allTokensNo = sTokensNo + pTokensNo + oTokensNo;
					//System.out.println("allTokensNo: " + allTokensNo);
					allSums.add(allTokensNo);
					
					maxTokensNo = Math.max(sTokensNo, pTokensNo);
					maxTokensNo = Math.max(maxTokensNo, oTokensNo);
					//System.out.println("Max no of tokens: " + maxTokensNo);
					allMaxs.add(maxTokensNo);
					
				}
			}
			
			for (double sum:allSums)
			{
				averageTokensSum +=sum;
			}
			//System.out.println("AllTokensSum: " + averageTokensSum);
			
			double propsNo = allSums.size();
			//System.out.println("propsNo: " + propsNo);
			if (propsNo==0)
			{
				averageTokensSum = 0;
			}
			else
			{
				averageTokensSum = averageTokensSum/propsNo;
			}
			//System.out.println("averageTokensSum: " + averageTokensSum);
			
			
			for (double max:allMaxs)
			{
				averageMaxTokensNo += max;
			}
			double maxNo = allMaxs.size();
			//ystem.out.println("maxNo: " + maxNo);
			if (maxNo==0)
			{
				averageMaxTokensNo = 0;
			}
			else
			{
				averageMaxTokensNo = averageMaxTokensNo/maxNo;
			}
			//System.out.println("averageMaxTokensNo: " + averageMaxTokensNo);
		}
		
		propProperties.add(averageTokensSum);
		//System.out.println(propProperties.get(0));
		propProperties.add(averageMaxTokensNo);
		//System.out.println(propProperties.get(1));
		return propProperties;
	}
	
	public static List<Boolean> doesPropContainEntityLinkPerSentence (String allProp, String entityid) throws IOException
	{
		isEntityLinkInSubject = false;
		isEntityLinkInObject = false;
		isEntityLinkFound = false;
		List<Boolean> containsEntityLink = new ArrayList<Boolean>();
		Map<String, String[]> map = getAllEntityLinksPerEntity(entityid);
		String [] split = allProp.split("\t");
		if (split.length!=1)
		{
			for (int i=1;i<split.length;i++)
			{
				String prop = split[i];
				String [] propSplit = prop.split(",");
				if (propSplit.length>2)
				{
					String subject = propSplit[0];
					subject = subject.replaceAll("\"", "");
					subject = subject.replace("(", "");
					//System.out.println("Subject: " +  subject);
					String object = propSplit[2];
					object = object.replaceAll("\"", "");
					object = object.replace(")", "");
					//System.out.println("Object: " + object);	
					for (Map.Entry<String, String[]> m:map.entrySet())
					{
						String [] entityLinks = m.getValue();
						String name2 = entityLinks[2];
						//System.out.println("name2out: " + name2);
						if (isEntityLinkInSubject==false && isEntityLinkFound==false)
						{
							if (subject.toLowerCase().contains(name2.toLowerCase()) && name2.length()!=1)
							{	
								isEntityLinkInSubject = true;
//								System.out.println("subject in proposition: " + i);
//								System.out.println("entitylinkinsubject:" + name2);
								//System.out.println( i + "INNN - Subject");
								
							}
						}
						if (isEntityLinkInObject==false && isEntityLinkFound==false)
						{
							
							if (object.toLowerCase().contains(name2.toLowerCase()) && name2.length()!=1)
							{	
								isEntityLinkInObject = true;
//								System.out.println("object in proposition: " + i);
//								System.out.println("entitylinkinobject: " + name2);
								//System.out.println(i + "INNN - Object");
								
							}
						}	
					}
					if (isEntityLinkInSubject && isEntityLinkInObject)
					{
						isEntityLinkFound = true;
						break;
					}
				}
				
			}
		}
		containsEntityLink.add(isEntityLinkInSubject);
		containsEntityLink.add(isEntityLinkInObject);
		
//		System.out.println("isEntityLinkInSubject?:" + containsEntityLink.get(0));
//		System.out.println("isEntityLinkInObject?:" + containsEntityLink.get(1));
		
		return containsEntityLink;
	}
	
	
	public static List<Boolean> doesPropContainRelevantEntityPerSentence (String allProp, String entityid) throws IOException
	{
		List<Boolean> containsRelevantEntity = new ArrayList<Boolean>();
		String queryID = entityid.split("_")[0];
		String ownEntityTitle = getWikiTitleSFPerEntityID(entityid);
		//System.out.println("ownEntityTitle: " + ownEntityTitle);
		String allRelevantEntitiesFilePath = "/Users/AminaKadry/Desktop/Evaluate_L2R/relevantEntities/" + queryID + "_relevantEntities.txt";
		//File allRelevantEntitiesFile = new File(allRelevantEntitiesFilePath);
		List<String> allRelevantEntitiesList = getAllRelevantEntitiesNames2PerQuery(queryID);
		isOwnEntityInSubject = false;
		isOwnEntityInObject = false;
		isRelevantEntityInSubject = false;
		isRelevantEntityInObject = false;
		isRelevantEntityFound = false;
		String [] split = allProp.split("\t");
		if (split.length!=1)
		{
			for (int i=1;i<split.length;i++)
			{
				String prop = split[i];
				String [] propSplit = prop.split(",");
				if (propSplit.length>2)
				{
					String subject = propSplit[0];
					subject = subject.replaceAll("\"", "");
					subject = subject.replace("(", "");
					//System.out.println("Subject: " +  subject);
					String object = propSplit[2];
					object = object.replaceAll("\"", "");
					object = object.replace(")", "");
					//System.out.println("Object: " + object);	
					for (String relevantEntityName:allRelevantEntitiesList)
					{
						if (isRelevantEntityInSubject==false && isRelevantEntityFound==false)
						{
							if (subject.toLowerCase().contains(relevantEntityName.toLowerCase()))
							{
								isRelevantEntityInSubject = true;
								if (relevantEntityName.toLowerCase().equals(ownEntityTitle.toLowerCase()))
								{
									isOwnEntityInSubject = true;
								}	
							}
						}
						
						
						if (isRelevantEntityInObject==false && isRelevantEntityFound==false)
						{
							if (object.toLowerCase().contains(relevantEntityName.toLowerCase()))
							{
								isRelevantEntityInObject= true;
								if (relevantEntityName.toLowerCase().equals(ownEntityTitle.toLowerCase()))
								{
									isOwnEntityInObject = true;
								}	
							}	
						}	
					}
					
					if (isRelevantEntityInSubject && isRelevantEntityInObject)
					{
						isRelevantEntityFound = true;
						break;
					}	
				}
				
				
			}
		}
		
		containsRelevantEntity.add(isRelevantEntityInSubject);
		containsRelevantEntity.add(isRelevantEntityInObject);
		containsRelevantEntity.add(isOwnEntityInSubject);
		containsRelevantEntity.add(isOwnEntityInObject);
		
		//System.out.println("isRelevantEntityInSubject?:" + containsRelevantEntity.get(0));
		//System.out.println("isRelevantEntityInObject?:" + containsRelevantEntity.get(1));
		//System.out.println("isOwnEntityInSubject?:" + containsRelevantEntity.get(2));
		//System.out.println("isOwnEntityInObject?:" + containsRelevantEntity.get(3));
			
		return containsRelevantEntity;
	}
	
	
	public static List<Boolean> doesPropContainQueryTermPerSentence (String allProp, String entityID) throws IOException
	{
		String queryID = entityID.split("_")[0];
		List<Boolean> containsQueryTerms = new ArrayList<Boolean>();
		List<String> queryTerms = getQueryTerms(queryID);
		isQTermInSubject = false;
		isQTermInPredicate = false;
		isQTermInObject = false;
		isQTermFound = false;
		String [] split = allProp.split("\t");
		if (split.length!=1)
		{
			for (int i=1;i<split.length;i++)
			{
				String prop = split[i];
				String [] propSplit = prop.split(",");
				if (propSplit.length>2)
				{
					String subject = propSplit[0];
					subject = subject.replaceAll("\"", "");
					subject = subject.replace("(", "");
					//System.out.println("Subject: " +  subject);
					String predicate = propSplit[1];
					predicate = predicate.replaceAll("\"", "");
					//System.out.println("Predicate: " + predicate);
					String object = propSplit[2];
					object = object.replaceAll("\"", "");
					object = object.replace(")", "");
					//System.out.println("Object: " + object);
					for (String qTerm:queryTerms)
					{
						if (isQTermInSubject==false && isQTermFound==false)
						{
							if (subject.toLowerCase().contains(qTerm.toLowerCase()))
							{
								isQTermInSubject =true;
							}
						}
						
						if (isQTermInPredicate==false && isQTermFound==false)
						{
							if (predicate.toLowerCase().contains(qTerm.toLowerCase()))
							{
								isQTermInPredicate = true;
							}
						}
						
						if (isQTermInObject==false && isQTermFound==false)
						{
							if (object.toLowerCase().contains(qTerm.toLowerCase()))
							{
								isQTermInObject = true;
							}
						}	
						
					}
					
					if (isQTermInSubject && isQTermInObject && isQTermInPredicate)
					{
						isQTermFound = true;
						break;
					}
				}
				
			}
		}
		
		containsQueryTerms.add(isQTermInSubject);
		containsQueryTerms.add(isQTermInPredicate);
		containsQueryTerms.add(isQTermInObject);
		//System.out.println("isQTermInSubject? " + isQTermInSubject);
		//System.out.println("isQTermInPredicate? " + isQTermInPredicate);
		//System.out.println("isQTermInObject? " + isQTermInObject);
				 
		return containsQueryTerms;
	}
	
	public static List<Boolean> doesPropContainEntityPerSentence (String allProp, String entityid) throws IOException
	{
		isEntityInSubject = false;
		isEntityInObject = false;
		isEntityInBoth = false;
		List<Boolean> containsEntityLink = doesPropContainEntityLinkPerSentence(allProp, entityid);
		List<Boolean> containsRelevantEntity = doesPropContainRelevantEntityPerSentence(allProp, entityid);
		List<Boolean> containsEntity = new ArrayList<Boolean>();
		if (containsEntityLink.get(0) || containsRelevantEntity.get(0))
		{
			isEntityInSubject = true;
		}
		if (containsEntityLink.get(1) || containsEntityLink.get(1))
		{
			isEntityInObject = true;
		}
		if (isEntityInSubject && isEntityInObject)
		{
			isEntityInBoth = true;
		}
		
		containsEntity.add(isEntityInSubject);
		containsEntity.add(isEntityInObject);
		containsEntity.add(isEntityInBoth);
		return containsEntity;
	}
	
	public static List<String> doesPropContainNamedEntityPerSentence(String allProp, String entityid) throws ClassCastException, ClassNotFoundException, IOException
	{
		String serializedClassifier = "/Users/AminaKadry/Downloads/stanford-ner-2015-12-09/classifiers/english.all.3class.distsim.crf.ser.gz";
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
		
		List<String> containsNamedEntity = new ArrayList<String>();
		isNEInSubject = false;
		isNEInObject = false;
		isNEFound = false;
		String neSubject = "0";
		String neObject = "0";
		String [] split = allProp.split("\t");
		if (split.length>1)
		{
			for (int i=1;i<split.length;i++)
			{
				String prop = split[i];
				String [] propSplit = prop.split(",");
				if (propSplit.length==3)
				{
					String subject = propSplit[0];
					subject = subject.replaceAll("\"", "");
					subject = subject.replace("(", "");
					List<List<CoreLabel>> classifySubject = classifier.classify(subject);
					for (List<CoreLabel> sentSubject : classifySubject)
					{
						for (CoreLabel wordSubject : sentSubject)
						{
							
							String neCategory = wordSubject.get(AnswerAnnotation.class);
							
							if (isNEInSubject == false && isNEFound==false)
							{
								if (neCategory.equals("PERSON") || neCategory.equals("LOCATION") || neCategory.equals("ORGANIZATION"))
								{
									isNEInSubject = true;
								}
							}
							
						}
						
						if (isNEInSubject)
						{
							neSubject = "1";
							break;
						}		
					}
					
					String object = propSplit[2];
					object = object.replaceAll("\"", "");
					object = object.replace(")", "");
					List<List<CoreLabel>> classifyObject = classifier.classify(object);
					for (List<CoreLabel> sentObject : classifyObject)
					{
						for (CoreLabel wordObject : sentObject)
						{
							//System.out.println("wordObject: " + wordObject);
							String neCategory = wordObject.get(AnswerAnnotation.class);
							
							if (isNEInObject == false & isNEFound==false)
							{
								if (neCategory.equals("PERSON") || neCategory.equals("LOCATION") || neCategory.equals("ORGANIZATION"))
								{
									
									isNEInObject = true;
								}
							}
							
						}
						
						if (isNEInObject)
						{
							neObject = "1";
							break;
						}
						
					}
					
					if (isNEInSubject && isNEInObject)
					{
						isNEFound = true;
						break;
					}
				}
			}
		}
		containsNamedEntity.add(neSubject);
		containsNamedEntity.add(neObject);
		return containsNamedEntity;
	}
	
	public static String getNewEntityID (String entityid)
	{
		String newID = "";
		int id = Integer.parseInt(entityid.split("_")[1]);
		if (id<10)
		{
			newID = "0"+id;
		}
		else
		{
			newID = id +"";
		}
		//System.out.println(newID);
		return newID;
	}
	
	 public static List<String> getNamedEntities (String sentence, String entityid) throws ClassCastException, ClassNotFoundException, IOException
	 {
		 String serializedClassifier = "/Users/AminaKadry/Downloads/stanford-ner-2015-12-09/classifiers/english.all.3class.distsim.crf.ser.gz";
		 List<String> namedEntities = new ArrayList<String>();
		 AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
		 sentence = sentence.split("\t")[1];
	     List<List<CoreLabel>> classify = classifier.classify(sentence);
	     boolean locationFound = false;
	     boolean personFound = false;
	     boolean organizationFound = false;
	     boolean neFound = false;
	     String loc = "";
	     String per = "";
	     String org = "";
	     String ne = "";
	     for (List<CoreLabel> sent : classify)
	     {
	    	 for (CoreLabel word : sent)
	    	 {
	    		 String neCategory = word.get(AnswerAnnotation.class);
	    		 if (neCategory.equals("LOCATION") && !locationFound)
	    		 {
	    			 locationFound = true;
	    		 }
	    		 if (neCategory.equals("PERSON") && !personFound)
	    		 {
	    			 personFound = true;
	    		 }
	    		 if (neCategory.equals("ORGANIZATION") && !organizationFound)
	    		 {
	    			 organizationFound = true;
	    		 }
	    		 if (locationFound && personFound && organizationFound)
	    		 {
	    			 break;
	    		 }
	    		
	    	 }
	    	
	     }
	     if (locationFound || personFound || organizationFound)
	     {
	    	 neFound = true;
	    	 ne = "1";
	     }
	     else
	     {
	    	 ne = "0";
	     }
	     if (locationFound)
	     {
	    	 loc = "1";
	     }
	     else
	     {
	    	 loc = "0";
	     }
	     if (personFound)
	     {
	    	 per = "1";
	     }
	     else
	     {
	    	 per = "0";
	     }
	     if (organizationFound)
	     {
	    	 org = "1";
	     }
	     else
	     {
	    	 org = "0";
	     }
	     
	     namedEntities.add(ne);
	     namedEntities.add(per);
	     namedEntities.add(loc);
	     namedEntities.add(org);
//	     for (String NE:namedEntities)
//	     {
//	    	 System.out.println(NE);
//	     }
	     return namedEntities;
	              
	 }
	
	
	public static List<String> getDPPathPropertiesPerSentence (String sentence, String entityid) throws IOException
	{
		List<String> allProps = new ArrayList<String>();
		List<Tree> path = new ArrayList<Tree>();
		Properties props = new Properties();
        props.setProperty("annotators","tokenize, ssplit, pos, lemma, ner, parse");
        props.put("threads", "8");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        String queryid = entityid.split("_")[0];
        String entityTitle = getWikiTitlePerEntityID(entityid);
        List<String> qTerms= getQueryTerms(queryid);
        String size = "";
		String root = "";
		String gtRoot = "0";
		String gtQT = "0";
		String id = sentence.split("\t")[0];
		String sentenceID = "sent\\" + queryid + "_" + entityTitle+ "\\S\\" + id;
		Map<String, String> annotatedSentences = getAnnotatedSentencesPerEntity(entityid);
		if (annotatedSentences.containsKey(sentenceID))
		{
			String s = sentence.split("\t")[1];
			Annotation annotation = new Annotation(s);
	        pipeline.annotate(annotation);
	        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
	        Map<String, List<String>> map = getAllEntitiesPerEntity(entityid);
			for (CoreMap sent:sentences)
	        {
	        	Tree tree = sent.get(TreeAnnotation.class);
	        	SemanticGraph dependencies = sent.get(CollapsedCCProcessedDependenciesAnnotation.class);
	        	//System.out.println(dependencies.getRoots());
	        	if (!dependencies.getRoots().toString().equals("[]"))
	        	{
	        		IndexedWord firstRoot = dependencies.getFirstRoot();
		        	root = firstRoot.toString().split("/")[0];
	        	}
	        	
	        	List<Tree> leaves = new ArrayList<>();
	        	leaves = tree.getLeaves(leaves);
	            Tree node1 =null;
	            Tree node2 = null;
	            //List<Tree> path = new ArrayList<Tree>();
	            for (Tree leave:leaves)
	            {
	            	boolean entityNotFound = false;
	            	String leaveString = leave.toString().toLowerCase();
	            	//System.out.println("leaveString: " + leaveString);
	            	Tree leaveParentParent = (leave.parent(tree)).parent(tree);
	            	//System.out.println("leaveParentParent: " + leaveParentParent);
	            	String leaveParentParentString = leaveParentParent.toString().toLowerCase();
	            	for (Map.Entry<String, List<String>> m:map.entrySet())
	            	{
	            		List<String> allEntities = m.getValue();
	            		String mention = allEntities.get(0).toLowerCase();
	            		String name1 = allEntities.get(1).toLowerCase();
	            		String name2 = allEntities.get(2).toLowerCase();
	            		String [] name2Split = name2.split(" ");
	            		List<String> name2Words = new ArrayList<String>();
	            		for (int i=0;i<name2Split.length;i++)
	            		{
	            			name2Words.add(name2Split[i]);
	            		}
	            		if (leaveString.equals(mention)||leaveString.equals(name1))
	            		{
	            			//System.out.println("mention: " + mention);
	            			//System.out.println("Leave equals mention");
	            			for (String name2Word:name2Words)
	            			{
	            				if (!leaveParentParentString.contains(name2Word))
	            				{
	            					entityNotFound = true;
	            					break;
	            				}
	            			}
	            			if (entityNotFound)
	            			{
	            				//System.out.println("ENTITY NOT FOUND? " +  entityNotFound);
	            				break;
	            			}
	            			
	            			if (!entityNotFound)
	            			{
	            				if (node1==null)
	            				{
	            					node1 = leave;
	            					//System.out.println("node1: " + node1);
	            					break;
	            				}
	            				if (node1!=null && !mention.equals(node1.toString().toLowerCase()) && !name1.equals(node1.toString().toLowerCase()))
	            				{
	            					node2 = leave;
	            					//System.out.println("node2: " + node2);
	            					break;
	            				}
	            			}
	            		}
	            		if (node1!=null && node2!=null)
	            		{
	            			break;
	            		}
	            	}
	            	if (node1!=null && node2!=null)
	            	{
	            		path = tree.pathNodeToNode(node1, node2);
	            		pathSize = path.size();
	                	break;
	            	}
	            	else
	            	{
	            		pathSize = 0;
	            		
	            	}
	            	 	
	            }  

	        }
			
			size = Integer.toString(pathSize);
	        if (path!=null)
	        {
	        	if (path.toString().toLowerCase().contains(root.toLowerCase()) && !root.equals(""))
		        {
		        	gtRoot = "1";
		        }
		        else
		        {
		        	gtRoot = "0";
		        }
	        	
	        	for (String qTerm:qTerms)
				{
					if (path.toString().toLowerCase().contains(qTerm.toLowerCase()))
					{
						gtQT = "1";
						break;
					}	
				}
	        
	        }
	        
	        allProps.add(size);
	        allProps.add(gtRoot);
	        allProps.add(gtQT);
	        
		}
		   
		return allProps;
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
			
//			for (Map.Entry<String, String> m:map.entrySet())
//			{
//				System.out.println(m.getKey());
//				System.out.println(m.getValue());
//			}
			return map;
		}
	
	//returns the sentences which were annotated along with the given human relevance judgement
	public static Map<String, String> getAnnotatedSentencesPerEntity (String entityid) throws IOException
	{
		Map<String, String> annotatedSentencesMap = new TreeMap<String, String>();
		File ques1_filtered = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques1-filtered.txt");
		Map<String, String> quesAnnotationsMap = getQuesAnnotationsMap(ques1_filtered);
		String queryid = entityid.split("_")[0];
		//System.out.println(queryid);
		String entityTitle = getWikiTitlePerEntityID(entityid);
		//System.out.println(entityTitle);
		String sid = "sent\\" + queryid + "_" + entityTitle + "\\S\\";
		//System.out.println("sid: " + sid);
		for (Map.Entry<String, String> m:quesAnnotationsMap.entrySet())
		{
			String key = m.getKey();
			String [] keySplit = key.split("\\\\");
			String sentenceID = keySplit[0] + "\\" + keySplit[1] + "\\S\\";
			//System.out.println("sentenceID: " + sentenceID);
			String label = m.getValue();
			if (sentenceID.equals(sid))
			{
				annotatedSentencesMap.put(key, label);
			}
		}
		
		for (Map.Entry<String, String> m:annotatedSentencesMap.entrySet())
		{
			System.out.println(m.getKey() + "\t" + m.getValue());
		}
		return annotatedSentencesMap;
	}
	
	public static String getSentenceLabel (String sentence, String entityid) throws IOException
	{
		String sLabel = "";
		String entityTitle = getWikiTitlePerEntityID(entityid);
		String queryID = entityid.split("_")[0];
		File ques1 = new File("/Users/AminaKadry/Desktop/Association_Analysis/ques1-filtered.txt");
		Map<String, String> map = getQuesAnnotationsMap(ques1);
		String id = sentence.split("\t")[0];
		id = "sent\\" + queryID + "_" + entityTitle + "\\S\\" + id;
		sLabel = map.get(id);
		return sLabel;
	}
	
	public static Map<Integer, String> getAllFeaturesPerFile(String entityid) throws IOException, ClassCastException, ClassNotFoundException
	{
		System.out.println("-----Started " + entityid + "-----" );
		Map<Integer, String> map = new TreeMap<Integer, String>();
		String queryID = entityid.split("_")[0];
		String allSentencesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allSentences-SIDs.txt";
		File allSentences = new File(allSentencesFilePath);
		String newEntityID = getNewEntityID(entityid);
		newEntityID = queryID.substring(5, 8) + newEntityID;
		String allProp = "";
		String allClauses = "";
		String allFeaturesFilePath = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryID + "/" + entityid + "/" + entityid + "-allFeatures.txt";
		File allFeaturesFile = new File(allFeaturesFilePath);
		String entityTitle = getWikiTitlePerFileName(allSentences.getName());
		List<String> allFeatures = new ArrayList<String>();
		FileInputStream in = new FileInputStream(allSentences);
		BufferedReader br = new BufferedReader (new InputStreamReader(in));
		String line = "";
		String allFeaturesString = "";
		String featureLine = "";
		String propositionsLine = "";
		String clausesLine = "";
		Map<String, List<String>> allClausesMap = getAllClausesPerEntity(entityid);
		Map<String, List<String>> allPropositionsMap = getAllPropositionsPerEntity(entityid);
		PrintWriter writer = new PrintWriter(new FileWriter(allFeaturesFile, true));
		Map<String, String> annotatedSentences = getAnnotatedSentencesPerEntity(entityid);
		List<Double> isfSumAverage = getISFSumAndAveragePerFile(entityid);
		while ((line=br.readLine())!=null)
		{
			
			
			String [] split = line.split("\t");
			String id = split[0];
			String sentenceID = "sent\\" + queryID + "_" + entityTitle+ "\\S\\" + id;
			if (annotatedSentences.containsKey(sentenceID))
			{
				writer = new PrintWriter(new FileWriter(allFeaturesFile, true));
				featureLine = "";
				propositionsLine = "";
				clausesLine = "";
				String sentenceLabel = getSentenceLabel(line, entityid);
				allFeaturesString = "";
				System.out.println(sentenceID);
				int idInt = Integer.parseInt(id);
				id = "sent\\" + queryID + "_" + entityTitle + "\\S\\" + id;
				
				// ------ Text features ------
				
				int sLength = getSentenceLengthPerSentence(line, entityid);
				allFeatures.add(Integer.toString(sLength));
				double sPosition = getSentencePositionPerSentence(line, entityid);
				allFeatures.add(Double.toString(sPosition));
				double stopWordRatio = getStopWordsFractionPerSentence(line, entityid);
				allFeatures.add(Double.toString(stopWordRatio));
				double qTermRatio = getQTermsFractionPerSentence(line, entityid);
				allFeatures.add(Double.toString(qTermRatio));
				double isf_sum = isfSumAverage.get(0);
				allFeatures.add(Double.toString(isf_sum));
				double isf_average = isfSumAverage.get(1);
				allFeatures.add(Double.toString(isf_average));
				double tf_isf = getTFISFSumPerSentence(line, entityid);
				allFeatures.add(Double.toString(tf_isf));
				int numOfEntities = getNumOfEntitiesPerSentence(line, entityid);
				allFeatures.add(Integer.toString(numOfEntities));
				List<Double> posRatio = getPOSTagsCountsPerSentence(line, entityid);
				double nounsRatio = posRatio.get(0);
				allFeatures.add(Double.toString(nounsRatio));
				double verbsRatio = posRatio.get(1);
				allFeatures.add(Double.toString(verbsRatio));
				double adjRatio = posRatio.get(2);
				allFeatures.add(Double.toString(adjRatio));
				double advRatio = posRatio.get(3);
				allFeatures.add(Double.toString(advRatio));
				List<String> namedEntities = getNamedEntities(line, entityid);
				String anyNEFound = namedEntities.get(0);
				allFeatures.add(anyNEFound);
				String personFound = namedEntities.get(1);
				allFeatures.add(personFound);
				String locationFound = namedEntities.get(2);
				allFeatures.add(locationFound);
				String organizationFound = namedEntities.get(3);
				allFeatures.add(organizationFound);
				
				// ------ Dependency Parse Features ------
				
				List <String> allDPProperties = getDPPathPropertiesPerSentence(line, entityid);
				String pathLength = allDPProperties.get(0);
				allFeatures.add(pathLength);
				String goesThroughRoot = allDPProperties.get(1);
				allFeatures.add(goesThroughRoot);
				String goesThroughQTerm = allDPProperties.get(2);
				allFeatures.add(goesThroughQTerm);
				
				// ------ClausIE Features ------
				List<String> allPropositionsList = allPropositionsMap.get(sentenceID);
				for (String prop:allPropositionsList)
				{
					propositionsLine +=prop + "\t";
				}
				allProp = id + "\t" + propositionsLine;
				
				List<String> allClausesList = allClausesMap.get(sentenceID);
				for (String clause:allClausesList)
				{
					clausesLine += clause + "\t";
				}
				allClauses = id + "\t" + clausesLine;
				boolean clausieExtracts = doesClausieExtractPerSentence(allProp);
				if (clausieExtracts)
				{
					allFeatures.add("1");
				}
				else
				{
					allFeatures.add("0");
				}
				List<Boolean> clauseTypes = getClauseTypesPerSentence(allClauses);
				for (boolean clauseType:clauseTypes)
				{
					if (clauseType)
					{
						allFeatures.add("1");
					}
					else
					{
						allFeatures.add("0");
					}
				}
				List<Double> propositionProperties = getPropositionProperties(allProp, entityid);
				double averageTokensSum = propositionProperties.get(0);
				allFeatures.add(Double.toString(averageTokensSum));
				double averageMaxTokensNo = propositionProperties.get(1);
				allFeatures.add(Double.toString(averageMaxTokensNo));
				List<Boolean> containsEntity = doesPropContainEntityPerSentence(allProp, entityid);
				for (boolean cE:containsEntity)
				{
					if (cE)
					{
						allFeatures.add("1");	
					}
					else
					{
						allFeatures.add("0");
					}	
				}
				List<Boolean> containsRelevantEntity = doesPropContainRelevantEntityPerSentence(allProp, entityid);
				for (boolean cRE:containsRelevantEntity)
				{
					if (cRE)
					{
						allFeatures.add("1");	
					}
					else
					{
						allFeatures.add("0");
					}	
				}
				List<Boolean> containsEntityLink = doesPropContainEntityLinkPerSentence(allProp, entityid);
				for (boolean cEL:containsEntityLink)
				{
					if (cEL)
					{
						allFeatures.add("1");	
					}
					else
					{
						allFeatures.add("0");
					}
				}
				List<Boolean> containsQueryTerm = doesPropContainQueryTermPerSentence(allProp, entityid);
				for (boolean cQT:containsQueryTerm)
				{
					if (cQT)
					{
						allFeatures.add("1");	
					}
					else
					{
						allFeatures.add("0");
					}
				}
				List<String> containsNamedEntity = doesPropContainNamedEntityPerSentence(allProp, entityid);
				for (String cNE:containsNamedEntity)
				{
					allFeatures.add(cNE);
				}
				
				for (int i=0;i<allFeatures.size();i++)
				{
					String fValue = allFeatures.get(i);
					int x = i+1;
					String toAdd = " " + x + ":" + fValue;
					allFeaturesString += toAdd;
				}
				
				String comment = " # sentid = " + id;
				if (sentenceLabel==null)
				{
					sentenceLabel = "2";
				}
//				if (sentenceLabel.equals(""))
//				{
//					sentenceLabel = "2";
//				}
				featureLine = sentenceLabel + " " + "qid:" + newEntityID + allFeaturesString + comment;
				//System.out.println(featureLine);
				System.out.println(featureLine);
				writer.write(featureLine + "\n");
				writer.close();
				map.put(idInt, featureLine);
				allFeaturesString = "";
				allFeatures = new ArrayList<String>();
				
			}
			else
			{
				continue;
			}
		}
			
//		for (Map.Entry<Integer, String> m:map.entrySet())
//		{
//			System.out.println(m.getKey());
//			System.out.println(m.getValue());
//			writer.write(m.getValue());
//			writer.write("\n");
//		}
//		
//		writer.close();
		
		System.out.println("-----Finished " + entityid + "-----" );
		return map;
	}
	
	public static void getAllFeaturesPerQuery(String queryid) throws IOException, ClassCastException, ClassNotFoundException
	{
		 String ROOT_FILE_PATH = "/Users/AminaKadry/workspace/New_Clausie/ClausIE_new_parser/tests/wikidump/" + queryid;    
			File f = new File(ROOT_FILE_PATH);
	        File[] allSubFiles=f.listFiles();
	        for (File file : allSubFiles) {
	            if(file.isDirectory())
	            {
	            	String [] absolutePath_split = file.getAbsolutePath().split("/");
	            	String entity_id = absolutePath_split[absolutePath_split.length-1];
	            	System.out.println("-----Started " + entity_id + "-----");
	            	getAllFeaturesPerFile(entity_id);
	            	System.out.println("-----Finished " + entity_id + "-----");
	            }
	        }
	}
	
	
	
 	public static void main (String[] args) throws IOException, ClassCastException, ClassNotFoundException
	{
		
		
		//getAnnotatedSentencesPerEntity("query234_4");
		



		//getAllFeaturesPerQuery("query220");
// 		getAllFeaturesPerFile("query220_3");
// 		getAllFeaturesPerFile("query220_4");
// 		getAllFeaturesPerFile("query220_8");
 		//getAllFeaturesPerFile("query220_9");
 		//getAllFeaturesPerQuery("query300");
 		
// 		getAllFeaturesPerFile("query300_7");
//		 getAllFeaturesPerFile("query300_8");
//		 getAllFeaturesPerFile("query300_9");
//		 getAllFeaturesPerFile("query220_5");
//		 getAllFeaturesPerFile("query220_6");
 		
 		//getAllFeaturesPerFile("query231_11");
 		
// 		getAllFeaturesPerFile("query281_17");
// 		getAllFeaturesPerFile("query281_20");
// 		getAllFeaturesPerFile("query281_26");
// 		getAllFeaturesPerFile("query281_3");
 		
 		//getAllFeaturesPerFile("query224_17");
 		
 		getAnnotatedSentencesPerEntity("query224_17");
		
		//System.out.println(">>>>>>DONE<<<<<<");
				
	}
 	

}
