package com.example.apptarefasunificado

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var storage: TarefaStorage
    private lateinit var txtListaVazia: TextView
    private lateinit var txtContadorTarefas: TextView
    private lateinit var txtStatusFormulario: TextView
    private lateinit var editNome: EditText
    private lateinit var editDescricao: EditText
    private lateinit var btnSelecionarData: Button
    private lateinit var btnSelecionarHora: Button
    private lateinit var btnPrioridade: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnCancelarEdicao: Button
    private lateinit var btnFiltroTodas: Button
    private lateinit var btnFiltroPendentes: Button
    private lateinit var btnFiltroConcluidas: Button
    private lateinit var adapter: TarefaAdapter

    private lateinit var listaTarefasOriginal: MutableList<Tarefa>
    private var listaTarefasExibida: MutableList<Tarefa> = mutableListOf()

    private var dataSelecionada: String = ""
    private var horaSelecionada: String = ""
    private var prioridadeSelecionada: String = "Média"
    private var filtroAtual: String = "TODAS"
    private var tarefaEmEdicao: Tarefa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = TarefaStorage(this)
        listaTarefasOriginal = storage.carregar()

        editNome = findViewById(R.id.editNome)
        editDescricao = findViewById(R.id.editDescricao)
        txtStatusFormulario = findViewById(R.id.txtStatusFormulario)
        btnSelecionarData = findViewById(R.id.btnSelecionarData)
        btnSelecionarHora = findViewById(R.id.btnSelecionarHora)
        btnPrioridade = findViewById(R.id.btnPrioridade)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnCancelarEdicao = findViewById(R.id.btnCancelarEdicao)
        btnFiltroTodas = findViewById(R.id.btnFiltroTodas)
        btnFiltroPendentes = findViewById(R.id.btnFiltroPendentes)
        btnFiltroConcluidas = findViewById(R.id.btnFiltroConcluidas)
        txtListaVazia = findViewById(R.id.txtListaVazia)
        txtContadorTarefas = findViewById(R.id.txtContadorTarefas)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTarefas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Alternância de Prioridades
        btnPrioridade.setOnClickListener {
            when (prioridadeSelecionada) {
                "Média" -> atualizarBotaoPrioridade("Alta", R.color.colorPriorityHigh, "🔴 Alta")
                "Alta" -> atualizarBotaoPrioridade("Baixa", R.color.colorPriorityLow, "🟢 Baixa")
                else -> atualizarBotaoPrioridade("Média", R.color.colorPriorityMedium, "🟡 Média")
            }
        }

        // Seletores modais de Data e Hora
        btnSelecionarData.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, ano, mes, dia ->
                dataSelecionada = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, ano)
                btnSelecionarData.text = "📅 $dataSelecionada"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSelecionarHora.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hora, min ->
                horaSelecionada = String.format(Locale.getDefault(), "%02d:%02d", hora, min)
                btnSelecionarHora.text = "⏰ $horaSelecionada"
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // Adapter com CRUD
        adapter = TarefaAdapter(
            tarefas = listaTarefasExibida,
            onToggleConcluida = { tarefaModificada ->
                val index = listaTarefasOriginal.indexOfFirst { it.id == tarefaModificada.id }
                if (index != -1) {
                    listaTarefasOriginal[index] = tarefaModificada
                    storage.salvar(listaTarefasOriginal)
                    aplicarFiltro(filtroAtual)
                }
            },
            onEditClick = { tarefaSelecionada ->
                iniciarModoEdicao(tarefaSelecionada)
            },
            onDeleteClick = { tarefaAlvo ->
                AlertDialog.Builder(this)
                    .setTitle("Excluir tarefa")
                    .setMessage("Deseja realmente remover '${tarefaAlvo.nome}' permanentemente?")
                    .setPositiveButton("Excluir") { _, _ ->
                        listaTarefasOriginal.removeAll { it.id == tarefaAlvo.id }
                        storage.salvar(listaTarefasOriginal)
                        if (tarefaEmEdicao?.id == tarefaAlvo.id) {
                            cancelarEdicao()
                        }
                        aplicarFiltro(filtroAtual)
                        Toast.makeText(this, "Tarefa excluída!", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter

        // Filtros
        btnFiltroTodas.setOnClickListener { aplicarFiltro("TODAS") }
        btnFiltroPendentes.setOnClickListener { aplicarFiltro("PENDENTES") }
        btnFiltroConcluidas.setOnClickListener { aplicarFiltro("CONCLUIDAS") }

        btnCancelarEdicao.setOnClickListener {
            cancelarEdicao()
        }

        // Salvar / Atualizar
        btnSalvar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val descricao = editDescricao.text.toString().trim()

            if (nome.length < 3) {
                Toast.makeText(this, "O nome precisa ter pelo menos 3 caracteres!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dataFinal = if (dataSelecionada.isNotEmpty()) dataSelecionada else "Hoje"
            val horaFinal = if (horaSelecionada.isNotEmpty()) horaSelecionada else "Sem hora"

            if (tarefaEmEdicao != null) {
                val index = listaTarefasOriginal.indexOfFirst { it.id == tarefaEmEdicao!!.id }
                if (index != -1) {
                    val tarefaAtualizada = listaTarefasOriginal[index].copy(
                        nome = nome,
                        descricao = descricao,
                        data = dataFinal,
                        hora = horaFinal,
                        prioridade = prioridadeSelecionada
                    )
                    listaTarefasOriginal[index] = tarefaAtualizada
                    storage.salvar(listaTarefasOriginal)
                    Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show()
                }
                cancelarEdicao()
            } else {
                val novaTarefa = Tarefa(
                    id = System.currentTimeMillis(),
                    nome = nome,
                    descricao = descricao,
                    data = dataFinal,
                    hora = horaFinal,
                    prioridade = prioridadeSelecionada,
                    concluida = false
                )
                listaTarefasOriginal.add(0, novaTarefa)
                storage.salvar(listaTarefasOriginal)
                limparFormulario()
                Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show()
            }

            aplicarFiltro(filtroAtual)
        }

        aplicarFiltro("TODAS")
    }

    private fun iniciarModoEdicao(tarefa: Tarefa) {
        tarefaEmEdicao = tarefa
        txtStatusFormulario.text = "✏️ Editando: ${tarefa.nome}"
        btnSalvar.text = "Salvar Alterações"
        btnCancelarEdicao.visibility = View.VISIBLE

        editNome.setText(tarefa.nome)
        editDescricao.setText(tarefa.descricao)
        dataSelecionada = tarefa.data
        horaSelecionada = tarefa.hora
        btnSelecionarData.text = "📅 $dataSelecionada"
        btnSelecionarHora.text = "⏰ $horaSelecionada"

        when (tarefa.prioridade) {
            "Alta" -> atualizarBotaoPrioridade("Alta", R.color.colorPriorityHigh, "🔴 Alta")
            "Baixa" -> atualizarBotaoPrioridade("Baixa", R.color.colorPriorityLow, "🟢 Baixa")
            else -> atualizarBotaoPrioridade("Média", R.color.colorPriorityMedium, "🟡 Média")
        }
    }

    private fun cancelarEdicao() {
        tarefaEmEdicao = null
        txtStatusFormulario.text = "Nova Tarefa"
        btnSalvar.text = "Adicionar Tarefa"
        btnCancelarEdicao.visibility = View.GONE
        limparFormulario()
    }

    private fun limparFormulario() {
        editNome.text.clear()
        editDescricao.text.clear()
        dataSelecionada = ""
        horaSelecionada = ""
        btnSelecionarData.text = "📅 Data"
        btnSelecionarHora.text = "⏰ Hora"
        atualizarBotaoPrioridade("Média", R.color.colorPriorityMedium, "🟡 Média")
    }

    private fun atualizarBotaoPrioridade(prioridade: String, colorResId: Int, texto: String) {
        prioridadeSelecionada = prioridade
        btnPrioridade.text = texto
        btnPrioridade.setBackgroundColor(ContextCompat.getColor(this, colorResId))
    }

    private fun aplicarFiltro(filtro: String) {
        filtroAtual = filtro

        listaTarefasExibida = when (filtro) {
            "PENDENTES" -> listaTarefasOriginal.filter { !it.concluida }.toMutableList()
            "CONCLUIDAS" -> listaTarefasOriginal.filter { it.concluida }.toMutableList()
            else -> listaTarefasOriginal.toMutableList()
        }

        adapter.atualizarLista(listaTarefasExibida)

        val pendentes = listaTarefasOriginal.count { !it.concluida }
        val concluidas = listaTarefasOriginal.count { it.concluida }

        txtContadorTarefas.text = "Pendentes: $pendentes | Concluídas: $concluidas"
        txtListaVazia.visibility = if (listaTarefasExibida.isEmpty()) View.VISIBLE else View.GONE

        val corPrimaria = ContextCompat.getColor(this, R.color.colorPrimary)
        val corSuperficie = ContextCompat.getColor(this, R.color.colorSurfaceSecondary)

        btnFiltroTodas.setBackgroundColor(if (filtro == "TODAS") corPrimaria else corSuperficie)
        btnFiltroPendentes.setBackgroundColor(if (filtro == "PENDENTES") corPrimaria else corSuperficie)
        btnFiltroConcluidas.setBackgroundColor(if (filtro == "CONCLUIDAS") corPrimaria else corSuperficie)
    }
}