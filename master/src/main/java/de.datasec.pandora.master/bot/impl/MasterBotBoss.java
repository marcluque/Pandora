package de.datasec.pandora.master.bot.impl;

import de.datasec.pandora.master.Master;
import de.datasec.pandora.master.listener.MasterBotListener;
import de.datasec.pandora.shared.database.CassandraManager;
import de.datasec.pandora.shared.utils.UrlUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;

/**
 * Created by DataSec on 29.04.2017.
 */
public class MasterBotBoss {

    private MasterBotListener masterBotListener;

    private BlockingQueue<String> urlsToVisit;

    private UrlValidator urlValidator;

    private CassandraManager cassandraManager;

    private String currentUrl, tableName, column;

    private File latestUrl;

    public MasterBotBoss() {}

    public MasterBotBoss(MasterBotListener masterBotListener, String startUrl, BlockingQueue<String> urlsToVisit, UrlValidator urlValidator) {
        this.masterBotListener = masterBotListener;
        this.urlsToVisit = urlsToVisit;
        this.urlValidator = urlValidator;

        // Cassandra
        cassandraManager = Master.getCassandraManager();
        tableName = "visited";
        column = "url";

        // UrlBackup
        latestUrl = new File("latestUrl.txt");
        if (latestUrl.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(latestUrl.getName()), Charset.forName("UTF-8")))) {
                currentUrl = bufferedReader.readLine();
                startUrl = (currentUrl.length() > 0) ? currentUrl : startUrl;
                urlsToVisit.offer(bufferedReader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        urlsToVisit.offer(startUrl);
        System.out.printf("Master thread starting to crawl on: %s%n", startUrl);
    }

    protected void crawl() {
        while (true) {
            try {
                currentUrl = urlsToVisit.take();

                Connection con = Jsoup.connect(currentUrl).userAgent(UrlUtils.USER_AGENT).timeout(4000);
                Document doc = con.get();

                if (con.response().statusCode() != 200) {
                    break;
                }

                doc.getElementsByTag("a").forEach(tag -> {
                    String url = tag.attr("href");
                    if (urlValidator.isValid(url)) {
                        addUrl(url);
                    } else if (!(url.length() > 250)) {
                        repairAndAddUrl(url);
                    }
                });
            } catch (IOException | InterruptedException ignore) {}
        }
    }

    private void repairAndAddUrl(String url) {
        // Removes the 2 '//' in front of the link. In order to clean it up
        url = url.replace("//", "");

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
            if (urlsToVisit.size() > 0) {
                url = String.format("%s%s%s", "http://", currentUrl.split("/")[2], url);
            }

            if (urlValidator.isValid(url)) {
                addUrl(url);
            }
        }
        // Puts 'http://www.' in front so link works again
        else if (!url.startsWith("http://") || !url.contains("www")) {
            url = String.format("http://%s", url);
            if (urlValidator.isValid(url)) {
                addUrl(url);
            }
        }
    }

    private void addUrl(String url) {
        if (!cassandraManager.contains(tableName, column, column, url)) {
            urlsToVisit.offer(url);
            cassandraManager.insert(tableName, column, column, new String[]{column}, new Object[]{url});
            masterBotListener.onUrl(url);

            // Create url backup
            if (urlsToVisit.size() % 1000 == 0) {
                System.out.println(String.format("LINKS TO CRAWL: %d", urlsToVisit.size()));
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
    }
}
