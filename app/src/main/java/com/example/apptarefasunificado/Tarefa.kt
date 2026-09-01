package com.example.apptarefasunificado

data class Tarefa(
    val id: Long,
    val nome: String,
    val descricao: String = "",
    val prioridade: String = "",
    val concluida: Boolean = false,
    val data: String = "",
    val hora: String = ""
)