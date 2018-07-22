package ru.funfishk.fishka;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yandex.metrica.YandexMetrica;

/**
 * Created by Ghostman on 10.03.2018.
 */

public class Applicat extends MultiDexApplication {

    public static final String BASE_URL = "http://api.spooneys-apps.pw/v1/";
    public static Applicat application = null;


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        FirebaseAnalytics.getInstance(this);
        YandexMetrica.activate(getApplicationContext(), "41f6a047-c455-4b5d-a4d2-b8dd685b6584");
        YandexMetrica.enableActivityAutoTracking(this);

    }

    public static Applicat getInstance() {
        return application;
    }
}
