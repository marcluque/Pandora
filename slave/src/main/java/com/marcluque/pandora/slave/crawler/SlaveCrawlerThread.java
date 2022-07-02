package com.marcluque.pandora.slave.crawler;

import de.datasecs.pandora.shared.utils.UrlUtils;
import com.marcluque.pandora.slave.Slave;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by DataSecs on 27.11.2016.
 */
public class SlaveCrawlerThread implements Runnable {

    private static final String SANITIZER = "[^a-zA-Z0-9_ \\-_.\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u00FF]";

    private static final String SPLIT_PATTERN = "\\s+";

    private BlockingQueue<String> urls;

    private Set<String> keywords = new HashSet<>();

    private String url;

    private String title;

    private String description = "";

    public SlaveCrawlerThread(BlockingQueue<String> urls) {
        this.urls = urls;
    }

    @Override
    public void run() {
        while (SlaveCrawler.CRAWLING) {
            try {
                if ((url = urls.poll(30, TimeUnit.SECONDS)) == null) {
                    continue;
                }

                Connection con = Jsoup.connect(url).userAgent(UrlUtils.USER_AGENT).timeout(10 * 1000);
                Document doc = con.get();

                if (con.response().statusCode() != 200) {
                    continue;
                }

                // Url
                evalUrl(url);

                // Headlines h1
                doc.getElementsByTag("h1").stream()
                        .filter(element -> !element.children().isEmpty())
                        .forEach(element -> Arrays.stream(element.children().last().text().split(SPLIT_PATTERN)).forEach(this::checkAndAddKeyword));

                // Title
                title = doc.title();
                Arrays.stream(title.split(SPLIT_PATTERN)).forEach(this::checkAndAddKeyword);

                // Meta description
                Elements metaProperties = doc.select("meta[property]");
                metaProperties.stream().filter(node -> node.toString().toLowerCase().contains("description")).forEach(node -> Arrays.stream(node.attr("content").split(SPLIT_PATTERN)).forEach(this::checkAndAddKeyword));

                // Description to save in database
                Element metaDescription = Stream.concat(Stream.concat(metaProperties.stream(), doc.select("meta[name]").stream()), doc.select("meta[name=description]").stream())
                        .filter(node -> node.toString().toLowerCase().contains("description"))
                        .findAny().orElse(null);
                description = metaDescription != null ? metaDescription.attr("content").trim() : "";

                // Shorten description
                int wordAmount = (description.length() - description.replace(" ", "").length()) + 1;
                int lastIndex = 0;
                if (wordAmount > 15) {
                    for (int i = 0; i < 15; i++) {
                        lastIndex = description.indexOf(" ", lastIndex) + 1;
                    }

                    description = description.substring(0, lastIndex);
                }

                // Meta keywords
                Arrays.stream(doc.select("meta[name=keywords]").attr("content").split(SPLIT_PATTERN))
                        .forEach(this::checkAndAddKeyword);
            } catch (IOException | IllegalArgumentException | InterruptedException | UncheckedIOException ignore) {}

            keywords.forEach(keyword -> Slave.getCassandraManager().insert(keyword, new String[]{replaceUmlaut(url), replaceUmlaut(title), replaceUmlaut(description)}));
            keywords.clear();
        }
    }

    private void evalUrl(String url) {
        String[] urlParts = url.replace("www.", "").split("\\.");

        // Subdomain/domain
        checkAndAddKeyword(urlParts[0].split("/")[2]);

        Arrays.stream(urlParts).filter(s -> s.contains("/") && !s.startsWith("com"))
                .forEach(s -> Arrays.stream(s.split("/")).forEach(this::checkAndAddKeyword));
    }

    private void checkAndAddKeyword(String keyword) {
        if (keyword.length() > 0 && !keyword.startsWith("http")) {
            if (!Character.isDigit(keyword.charAt(0)) && Character.isAlphabetic(keyword.charAt(0))) {
                keyword = keyword.toLowerCase().trim().replaceAll(SANITIZER, "").replaceAll("[.,$]", "");
                keyword = replaceUmlaut(keyword);
                if (!keywords.contains(keyword) && keyword.length() > 0) {
                    keywords.add(keyword);
                }
            }
        }
    }

    private String replaceUmlaut(String input) {
        return input.replace("ü", "(u00fc)")
                .replace("ö", "(u00f6)")
                .replace("ä", "(u00e4)")
                .replace("ß", "(u00df)")
                .replace("\'", "(u0027)")
                .replace("\"", "(u0022)");
    }
}