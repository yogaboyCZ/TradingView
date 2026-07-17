package cz.yogaboy.core.network.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import cz.yogaboy.core.network.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.annotation.Named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import retrofit2.Converter
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private fun createMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
private fun createConverter(moshi: Moshi): Converter.Factory = MoshiConverterFactory.create(moshi)

@Named("log")
private fun createLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
    redactQueryParams("apikey")
    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
}

@Named("api")
private fun createApiInterceptor(): Interceptor = Interceptor { chain -> chain.proceed(chain.request()) }

@Named("chucker")
private fun createChuckerInterceptor(context: Context): Interceptor =
    ChuckerInterceptor.Builder(context).build()

private fun createHttpClient(
    @Named("api") api: Interceptor,
    @Named("chucker") chucker: Interceptor,
    @Named("log") log: Interceptor,
): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(100, TimeUnit.SECONDS)
    .readTimeout(100, TimeUnit.SECONDS)
    .addInterceptor(api)
    .addInterceptor(chucker)
    .addInterceptor(log)
    .build()

val networkModule = module {
    single { create(::createMoshi) }
    single { create(::createConverter) }
    single { create(::createLoggingInterceptor) }
    single { create(::createApiInterceptor) }
    single { create(::createChuckerInterceptor) }
    single { create(::createHttpClient) }
}
