package de.datasec.pandora.master;

/**
 * Created by DataSec on 27.11.2016.
 */
public class PandoraMaster {

    public static void main(String[] args) {
        new Master("https://stackoverflow.com/questions/17146048/malformed-url-no-protocol-error", 100).start();
    }
}
