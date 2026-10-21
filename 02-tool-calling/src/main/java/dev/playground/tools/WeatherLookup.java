package dev.playground.tools;

import java.util.Locale;
import java.util.Map;

public final class WeatherLookup {

    private static final Map<String, String> FORECASTS = Map.of(
            "beijing", "Beijing: 22°C, sunny",
            "北京", "北京：22°C，晴",
            "shanghai", "Shanghai: 18°C, cloudy",
            "上海", "上海：18°C，多云",
            "hangzhou", "Hangzhou: 20°C, light rain",
            "杭州", "杭州：20°C，小雨");

    private WeatherLookup() {
    }

    public static String lookup(String city) {
        if (city == null || city.isBlank()) {
            return "error: city must not be blank";
        }
        String trimmed = city.trim();
        String forecast = FORECASTS.get(trimmed.toLowerCase(Locale.ROOT));
        if (forecast == null) {
            forecast = FORECASTS.get(trimmed);
        }
        if (forecast != null) {
            return forecast;
        }
        return trimmed + ": 20°C, partly cloudy (stub, no live forecast)";
    }
}
