package de.datasec.pandora.slave.crawler;

import de.datasec.pandora.shared.utils.UrlUtils;
import de.datasec.pandora.slave.Slave;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by DataSec on 27.11.2016.
 */
public class SlaveCrawlerThread implements Runnable {

    private static final String SANITIZER = "[^a-zA-Z0-9_ \\-_.\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u00FF]";

    private BlockingQueue<String> urls;

    private Set<String> urlsToInsert = new HashSet<>();

    private Set<String> keywords = new HashSet<>();

    private String url;

    public SlaveCrawlerThread(BlockingQueue<String> urls) {
        this.urls = urls;
    }

    @Override
    public void run() {
        while (SlaveCrawler.CRAWLING) {
            try {
                url = urls.poll(10000L, TimeUnit.MILLISECONDS);

                if (url == null) {
                    continue;
                }

                Connection con = Jsoup.connect(url)
                        .userAgent(UrlUtils.USER_AGENT)
                        .ignoreHttpErrors(true)
                        .timeout(4000);
                Document doc = con.get();

                if (con.response().statusCode() != 200) {
                    continue;
                }

                // Url
                evalUrl(url);

                // Headlines h1
                doc.getElementsByTag("h1").stream()
                        .filter(element -> !element.children().isEmpty())
                        .forEach(element -> Arrays.stream(element.children().last().text().split("\\s+")).forEach(this::checkAndAddKeyword));

                // Title
                Arrays.stream(doc.title().split("\\s+")).forEach(this::checkAndAddKeyword);

                // Meta description
                Arrays.stream(doc.select("meta[name=description]").attr("content").split("\\s+"))
                        .forEach(this::checkAndAddKeyword);

                // Meta keywords
                Arrays.stream(doc.select("meta[name=keywords]").attr("content").split("\\s+"))
                        .forEach(this::checkAndAddKeyword);
            } catch (IOException | IllegalArgumentException | InterruptedException ignore) {}

            urlsToInsert.add(url);

            keywords.forEach(keyword -> Slave.getCassandraManager().insert("indexes", "keyword", new String[] {"keyword", "urls"}, new Object[] {keyword, urlsToInsert}));

            keywords.clear();
            urlsToInsert.clear();
        }
    }

    private void evalUrl(String url) {
        String[] urlParts = url.replace("www.", "").split("\\.");

        // First subdomain/domain
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
                .replace("ß", "(u00df)");
    }
}