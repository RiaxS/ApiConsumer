package cr.ac.ucr.apiconsumer.api;

import cr.ac.ucr.apiconsumer.modelo.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface WeatherService {


    @Headers("Content-Type: application/json")
    @GET("weather")
    Call<WeatherResponse> getWeatherByCoodinates(@Query("lat")double lat, @Query("lon")double lon);

}
