package com.example.training_tracker.data.models

import androidx.annotation.StringRes
import com.example.training_tracker.R

enum class Technique(@StringRes val label: Int){
    NORMAL(R.string.normal),
    PEAK_CONTRACTION(R.string.pico_de_contracao),
    DROP_SET(R.string.drop_set),
    REST_PAUSE(R.string.rest_pause),
    BI_SET(R.string.bi_set_super_set),
    ISOMETRIC(R.string.isometria),
    SLOW_ECCENTRIC(R.string.excentrica_lenta),
    PARTIAL_REPS(R.string.repeticoes_parciais),
    CLUSTER_SET(R.string.cluster_set),
    MYO_REPS(R.string.myo_reps)
}