package cr.ac.ucr.apiconsumer.modelo;

import java.util.List;

public class WeatherResponse {

    private Main main;
    private List<Weather> weather;
    private Sys sys;
    private  String name;

    public WeatherResponse() {
    }


    public WeatherResponse(Main main, List<Weather> weather, Sys sys, String name) {
        this.main = main;
        this.weather = weather;
        this.sys = sys;
        this.name = name;
    }

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "main=" + main +
                ", weather=" + weather +
                ", sys=" + sys +
                ", name='" + name + '\'' +
                '}';
    }
}
