package gui;

/**
 * Created by Elakiya on 7/4/2017.
 */
public interface UploadProgress {
   //start upload process
    void startUpload(String filename);
    //upload in progress
    void uploadInProgress(String filename, int progress);
    //complete the upload process
    void finishUpload(String filename);

}
