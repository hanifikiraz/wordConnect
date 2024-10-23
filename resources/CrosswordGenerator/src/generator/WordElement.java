package generator;

public class WordElement {

    public String word = "";

    public int index = 0;
    public int dbId;

    public WordElement(String word, int index, int dbId) {
        this.word = word;
        this.index = index;
        this.dbId = dbId;
    }
}
