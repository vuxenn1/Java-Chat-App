import java.time.*;
import java.time.format.*;
import java.io.*;

public class LogWriter
{
    private String logTxt = "";

    public String getLogTxt() {
        return logTxt;
    }

    public void logTextGenerator(String txt)
    {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String formattedDateTime = "["+currentDateTime.format(formatter)+"]";
        String text = formattedDateTime + " " + txt;

        logTxt += text + "\n";
    }

    public void log(String text)
    {
        String leaderboardPath = "log.txt";
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter(leaderboardPath));
            bw.write(text);
            bw.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}