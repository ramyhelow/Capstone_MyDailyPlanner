package com.ramyhelow.captone_mydailyplanner.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ramyhelow.captone_mydailyplanner.R;
import com.ramyhelow.captone_mydailyplanner.activities.NoteDetailActivity;
import com.ramyhelow.captone_mydailyplanner.model.Note;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.captone_mydailyplanner.utils.Utils.getDatabase;
import static com.ramyhelow.captone_mydailyplanner.utils.Utils.getUserId;

public class NotesFragment extends Fragment {

    public NotesFragment() {
    }

    @BindView(R.id.rv_notes)
    RecyclerView recyclerView;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    private FirebaseRecyclerAdapter<Note, NoteHolder> mAdapter;
    private LinearLayoutManager llm;
    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();

    public static Fragment getInstance() {
        return new NotesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.notes_fragment, container, false);
        getDatabase(getContext());
        ButterKnife.bind(this,rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Query query = mFirebaseDatabase
                .child("users")
                .child(getUserId())
                .child("notes");

        FirebaseRecyclerOptions<Note> options =
                new FirebaseRecyclerOptions.Builder<Note>()
                        .setQuery(query, Note.class)
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Note, NoteHolder>(options) {
            @NonNull
            @Override
            final public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_list_item, parent, false);
                return new NoteHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull NoteHolder holder, final int position, @NonNull final Note note) {
                final NoteHolder viewHolder = holder;
                if (note.getTitle() != null) {
                    viewHolder.noteTitleTv.setText(note.getTitle());
                }
                if (note.getContent() != null) {
                    viewHolder.noteContentTv.setText(note.getContent());
                }
                viewHolder.itemView.setOnClickListener(view1 -> {
                    Intent intent = new Intent(getContext(), NoteDetailActivity.class);
                    intent.putExtra("title", note.getTitle());
                    intent.putExtra("content", note.getContent());
                    intent.putExtra("id", note.getId());
                    intent.putExtra("noteKey", mAdapter.getRef(viewHolder.getAdapterPosition()).getKey());
                    startActivity(intent);
                });

                viewHolder.deleteNoteButton.setOnClickListener(view12 -> new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                        .title(viewHolder.noteTitleTv.getText())
                        .content(getString(R.string.delete_note_confirmation))
                        .positiveText(getString(R.string.yes))
                        .negativeText(getString(R.string.no))
                        .onPositive((dialog, which) -> {
                            mAdapter.getRef(viewHolder.getAdapterPosition()).removeValue();
                            mAdapter.notifyDataSetChanged();
                        })
                        .show());
            }
        };

        mAdapter.notifyDataSetChanged();
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        llm = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mAdapter);
        mAdapter.startListening();

        if (savedInstanceState != null) {
            final int positionIndex = savedInstanceState.getInt("rv_position_notes");
            final int topView = savedInstanceState.getInt("rv_top_view_notes");
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    try {
                        llm.scrollToPositionWithOffset(positionIndex, topView);
                    } catch (NullPointerException e) {
                        Log.v("NotesFragment", "NullPointerException");
                    }
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int positionIndexNotes;
        int topViewNotes;
        positionIndexNotes = llm.findFirstVisibleItemPosition();
        View startView = recyclerView.getChildAt(0);
        topViewNotes = (startView == null) ? 0 : (startView.getTop() - recyclerView.getPaddingTop());
        outState.putInt("rv_position_notes", positionIndexNotes);
        outState.putInt("rv_top_view_notes", topViewNotes);
    }

    public static class NoteHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.note_title)
        TextView noteTitleTv;
        @BindView(R.id.note_content)
        TextView noteContentTv;
        @BindView(R.id.delete_btn)
        Button deleteNoteButton;

        private NoteHolder(View itemView) {
            super(itemView);
            try {
                ButterKnife.bind(this, itemView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
