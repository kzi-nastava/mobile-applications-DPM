package com.example.dpm.Model;

import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeoUtils {

    // Reverse geocoding preko OSM Nominatim-a (kratka adresa + latinica)
    public static void fetchAddressFromCoordinates(
            double lat,
            double lon,
            OnSuccessListener<String> listener
    ) {
        new Thread(() -> {
            try {
                String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json"
                        + "&lat=" + lat
                        + "&lon=" + lon
                        + "&zoom=18&addressdetails=1";

                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "dpm-app");
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(result.toString());
                JSONObject addr = jsonObject.optJSONObject("address");

                // Kratko: ulica + broj, naselje (ako postoji), grad
                String road = addr != null ? addr.optString("road", "") : "";
                String house = addr != null ? addr.optString("house_number", "") : "";
                String suburb = addr != null ? addr.optString("suburb", "") : "";

                String city = "";
                if (addr != null) {
                    city = addr.optString("city", "");
                    if (city.isEmpty()) city = addr.optString("town", "");
                    if (city.isEmpty()) city = addr.optString("village", "");
                    if (city.isEmpty()) city = addr.optString("municipality", "");
                }

                String shortAddr = "";
                if (!road.isEmpty()) shortAddr += road;
                if (!house.isEmpty()) shortAddr += (shortAddr.isEmpty() ? "" : " ") + house;

                if (!suburb.isEmpty()) {
                    shortAddr += (shortAddr.isEmpty() ? "" : ", ") + suburb;
                }
                if (!city.isEmpty()) {
                    shortAddr += (shortAddr.isEmpty() ? "" : ", ") + city;
                }

                if (shortAddr.trim().isEmpty()) {
                    shortAddr = "Lat " + lat + ", Lon " + lon;
                }

                String finalAddr = toLatin(shortAddr);

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.onSuccess(finalAddr)
                );

            } catch (Exception e) {
                e.printStackTrace();

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.onSuccess("Unknown location")
                );
            }
        }).start();
    }

    // Srpska ćirilica -> latinica (dovoljno za UI)
    private static String toLatin(String s) {
        if (s == null) return "";

        return s
                .replace("А","A").replace("Б","B").replace("В","V").replace("Г","G").replace("Д","D")
                .replace("Ђ","Đ").replace("Е","E").replace("Ж","Ž").replace("З","Z").replace("И","I")
                .replace("Ј","J").replace("К","K").replace("Л","L").replace("Љ","Lj").replace("М","M")
                .replace("Н","N").replace("Њ","Nj").replace("О","O").replace("П","P").replace("Р","R")
                .replace("С","S").replace("Т","T").replace("Ћ","Ć").replace("У","U").replace("Ф","F")
                .replace("Х","H").replace("Ц","C").replace("Ч","Č").replace("Џ","Dž").replace("Ш","Š")
                .replace("а","a").replace("б","b").replace("в","v").replace("г","g").replace("д","d")
                .replace("ђ","đ").replace("е","e").replace("ж","ž").replace("з","z").replace("и","i")
                .replace("ј","j").replace("к","k").replace("л","l").replace("љ","lj").replace("м","m")
                .replace("н","n").replace("њ","nj").replace("о","o").replace("п","p").replace("р","r")
                .replace("с","s").replace("т","t").replace("ћ","ć").replace("у","u").replace("ф","f")
                .replace("х","h").replace("ц","c").replace("ч","č").replace("џ","dž").replace("ш","š");
    }
}