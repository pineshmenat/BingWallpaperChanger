/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bingo;

/**
 *
 * @author Pinesh
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.*;
import java.io.IOException;

public class BingWallpaperChanger {

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0";

    public static interface User32 extends Library {

        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SystemParametersInfo(int one, int two, String s, int three);
    }

    public static void main(String[] args) throws Exception {

        BingWallpaperChanger http = new BingWallpaperChanger();

        http.bingWallpaperDownloadAndSet();
    }

    // HTTP GET request
    private void bingWallpaperDownloadAndSet() throws Exception {

        try {
            String url = "http://www.bing.com/";
            String picturesPath = System.getProperty("user.home") + "\\Pictures\\";
            String videoPath = System.getProperty("user.home") + "\\Videos\\";

            URL obj = new URL(url);
            System.out.println("Sending 'GET' request to : " + url + "..........");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("Server Response Code : " + responseCode);

            if (responseCode == 200) {

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    //System.out.println(inputLine);
                }
                in.close();

                String stringToSearch = response.toString();

                // the pattern we want to search for
                String match = "g_img={url: \"";                //Find g_img tag
                int position = stringToSearch.indexOf(match);
                if (position != -1) {
                    String newString = stringToSearch.substring(position + 14);
                    int posend = newString.indexOf("\"");
                    String urlimg = "http://" + newString.substring(1, posend); //Substring of URL till "

                    String imagename = urlimg.substring(urlimg.lastIndexOf("/") + 1); //Save as image name
                    System.out.println("\nImage name: " + imagename);

                    System.out.println("Image URL: " + urlimg);

                    try (InputStream in1 = new URL(urlimg).openStream()) {    //Download Image
                        File restorefile = new File(picturesPath + imagename);
                        if (!restorefile.exists()) {
                            Files.copy(in1, Paths.get(picturesPath + imagename));
                            System.out.println("New Wallpaper saved at: " + picturesPath + imagename);
                        } else {
                            System.out.println("Wallpaper Already Exists at: " + picturesPath + imagename);
                        }
                    }

                    if (User32.INSTANCE.SystemParametersInfo(0x0014, 0, (picturesPath + imagename), 1)) //Set as wallpaper
                    {
                        System.out.println("Bingo, Wallpaper Set!!\n");
                    } else {
                        System.out.println("Error Setting Wallpaper!!");
                    }
                }
                else
                    System.out.println("Image not found!!");

                String matchV = "mp4hd\", \"video\\/mp4;";                //Find g_img tag
                int positionV = stringToSearch.indexOf(matchV);

                if (positionV != -1) {
                    String newStringV = stringToSearch.substring(positionV + 62);

                    int posendV = newStringV.indexOf("\"");
                    String urlVid = "http://" + newStringV.substring(0, posendV); //Substring of URL till "

                    String vidname = urlVid.substring(urlVid.lastIndexOf("/") + 1); //Save as image name
                    System.out.println("Video name: " + vidname);

                    urlVid = urlVid.replaceAll("\\\\/", "/");
                    System.out.println("Video URL: " +  urlVid);
                    
                    try (InputStream in1 = new URL(urlVid).openStream()) {    //Download Image
                        File restorefile = new File(videoPath + vidname);
                        if (!restorefile.exists()) {
                            Files.copy(in1, Paths.get(videoPath + vidname));
                            System.out.println("New Video saved at: " + videoPath + vidname);
                        } else {
                            System.out.println("Video Already Exists at: " + videoPath + vidname);
                        }

                        System.exit(0);
                    }
                }
                else
                    System.out.println("Video Not Found!!");

            }
        } catch (IOException e) {
            System.out.println(e.fillInStackTrace());
        }
    }
}
