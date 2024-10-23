package generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Generator {
	
	
	
	
	
	

	private int rows = Main.maxRows;

	private int cols = Main.maxCols;
	
	private List<DBWord> dbWords = null;// new DBWord[]{new DBWord(1, "ŞAM"), new DBWord(3, "AŞ"), new DBWord(5, "HAMAM"), new DBWord(7, "MAAŞ"), new DBWord(9, "PAŞA"), new DBWord(11, "MAŞA"), new DBWord(13, "MASA"), new DBWord(15, "AS"), new DBWord(17, "TASA") };

    private String ACROSS = "Across";

    private String Down = "Down";

    private CrosswordCell[][] grid = new CrosswordCell[rows][];

    private CrosswordCell[][] best = null;

    private List<WordElement> word_elements = new ArrayList<>();

    private List<List<WordElement>> groups = new ArrayList<>();

    private ArrayList<WordElement> badWords = new ArrayList<>();

    private HashMap<String, ArrayList<int[]>> char_index = new HashMap<>();

    private int tries = 5;
   
    private int generatedWordCount = 0;
      
    private int exactNumberOFWordsToGenerate; 
    
    

    
    
    public void setBoardSize(int rows, int cols) {
    	this.rows = rows;
    	this.cols = cols;
    }
    
    
    
    
    
    public void setExactNumberOFWordsToGenerate(int number) {
		exactNumberOFWordsToGenerate = number;
	}





	public void setDataSource(List<DBWord> dbWords) {
    	this.dbWords = dbWords;
    }
    
    
    
    public Board generate() {
    	badWords = new ArrayList<>();
    	
    	

        initializeArray();

        best = loadSquareGrid(tries);

        if (best != null){

     
        	/*for(int i=0;i<best.length;i++) {
        		for(int j=0;j<best[i].length;j++) {
        			CrosswordCell cell = best[i][j];
        			if(cell != null)
        				System.out.println(i+", "+j+" "+cell.character);
        		}
        	}*/


            //customGridView.setNumRows(best.length);

            //customGridView.setNumColumns(best[0].length);



        	Board board = new Board();
        	board.setWidth(best[0].length);
        	board.setHeight(best.length);
        	board.setGroups(getLegend(best));
        	
        	//System.out.println("kotu:"+badWords);
        	
        	return board;
        }
        
        return null;
    }
    
    
    
    
    
    
    private CrosswordCell[][] loadSquareGrid(int maxTries){

        CrosswordCell[][] bestGrid = null;
        generatedWordCount = 0;

        double bestRatio = 0;

        for (int i = 0 ; i < maxTries; i++){

            CrosswordCell[][] a_grid = loadGrid(1);

            if (a_grid == null) continue;
            
            
            //System.out.println(generatedWordCount + ", " + maxWordsToFind + ", "+a_grid);
            if(generatedWordCount >= exactNumberOFWordsToGenerate - 1)
            	return a_grid;
            

            double ratio = Math.min(a_grid.length, a_grid[0].length) * 1.0 / Math.max(a_grid.length, a_grid[0].length);

            if (ratio > bestRatio){
                bestGrid = a_grid;
                bestRatio = ratio;
            }

            if (bestRatio == 1) break;

        }

        /*if (bestGrid == null){
            loadSquareGrid(tries);
            //System.out.println("Bad word Occurred");
        } else {
            return bestGrid;
        }*/

        return bestGrid;

    }
    
    
    
    
    
    

    private CrosswordCell[][] loadGrid(int maxTries){

        word_elements = new ArrayList<>();



        doneData();

        for (int i = 0; i < maxTries; i++){

            clear();

            String start_dir = setWordDirection();

            int row = (int) Math.floor(grid.length / 2);

            int column = (int) Math.floor(grid[0].length / 2);

            WordElement wordElement = word_elements.get(0);

            if (start_dir.equals(ACROSS)){
                column -= (int)Math.floor(wordElement.word.length() / 2);
            }
            else {
                row -= (int)Math.floor(wordElement.word.length() / 2);
            }

            if (canPlaceWordAt(wordElement.word, row, column, start_dir) != -1){

                placeWordAt(wordElement.word, wordElement.index, row, column, start_dir);

            }
            else {

                if (badWords == null)
                    badWords = new ArrayList<>();

                badWords.add(wordElement);

                return null;

            }

            groups = new ArrayList<>();

            word_elements.remove(wordElement);

            groups.add(word_elements);

            boolean wordHasBeenAddedToGrid = false;

            for (int g = 0; g < groups.size(); g++){

                wordHasBeenAddedToGrid = false;

                for (int j = 0; j < groups.get(g).size(); j++){

                    WordElement wordElementInner = groups.get(g).get(j);

                    BestPositionModel bestPositionModel = findPositionForWord(wordElementInner.word);

                    if (bestPositionModel == null){

                        if (groups.size() - 1 == g) groups.add(g + 1, new ArrayList<WordElement>());

                        groups.get(g + 1).add(wordElementInner);

                    } else {

                        int r = bestPositionModel.getRow();

                        int c = bestPositionModel.getColumn();

                        String dir = bestPositionModel.getDirection();

                        placeWordAt(wordElementInner.word, wordElementInner.index, r, c, dir);

                        wordHasBeenAddedToGrid = true;
                        generatedWordCount++;
                       // System.out.println(generatedWordCount + ", " + maxWordsToFind);
                        if(generatedWordCount >= exactNumberOFWordsToGenerate - 1)
                        	return minimizeGrid();;
                    }

                }

                if (!wordHasBeenAddedToGrid) break;


            }

            if (wordHasBeenAddedToGrid) {
                return minimizeGrid();
            }
            else {
                //System.out.println("Unknown Error");
            }

        }

        badWords.addAll(groups.get(groups.size() - 1));

        return null;

    }





    private CrosswordCell[][] minimizeGrid(){

        int r_min = rows - 1, r_max = 0, c_min = cols - 1, c_max = 0;

        for (int r = 0; r < rows; r++){

            for (int c = 0; c < cols; c++){

                CrosswordCell crosswordCell = grid[r][c];

                if (crosswordCell != null){

                    if (r < r_min) r_min = r;
                    if (r > r_max) r_max = r;
                    if (c < c_min) c_min = c;
                    if (c > c_max) c_max = c;

                }

            }

        }

        int rows = r_max - r_min + 1;
        int columns = c_max - c_min + 1;

        CrosswordCell[][] newGrids = new CrosswordCell[rows][];

        for (int r = 0; r < rows; r++){

            for (int c = 0; c < columns; c++){

                newGrids[r] = new CrosswordCell[columns];

            }

        }

        for (int r = r_min, r2 = 0; r2 < rows; r++, r2++){

            for (int c = c_min, c2 = 0; c2 < columns; c++, c2++){

                newGrids[r2][c2] = grid[r][c];

            }

        }

        return newGrids;

    }

    private BestPositionModel findPositionForWord(String word){

        ArrayList<BestPositionModel> bestPositionModels = new ArrayList<>();

        for (int i = 0; i < word.length(); i++){

            ArrayList<int[]> possibleLocationsOnGrid = char_index.get(String.valueOf(word.charAt(i)));

            if (possibleLocationsOnGrid != null && possibleLocationsOnGrid.size() > 0){

                for (int j = 0; j < possibleLocationsOnGrid.size(); j++){

                    int[] point = possibleLocationsOnGrid.get(j);

                    int r = point[0];

                    int c = point[1];

                    int across = canPlaceWordAt(word, r, c - i, ACROSS);

                    int down = canPlaceWordAt(word, r - i, c, Down);

                    if (across != -1){
                        BestPositionModel bestPositionModel = new BestPositionModel();
                        bestPositionModel.setColumn(c - i);
                        bestPositionModel.setRow(r);
                        bestPositionModel.setDirection(ACROSS);
                        bestPositionModel.setIntersections(across);
                        bestPositionModels.add(bestPositionModel);
                    }
                    else if (down != -1){
                        BestPositionModel bestPositionModel = new BestPositionModel();
                        bestPositionModel.setColumn(c);
                        bestPositionModel.setRow(r - i);
                        bestPositionModel.setDirection(Down);
                        bestPositionModel.setIntersections(down);
                        bestPositionModels.add(bestPositionModel);
                    }

                }

            }
        }

        if (bestPositionModels.size() == 0)
            return null;

        int val = (int) Math.floor(Math.random() * bestPositionModels.size());

        return bestPositionModels.get(val);

    }

    private void clear(){

        for (int r = 0; r  < grid.length; r++){

            for (int c = 0; c < grid[r].length; c++){

                grid[r][c] = null;

            }

        }

        char_index = new HashMap<>();

    }

    private int canPlaceWordAt(String word, int row, int column, String start_dir){

        int intersections = 0;

        if (row < 0 || row >= grid.length || column < 0 || column >= grid[row].length)

            return -1;

        if (start_dir.equals(ACROSS)){

            if (column + word.length() > grid[row].length)
                return -1;

            if (column - 1 >= 0 && grid[row][column - 1] != null)
                return -1;

            if (column + word.length() < grid[row].length && grid[row][column + word.length()] != null)
                return -1;

            for (int r = row - 1, c = column, i = 0; r >= 0 && c < column + word.length(); c++, i++){

                boolean isEmpty = grid[r][c] == null;

                boolean isIntersection = grid[row][c] != null && grid[row][c].character.equals(String.valueOf(word.charAt(i)));

                boolean canPlaceHere = isEmpty || isIntersection;

                if (!canPlaceHere){
                    return -1;
                }

            }

            for (int r = row + 1, c = column, i = 0; r < grid.length && c < column + word.length(); c++, i++){

                boolean isEmpty = grid[r][c] == null;

                boolean isIntersection = grid[row][c] != null && grid[row][c].character.equals(String.valueOf(word.charAt(i)));

                boolean canPlaceHere = isEmpty || isIntersection;

                if (!canPlaceHere){
                    return -1;
                }

            }

            intersections = 0;

            for (int c = column, i = 0; c < column + word.length(); c++, i++){

                Character character = word.charAt(i);

                int result = canPlaceCharAt(character.toString(), row, c);

                if (result == -1)
                    return -1;

                intersections += result;

            }

        }
        else if (start_dir.equals(Down)){

            if (row + word.length() > grid.length)
                return -1;

            if (row - 1 >= 0 && grid[row - 1][column] != null)
                return -1;

            if (row + word.length() < grid.length && grid[row + word.length()][column] != null)
                return -1;

            for (int c = column - 1, r = row, i = 0; c >= 0 && r < row + word.length(); r++, i++){

                boolean isEmpty = grid[r][c] == null;

                boolean isIntersection = grid[r][column] != null && grid[r][column].character.equals(String.valueOf(word.charAt(i)));

                boolean canPlaceHere = isEmpty || isIntersection;

                if (!canPlaceHere){
                    return -1;
                }

            }

            for (int c = column + 1, r = row, i = 0; r < row + word.length() && c < grid[r].length; r++, i++){
                boolean isEmpty = grid[r][c] == null;

                boolean isIntersection = grid[r][column] != null && grid[r][column].character.equals(String.valueOf(word.charAt(i)));

                boolean canPlaceHere = isEmpty || isIntersection;

                if (!canPlaceHere){
                    return -1;
                }

            }

            intersections = 0;

            for (int r = row, i = 0; r < row + word.length(); r++, i++){

                Character character = word.charAt(i);

                int result = canPlaceCharAt(character.toString(), r, column);

                if (result == -1){
                    return -1;
                }

                intersections += result;

            }

        }

        return intersections;
    }

    private void placeWordAt(String word, int index, int row, int column, String start_dir){

        if (start_dir.equals(ACROSS)){
            for (int c = column, i = 0; c < column + word.length(); c++, i++){

                addCellToGrid(word, index, i, row, c, start_dir);

            }
        }
        else if (start_dir.equals(Down)){
            for (int r = row, i = 0; r < row + word.length(); r++, i++){

                addCellToGrid(word, index, i, r, column, start_dir);

            }
        }

    }

    private void addCellToGrid(String word, int index, int i , int row, int column, String start_dir){

        Character character = word.charAt(i);

        if (grid[row][column] == null){

            grid[row][column] = new CrosswordCell(character.toString());

            if (char_index.get(character.toString()) == null){

                ArrayList<int[]> ints = new ArrayList<>();

                ints.add(new int[]{row, column});

                char_index.put(character.toString(), ints);

            }
            else {

                ArrayList<int[]> ints = char_index.get(character.toString());

                ints.add(new int[]{row, column});

                char_index.put(character.toString(), ints);

            }

        }

        boolean isStartOfWord = i == 0;

        if (grid[row][column] != null){

            if (start_dir.equals(ACROSS)){

                if (i == 0){
                    grid[row][column].startAcross = new int[]{row, column};
                }
                else if (i == word.length() - 1){
                    grid[row][column].endAcross = new int[]{row, column};
                }

                if (grid[row][column].acrossNode == null)
                    grid[row][column].acrossNode = new CrosswordCellNode(isStartOfWord, index);



            }
            else if (start_dir.equals(Down)){

                if (i == 0){
                    grid[row][column].startDown = new int[]{row, column};
                }
                else if (i == word.length() - 1){
                    grid[row][column].endDown = new int[]{row, column};
                }

                if (grid[row][column].downNode == null)
                    grid[row][column].downNode = new CrosswordCellNode(isStartOfWord, index);



            }

        }

    }

    private int canPlaceCharAt(String word, int row, int column){

        if (grid[row][column] == null){
            return 0;
        }

        if (grid[row][column].character.equals(word)){
            return 1;
        }

        return -1;

    }

    private void initializeArray(){

        grid = new CrosswordCell[rows][];

        for(int i = 0 ; i < rows; i++){

            grid[i] = new CrosswordCell[cols];


        }

     

    }

    private void doneData(){

        word_elements = new ArrayList<>();

        for (int i = 0; i < dbWords.size(); i++){

            word_elements.add(new WordElement(dbWords.get(i).word, i, dbWords.get(i).id));

        }

        Collections.sort(word_elements, new Comparator<WordElement>() {
            @Override
            public int compare(WordElement a, WordElement b) {
                int diff = b.word.length() - a.word.length();
                return diff;
            }
        });

    }

    private String setWordDirection(){
        int value = (int) Math.floor(Math.random() * 2);
        return value == 1 ? ACROSS : Down;
    }
    
    private Groups getLegend(CrosswordCell[][] grid) {
    	Groups groups = new Groups();
    	
    	int position = 1;
    	
    	for(var r = 0; r < grid.length; r++){	
            for(var c = 0; c < grid[r].length; c++){
            	var cell = grid[r][c];
                var increment_position = false;
                
       
            	if(cell != null && cell.acrossNode != null && cell.acrossNode.isStartOfWord) {
            		int index = cell.acrossNode.index;
            		Group g = new Group();
            		g.position = position;
            		g.index = index;
            		g.word = dbWords.get(index).word;
            		g.dbId = dbWords.get(index).id;
            		g.row = r;
            		g.col = c;
            		g.dir = "H";
            		groups.across.add(g);
            		increment_position = true;
            	}
            	
            	
            	if(cell != null && cell.downNode != null && cell.downNode.isStartOfWord) {
            		int index = cell.downNode.index;
            		Group g = new Group();
            		g.position = position;
            		g.index = index;
            		g.word = dbWords.get(index).word;
            		g.dbId = dbWords.get(index).id;
            		g.row = r;
            		g.col = c;
            		g.dir = "D";
            		groups.down.add(g);
            		increment_position = true;
            	}
            	
            	if(increment_position) position++;

            }
        }
    	
    	return groups;
    }
}
