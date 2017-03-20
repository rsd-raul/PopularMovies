package com.raul.rsd.android.popularmovies;

import android.app.Application;

import com.raul.rsd.android.popularmovies.data.MoviesProvider;
import com.raul.rsd.android.popularmovies.view.ActorActivity;
import com.raul.rsd.android.popularmovies.view.MovieActivity;
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
        void inject(MovieActivity movieActivity);
        void inject(ActorActivity actorActivity);
        void inject(MoviesProvider moviesProvider);
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
