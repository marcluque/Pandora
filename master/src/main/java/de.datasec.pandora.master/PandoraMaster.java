package de.datasec.pandora.master;

/**
 * Created by DataSec on 27.11.2016.
 */
public class PandoraMaster {

    public static void main(String[] args) {
        new Master(args[0], Integer.parseInt(args[1]), "https://www.youtube.de/", 10).start();
        //new Master("localhost", 805, "https://www.youtube.de/", 10).start();
    }
}
