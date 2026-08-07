package com.example.training_tracker.data.remote

import com.example.training_tracker.BuildConfig
import com.example.training_tracker.data.local.SafeEnumTypeAdapterFactory
import com.example.training_tracker.data.remote.auth.AuthApi
import com.example.training_tracker.data.remote.exercise.ExerciseApi
import com.example.training_tracker.data.remote.history.HistoryApi
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.data.remote.workout.WorkoutApi
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Vai imprimir o corpo da resposta no Logcat
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer<LocalDate> { json, _, _ ->
            LocalDate.parse(json.asString)
        })
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(LocalTime::class.java, JsonDeserializer<LocalTime> { json, _, _ ->
            LocalTime.parse(json.asString)
        })
        .registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.toString())
        })
        .registerTypeAdapter(UUID::class.java, JsonDeserializer<UUID> { json, _, _ ->
            UUID.fromString(json.asString)
        })
        .registerTypeAdapter(UUID::class.java, JsonSerializer<UUID> { src, _, _ ->
            JsonPrimitive(src.toString())
        })
        // mesma protecao contra enums desconhecidos usada nos Converters do Room,
        // para nao quebrar a desserializacao inteira do objeto por um valor invalido
        .registerTypeAdapterFactory(SafeEnumTypeAdapterFactory())
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val exerciseApi: ExerciseApi by lazy {
        retrofit.create(ExerciseApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val workoutApi: WorkoutApi by lazy {
        retrofit.create(WorkoutApi::class.java)
    }

    val historyApi: HistoryApi by lazy {
        retrofit.create(HistoryApi::class.java)
    }

}