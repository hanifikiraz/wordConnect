package generator;

import java.util.ArrayList;
import java.util.List;

public class Board {

	private int width, height;
	
	
	private List<Word> acrossWords;
	private List<Word> downWords;
	

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
		
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}


	public List<Word> getAcrossWords() {
		return acrossWords;
	}

	public List<Word> getDownWords() {
		return downWords;
	}

	public void setGroups(Groups groups) {
		
		acrossWords = new ArrayList<Word>();
		downWords = new ArrayList<Word>();
		
		
		for(Group g : groups.across) {
			Word word = new Word();
			word.answer = g.word;
			word.dbId = g.dbId;
			word.startX = g.col;
			word.startY = height - 1 - g.row;
			acrossWords.add(word);
		}
		
		for(Group g : groups.down) {
			Word word = new Word();
			word.answer = g.word;
			word.dbId = g.dbId;
			word.startX = g.col;
			word.startY = height - 1 - g.row;
			downWords.add(word);
		}
	}
	
	@Override
	public String toString() {
		return "width: " + width + ", height: " + height + "\nacross: " + acrossWords +"\ndown: " + downWords;
	}
}
