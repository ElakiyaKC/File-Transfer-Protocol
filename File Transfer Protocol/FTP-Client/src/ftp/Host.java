package ftp;
/**
 * Created by Elakiya on 7/4/2017.
 */

public class Host {

    public static String ServerPath() {
		return System.getProperty("user.home");
    }

    public static String HostName() {
        return "localhost";
    }
}
