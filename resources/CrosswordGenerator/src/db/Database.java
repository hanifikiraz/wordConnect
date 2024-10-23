package db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import generator.Board;
import generator.DBWord;
import generator.Main;
import generator.Main.Config;
import generator.Word;

public class Database {
	
	public static final String LEVELS_FOLDER_NAME = "levels";
	

	
	
	public static List<DBWord> selectWords(String folder, String regex, int maxScore, int minWordLength, String language) throws SQLException {
		
		
		int thisLevel = findNextJsonFileNameFromLevelsFolder(language, folder);

		
		
		String sql 	= "SELECT id, word FROM words_" + language + " "
					+ "WHERE score > 0 "
					+ "AND score <= " + maxScore + " "
					+ "AND " + thisLevel + " >= reappear "
					+ "AND length(word) <= " + regex.length() + " AND length(word) >= " + minWordLength + " "
					+ "ORDER BY random()";

		

		List<String> lettersPermList = new ArrayList<>();
		printPermutn(regex, "", lettersPermList);
		
	
		

        Connection conn = connect();  
        Statement stmt  = conn.createStatement();  
        ResultSet rs    = stmt.executeQuery(sql);  
          
        List<DBWord> list = new ArrayList<>();
        
        
        while (rs.next()) {
        	
        	int id = rs.getInt("id");
        	String word = rs.getString("word");
        	
        
        	if(!regexContainsWord(lettersPermList, word))
        		continue;
        	
        	
        	
        	DBWord w = new DBWord(id, word);
            list.add(w);  
        }
        
        stmt.close();
        rs.close();
        conn.close();
        
        
        return list;

	}
	
	
	
	
	
	public static void markUsedWords(Board board, Config cfg, String folder) throws SQLException {
		ArrayList<Integer> words = new ArrayList<>();
		
		for(Word word : board.getAcrossWords()) {
			words.add(word.dbId);
		}
		
		for(Word word : board.getDownWords()) {
			words.add(word.dbId);
		}
		
		int lastLevel = findNextJsonFileNameFromLevelsFolder(cfg.language, folder);
	
		
		for(Integer wordId : words) {
			updateUsedWord(wordId, lastLevel, cfg.reappear + new Random().nextInt(words.size()), cfg.language);
		}
	}
	
	
	
	
	private static void updateUsedWord(int wordId, int lastLevel, int skipCount, String language) throws SQLException {
		Connection conn = connect();
		
		PreparedStatement stmt = conn.prepareStatement("UPDATE words_" + language + " SET reappear=? WHERE id=?");

		stmt.setInt(1, lastLevel + skipCount);
		stmt.setInt(2, wordId);
		

		
		stmt.executeUpdate();
		stmt.close();
		conn.close();
	}
	
	
	
	
	
	
	
	private static boolean regexContainsWord(List<String> lettersPermList, String word) {
		
		
		for(String letters : lettersPermList)		
			if(letters.indexOf(word) > -1)
				return true;
		
		
		return false;
		
	}
	
	
	
