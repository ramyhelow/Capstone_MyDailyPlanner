package com.ramyhelow.captone_mydailyplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.ramyhelow.captone_mydailyplanner.R;
import com.ramyhelow.captone_mydailyplanner.model.Note;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.captone_mydailyplanner.utils.Utils.getCurrentDateAndTime;
import static com.ramyhelow.captone_mydailyplanner.utils.Utils.isEmpty;

public class AddNoteActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_save_note)
    FloatingActionButton fabSaveNote;
    @BindView(R.id.et_note_title)
    EditText noteTitleEditText;
    @BindView(R.id.et_note_content)
    EditText noteContentEditText;
    private String uId;
    private String edit = "edit";
    private int isEdit = 0;
    private String title = "title";
    private String content = "content";
    private String noteKey = "key";
    private String noteId = "id";
    private String id;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_note_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        if (intent.hasExtra(edit)){
            isEdit = 1;
            if (intent.hasExtra(title)) {
                String noteTitle = intent.getStringExtra(title);
                noteTitleEditText.setText(noteTitle);
            }
            if (intent.hasExtra(content)){
                String noteContent = intent.getStringExtra(content);
                noteContentEditText.setText(noteContent);
            }
            if (intent.hasExtra(noteId)){
                id = intent.getStringExtra(noteId);
            }
            if (intent.hasExtra(noteKey)){
                key = intent.getStringExtra(noteKey);
            }
        }
        fabSaveNote.setOnClickListener(view -> {
            if (isEmpty(noteTitleEditText) || isEmpty(noteContentEditText)) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.no_note_data, Toast.LENGTH_LONG);
                toast.show();
            } else {
                saveNote();
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.note_created, Toast.LENGTH_SHORT);
                toast.show();
                finish();
            }
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (isEdit == 0) {
            getSupportActionBar().setTitle(R.string.create_new_note);
        } else {
            getSupportActionBar().setTitle(R.string.edit_note);
        }
    }

    void saveNote() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (user != null) {
            uId = user.getUid();
        } else {
            Log.v("Auth", "User ID is null");
        }

        if (isEdit == 1) {
            if (key != null) {
                final Note note = new Note();
                note.setTitle(noteTitleEditText.getText().toString());
                note.setContent(noteContentEditText.getText().toString());
                note.setId(id);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put( key, note.toFirebaseObject());
                database.getReference("users").child(uId).child("notes").updateChildren(childUpdates);
            } else {
                Log.v("AddNoteActivity", "Null Database Key");
            }

        } else {
            String key = database.getReference("taskList").push().getKey();
            String noteId = getCurrentDateAndTime();
            final Note note = new Note();
            note.setTitle(noteTitleEditText.getText().toString());
            note.setContent(noteContentEditText.getText().toString());
            note.setId(noteId);
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put( key, note.toFirebaseObject());
            database.getReference("users").child(uId).child("notes").updateChildren(childUpdates);
        }
    }
}
