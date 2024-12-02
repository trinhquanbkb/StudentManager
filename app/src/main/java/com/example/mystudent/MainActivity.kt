package com.example.mystudent

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import androidx.compose.material3.Button as ComposeButton

class MainActivity : AppCompatActivity() {
  private lateinit var btn: Button
  // Initial list of students
  val students = mutableListOf(
    StudentModel("Nguyễn Văn An", "SV001"),
    StudentModel("Trần Thị Bảo", "SV002"),
    StudentModel("Lê Hoàng Cường", "SV003"),
    StudentModel("Phạm Thị Dung", "SV004"),
    StudentModel("Đỗ Minh Đức", "SV005"),
    StudentModel("Vũ Thị Hoa", "SV006"),
    StudentModel("Hoàng Văn Hải", "SV007"),
    StudentModel("Bùi Thị Hạnh", "SV008"),
    StudentModel("Đinh Văn Hùng", "SV009"),
    StudentModel("Nguyễn Thị Linh", "SV010"),
    StudentModel("Phạm Văn Long", "SV011"),
    StudentModel("Trần Thị Mai", "SV012"),
    StudentModel("Lê Thị Ngọc", "SV013"),
    StudentModel("Vũ Văn Nam", "SV014"),
    StudentModel("Hoàng Thị Phương", "SV015"),
    StudentModel("Đỗ Văn Quân", "SV016"),
    StudentModel("Nguyễn Thị Thu", "SV017"),
    StudentModel("Trần Văn Tài", "SV018"),
    StudentModel("Phạm Thị Tuyết", "SV019"),
    StudentModel("Lê Văn Vũ", "SV020")
  )
  private var deletedStudent: StudentModel? by mutableStateOf(null)
  private var deletedPosition: Int? by mutableStateOf(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    btn = findViewById(R.id.btn_add_new)



    // Adapter for RecyclerView
    val studentAdapter = StudentAdapter(students,
      onEditClick = { student, position -> showEditDialog(student, position) },
      onDeleteClick = { student, position -> showDeleteConfirmation(student, position) })

    findViewById<RecyclerView>(R.id.recycler_view_students).run {
      adapter = studentAdapter
      layoutManager = LinearLayoutManager(this@MainActivity)
    }

    btn.setOnClickListener {
      // Inflate layout dialog_add
      val inflater = LayoutInflater.from(this)
      val dialogView = inflater.inflate(R.layout.dialog_add, null)

      // Get references to EditText fields
      val editName = dialogView.findViewById<EditText>(R.id.name)
      val editId = dialogView.findViewById<EditText>(R.id.mssv)

      // Create AlertDialog
      val builder = AlertDialog.Builder(this)
      builder.setView(dialogView)

      builder.setPositiveButton("OK") { dialog, _ ->
        // Get data from EditText
        val name = editName.text.toString().trim()
        val id = editId.text.toString().trim()

        // Validate input
        if (name.isNotEmpty() && id.isNotEmpty()) {
          // Add new student to the top of the list
          students.add(0, StudentModel(name, id))

          // Notify adapter about data change
          studentAdapter.notifyItemInserted(0)
          findViewById<RecyclerView>(R.id.recycler_view_students).scrollToPosition(0)
        }
        dialog.dismiss()
      }
      builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
      }

      // Show dialog
      val dialog = builder.create()
      dialog.show()
    }
  }

  private fun showEditDialog(student: StudentModel, position: Int) {
    val inflater = LayoutInflater.from(this)
    val dialogView = inflater.inflate(R.layout.dialog_add, null)

    val editName = dialogView.findViewById<EditText>(R.id.name)
    val editId = dialogView.findViewById<EditText>(R.id.mssv)

    // Pre-fill with current student info
    editName.setText(student.studentName)
    editId.setText(student.studentId)

    AlertDialog.Builder(this)
      .setView(dialogView)
      .setPositiveButton("Update") { dialog, _ ->
        val newName = editName.text.toString().trim()
        val newId = editId.text.toString().trim()

        if (newName.isNotEmpty() && newId.isNotEmpty()) {
          // Update student info
          students[position] = StudentModel(newName, newId)
          findViewById<RecyclerView>(R.id.recycler_view_students).adapter?.notifyItemChanged(position)
        } else {
          Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
        }
        dialog.dismiss()
      }
      .setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
  }

  // Show delete confirmation dialog
  private fun showDeleteConfirmation(student: StudentModel, position: Int) {
    AlertDialog.Builder(this)
      .setMessage("Are you sure you want to delete ${student.studentName}?")
      .setPositiveButton("Yes") { dialog, _ ->
        // Lưu lại thông tin sinh viên đã bị xóa
        deletedStudent = students[position]
        deletedPosition = position

        // Xóa sinh viên khỏi danh sách
        val removedStudent =students.removeAt(position)
        findViewById<RecyclerView>(R.id.recycler_view_students).adapter?.notifyItemRemoved(position)

//        // Hiển thị Snackbar
        Snackbar.make(
          findViewById(android.R.id.content),
          "Deleted ${removedStudent.studentName}",
          Snackbar.LENGTH_LONG
        ).setAction("Undo") {
          // Restore the removed student
          students.add(position, removedStudent)
          findViewById<RecyclerView>(R.id.recycler_view_students).adapter?.notifyItemInserted(position)
        }.show()

        dialog.dismiss()
      }
      .setNegativeButton("No") { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
  }

  // Show Compose Snackbar
  private fun showComposeSnackbar(message: String) {
    setContent {
      SnackbarWithMessage(message)
    }
  }

  // A Composable function to display the Snackbar
  @Composable
  fun SnackbarWithMessage(message: String) {
    var snackbarVisible by remember { mutableStateOf(true) }

    // Timer to hide the Snackbar after 5 seconds
    LaunchedEffect(Unit) {
      delay(5000) // 5 seconds
      snackbarVisible = false
    }

    if (snackbarVisible) {
      Snackbar(
        action = {
          ComposeButton(onClick = {
            // Restore the deleted student if "Undo" is clicked
            deletedStudent?.let {
              students.add(deletedPosition ?: 0, it)
              findViewById<RecyclerView>(R.id.recycler_view_students).adapter?.notifyItemInserted(deletedPosition ?: 0)
            }
          }) {
            Text("Undo")
          }
        }
      ) {
        Text(text = message)
      }
    }
  }
}
