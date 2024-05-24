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

//        sleep(5000);
//        Connection.Response connection = Jsoup.connect("https://playback.ru")
//                .ignoreContentType(true)
//                .timeout(1000)
//                .followRedirects(false)
//                .execute();
//        Document doc = connection.parse();
//        String snippet = "Чехол для смартфона, как хороший костюм. " +
//                "Должен выглядеть качественно, дорого и сидеть как " +
//                "«влитой». Когда цифровые устройства только заполняли рынок, " +
//                "отыскать заветную «одежку» для смартфона было не так просто. " +
//                "А если и предлагались чехлы, то громоздкие, не аккуратные и хлипкие." +
//                " Но всё это давно ушло в прошлое. Современные чехлы для смартфонов удобно" +
//                " держать в руке, они четко повторяют контуры устройства, легко трансформируются в" +
//                " подставку, что позволяет вам не только просматривать веб-страницы, но и любимые фильмы. " +
//                "Богатая палитра цветов, позволяет не ограничиваться двумя цветами. Выбирая чехол для смартфона," +
//                " необходимо соблюдать главное правило - он должен выполнять не только защитную функцию, но и не мешать " +
//                "пользоваться телефоном. Чехлы для смартфонов должны быть изготовлены непосредственно под вашу" +
//                " модель телефона. Смартфон в чехле большого размера будет постоянно болтаться " +
//                "из стороны в сторону, что неизбежно приведет к образованию потертостей и царапин" +
//                " на его поверхности. Пользы от такого чехла не будет, а вот серьезно навредить и " +
//                "испортить вам настроение он может. Учитывая все эти нюансы, вам не составит особого" +
//                " труда выбрать тот чехол, что подойдет именно вашему цифровому гаджету.";
//        int indexOf = snippet.indexOf("чехол");
//        String s = snippet
//                .substring(indexOf)
//                .replace("чехол", "<b>" + "чехол" + "</b>");
//
//        int lenthOfText = Math.min(258, s.length());
//        System.out.println(lenthOfText);
//        String ss;
//        StringBuilder st = new StringBuilder();
//        ss = s.substring(0,lenthOfText);
//        st.append(ss);
//        System.out.println(st);
//        String[] lines = s.split("\\.");

//        StringBuilder stringBuilder = new StringBuilder();
//        int linesLimit = Math.min(lines.length,3);
//        for (int l=0;l< linesLimit;l++) {
//            stringBuilder.append(lines[l]).append(".");
//        }
//        System.out.println(stringBuilder);


    }
}
