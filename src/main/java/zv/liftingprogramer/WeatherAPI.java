package zv.liftingprogramer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import zv.liftingprogramer.characters.Character;

public class WeatherAPI {
    private static final String API_KEY = "4f65d81615a8746a7b0f49736a62c854"; // Reemplaza con tu clave real
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
            default:
                condition = WeatherCondition.CLOUDY;
        }

        return new WeatherData(condition, description, temp, humidity);
    }

    private static WeatherData getFallbackWeather() {
        return new WeatherData(WeatherCondition.CLOUDY, "nublado", 20.0, 65);
    }

    public static void applyWeatherEffects(Character character, WeatherData weather) {
        double originalAttack = character.getAttack();
        double originalDefend = character.getDefend();
        double originalSpeed = character.getSpeed();

        switch (weather.condition) {
            case SUNNY:
                character.attack *= 1.2;
                character.speed *= 1.1;
                break;
            case CLOUDY:
                break;
            case RAINY:
                character.attack *= 0.9;
                character.speed *= 0.9;
                character.defend *= 1.05;
                break;
            case STORMY:
                character.attack *= 0.8;
                character.defend *= 1.1;
                break;
            case SNOWY:
                character.speed *= 0.8;
                character.defend *= 1.15;
                break;
            case FOGGY:
                character.speed *= 0.85;
                break;
            case WINDY:
                character.speed *= 1.15;
                character.defend *= 0.95;
                break;
        }

        character.weatherAttackModifier = character.getAttack() / originalAttack;
        character.weatherDefendModifier = character.getDefend() / originalDefend;
        character.weatherSpeedModifier = character.getSpeed() / originalSpeed;
    }

    public static void removeWeatherEffects(Character character) {
        if (character.weatherAttackModifier != 1.0) {
            character.attack /= character.weatherAttackModifier;
        }
        if (character.weatherDefendModifier != 1.0) {
            character.defend /= character.weatherDefendModifier;
        }
        if (character.weatherSpeedModifier != 1.0) {
            character.speed /= character.weatherSpeedModifier;
        }

        character.weatherAttackModifier = 1.0;
        character.weatherDefendModifier = 1.0;
        character.weatherSpeedModifier = 1.0;
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