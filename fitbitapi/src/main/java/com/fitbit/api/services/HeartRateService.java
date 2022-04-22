package com.fitbit.api.services;

import android.app.Activity;
import android.content.Loader;

import com.fitbit.api.exceptions.MissingScopesException;
import com.fitbit.api.exceptions.TokenExpiredException;
import com.fitbit.api.loaders.ResourceLoaderFactory;
import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.HeartRateContainer;
import com.fitbit.authentication.Scope;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by jboggess on 10/3/16.
 */
public class HeartRateService {

    private final static String HEART_RATE_URL = "https://api.fitbit.com/1/user/-/activities/heart/date/%s/1d/1min/time/%s/%s.json";
    private static final ResourceLoaderFactory<HeartRateContainer> USER_HEART_RATE_LOADER_FACTORY = new ResourceLoaderFactory<HeartRateContainer>(HEART_RATE_URL, HeartRateContainer.class);

    public static Loader<ResourceLoaderResult<HeartRateContainer>> getHeartRateSummaryLoader(Activity activityContext, Long startTime, Long endTime) throws MissingScopesException, TokenExpiredException {
        return USER_HEART_RATE_LOADER_FACTORY.newResourceLoader(activityContext, new Scope[]{Scope.heartrate}, getStringDate(startTime), getStringTime(startTime), getStringTime(endTime));
    }

    public static String getStringDate(Long d) {
        LocalDateTime date;
        String formatDate = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date = Instant.ofEpochSecond(d).atZone(ZoneId.of("UTC")).toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            formatDate = date.format(formatter);
        }

        return formatDate;
    }



    public static String getStringTime(Long d) {
        LocalDateTime date;
        String formatDate = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            date = Instant.ofEpochSecond(d).atZone(ZoneId.of("UTC")).toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            formatDate = date.format(formatter);
        }

        return formatDate;
    }
}
