package com.ramyhelow.captone_mydailyplanner.activities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramyhelow.captone_mydailyplanner.R;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.captone_mydailyplanner.utils.Utils.getUserId;

public class NoteDetailActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_main)
    FloatingActionButton fabMain;
    @BindView(R.id.fab_project)
    FloatingActionButton fabProject;
    @BindView(R.id.fab_note)
    FloatingActionButton fabNote;
    @BindView(R.id.fab_task)
    FloatingActionButton fabTask;
    @BindView(R.id.layout_fab_project)
    LinearLayout layoutFabProject;
    @BindView(R.id.layout_fab_note)
    LinearLayout layoutFabNote;
    @BindView(R.id.layout_fab_task)
    LinearLayout layoutFabTask;
    @BindView(R.id.note_details_title)
    TextView mNoteTitleTextView;
    @BindView(R.id.note_details_content)
    TextView mNoteContentTextView;

    private Animation rotate_forward,rotate_backward;

    boolean isFABOpen=false;
    private String mNoteTitle = "title";
    private String mNoteContent = "content";
    private String mNoteKey = "noteKey";
    private String mNoteId = "id";

    private String noteTitleTvStr;
    private String noteContentTvStr;
    private String noteKey;
    private String noteId;

    ValueEventListener valueEventListener;
    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_detail_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noteTitleTvStr = dataSnapshot.child("title").getValue(String.class);
                mNoteTitleTextView.setText(noteTitleTvStr);
                noteContentTvStr = dataSnapshot.child("content").getValue(String.class);
                mNoteContentTextView.setText(noteContentTvStr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.note_details));

        Intent intent = getIntent();
        if (intent.hasExtra(mNoteTitle)) {
            noteTitleTvStr = intent.getStringExtra(mNoteTitle);
            mNoteTitleTextView.setText(noteTitleTvStr);
        }

        if (intent.hasExtra(mNoteContent)) {
            noteContentTvStr = intent.getStringExtra(mNoteContent);
            mNoteContentTextView.setText(noteContentTvStr);
        }

        if (intent.hasExtra(mNoteKey)){
            noteKey = intent.getStringExtra(mNoteKey);
        }

        if (intent.hasExtra(mNoteId)){
            noteId = intent.getStringExtra(mNoteId);
        }

        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        fabMain.setOnClickListener(view -> {
            if(!isFABOpen){
                showFABMenu();
                fabMain.startAnimation(rotate_forward);
            }else{
                closeFABMenu();
                fabMain.startAnimation(rotate_backward);
            }
        });

        fabTask.setOnClickListener(view -> {
            fabMain.startAnimation(rotate_backward);
            closeFABMenu();
            startActivity(new Intent(getApplicationContext(), AddTaskActivity.class));
        });

        fabNote.setOnClickListener(view -> {
            fabMain.startAnimation(rotate_backward);
            closeFABMenu();
            startActivity(new Intent(getApplicationContext(), AddNoteActivity.class));
        });

        fabProject.setOnClickListener(view -> {
            fabMain.startAnimation(rotate_backward);
            closeFABMenu();
            startActivity(new Intent(getApplicationContext(), AddProjectActivity.class));
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        mFirebaseDatabase
                .child("users")
                .child(getUserId())
                .child("notes")
                .child(noteKey).addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop(){
        super.onStop();
        mFirebaseDatabase.removeEventListener(valueEventListener);
    }


    private void showFABMenu(){
        isFABOpen=true;
        layoutFabProject.setVisibility(View.VISIBLE);
        layoutFabNote.setVisibility(View.VISIBLE);
        layoutFabTask.setVisibility(View.VISIBLE);
        layoutFabProject.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        layoutFabNote.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
        layoutFabTask.animate().translationY(-getResources().getDimension(R.dimen.standard_145));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        layoutFabProject.animate().translationY(0);
        layoutFabNote.animate().translationY(0);
        layoutFabTask.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFABOpen) {
                    layoutFabProject.setVisibility(View.GONE);
                    layoutFabNote.setVisibility(View.GONE);
                    layoutFabTask.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.edit_option) {
            Intent intent = new Intent(getApplicationContext(), AddNoteActivity.class);
            intent.putExtra("title",mNoteTitleTextView.getText());
            intent.putExtra("content", mNoteContentTextView.getText());
            intent.putExtra("id", noteId);
            intent.putExtra("key", noteKey);
            intent.putExtra("edit", "1");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
