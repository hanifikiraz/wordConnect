package generator;

public class DBWord {
	
	public int id;
	public String word;
	
	
	
	
	public DBWord(int id, String word) {
		super();
		this.id = id;
		this.word = word;
	}

	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		DBWord other = (DBWord)obj;
		return this.id == other.id;
	}
	
	
	
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return id;
	}
	
	
	
	@Override
	public String toString() {
		return "DBWord [id=" + id + ", word=" + word + "]";
	}
	
	
	
	
}
