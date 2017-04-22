package de.datasec.pandora.slave;

/**
 * Created by DataSec on 27.11.2016.
 */
public class PandoraSlave {

    public static void main(String[] args) {
        new Slave(args[0], Integer.parseInt(args[1])).connect();
        //new Slave("localhost", 805).connect();
        /*CassandraManager cassandraManager = new CassandraManager("127.0.0.1", "indexes");

        cassandraManager.connect(DefaultRetryPolicy.INSTANCE);
        Set<String> set = new HashSet<>();
        set.add("www.asdf.de");
        //System.out.println(cassandraManager.insert("users", new String[]{"user_id", "user_name", "user_pw"}, new Object[]{0, "datasec", "12"}));
        //System.out.println(cassandraManager.insert("users", new String[]{"user_id", "user_name", "user_pw"}, new Object[]{1, "datasec", "12"}));
        Set<String> strings = new HashSet<>();
        strings.add("asdf4");
        strings.add("asdf5");
        strings.add("asdf6");
        cassandraManager.insert("indexes", new String[]{"id", "keyword", "urls", "favicon"}, new Object[]{7, "keyword2", strings, "www.youtube.com"});
        //System.out.println(cassandraManager.contains("indexes", "keyword2"));
        //System.out.println(cassandraManager.insert("indexes", new String[]{"id", "keyword", "urls", "favicon"}, new Object[]{8, "keyword2", strings, "www.youtube.com"}));
        cassandraManager.update("indexes", "keyword2", set);
        //cassandraManager.select();
        cassandraManager.disconnect();*/
    }
}
