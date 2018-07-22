package ru.funfishk.fishka.clientserver;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.funfishk.fishka.Applicat;

/**
 * Created by Ghostman on 10.03.2018.
 */

public class ServerConnection {
    private OkHttpClient.Builder httpClient = null;
    private Retrofit.Builder builder = null;

    public ServerConnection() {
        httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        builder = new Retrofit.Builder().baseUrl(Applicat.BASE_URL).addConverterFactory(GsonConverterFactory.create());
    }

    //Create service
    public <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);

    }
}
