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
import androidx.cardview.widget.CardView;
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
import com.ramyhelow.captone_mydailyplanner.activities.CompletedCategoryActivity;
import com.ramyhelow.captone_mydailyplanner.activities.NormalCategoryActivity;
import com.ramyhelow.captone_mydailyplanner.activities.ProjectDetailActivity;
import com.ramyhelow.captone_mydailyplanner.model.Project;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ramyhelow.captone_mydailyplanner.utils.Utils.getDatabase;
import static com.ramyhelow.captone_mydailyplanner.utils.Utils.getUserId;

public class TasksFragment extends Fragment {

    public TasksFragment() {}

    @BindView(R.id.rv_projects)
    RecyclerView recyclerView;
    @BindView(R.id.card_today)
    CardView todayCard;
    @BindView(R.id.card_tomorrow)
    CardView tomorrowCard;
    @BindView(R.id.card_other_time)
    CardView otherTimeCard;
    @BindView(R.id.card_completed_tasks)
    CardView completedTasksCard;
    @BindView(R.id.loading_indicator)
    ProgressBar loadingIndicator;

    private LinearLayoutManager llm;
    private FirebaseRecyclerAdapter<Project, ProjectHolder> mAdapter;
    private DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();

    public static Fragment getInstance() {
        return new TasksFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.tasks_fragment, container, false);
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
                .child("projects");

        FirebaseRecyclerOptions<Project> options =
                new FirebaseRecyclerOptions.Builder<Project>()
                        .setQuery(query, Project.class)
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Project, ProjectHolder>(options) {
            @NonNull
            @Override
            final public ProjectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_list_item, parent, false);
                return new ProjectHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ProjectHolder holder, final int position, @NonNull final Project project) {
                final ProjectHolder viewHolder = holder;
                if (project.getTitle() != null) {
                    viewHolder.projectNameTv.setText(project.getTitle());
                }
                viewHolder.itemView.setOnClickListener(view1 -> {
                    final Intent intent = new Intent(getContext(), ProjectDetailActivity.class);
                    intent.putExtra("title", project.getTitle());
                    mFirebaseDatabase
                            .child("users")
                            .child(getUserId())
                            .child("projects")
                            .orderByChild("id")
                            .equalTo(project.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                String projectKey = childSnapshot.getKey();
                                intent.putExtra("projectKey", projectKey);
                                intent.putExtra("id", project.getId());
                                startActivity(intent);
                                Log.v("TasksFragment", "clicked projectKey is: " + projectKey);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                });

                viewHolder.deleteProjectBtn.setOnClickListener(view12 -> new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                        .title(viewHolder.projectNameTv.getText())
                        .content(getString(R.string.delete_project_confirmation))
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
        llm = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setFocusable(false);
        mAdapter.startListening();

        mFirebaseDatabase.child("users").child(getUserId()).child("projects")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        loadingIndicator.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

        todayCard.setOnClickListener(view13 -> {
            Intent intent = new Intent(getContext(), NormalCategoryActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.today));
            startActivity(intent);
        });

        tomorrowCard.setOnClickListener(view14 -> {
            Intent intent = new Intent(getContext(), NormalCategoryActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.tomorrow));
            startActivity(intent);
        });

        otherTimeCard.setOnClickListener(view15 -> {
            Intent intent = new Intent(getContext(), NormalCategoryActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.other_time));
            startActivity(intent);
        });

        completedTasksCard.setOnClickListener(view16 -> {
            Intent intent = new Intent(getContext(), CompletedCategoryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.startListening();
    }

    public static class ProjectHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.project_name)
        TextView projectNameTv;
        @BindView(R.id.delete_btn)
        Button deleteProjectBtn;

        private ProjectHolder(View itemView) {
            super(itemView);
            try {
                ButterKnife.bind(this, itemView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
