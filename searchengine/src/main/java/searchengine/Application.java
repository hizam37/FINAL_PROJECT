package searchengine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import searchengine.crawler.WebCrawler;
import searchengine.dto.searchDto.SearchDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import static java.lang.Thread.sleep;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws InterruptedException, IOException {
        SpringApplication.run(Application.class, args);

//        String snippet = "цвет, позволяет не ограничиваться двумя цветами. " + "\n"+
//                "выбирая чехол для смартфона, необходимо соблюдать главное правило - он должен выполнять не только защитную функцию, но и не мешать пользоваться телефоном. чехлы для смарт";
//        int indexOfTheSearchedWord = snippet.indexOf("позволяет");
//        if (indexOfTheSearchedWord != -1) {
//            String s = snippet
//                    .substring(indexOfTheSearchedWord)
//                    .replace("цвет", "<b>" + "цвет" + "</b>");
//            int lengthOfText = Math.min(240, s.length());
//            StringBuilder st = new StringBuilder();
//            String lines = s.substring(0, lengthOfText);
//           String[] sss = lines.split("\n");
//            for (String string : sss) {
//                st.append(string);
//            }
//            System.out.println(st);
//        }
    }
}
