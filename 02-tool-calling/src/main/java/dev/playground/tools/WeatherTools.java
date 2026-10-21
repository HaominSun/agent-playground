package dev.playground.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTools {

    @Tool(description = "Look up the current weather for a city. This is a local stub, not a live forecast.",
            resultConverter = PlainTextToolResultConverter.class)
    public String getWeather(@ToolParam(description = "City name, for example Beijing or 上海") String city) {
        return WeatherLookup.lookup(city);
    }
}
