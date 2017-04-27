package de.datasec.pandora.slave.crawler;

import de.datasec.pandora.shared.utils.UrlUtils;
import de.datasec.pandora.slave.Slave;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
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

    private String url;

    private Set<String> keywords = new HashSet<>();

    public SlaveCrawlerThread(BlockingQueue<String> urls) {
        this.urls = urls;
    }

    @Override
    public void run() {
        while (SlaveCrawler.CRAWLING) {
            try {
                url = urls.poll(10000L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
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
                doc.getElementsByTag("h1").forEach(element -> {
                    if(!element.children().isEmpty()) {
                        for (String headline : element.children().last().text().split("\\s+")) {
                            checkAndAddKeyword(headline);
                        }
                    }
                });

                // Title
                for (String s : doc.title().split("\\s+")) {
                    checkAndAddKeyword(s);
                }

                // Meta description
                for (String s : doc.select("meta[name=description]").attr("content").split("\\s+")) {
                    checkAndAddKeyword(s);
                }

                // Meta keywords
                for (String s : doc.select("meta[name=keywords]").attr("content").split("\\s+")) {
                    checkAndAddKeyword(s);
                }
            } catch (IOException ignore) {}

            urlsToInsert.add(url);

            keywords.forEach(keyword -> Slave.getCassandraManager().insert("indexes", "keyword", "keyword", new String[] {"keyword", "urls"}, new Object[] {keyword, urlsToInsert}, 0));

            keywords.clear();
            urlsToInsert.clear();
        }
    }

    private void evalUrl(String url) {
        String[] urlParts = url.replace("www.", "").split("\\.");

        // First subdomain/domain
        String domain = urlParts[0].split("/")[2];
        if (!keywords.contains(domain) && domain.length() > 0) {
            keywords.add(domain);
        }

        for (String s : urlParts) {
            if (s.contains("/")) {
                if (!s.startsWith("http")) {
                    for (int i = 1; i < s.split("/").length; i++) {
                        if (s.split("[^a-zA-Z0-9_]")[i].length() > 0) {
                            keywords.add(s.split("[^a-zA-Z0-9_]")[i]);
                        }
                    }
                }
                continue;
            }

            // Subdomains
            if (!keywords.contains(s) && !s.startsWith("com") && s.matches("^[0-9]") && s.length() > 0) {
                keywords.add(s);
            }
        }
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
