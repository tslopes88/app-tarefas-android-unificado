package com.example.apptarefasunificado

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class TarefaStorage(context: Context) {

    private val preferences = context.getSharedPreferences("PreferenciasApp", Context.MODE_PRIVATE)
    private val CHAVE_TAREFAS = "lista_tarefas_v_final"
    private val TAG = "TarefaStorage"

    fun salvar(lista: List<Tarefa>) {
        val jsonArray = JSONArray()
        for (tarefa in lista) {
            val jsonObject = JSONObject().apply {
                put("id", tarefa.id)
                put("nome", tarefa.nome)
                put("descricao", tarefa.descricao)
                put("data", tarefa.data)
                put("hora", tarefa.hora)
                put("prioridade", tarefa.prioridade)
                put("concluida", tarefa.concluida)
            }
            jsonArray.put(jsonObject)
        }
        preferences.edit().putString(CHAVE_TAREFAS, jsonArray.toString()).apply()
    }

    fun carregar(): MutableList<Tarefa> {
        val jsonString = preferences.getString(CHAVE_TAREFAS, null) ?: return mutableListOf()
        val lista = mutableListOf<Tarefa>()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lista.add(
                    Tarefa(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        nome = obj.optString("nome", "Sem título"),
                        descricao = obj.optString("descricao", ""),
                        data = obj.optString("data", "Hoje"),
                        hora = obj.optString("hora", "Sem hora"),
                        prioridade = obj.optString("prioridade", "Média"),
                        concluida = obj.optBoolean("concluida", false)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao desserializar JSON de tarefas, iniciando lista vazia", e)
            return mutableListOf()
        }

        return lista
    }
}