	static void printPermutn(String str, String ans, List<String> list) 
    { 
  
        // If string is empty 
        if (str.length() == 0) { 
        	list.add(ans);
            return; 
        } 
  
        for (int i = 0; i < str.length(); i++) { 
  
            // ith character of str 
            char ch = str.charAt(i); 
  
            // Rest of the string after excluding  
            // the ith character 
            String ros = str.substring(0, i) +  
                         str.substring(i + 1); 
  
            // Recurvise call 
            printPermutn(ros, ans + ch, list); 
        } 
    } 
	
	
	
	
	public static DBWord findRandomWordTobeLetters(String folder, int length, int score, String language) throws SQLException {
		
		int thisLevel = findNextJsonFileNameFromLevelsFolder(language, folder);
		
		String sql 	= "SELECT id, word FROM words_" + language + " "
					+ "WHERE length(word) = " + length + " "
					+ "AND score > 0 " //FIXED THIS
					+ "AND score <= " + score + " "
					+ "AND " + thisLevel + " >= reappear "
					+ "ORDER BY RANDOM() "
					+ "LIMIT 1";
		
		Connection conn = connect();  
        Statement stmt  = conn.createStatement();  
        ResultSet rs    = stmt.executeQuery(sql);
        
        
        if(rs.next()) {
        	int id = rs.getInt("id");
        	String word = rs.getString("word");
        	
        	DBWord w = new DBWord(id, word);
        	stmt.close();
            rs.close();
            conn.close();
            return w;
        }
        
        return null;
        	 
	}

	
	public static boolean doesLevelAlreadyExistInLevelsFolder(Board board, String letters, String language, File file) throws IOException {
		
		if(!file.exists()) {
			file.mkdirs();
			return false;
		}
		
		File[] list = file.listFiles();
		
		
		
		if(list.length == 0)
			return false;
		
		
		
		
		List<Word> allWords = new ArrayList<>();
		allWords.addAll(board.getAcrossWords());
		allWords.addAll(board.getDownWords());
		
		
		int[] wordIds = new int[allWords.size()];

		for(int i = 0; i < allWords.size(); i++)
			wordIds[i] = allWords.get(i).dbId;
		
		Arrays.sort(wordIds);
		letters = sortStringLetters(letters);
		
		
		
		for(int i = 0; i < list.length; i++) {
			JSONObject level = readLevelFromFile(list[i]);
			
			String[] levelData = level.getString("o").split(",");
			
			String levelLetters = sortStringLetters(levelData[2]); 
			if(!letters.equals(levelLetters))
				continue;
			
			JSONArray levelAcrossWords = level.getJSONArray("a");
			JSONArray levelDownWords = level.getJSONArray("d");
			
			List<Integer> existingIds = new ArrayList<>();
			
	
			for(int j = 0; j < levelAcrossWords.length(); j++) {
				String wordData[] = levelAcrossWords.getString(j).split(",");
				existingIds.add(Integer.parseInt(wordData[0]));
			}
						
			for(int j = 0; j < levelDownWords.length(); j++) {
				String wordData[] = levelDownWords.getString(j).split(",");
				existingIds.add(Integer.parseInt(wordData[0]));
			}	
			
			
			
			int[] levelWordIds = new int[existingIds.size()];
			
			for(int j = 0; j < existingIds.size(); j++)
				levelWordIds[j] = existingIds.get(j);
			
			Arrays.sort(levelWordIds);
			
	
			if(Arrays.equals(wordIds, levelWordIds))
				return true;
		}
			
		return false;
		
	}
	
	
	
