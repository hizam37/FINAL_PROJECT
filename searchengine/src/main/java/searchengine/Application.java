package searchengine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException {

//        String string = "Чехол";
//        String newString = Arrays.toString(string.toCharArray());
//        System.out.println(newString);

        SpringApplication.run(Application.class, args);
//
//        Document doc = Jsoup.connect("http://playback.ru/product/1123979.html").get();
//        String snippet = "";
//        Element element = doc.select(":containsOwn(" + "смартфон" + ")").first();
//        if (element != null) {
//            snippet = element.text().toLowerCase();
//        }
//        int indexOfTheSearchedWord = snippet.indexOf("смартфон".toLowerCase());
//        if (indexOfTheSearchedWord != -1) {
//            String s = snippet
//                    .substring(indexOfTheSearchedWord)
//                    .replace("смартфон".toLowerCase(), "<b>" + "смартфон".toLowerCase() + "</b>")
//                    .replaceAll("<br>", " ");
//            StringBuilder fixedLetter = new StringBuilder();
//            boolean capitalizeNext = false;
//            for (char l : s.toCharArray()) {
//                if (Character.isLetter(l) && l != 'b') {
//                    if (capitalizeNext) {
//                        fixedLetter.append(Character.toUpperCase(l));
//                        capitalizeNext = false;
//                    } else {
//                        fixedLetter.append(l);
//                    }
//                } else {
//                    fixedLetter.append(l);
//                    if (l == '.') {
//                        capitalizeNext = true;
//                    }
//                }
//            }
//            String[] words = fixedLetter.toString().split(" ");
//            StringBuilder line = new StringBuilder();
//            StringBuilder limitedLine = new StringBuilder();
//            int lineCount = 0;
//            for (String word : words) {
//                if (line.length() + word.length() > 90) {
//                    limitedLine.append(line).append("\n");
//                    line = new StringBuilder();
//                    lineCount++;
//                    if (lineCount == 3) {
//                        break;
//                    }
//                }
//                line.append(word).append(" ");
//            }
//            limitedLine.append(line);
//            System.out.println("<b>" + limitedLine.substring(3, 4).toUpperCase() + limitedLine.substring(4));
        }


}




