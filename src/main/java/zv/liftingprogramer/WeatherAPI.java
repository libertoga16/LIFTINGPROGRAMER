package zv.liftingprogramer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherAPI {
    // Clave de API para autenticarse ante OpenWeatherMap.
    private static final String API_KEY = "4f65d81615a8746a7b0f49736a62c854";
    // URL base de la API, configurada para obtener el clima de Madrid, España, en unidades métricas.
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=Madrid,es&units=metric&appid=";
    
// Clase estática anidada que encapsula la información meteorológica.
    // Es un contenedor inmutable (sus atributos son finales) con la condición, descripción, temperatura y humedad.
     public static class WeatherData {
        public final WeatherCondition condition; // Condición general (SUNNY, CLOUDY, etc.)
        public final String description;          // Descripción detallada del clima (por ejemplo, "clear sky")
        public final double temperature;          // Temperatura en grados Celsius
        public final int humidity;                // Humedad en porcentaje

        // Constructor que inicializa los atributos de la clase WeatherData.
        public WeatherData(WeatherCondition condition, String description, double temperature, int humidity) {
            this.condition = condition;
            this.description = description;
            this.temperature = temperature;
            this.humidity = humidity;
        }
    }
    // Enumeración que define las distintas condiciones meteorológicas que la aplicación soporta.
     
    public enum WeatherCondition {
        
        
        SUNNY,    // Soleado
        CLOUDY,   // Nublado
        RAINY,    // Lluvioso
        STORMY,   // Tormentoso
        SNOWY,    // Nevado
        FOGGY,    // Con niebla
        WINDY     // Ventoso
    }
    
    

    // Método público que obtiene el clima actual mediante una petición HTTP a OpenWeatherMap.
    public static WeatherData getCurrentWeather() {
        try {
            // Se construye la URL completa concatenando la API_URL y la clave API_KEY.
            URL url = new URL(API_URL + API_KEY);
            // Se abre una conexión HTTP con la URL.
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Se establece el método de solicitud a GET.
            conn.setRequestMethod("GET");

            // Se verifica si la respuesta HTTP es exitosa (código 200).
            if (conn.getResponseCode() != 200) {
                // Si no es exitosa, se retorna un objeto WeatherData de respaldo.
                return getFallbackWeather();
            }

            // Se crea un BufferedReader para leer la respuesta de la API.
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;

            // Se lee la respuesta línea por línea y se añade al StringBuilder.
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Se cierra el BufferedReader.
            in.close();
            // Se desconecta la conexión HTTP.
            conn.disconnect();

            // Se parsea la cadena JSON obtenida a un objeto JsonObject usando la librería Gson.
            JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();
            // Se procesa el JSON para extraer la información meteorológica y se retorna un objeto WeatherData.
            return parseWeatherData(json);
        } catch (Exception e) {
            // Si ocurre cualquier excepción, se imprime la traza del error...
            e.printStackTrace();
            // ...y se retorna un objeto WeatherData de respaldo.
            return getFallbackWeather();
        }
    }

    // Método privado que procesa el JsonObject obtenido de la API y extrae los datos de clima.
    private static WeatherData parseWeatherData(JsonObject json) {
        // Se extrae el campo "main" del primer elemento del array "weather" (por ejemplo, "Clear", "Clouds", etc.).
        String mainCondition = json.getAsJsonArray("weather")
                .get(0).getAsJsonObject()
                .get("main").getAsString();

        // Se extrae la descripción detallada del clima (por ejemplo, "clear sky", "broken clouds").
        String description = json.getAsJsonArray("weather")
                .get(0).getAsJsonObject()
                .get("description").getAsString();

        // Se obtiene la temperatura actual desde el objeto "main" del JSON.
        double temp = json.getAsJsonObject("main").get("temp").getAsDouble();
        // Se obtiene la humedad (en porcentaje) desde el mismo objeto "main".
        int humidity = json.getAsJsonObject("main").get("humidity").getAsInt();

        // Se declara una variable para la condición meteorológica (WeatherCondition).
        WeatherCondition condition;
        // Se utiliza un switch para mapear el valor extraído de "main" a una de las condiciones definidas.
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
                // En caso de que no se reconozca la condición, se asigna por defecto CLOUDY.
                condition = WeatherCondition.CLOUDY;
        }

        // Se crea y retorna un objeto WeatherData con los datos extraídos.
        return new WeatherData(condition, description, temp, humidity);
    }

    // Método privado que retorna un objeto WeatherData con valores por defecto en caso de error.
    private static WeatherData getFallbackWeather() {
        // Se retorna un clima por defecto: nublado, temperatura 20°C y humedad 65%.
        return new WeatherData(WeatherCondition.CLOUDY, "nublado", 20.0, 65);
    }

    // Método público que aplica efectos meteorológicos a un jugador.
    // Modifica los atributos (ataque, defensa, velocidad) del jugador en función de la condición del clima.
    public static void applyWeatherEffects(PLAYER player, WeatherData weather) {
        // Aquí se podrían guardar los valores base si no están establecidos (comentario pendiente: "Guardar los valores base si no están establecidos").
       
        // Se aplican modificadores a las estadísticas del jugador según la condición del clima.
        switch (weather.condition) {
            case SUNNY:
                // Si el clima es soleado, se incrementa el ataque en un 25% y la velocidad en un 15%.
                player.setAttack(player.getBaseAttack() * 1.25);
                player.setSpeed(player.getBaseSpeed() * 1.15);
                break;
            case CLOUDY:
                // En clima nublado no se aplican efectos.
                break;
            case RAINY:
                // Clima lluvioso: disminuye el ataque al 90% y la velocidad al 85%, pero aumenta la defensa en un 10%.
                player.setAttack(player.getBaseAttack() * 0.9);
                player.setSpeed(player.getBaseSpeed() * 0.85);
                player.setDefend(player.getBaseDefend() * 1.10);
                break;
            case STORMY:
                // Clima tormentoso: reduce el ataque al 80% y aumenta la defensa en un 15%.
                player.setAttack(player.getBaseAttack() * 0.8);
                player.setDefend(player.getBaseDefend() * 1.15);
                break;
            case SNOWY:
                // Clima nevado: reduce la velocidad al 75% y aumenta la defensa en un 20%.
                player.setSpeed(player.getBaseSpeed() * 0.75);
                player.setDefend(player.getBaseDefend() * 1.20);
                break;
            case FOGGY:
                // Clima con niebla: reduce la velocidad al 80% y reduce el ataque al 90%.
                player.setSpeed(player.getBaseSpeed() * 0.8);
                player.setAttack(player.getBaseAttack() * 0.9);
                break;
            case WINDY:
                // Clima ventoso: aumenta la velocidad en un 20% y reduce la defensa en un 10%.
                player.setSpeed(player.getBaseSpeed() * 1.20);
                player.setDefend(player.getBaseDefend() * 0.9);
                break;
        }

        // Se aseguran valores mínimos en cada atributo para evitar que sean demasiado bajos o negativos.
        player.setAttack(Math.max(1, player.getAttack()));
        player.setDefend(Math.max(0.5, player.getDefend()));
        player.setSpeed(Math.max(0.5, player.getSpeed()));
    }

    
   
}
