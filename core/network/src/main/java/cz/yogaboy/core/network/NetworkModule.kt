package cz.yogaboy.core.network

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { Moshi.Builder().build() }
    single<Converter.Factory> { MoshiConverterFactory.create(get()) }

    single<Interceptor>(named("log")) {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
    }
    single<Interceptor>(named("api")) { Interceptor { chain -> chain.proceed(chain.request()) } }
    single<Interceptor>(named("chucker")) { ChuckerInterceptor.Builder(androidContext()).build() }

    single {
        OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .addInterceptor(get<Interceptor>(named("api")))
            .addInterceptor(get<Interceptor>(named("chucker")))
            .addInterceptor(get<Interceptor>(named("log")))
            .build()
    }

    single {
        Retrofit.Builder()
            .client(get())
            .baseUrl(BuildConfig.BASE_URI)
            .addConverterFactory(get())
            .build()
    }

    single { get<Retrofit>().create(TvApi::class.java) }

    single {
        ApiFactory(
            logInterceptor = get(named("log")),
            apiInterceptor = get(named("api")),
            chuckerInterceptor = get(named("chucker")),
            converterFactory = get(),
            context = androidContext()
        )
    }
}
