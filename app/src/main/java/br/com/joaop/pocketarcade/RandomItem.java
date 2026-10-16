package br.com.joaop.pocketarcade;

public class RandomItem {
    private final String word;
    private final String category;
    public RandomItem(String word, String category) { this.word = word; this.category = category; }
    public String getWord() { return word; }
    public String getCategory() { return category; }
}
