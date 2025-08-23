package cz.yogaboy.core.network

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class ApiFactory(
    logInterceptor: Interceptor,
    apiInterceptor: Interceptor,
    chuckerInterceptor: ChuckerInterceptor,
    converterFactory: Converter.Factory,
    context: Context,
) {
    private val timeout = 100L

    private val apiClient = OkHttpClient().newBuilder().apply {
        connectTimeout(timeout, TimeUnit.SECONDS)
        readTimeout(timeout, TimeUnit.SECONDS)
        addInterceptor(apiInterceptor)
        addInterceptor(chuckerInterceptor)
        addInterceptor(logInterceptor)
    }.build()

    private val retrofitApi: Retrofit = Retrofit.Builder()
        .client(apiClient)
        .baseUrl(TvApi.BASE_URL)
        .addConverterFactory(converterFactory)
        .build()

    val API_SERVICE: TvApi = retrofitApi.create(TvApi::class.java)
}
