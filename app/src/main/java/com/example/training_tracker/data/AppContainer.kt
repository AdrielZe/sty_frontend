package com.example.training_tracker.data

import com.example.training_tracker.data.repository.UserRepository
import com.example.training_tracker.data.repository.WorkoutRepository

// Esta interface define o que o container deve fornecer
interface AppContainer {
    val userRepository: UserRepository
    val workoutRepository: WorkoutRepository
}

// Esta é a implementação real do container
class DefaultAppContainer : AppContainer {

    // O 'by lazy' garante que o UserRepository só será instanciado
    // na primeira vez que for chamado, e depois a mesma instância será reutilizada.
    override val userRepository: UserRepository by lazy {
        UserRepository()
    }

    override val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository()
    }
}