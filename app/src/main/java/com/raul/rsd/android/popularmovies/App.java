package com.raul.rsd.android.popularmovies;

import android.app.Application;
import com.raul.rsd.android.popularmovies.view.DetailsActivity;
import com.raul.rsd.android.popularmovies.view.MainActivity;
import javax.inject.Singleton;
import dagger.Component;

public class App extends Application {

    // ------------------------- ATTRIBUTES --------------------------

    private AppComponent appComponent;

    // -------------------------- INTERFACE --------------------------

    @Singleton
    @Component(modules = AppModule.class)
    public interface AppComponent {
        void inject(App application);
        void inject(MainActivity mainActivity);
        void inject(DetailsActivity detailsActivity);
    }

    // ------------------------- CONSTRUCTOR -------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerApp_AppComponent
                .builder()
                .appModule(
                        new AppModule(this))
                .build();
        appComponent.inject(this);
    }

    public AppComponent getComponent() {
        return appComponent;
    }

}
