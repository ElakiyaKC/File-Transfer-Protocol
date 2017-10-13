package ftp;
/**
 * Created by Elakiya on 7/3/2017.
 */
public class Host {
    /**
     * method to fetch the current server path based on working mode
     *
     */
    public static String getServerPath() {
		return System.getProperty("user.home");
    }
}
