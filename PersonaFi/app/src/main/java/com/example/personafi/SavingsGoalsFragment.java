package com.example.personafi;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SavingsGoalsFragment extends Fragment implements SavingsGoalAdapter.OnProgressUpdatedListener {

    private RecyclerView recyclerViewGoals;
    private View emptyStateContainer;
    private SavingsGoalAdapter savingsGoalAdapter;
    private List<SavingsGoal> savingsGoalList = new ArrayList<>();
    private OnGoalInteractionListener mListener;
    private AnimatedShapesView animatedShapesView;
    private boolean isViewCreated = false;

    public interface OnGoalInteractionListener {
        void onGoalAddedOrProgressUpdated();
        List<SavingsGoal> getSavingsGoalList();
        void setSavingsGoalList(List<SavingsGoal> goals);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnGoalInteractionListener) {
            mListener = (OnGoalInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnGoalInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_savings_goals, container, false);
        isViewCreated = true;

        animatedShapesView = view.findViewById(R.id.animatedShapesView);
        animatedShapesView.configureSavingsTheme();

        recyclerViewGoals = view.findViewById(R.id.recyclerViewGoalsFragment);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        setupRecyclerView();
        loadGoals();

        return view;
    }

    private void setupRecyclerView() {
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGoals.setHasFixedSize(true);
        recyclerViewGoals.setItemAnimator(null); // Disable animations for better performance
    }

    private void loadGoals() {
        if (mListener != null) {
            savingsGoalList = mListener.getSavingsGoalList();
            if (savingsGoalList == null) {
                savingsGoalList = new ArrayList<>();
                mListener.setSavingsGoalList(savingsGoalList);
            }
            updateAdapter();
        }
    }

    private void updateAdapter() {
        if (!isViewCreated) return;
        
        if (savingsGoalAdapter == null) {
            savingsGoalAdapter = new SavingsGoalAdapter(savingsGoalList, getContext(), this);
            recyclerViewGoals.setAdapter(savingsGoalAdapter);
        } else {
            savingsGoalAdapter.updateGoals(savingsGoalList);
        }
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (!isViewCreated) return;
        
        if (savingsGoalList == null || savingsGoalList.isEmpty()) {
            recyclerViewGoals.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerViewGoals.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    public void showAddGoalDialog() {
        GoalDialog dialog = GoalDialog.newInstance();
        dialog.setOnGoalSetListener((name, targetAmount, position) -> {
            SavingsGoal newGoal = new SavingsGoal(name, targetAmount);
            savingsGoalList.add(newGoal);
            if (mListener != null) {
                mListener.onGoalAddedOrProgressUpdated();
            }
            updateAdapter();
        });
        dialog.show(getChildFragmentManager(), "AddGoalDialog");
    }

    @Override
    public void onProgressUpdated() {
        if (mListener != null) {
            mListener.onGoalAddedOrProgressUpdated();
        }
    }

    @Override
    public void onEditGoal(SavingsGoal goal, int position) {
        GoalDialog dialog = GoalDialog.newInstance(goal.getName(), goal.getTargetAmount(), position);
        dialog.setOnGoalSetListener((name, targetAmount, pos) -> {
            if (pos >= 0 && pos < savingsGoalList.size()) {
                SavingsGoal existingGoal = savingsGoalList.get(pos);
                existingGoal.setName(name);
                existingGoal.setTargetAmount(targetAmount);
                if (mListener != null) {
                    mListener.onGoalAddedOrProgressUpdated();
                }
                updateAdapter();
            }
        });
        dialog.show(getChildFragmentManager(), "EditGoalDialog");
    }

    @Override
    public void onDeleteGoal(int position) {
        if (position >= 0 && position < savingsGoalList.size()) {
            savingsGoalList.remove(position);
            if (mListener != null) {
                mListener.onGoalAddedOrProgressUpdated();
            }
            updateAdapter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGoals();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mListener != null) {
            mListener.setSavingsGoalList(savingsGoalList);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void refreshList() {
        if (isViewCreated) {
            loadGoals();
        }
    }
} 