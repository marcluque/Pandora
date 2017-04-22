package de.datasec.pandora.shared.utils;

/**
 * Created by DataSec on 21.04.2017.
 */
public class Utils {

    public static void cleanUp(Object... objects) {
        for (Object object : objects) {
            object = null;
        }
    }
}
