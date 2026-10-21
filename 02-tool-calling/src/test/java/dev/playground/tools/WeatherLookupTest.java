package dev.playground.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WeatherLookupTest {

    @Test
    void returnsKnownForecastsIgnoringAsciiCase() {
        assertThat(WeatherLookup.lookup("北京")).isEqualTo("北京：22°C，晴");
        assertThat(WeatherLookup.lookup("  Beijing ")).isEqualTo("Beijing: 22°C, sunny");
        assertThat(WeatherLookup.lookup("上海")).contains("多云");
    }

    @Test
    void unknownCityIsStillAStubForecast() {
        assertThat(WeatherLookup.lookup("Mars")).isEqualTo("Mars: 20°C, partly cloudy (stub, no live forecast)");
    }

    @Test
    void blankCityIsAnErrorResult() {
        assertThat(WeatherLookup.lookup("  ")).isEqualTo("error: city must not be blank");
        assertThat(WeatherLookup.lookup(null)).isEqualTo("error: city must not be blank");
    }
}
