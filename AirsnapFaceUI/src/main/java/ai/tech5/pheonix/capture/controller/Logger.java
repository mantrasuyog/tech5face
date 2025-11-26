package ai.tech5.pheonix.capture.controller;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Logger {


    public static void logException(String tag, Exception e, File logFILE,boolean writeLogs) {
        e.printStackTrace();
        addToLog(tag, Log.getStackTraceString(e), logFILE,writeLogs);
    }

    /**
     * Adding message to the Log file
     *
     * @param tag     tag
     * @param message message
     */
    public static void addToLog(String tag, String message, File logFile,boolean writeLogs) {
        BufferedWriter buf = null;

        try {

            Log.d(tag, message);


            if(writeLogs) {


                if (!logFile.exists()) {
                    logFile.createNewFile();
                }


                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss_SSS", Locale.US);
                String currentDateandTime = sdf.format(new Date());

                //BufferedWriter for performance, true to set append to file flag
                buf = new BufferedWriter(new FileWriter(logFile, true));

                buf.write(currentDateandTime + ": " + message);
                //buf.append(message);
                buf.newLine();
                buf.flush();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buf != null) {
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