	private static JSONObject readLevelFromFile(File jsonFile) throws IOException {
		InputStream is = new FileInputStream(jsonFile);
		Reader r = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(r);
		
		String line = "";
		StringBuilder sb = new StringBuilder();
		
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		
		br.close();
		
		return new JSONObject(sb.toString());
	}
	
	
	
	

	
	
	
	public static JSONObject boardAndLettersToJson(Board board, String letters) {
		JSONObject json = new JSONObject();
		
		json.put("o", board.getWidth() + "," + board.getHeight() + "," + letters);
		

		json.put("a", wordsToJson(board.getAcrossWords()));	
		json.put("d", wordsToJson(board.getDownWords()));
		
		return json;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private static JSONArray getLevelsJson(String language, File jsonFile) throws IOException {
		
		
		
		if(!jsonFile.exists()) {
			jsonFile.createNewFile();
			FileWriter fw = new FileWriter(jsonFile);
			fw.write("[]");
			fw.flush();
			fw.close();
			return new JSONArray();
		}
		
		
		InputStream is = new FileInputStream(jsonFile);
		Reader r = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(r);
		
		String line = "";
		StringBuilder sb = new StringBuilder();
		
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		
		br.close();
		
		return new JSONArray(sb.toString());
		
	}
	
	
	
	
	
	public static void writeAllWordsAsJson(String language, String folder) throws SQLException, IOException {
		
		File langFolder = new File(folder + File.separator + language);
		
		if(!langFolder.exists())
			langFolder.mkdirs();
		
		
		File jsonFile = new File(folder + File.separator + language + File.separator + "words.txt");
		
		if(jsonFile.exists())
			return;
		
		
		String sql 	= "SELECT id, word FROM words_" + language + " WHERE score >= 0";
		
		
		Connection conn = connect();  
        Statement stmt  = conn.createStatement();  
        ResultSet rs    = stmt.executeQuery(sql);  
          
        
        StringBuilder sb = new StringBuilder();
      
        String delim = "";
        
        while (rs.next()) {
        	
        	sb.append(delim);
        	
        	int id = rs.getInt("id");
        	String word = rs.getString("word");
        	
        	sb.append(id);
        	sb.append(":");
        	sb.append(word);
        	
        	if(delim.length() == 0)
        		delim = ":";
        }
        
        
        stmt.close();
        rs.close();
        conn.close();
        
        
        FileWriter fw = new FileWriter(jsonFile);
        fw.write(sb.toString());
        fw.flush();
        fw.close();
	}
	
	
	
public static void writeVulgarWords(String language, String folder) throws SQLException, IOException {
		
		File langFolder = new File(folder + File.separator + language);
		
		if(!langFolder.exists())
			langFolder.mkdirs();
		
		
		File jsonFile = new File(folder + File.separator + language + File.separator + "vulgar.txt");
		
		if(jsonFile.exists())
			return;
		
		
		String sql 	= "SELECT word FROM words_" + language + " WHERE score = -1";
		
		
		Connection conn = connect();  
        Statement stmt  = conn.createStatement();  
        ResultSet rs    = stmt.executeQuery(sql);  
          
        
        StringBuilder sb = new StringBuilder();
      
        String delim = "";
        
        while (rs.next()) {
        	
        	sb.append(delim);

        	String word = rs.getString("word");

        	sb.append(word);
        	
        	if(delim.length() == 0)
        		delim = ",";
        }
        
        
        stmt.close();
        rs.close();
        conn.close();
        
        
        FileWriter fw = new FileWriter(jsonFile);
        fw.write(sb.toString());
        fw.flush();
        fw.close();
	}
	
	
	
	public static void saveLevelAsJsonFile(String letters, Board board, Config cfg, String folder) throws IOException {
		
		File targetFolder = new File(folder + File.separator + cfg.language + File.separator +  LEVELS_FOLDER_NAME);
		
		if(!targetFolder.exists())
			targetFolder.mkdirs();
		
		
		JSONObject json = boardAndLettersToJson(board, letters);
		
		String fileName = null;
		
	
		fileName = String.valueOf(findNextJsonFileNameFromLevelsFolder(cfg.language, folder));


		if(fileName != null) {
			FileWriter fw = new FileWriter(new File(targetFolder, fileName));
			fw.write(json.toString());
			fw.flush();
			fw.close();
		}
		
		
		
	}
	
	
	public static int findNextJsonFileNameFromLevelsFolder(String language, String folder) {
		File file = new File(folder + File.separator + language + File.separator + LEVELS_FOLDER_NAME);
		
		if(!file.exists()) {
			return 0;
		}
		
		String[] files = file.list();
		
		if(files.length == 0)
			return 0;
		
		
		List<Integer> nums = new ArrayList<Integer>();
		
		for(String s : files)
			nums.add(Integer.parseInt(s));
		
		Collections.sort(nums);
		
		return nums.get(nums.size() - 1) + 1;
		
	}
	
	
	
	
	
	
	
	
	
	public static void saveLevelAsJson(String letters, Board board, String language, String folder) throws IOException{
		
		File jsonFile = new File(folder + File.separator + language + File.separator + "levels.json");
		
		
		
		JSONArray existingJson = getLevelsJson(language, jsonFile);
		

		
		JSONObject json = boardAndLettersToJson(board, letters);
		
		existingJson.put(json);
		

		FileWriter fw = new FileWriter(jsonFile);
		fw.write(existingJson.toString());
		fw.flush();
		fw.close();
		
	}

	
	
	private static JSONArray wordsToJson(List<Word> words) {
		
		JSONArray jsonWords = new JSONArray();
		
		for(Word word : words) {
			jsonWords.put(word.dbId + "," + word.startX + "," + word.startY);
		}
		return jsonWords;
	}
	
	
	
	
	
	
	
	
	
	public static String sortStringLetters(String input) {
		char[] letterChars = input.toCharArray();
		Arrays.sort(letterChars);
		return new String(letterChars);
	}
	
	
	

	
	static private Connection connect() throws SQLException {  
        // SQLite connection string  
        Connection conn = null;  
        conn = DriverManager.getConnection(Main.databaseJDBCPath);  

        return conn;  
    }
}
