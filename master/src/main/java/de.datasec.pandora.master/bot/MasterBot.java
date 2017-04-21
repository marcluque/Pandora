package de.datasec.pandora.master.bot;

import de.datasec.pandora.master.listener.MasterBotListener;
import de.datasec.pandora.master.roundrobinlist.RoundRobinList;
import de.datasec.pandora.shared.utils.UrlUtils;
import de.jackwhite20.cascade.server.Server;
import de.jackwhite20.cascade.shared.session.Session;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by DataSec on 01.12.2016.
 */
public class MasterBot {

    private MasterBotListener masterBotListener;

    private Server server;

    private UrlValidator urlValidator = new UrlValidator();

    private Queue<String> urlsToVisit = new ConcurrentLinkedQueue<>();

    private Set<String> visited = new HashSet<>();

    private String currentUrl;

    public MasterBot(Server server, RoundRobinList<Session> sessions, String startUrl, int urlsPerPacket) {
        this.server = server;
        masterBotListener = new MasterBotListener(sessions, urlsPerPacket);
        urlsToVisit.offer(startUrl);
        System.out.println("Starting to crawl on: " + startUrl);
    }

    public void crawl() {
        while ((currentUrl = urlsToVisit.poll()) != null) {
            try {
                Connection con = Jsoup.connect(currentUrl)
                        .userAgent(UrlUtils.USER_AGENT)
                        .timeout(4000);
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
            } catch (IOException e) {
                // Ignore timeouts
                if (e instanceof SocketTimeoutException) {
                    continue;
                }
                // Print where the 404 occured
                else if (e instanceof HttpStatusException) {
                    System.err.println("404 at: " + currentUrl);
                    continue;
                }

                e.printStackTrace();
            }
        }

        System.out.println("Finished! \n Waiting for new links.. " + " Finished at: " + currentUrl);
        server.stop();
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
            if (visited.size() > 0) {
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
        if (!visited.contains(url)) {
            urlsToVisit.offer(url);
            visited.add(url);
            masterBotListener.onUrl(url);
            //TODO: DELETE OR NOT?
            //System.out.println("LINK: " + url);

            if (visited.size() % 1000 == 0) {
                System.out.printf("VISITED: %d\nLINKS TO CRAWL: %d%n", visited.size(), urlsToVisit.size());
            }
        }
    }
}
