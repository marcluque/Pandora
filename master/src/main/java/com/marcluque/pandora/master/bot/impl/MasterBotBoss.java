package com.marcluque.pandora.master.bot.impl;

import com.marcluque.pandora.master.Master;
import com.marcluque.pandora.master.bot.MasterBot;
import com.marcluque.pandora.master.listener.MasterBotListener;
import com.marcluque.pandora.shared.database.CassandraManager;
import com.marcluque.pandora.shared.utils.UrlUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Created by marcluque on 29.04.2017.
 */
public class MasterBotBoss {

    private final MasterBotListener masterBotListener;

    private final BlockingQueue<String> urlsToVisit;

    private final UrlValidator urlValidator;

    private final Set<String> stopWords = new HashSet<>();

    private final CassandraManager cassandraManager;

    private String currentUrl;

    private final String tableName;

    private final String column;

    public MasterBotBoss(MasterBotListener masterBotListener, String startUrl, UrlValidator urlValidator) {
        this.masterBotListener = masterBotListener;
        urlsToVisit = MasterBot.urlsToVisit;
        this.urlValidator = urlValidator;

        // Stop words
        stopWords.addAll(Arrays.asList("accounts", ".ly"));

        // Cassandra
        cassandraManager = Master.getCassandraManager();
        tableName = "visited";
        column = "url";

        try {
            urlsToVisit.put(startUrl);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.printf("Thread starting to crawl on: %s%n", startUrl);
    }

    protected void crawl() {
        final String[] url = new String[1];

        while (true) {
            try {
                currentUrl = urlsToVisit.take();

                Connection con = Jsoup.connect(currentUrl).userAgent(UrlUtils.USER_AGENT).timeout(10 * 1000);
                Document doc = con.get();

                if (con.response().statusCode() != 200) {
                    break;
                }

                doc.getElementsByTag("a").stream()
                        .filter(tag -> !containsStopWord(url[0] = tag.attr("href")) && url[0].length() > 0)
                        .forEach(tag -> repairAndAddUrl(url[0]));
            } catch (IOException | InterruptedException | UncheckedIOException | ArrayIndexOutOfBoundsException ignore) {}
        }
    }

    private boolean containsStopWord(String url) {
        return stopWords.stream().anyMatch(url::contains);
    }

    private void repairAndAddUrl(String url) {
        if (url.startsWith("#") || url.startsWith("javascript")) {
            return;
        } else if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        // Removes the 2 '//' in front of the link, if there are some
        url = url.startsWith("//") ? url.replace("//", "") : url;

        // Check first if link is valid
        if (urlValidator.isValid(url)) {
            addUrl(url);
        }
        // Repairs the link, when there is just the protocol missing
        else if (url.startsWith("www")) {
            url = String.format("http://%s", url);
            if (urlValidator.isValid(url)) {
                addUrl(url);
            }
        }
        // Repairs the link, when for instance just a "/terms" is given
        else if (url.startsWith("/")) {
            url = String.format("%s%s%s", "http://", currentUrl.split("/")[2], url);

            if (urlValidator.isValid(url)) {
                addUrl(url);
            }
        }
        // Puts 'http://www.' in front so link works again
        else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = !url.startsWith("www") ? String.format("http://%s/%s", currentUrl.split("/")[2], url) : String.format("http://%s", url);

            if (urlValidator.isValid(url)) {
                addUrl(url);
            }
        }
        // Last check, probably domain wasn't recognized by urlValidator
        else if (!urlValidator.isValid(url)) {
            if (url.split("/")[2].split("\\.").length >= 2) {
                addUrl(url);
            }
        }
    }

    private void addUrl(String url) {
        if (cassandraManager.contains(tableName, column, url)) {
            url = url.replace("https", "http");

            try {
                urlsToVisit.put(url);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            masterBotListener.onUrl(url);

            // Create url backup
            if (urlsToVisit.size() % (10 * 10000) == 0) {
                System.out.printf("LINKS TO CRAWL: %d%n", urlsToVisit.size());
                //createBackup(latestUrl);
            }
        }

        // This is done anyway, as the counter is incremented when url exists and entry is created when url not exists
        cassandraManager.insertCounterTable(url);
    }
}