package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public HabrCareerParse(DateTimeParser dateTimeParser) {
    }

    private static List<String> templateReferences() {
        List<String> references = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String temp = String.format("%s?page=%d", HabrCareerParse.PAGE_LINK, i);
            references.add(temp);
        }
        return references;
    }

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(
                ".vacancy-description__text");
        return rows.text();
    }

    public static void main(String[] args) {
        List<Post> result = new ArrayList<>();
        List<String> references = templateReferences();
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        for (String s : references) {
            result.addAll(habrCareerParse.list(s));
        }
        for (Post p : result) {
            System.out.println(p);
        }
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> posts.addAll(createPostList((row))));
        return posts;
    }

    private static List<Post> createPostList(Element element) {
        List<Post> postList = new ArrayList<>();
        postList.add(createPost(element));
        return postList;
    }

    private static Post createPost(Element element) {
        Element vacancyDate = element.select(".vacancy-card__date").first();
        assert vacancyDate != null;
        String date = String.format("%s", vacancyDate.select("time")
                .attr("datetime"));
        Element titleElement = element.select(".vacancy-card__title").first();
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
        return new Post(vacancyName, link, description, new HabrCareerDateTimeParser().parse(date));
    }
}