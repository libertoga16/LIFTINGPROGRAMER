package zv.liftingprogramer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import zv.liftingprogramer.characters.PLAYER;

public class WeatherAPI {
    private static final String API_KEY = "4f65d81615a8746a7b0f49736a62c854";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=Granada,es&units=metric&appid=";

    public enum WeatherCondition {
        SUNNY, CLOUDY, RAINY, STORMY, SNOWY, FOGGY, WINDY
    }

    public static WeatherData getCurrentWeather() {
        try {
            URL url = new URL(API_URL + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                return getFallbackWeather();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();
            return parseWeatherData(json);
        } catch (Exception e) {
            e.printStackTrace();
            return getFallbackWeather();
        }
    }

    private static WeatherData parseWeatherData(JsonObject json) {
        String mainCondition = json.getAsJsonArray("weather")
                .get(0).getAsJsonObject()
                .get("main").getAsString();

        String description = json.getAsJsonArray("weather")
                .get(0).getAsJsonObject()
                .get("description").getAsString();

        double temp = json.getAsJsonObject("main").get("temp").getAsDouble();
        int humidity = json.getAsJsonObject("main").get("humidity").getAsInt();

        WeatherCondition condition;
        switch (mainCondition.toLowerCase()) {
            case "clear":
                condition = WeatherCondition.SUNNY;
                break;
            case "clouds":
                condition = WeatherCondition.CLOUDY;
                break;
            case "rain":
            case "drizzle":
                condition = WeatherCondition.RAINY;
                break;
            case "thunderstorm":
                condition = WeatherCondition.STORMY;
                break;
            case "snow":
                condition = WeatherCondition.SNOWY;
                break;
            case "fog":
            case "mist":
                condition = WeatherCondition.FOGGY;
                break;
            case "wind":
                condition = WeatherCondition.WINDY;
                break;
            default:
                condition = WeatherCondition.CLOUDY;
        }

        return new WeatherData(condition, description, temp, humidity);
    }

    private static WeatherData getFallbackWeather() {
        return new WeatherData(WeatherCondition.CLOUDY, "nublado", 20.0, 65);
    }

    public static void applyWeatherEffects(PLAYER player, WeatherData weather) {
        // Guardar los valores originales si no están establecidos
        if (player.getOriginalAttack() <= 0) player.setOriginalAttack(player.getAttack());
        if (player.getOriginalDefend() <= 0) player.setOriginalDefend(player.getDefend());
        if (player.getOriginalSpeed() <= 0) player.setOriginalSpeed(player.getSpeed());

        // Aplicar modificadores según el clima
        switch (weather.condition) {
            case SUNNY:
                player.setAttack(player.getOriginalAttack() * 1.25);
                player.setSpeed(player.getOriginalSpeed() * 1.15);
                break;
            case CLOUDY:
                // Sin efectos
                break;
            case RAINY:
                player.setAttack(player.getOriginalAttack() * 0.9);
                player.setSpeed(player.getOriginalSpeed() * 0.85);
                player.setDefend(player.getOriginalDefend() * 1.10);
                break;
            case STORMY:
                player.setAttack(player.getOriginalAttack() * 0.8);
                player.setDefend(player.getOriginalDefend() * 1.15);
                break;
            case SNOWY:
                player.setSpeed(player.getOriginalSpeed() * 0.75);
                player.setDefend(player.getOriginalDefend() * 1.20);
                break;
            case FOGGY:
                player.setSpeed(player.getOriginalSpeed() * 0.8);
                player.setAttack(player.getOriginalAttack() * 0.9);
                break;
            case WINDY:
                player.setSpeed(player.getOriginalSpeed() * 1.20);
                player.setDefend(player.getOriginalDefend() * 0.9);
                break;
        }

        // Asegurar valores mínimos
        player.setAttack(Math.max(1, player.getAttack()));
        player.setDefend(Math.max(0.5, player.getDefend()));
        player.setSpeed(Math.max(0.5, player.getSpeed()));
    }

    public static void removeWeatherEffects(PLAYER player) {
        if (player.getOriginalAttack() > 0) {
            player.setAttack(player.getOriginalAttack());
        }
        if (player.getOriginalDefend() > 0) {
            player.setDefend(player.getOriginalDefend());
        }
        if (player.getOriginalSpeed() > 0) {
            player.setSpeed(player.getOriginalSpeed());
        }
        
        // Restablecer valores originales
        player.resetOriginalStats();
    }

    public static class WeatherData {
        public final WeatherCondition condition;
        public final String description;
        public final double temperature;
        public final int humidity;

        public WeatherData(WeatherCondition condition, String description, double temperature, int humidity) {
            this.condition = condition;
            this.description = description;
            this.temperature = temperature;
            this.humidity = humidity;
        }
    }
}