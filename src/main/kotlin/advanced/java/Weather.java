package advanced.java;

import java.awt.*;

public class Weather {

    public void updateWeather(int degrees) {
        String description;
        Color color;
        if (degrees < 5) {
            description = "cold";
            color = Color.BLUE;
        } else if (degrees < 23) {
            description = "mild";
            color = Color.YELLOW;
        } else {
            description = "hot";
            color = Color.RED;
        }
        // ...
    }
}
