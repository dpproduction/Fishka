package ru.funfishk.fishka.clientserver;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.funfishk.fishka.models.MainResponseModel;

/**
 * Created by Ghostman on 10.03.2018.
 */

public interface Repository {
    //Get feed
    @GET("feed")
    Call<MainResponseModel> getFeed(@Query("page") int page, @Query("per_page") int per_page, @Query("locale") String locale);
}
