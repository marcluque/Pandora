package de.datasec.pandora.master.bot.impl;

import de.datasec.pandora.master.Master;
import de.datasec.pandora.master.bot.MasterBot;
import de.datasec.pandora.master.listener.MasterBotListener;
import de.datasec.pandora.shared.database.CassandraManager;
import de.datasec.pandora.shared.utils.UrlUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Created by DataSec on 29.04.2017.
 */
public class MasterBotBoss {

    private MasterBotListener masterBotListener;

    private BlockingQueue<String> urlsToVisit;

    private UrlValidator urlValidator;

    private Set<String> stopWords = new HashSet<>();

    private CassandraManager cassandraManager;

    private String currentUrl, tableName, column;

    private File latestUrl;

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

        // UrlBackup
        /*latestUrl = new File("latestUrl.txt");
        if (latestUrl.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(latestUrl.getName()), Charset.forName("UTF-8")))) {
                currentUrl = bufferedReader.readLine();
                startUrl = (currentUrl.length() > 0) ? currentUrl : startUrl;
                urlsToVisit.offer(bufferedReader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        urlsToVisit.offer(startUrl);
        System.out.printf("Thread starting to crawl on: %s%n", startUrl);
    }

    protected void crawl() {
        final String[] url = new String[1];

        while (true) {
            try {
                if ((currentUrl = urlsToVisit.take()) == null) {
                    continue;
                }

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
        if (!cassandraManager.contains(tableName, column, url)) {
            url = url.startsWith("https") ? url.replace("https", "http") : url;

            urlsToVisit.offer(url);
            masterBotListener.onUrl(url);

            // Create url backup
            if (urlsToVisit.size() % (10 * 10000) == 0) {
                System.out.println(String.format("LINKS TO CRAWL: %d", urlsToVisit.size()));
                //createBackup(latestUrl);
            }
        }

        // This is done anyway, as the counter is incremented when url exists and entry is created when url not exists
        cassandraManager.insertCounterTable(url);
    }

    // TODO: Make backup enough urls for the amount of threads. Probably separate this to MasterBot class, so there are no conflicts!
    private void createBackup(File latestUrl) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(latestUrl)))) {
            String temp = urlsToVisit.poll();
            bufferedWriter.write(temp);
            bufferedWriter.write("\n");
            bufferedWriter.write(urlsToVisit.peek());
            urlsToVisit.offer(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}