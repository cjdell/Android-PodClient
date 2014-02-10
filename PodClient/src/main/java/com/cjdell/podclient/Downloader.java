package com.cjdell.podclient;

import android.content.Context;
import android.os.PowerManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by cjdell on 24/11/13.
 */
public class Downloader {

    private Context context;
    private Boolean cancelled = false;

    public Downloader(Context context) {
        this.context = context;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public String downloadFile(String sUrl, String savePath) {
        URL url = null;

        try {
            url = new URL(sUrl);
        }
        catch (MalformedURLException e) {
            return "Invalid URL";
        }

        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

        wl.acquire();

        try {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(savePath);
                //output = this.context.openFileOutput(savePath, Context.MODE_PRIVATE);

                byte data[] = new byte[4096];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (this.cancelled) return "Cancelled";
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            }
            catch (Exception e) {
                return e.toString();
            }
            finally {
                try {
                    if (output != null) output.close();
                    if (input != null) input.close();
                }
                catch (IOException ignored) { }

                if (connection != null) connection.disconnect();
            }
        }
        finally {
            wl.release();
        }

        return "Complete";
    }

    protected void publishProgress(int percent) {

    }
}
