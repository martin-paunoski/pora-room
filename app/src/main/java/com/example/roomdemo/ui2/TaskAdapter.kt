package com.example.roomdemo.ui2

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.roomdemo.data.Task
import com.example.roomdemo.databinding.ItemTaskBinding

/**
 * Adapter za prikaz seznama opravil
 * Uporablja DiffUtil za učinkovito posodabljanje
 */
class TaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onCheckClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.apply {
                tvTitle.text = task.title
                tvDescription.text = task.description ?: "Ni opisa"
                checkBox.isChecked = task.isCompleted

                // Vizualno označimo dokončana opravila
                if (task.isCompleted) {
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvTitle.alpha = 0.5f
                    tvDescription.alpha = 0.5f
                } else {
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvTitle.alpha = 1.0f
                    tvDescription.alpha = 0.7f
                }

                // Barvna indikacija prioritete
                val color = when (task.priority) {
                    2 -> android.graphics.Color.parseColor("#F44336") // Rdeča
                    1 -> android.graphics.Color.parseColor("#FF9800") // Oranžna
                    else -> android.graphics.Color.parseColor("#4CAF50") // Zelena
                }
                priorityIndicator.setBackgroundColor(color)

                // Click listeners
                root.setOnClickListener { onItemClick(task) }
                checkBox.setOnClickListener { onCheckClick(task) }
                btnDelete.setOnClickListener { onDeleteClick(task) }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}