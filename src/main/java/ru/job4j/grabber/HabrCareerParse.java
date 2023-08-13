package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static List<String> templateRefferences() {
        List<String> refferences = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String temp = String.format("%s?page=%d", HabrCareerParse.PAGE_LINK, i);
            refferences.add(temp);
        }
        return refferences;
    }

    private static String retrieveDescription(String link) throws IOException {
        String result;
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(
                ".vacancy-description__text");
        result = rows.text();
        return result;
    }

    public static void main(String[] args) throws IOException {
        List<String> refferences = templateRefferences();
        for (String s : refferences) {
            Connection connection = Jsoup.connect(s);
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
                String description;
                try {
                    description = retrieveDescription(link);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("%s %s %s%n%s%n", vacancyName, link, date, description);
                System.out.println("_________________________________________________________________________________");
            });
        }
    }
}