package de.datasec.pandora.master.listener;

/**
 * Created by Marc on 04.12.2016.
 */
public interface BotListener {

    void onUrl(String url);

    void onImgUrl(String url, String imgUrl);
}
