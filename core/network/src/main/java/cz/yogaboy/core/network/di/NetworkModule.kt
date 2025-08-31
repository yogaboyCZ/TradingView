package cz.yogaboy.core.network.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.squareup.moshi.Moshi
import cz.yogaboy.core.network.BuildConfig
import retrofit2.Converter
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

val networkModule = module {
    single { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    single<Converter.Factory> { MoshiConverterFactory.create(get()) }

    single<Interceptor>(named("log")) {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
    }
    single<Interceptor>(named("api")) { Interceptor { chain -> chain.proceed(chain.request()) } }
    single<Interceptor>(named("chucker")) { ChuckerInterceptor.Builder(get<Context>()).build() }

    single {
        OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .addInterceptor(get<Interceptor>(named("api")))
            .addInterceptor(get<Interceptor>(named("chucker")))
            .addInterceptor(get<Interceptor>(named("log")))
            .build()
    }
}