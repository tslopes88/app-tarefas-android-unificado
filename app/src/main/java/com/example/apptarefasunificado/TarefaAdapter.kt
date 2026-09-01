package com.example.apptarefasunificado

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apptarefasunificado.databinding.TarefaIndividualBinding

class TarefaAdapter(
    private val onToggleConcluida: (Tarefa) -> Unit,
    private val onEditClick: (Tarefa) -> Unit,
    private val onDeleteClick: (Tarefa) -> Unit
) : ListAdapter<Tarefa, TarefaAdapter.TarefaViewHolder>(TarefaDiffCallback()) {

    class TarefaViewHolder(val binding: TarefaIndividualBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val binding = TarefaIndividualBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TarefaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        val tarefa = getItem(position)
        val context = holder.itemView.context
        val b = holder.binding

        b.tvNome.text = tarefa.nome
        b.tvDescricao.text = tarefa.descricao

        val dataHoraFormatada = when {
            tarefa.data.isNotEmpty() && tarefa.hora.isNotEmpty() -> "${tarefa.data} · ${tarefa.hora}"
            tarefa.data.isNotEmpty() -> tarefa.data
            tarefa.hora.isNotEmpty() -> tarefa.hora
            else -> ""
        }
        b.tvDataHora.text = dataHoraFormatada
        b.tvDataHora.visibility = if (dataHoraFormatada.isNotEmpty()) View.VISIBLE else View.GONE
        b.tvDescricao.visibility = if (tarefa.descricao.isNotEmpty()) View.VISIBLE else View.GONE

        // Efeito de riscado e cor de conclusão
        val flags = if (tarefa.concluida) {
            Paint.STRIKE_THRU_TEXT_FLAG or b.tvNome.paintFlags
        } else {
            b.tvNome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        b.tvNome.paintFlags = flags

        val corTexto = if (tarefa.concluida) {
            ContextCompat.getColor(context, R.color.colorTextCompleted)
        } else {
            ContextCompat.getColor(context, R.color.colorText)
        }
        b.tvNome.setTextColor(corTexto)

        // Limpa o listener antes de setar o estado para evitar disparos em reciclagem de View
        b.cbConcluida.setOnCheckedChangeListener(null)
        b.cbConcluida.isChecked = tarefa.concluida
        b.cbConcluida.setOnCheckedChangeListener { _, isChecked ->
            onToggleConcluida(tarefa.copy(concluida = isChecked))
        }

        // Faixa de Prioridade
        b.viewPrioridade.setBackgroundColor(
            when (tarefa.prioridade) {
                "Alta" -> ContextCompat.getColor(context, R.color.colorPriorityHigh)
                "Baixa" -> ContextCompat.getColor(context, R.color.colorPriorityLow)
                else -> ContextCompat.getColor(context, R.color.colorPriorityMedium)
            }
        )

        b.root.setOnClickListener {
            onEditClick(tarefa)
        }

        b.root.setOnLongClickListener {
            onDeleteClick(tarefa)
            true
        }
    }

    class TarefaDiffCallback : DiffUtil.ItemCallback<Tarefa>() {
        override fun areItemsTheSame(oldItem: Tarefa, newItem: Tarefa): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tarefa, newItem: Tarefa): Boolean = oldItem == newItem
    }
}