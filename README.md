# PopularMovies

First and Second project for the Google/Udacity scholarship, the "Associate Android Developer Fast Track".

## Udacity reviews:

* [Popular Movies Mk1](https://review.udacity.com/#!/reviews/359360/shared)
* [Popular Movies Mk2](https://review.udacity.com/#!/reviews/422374/shared)

## Features available:

* Discover the most popular, most rated and upcoming (**v2.0**) movies.
* Access to details about those movies.
* Store offline your favourite movies. **v2.0**
* Access trailers, reviews and cast. **v2.0**
* Search movies by name. **v2.0**
* Search actors by name. **v2.0**

## Configuration:

This app uses [The Movie Database](https://www.themoviedb.org/documentation/api) as source of content, therefore, in order to build your own version you will need to include a valid API key for the service in your `gradle.properties` file under the name `TMDBApiKeyV3`.

 **DISCLAIMER:** This application is under **ACTIVE** development, use with caution.
 
## Ext. Libraries in use

* [ButterKnife](https://github.com/JakeWharton/butterknife)
* [Retrofit 2](https://github.com/square/retrofit)
* [Picasso](https://github.com/bumptech/glide)
* [MaterialDialogs](https://github.com/afollestad/material-dialogs)
* [Dagger 2](https://google.github.io/dagger/) **v2.0**
* [FastAdapter](https://github.com/mikepenz/FastAdapter) **v2.0**
* [SearchView](https://github.com/lapism/SearchView) **v2.0**
* [BottomNavigation](https://github.com/aurelhubert/ahbottomnavigation) **v2.0**
* [FloatingActionButton](https://github.com/Clans/FloatingActionButton) **v1.0**
 
## Future:

Features to be implemented:
* In Theaters - location based feature (Cinema search if possible).
* [Trakt.tv](https://trakt.tv/) integration.
* [IMDB](http://www.imdb.com/) integration.
* Upcoming movies with release reminder (notification/event).

Ext. Libraries to be used:
* [Realm](https://github.com/realm/realm-java)  **v3.0**
* [RxJava](https://github.com/ReactiveX/RxJava) **v3.0**
* [RxAndroid](https://github.com/ReactiveX/RxAndroid) **v3.0**
