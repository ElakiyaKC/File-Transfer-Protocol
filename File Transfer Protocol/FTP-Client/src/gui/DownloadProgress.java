package gui;

/**
 * Created by Elakiya on 7/4/2017.
 */
public interface DownloadProgress {
   //Start the download
    void startDownload(String filename);
    //Download progress
    void downloadInProgress(String filename, int progress);
    //Complete the download
    void finishDownload(String filename);
}
