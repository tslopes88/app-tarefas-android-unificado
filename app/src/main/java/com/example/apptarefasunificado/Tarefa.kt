package com.example.apptarefasunificado

data class Tarefa(
    val id: Long = System.currentTimeMillis(),
    var nome: String,
    var descricao: String,
    var data: String,
    var hora: String,
    var prioridade: String = "Média",
    var concluida: Boolean = false
)