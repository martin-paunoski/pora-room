package com.example.roomdemo

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdemo.data.Task
import com.example.roomdemo.databinding.ActivityMainBinding
import com.example.roomdemo.ui2.TaskAdapter
import com.example.roomdemo.ui2.TaskViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Modern ViewModel delegation
    private val viewModel: TaskViewModel by viewModels()

    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setupToolbar()
        setupWindowInsets()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayShowTitleEnabled(true)
        }
        // Alternative: directly set on toolbar
        binding.toolbar.title = getString(R.string.app_name)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.root.updatePadding(
                left = systemBars.left,
                right = systemBars.right
            )

            binding.fabAdd.updatePadding(
                bottom = systemBars.bottom
            )

            insets
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onItemClick = ::showTaskDetails,
            onCheckClick = viewModel::toggleComplete,
            onDeleteClick = ::confirmDelete
        )

        binding.recyclerView.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupObservers() {
        // Use modern coroutine-based observation
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe tasks
                launch {
                    viewModel.allTasks.collect { tasks ->
                        adapter.submitList(tasks)
                        updateEmptyViewVisibility(tasks.isEmpty())
                    }
                }

                // Observe error messages
                launch {
                    viewModel.errorMessage.collect { message ->
                        message?.let {
                            showErrorSnackbar(it)
                            viewModel.clearError()
                        }
                    }
                }

                // Observe success messages
                launch {
                    viewModel.successMessage.collect { message ->
                        message?.let {
                            showSuccessSnackbar(it)
                            viewModel.clearSuccess()
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            showAddTaskDialog()
        }

        binding.btnDeleteCompleted.setOnClickListener {
            confirmDeleteCompleted()
        }
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAnchorView(binding.fabAdd)
            .show()
    }

    private fun showSuccessSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.fabAdd)
            .show()
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.etTitle)
        val descInput = dialogView.findViewById<TextInputEditText>(R.id.etDescription)

        MaterialAlertDialogBuilder(this)
            .setTitle("Novo opravilo")
            .setView(dialogView)
            .setPositiveButton("Dodaj") { _, _ ->
                val title = titleInput.text.toString().trim()
                val desc = descInput.text.toString().trim()

                if (title.isNotBlank()) {
                    val task = Task(
                        title = title,
                        description = desc.ifBlank { null },
                        priority = 0
                    )
                    viewModel.insert(task)
                } else {
                    showErrorSnackbar("Naslov ne sme biti prazen")
                }
            }
            .setNegativeButton("Prekliči", null)
            .show()
    }

    private fun showTaskDetails(task: Task) {
        val priority = when (task.priority) {
            0 -> "Nizka"
            1 -> "Srednja"
            2 -> "Visoka"
            else -> "Neznana"
        }

        val message = buildString {
            appendLine("Naslov: ${task.title}")
            appendLine()
            appendLine("Opis: ${task.description ?: "Ni opisa"}")
            appendLine()
            appendLine("Prioriteta: $priority")
            appendLine()
            append("Status: ${if (task.isCompleted) "Dokončano" else "Aktivno"}")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Podrobnosti opravila")
            .setMessage(message)
            .setPositiveButton("V redu", null)
            .show()
    }

    private fun confirmDelete(task: Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Brisanje opravila")
            .setMessage("Ali ste prepričani, da želite izbrisati '${task.title}'?")
            .setPositiveButton("Izbriši") { _, _ ->
                viewModel.delete(task)
            }
            .setNegativeButton("Prekliči", null)
            .show()
    }

    private fun confirmDeleteCompleted() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Brisanje dokončanih")
            .setMessage("Ali želite izbrisati vsa dokončana opravila?")
            .setPositiveButton("Izbriši") { _, _ ->
                viewModel.deleteCompleted()
            }
            .setNegativeButton("Prekliči", null)
            .show()
    }
}