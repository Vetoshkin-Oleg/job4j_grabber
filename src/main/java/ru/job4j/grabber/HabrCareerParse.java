package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element vacancyDate = row.select(".vacancy-card__date").first();
            assert vacancyDate != null;
            String date = String.format("%s", vacancyDate.select("time")
                    .attr("datetime"));
            Element titleElement = row.select(".vacancy-card__title").first();
            assert titleElement != null;
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s %s %s%n", vacancyName, link, date);
        });
    }
}