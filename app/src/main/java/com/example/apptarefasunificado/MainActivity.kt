package com.example.apptarefasunificado

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apptarefasunificado.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storage: TarefaStorage
    private lateinit var adapter: TarefaAdapter

    private var listaTarefasOriginal: MutableList<Tarefa> = mutableListOf()
    private var dataSelecionada: String = ""
    private var horaSelecionada: String = ""
    private var prioridadeSelecionada: String = "Média"
    private var filtroAtual: String = "TODAS"
    private var tarefaEmEdicao: Tarefa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = TarefaStorage(this)
        listaTarefasOriginal = storage.carregar()

        configurarRecyclerView()
        configurarBotoes()
        aplicarFiltro("TODAS")
    }

    private fun configurarRecyclerView() {
        binding.recyclerViewTarefas.layoutManager = LinearLayoutManager(this)

        adapter = TarefaAdapter(
            onToggleConcluida = { tarefaModificada ->
                val index = listaTarefasOriginal.indexOfFirst { it.id == tarefaModificada.id }
                if (index != -1) {
                    listaTarefasOriginal[index] = tarefaModificada
                    storage.salvar(listaTarefasOriginal)
                    aplicarFiltro(filtroAtual)
                }
            },
            onEditClick = { tarefa ->
                iniciarModoEdicao(tarefa)
            },
            onDeleteClick = { tarefaAlvo ->
                AlertDialog.Builder(this)
                    .setTitle("Excluir Tarefa")
                    .setMessage("Deseja realmente remover \"${tarefaAlvo.nome}\"?")
                    .setPositiveButton("Excluir") { dialog, _ ->
                        listaTarefasOriginal.removeAll { it.id == tarefaAlvo.id }
                        storage.salvar(listaTarefasOriginal)
                        if (tarefaEmEdicao?.id == tarefaAlvo.id) {
                            cancelarEdicao()
                        }
                        aplicarFiltro(filtroAtual)
                        Toast.makeText(this, "Tarefa excluída!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        binding.recyclerViewTarefas.adapter = adapter
    }

    private fun configurarBotoes() {
        // Prioridade
        binding.btnPrioridade.setOnClickListener {
            when (prioridadeSelecionada) {
                "Média" -> atualizarBotaoPrioridade("Alta", R.color.colorPriorityHigh, "🔴 Alta")
                "Alta" -> atualizarBotaoPrioridade("Baixa", R.color.colorPriorityLow, "🟢 Baixa")
                else -> atualizarBotaoPrioridade("Média", R.color.colorPriorityMedium, "🟡 Média")
            }
        }

        // Data
        binding.btnSelecionarData.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, ano, mes, dia ->
                dataSelecionada = String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, ano)
                binding.btnSelecionarData.text = "📅 $dataSelecionada"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Hora
        binding.btnSelecionarHora.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hora, min ->
                horaSelecionada = String.format(Locale.getDefault(), "%02d:%02d", hora, min)
                binding.btnSelecionarHora.text = "⏰ $horaSelecionada"
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // Filtros
        binding.btnFiltroTodas.setOnClickListener { aplicarFiltro("TODAS") }
        binding.btnFiltroPendentes.setOnClickListener { aplicarFiltro("PENDENTES") }
        binding.btnFiltroConcluidas.setOnClickListener { aplicarFiltro("CONCLUIDAS") }

        // Cancelar Edição
        binding.btnCancelarEdicao.setOnClickListener {
            cancelarEdicao()
        }

        // Salvar / Atualizar
        binding.btnSalvar.setOnClickListener {
            val nome = binding.editNome.text.toString().trim()
            val descricao = binding.editDescricao.text.toString().trim()

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
                cancelarEdicao()
                Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show()
            }

            aplicarFiltro(filtroAtual)
        }
    }

    private fun iniciarModoEdicao(tarefa: Tarefa) {
        tarefaEmEdicao = tarefa
        binding.txtStatusFormulario.text = "✏️ Editando: ${tarefa.nome}"
        binding.btnSalvar.text = "Salvar Alterações"
        binding.btnCancelarEdicao.visibility = View.VISIBLE

        binding.editNome.setText(tarefa.nome)
        binding.editDescricao.setText(tarefa.descricao)
        dataSelecionada = tarefa.data
        horaSelecionada = tarefa.hora
        binding.btnSelecionarData.text = if (dataSelecionada.isNotEmpty()) "📅 $dataSelecionada" else "📅 Data"
        binding.btnSelecionarHora.text = if (horaSelecionada.isNotEmpty()) "⏰ $horaSelecionada" else "⏰ Hora"

        when (tarefa.prioridade) {
            "Alta" -> atualizarBotaoPrioridade("Alta", R.color.colorPriorityHigh, "🔴 Alta")
            "Baixa" -> atualizarBotaoPrioridade("Baixa", R.color.colorPriorityLow, "🟢 Baixa")
            else -> atualizarBotaoPrioridade("Média", R.color.colorPriorityMedium, "🟡 Média")
        }

        binding.editNome.requestFocus()
        binding.editNome.setSelection(binding.editNome.text.length)
    }

    private fun cancelarEdicao() {
        tarefaEmEdicao = null
        binding.txtStatusFormulario.text = "Nova Tarefa"
        binding.btnSalvar.text = "Adicionar Tarefa"
        binding.btnCancelarEdicao.visibility = View.GONE
        limparFormulario()

        binding.editNome.clearFocus()
        binding.editDescricao.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun limparFormulario() {
        binding.editNome.text.clear()
        binding.editDescricao.text.clear()
        dataSelecionada = ""
        horaSelecionada = ""
        binding.btnSelecionarData.text = "📅 Data"
        binding.btnSelecionarHora.text = "⏰ Hora"
        atualizarBotaoPrioridade("Média", R.color.colorPriorityMedium, "🟡 Média")
    }

    private fun atualizarBotaoPrioridade(prioridade: String, colorResId: Int, texto: String) {
        prioridadeSelecionada = prioridade
        binding.btnPrioridade.text = texto
        binding.btnPrioridade.setBackgroundColor(ContextCompat.getColor(this, colorResId))
    }

    private fun aplicarFiltro(filtro: String) {
        filtroAtual = filtro

        val listaFiltrada = when (filtro) {
            "PENDENTES" -> listaTarefasOriginal.filter { !it.concluida }
            "CONCLUIDAS" -> listaTarefasOriginal.filter { it.concluida }
            else -> listaTarefasOriginal
        }

        // ListAdapter compara as duas listas em segundo plano e renderiza apenas a diferença
        adapter.submitList(listaFiltrada.toList())

        val pendentes = listaTarefasOriginal.count { !it.concluida }
        val concluidas = listaTarefasOriginal.count { it.concluida }

        binding.txtContadorTarefas.text = "Pendentes: $pendentes | Concluídas: $concluidas"
        binding.txtListaVazia.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE

        val corPrimaria = ContextCompat.getColor(this, R.color.colorPrimary)
        val corSuperficie = ContextCompat.getColor(this, R.color.colorSurfaceSecondary)

        binding.btnFiltroTodas.setBackgroundColor(if (filtro == "TODAS") corPrimaria else corSuperficie)
        binding.btnFiltroPendentes.setBackgroundColor(if (filtro == "PENDENTES") corPrimaria else corSuperficie)
        binding.btnFiltroConcluidas.setBackgroundColor(if (filtro == "CONCLUIDAS") corPrimaria else corSuperficie)
    }
}