package com.example.apptarefasunificado

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TarefaAdapter(
    private var tarefas: MutableList<Tarefa>,
    private val onToggleConcluida: (Tarefa) -> Unit,
    private val onEditClick: (Tarefa) -> Unit,
    private val onDeleteClick: (Tarefa) -> Unit
) : RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder>() {

    class TarefaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkConcluida: CheckBox = itemView.findViewById(R.id.checkConcluida)
        val txtNome: TextView = itemView.findViewById(R.id.itemNome)
        val txtDescricao: TextView = itemView.findViewById(R.id.itemDescricao)
        val txtData: TextView = itemView.findViewById(R.id.itemData)
        val txtHora: TextView = itemView.findViewById(R.id.itemHora)
        val txtPrioridade: TextView = itemView.findViewById(R.id.itemPrioridade)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditarItem)
        val btnExcluir: ImageButton = itemView.findViewById(R.id.btnExcluirItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarefaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tarefa_individual, parent, false)
        return TarefaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TarefaViewHolder, position: Int) {
        val tarefa = tarefas[position]
        val context = holder.itemView.context

        holder.txtNome.text = tarefa.nome
        holder.txtDescricao.text = if (tarefa.descricao.isNotBlank()) tarefa.descricao else "Sem descrição"
        holder.txtData.text = "📅 ${tarefa.data}"
        holder.txtHora.text = "⏰ ${tarefa.hora}"
        holder.txtPrioridade.text = tarefa.prioridade.uppercase()

        // Cores de prioridade obtidas do colors.xml
        val corPrioridade = when (tarefa.prioridade) {
            "Alta" -> ContextCompat.getColor(context, R.color.colorPriorityHigh)
            "Média" -> ContextCompat.getColor(context, R.color.colorPriorityMedium)
            else -> ContextCompat.getColor(context, R.color.colorPriorityLow)
        }
        holder.txtPrioridade.setBackgroundColor(corPrioridade)

        holder.checkConcluida.setOnCheckedChangeListener(null)
        holder.checkConcluida.isChecked = tarefa.concluida

        if (tarefa.concluida) {
            holder.txtNome.paintFlags = holder.txtNome.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.txtNome.setTextColor(ContextCompat.getColor(context, R.color.colorTextCompleted))
            holder.itemView.alpha = 0.65f
        } else {
            holder.txtNome.paintFlags = holder.txtNome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.txtNome.setTextColor(ContextCompat.getColor(context, R.color.colorText))
            holder.itemView.alpha = 1.0f
        }

        holder.checkConcluida.setOnCheckedChangeListener { _, isChecked ->
            tarefa.concluida = isChecked
            onToggleConcluida(tarefa)
        }

        holder.btnEditar.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && pos < tarefas.size) {
                onEditClick(tarefas[pos])
            }
        }

        holder.btnExcluir.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && pos < tarefas.size) {
                onDeleteClick(tarefas[pos])
            }
        }
    }

    override fun getItemCount(): Int = tarefas.size

    fun atualizarLista(novaLista: List<Tarefa>) {
        this.tarefas = novaLista.toMutableList()
        notifyDataSetChanged()
    }
}