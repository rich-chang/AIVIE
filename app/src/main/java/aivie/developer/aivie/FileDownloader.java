package aivie.developer.aivie;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FileDownloader {

    private static boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "FileDownloader";
    private static final int MEGABYTE = 1024 * 1024;

    public static void downloadFile(String fileUrl, File directory) {

        try {
            if (DEBUG) Log.v(TAG, "downloadFile() invoked ");
            if (DEBUG) Log.v(TAG, "downloadFile() fileUrl " + fileUrl);
            if (DEBUG) Log.v(TAG, "downloadFile() directory " + directory);

            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(directory);
            int totalSize = urlConnection.getContentLength();

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bufferLength);
            }
            fileOutputStream.close();
            if (DEBUG) Log.v(TAG, "downloadFile() completed ");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (DEBUG) Log.e(TAG, "downloadFile() error" + e.getMessage());
            if (DEBUG) Log.e(TAG, "downloadFile() error" + e.getStackTrace());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (DEBUG) Log.e(TAG, "downloadFile() error" + e.getMessage());
            if (DEBUG) Log.e(TAG, "downloadFile() error" + e.getStackTrace());
        } catch (IOException e) {
            e.printStackTrace();
            if (DEBUG) Log.e(TAG, "downloadFile() error" + e.getMessage());
            if (DEBUG) Log.e(TAG, "downloadFile() error" + e.getStackTrace());
        }
    }
}