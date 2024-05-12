package searchengine.lemmatization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Lemmatizater {
    private static final LuceneMorphology RussianMorph;

    static {
        try {
            RussianMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isRussianWord(String word) {
        List<String> Russianwords = RussianMorph.getMorphInfo(word);
        for (String russianWord : Russianwords) {
            if (russianWord.contains("СОЮЗ") ||
                    russianWord.contains("ЧАСТ") ||
                    russianWord.contains("ПРЕДЛ") ||
                    russianWord.contains("МЕЖД")) {
                return true;
            }
        }
        return false;
    }

    public static HashMap<String, Integer> splitTextIntoWords(String text) {
        HashMap<String, Integer> numberedWords = new HashMap<>();
        String myText = text.replaceAll("[^а-яА-ЯёЁ]", " ").toLowerCase(Locale.ROOT);
        String[] textInput = myText.split("\\s+");
        List<String> alternativeWords;
        Integer integer;
        for (String wordsIntoRow : textInput) {
            if (!wordsIntoRow.isEmpty()) {
                if (!isRussianWord(wordsIntoRow)) {
                    alternativeWords = RussianMorph.getNormalForms(wordsIntoRow);
                    for (String alternativeWord : alternativeWords) {
                        integer = numberedWords.getOrDefault(alternativeWord, 0);
                        numberedWords.put(alternativeWord, integer + 1);
                    }
                }
            }
        }
        return numberedWords;
    }

}

