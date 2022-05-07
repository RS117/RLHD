package rs117.hd.utils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Utils {

    public static String stripHtml(String text) {
        String formatted = text.replace("<br>","\n").replaceAll("\\<[^>]*>","");
        return formatted;
    }

    public static String getSpecs(String runeliteVersion) {

        StringBuilder temp = new StringBuilder("============  117 HD Specification =================\n");
        temp.append("Runelite Version: " + runeliteVersion+ "\n");
        temp.append("Plugin Version: " + runeliteVersion + "\n");
        temp.append("Client Architecture: " + System.getProperty("sun.arch.data.model") + "\n");
        temp.append("System Architecture: " + System.getProperty("os.arch")  + "\n");
        temp.append("Operating System: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ")" + "\n");
        temp.append("Processor: " + System.getenv("PROCESSOR_IDENTIFIER") + "\n");
        temp.append("Free memory: " + bytesToMb(Runtime.getRuntime().freeMemory()) + "\n");
        temp.append("Maximum memory: " + bytesToMb(Runtime.getRuntime().maxMemory()) + "\n");
        temp.append("===================================================\n");

        return temp.toString();
    }

    public static String bytesToMb(long bytes)
    {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950)
        {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

}