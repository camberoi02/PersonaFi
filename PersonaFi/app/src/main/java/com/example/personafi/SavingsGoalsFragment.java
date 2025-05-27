package com.example.personafi;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
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
    private boolean isDialogShowing = false;
    private static final String TAG_ADD_DIALOG = "AddGoalDialog";
    private static final String TAG_EDIT_DIALOG = "EditGoalDialog";

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

        TextInputEditText searchEditText = view.findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (savingsGoalAdapter != null) {
                    savingsGoalAdapter.filterGoals(s.toString());
                    updateEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupRecyclerView();
        loadGoals();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isViewCreated) {
            setupRecyclerView();
            loadGoals();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isViewCreated && recyclerViewGoals != null) {
            loadGoals();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissAllDialogs();
        
        if (mListener != null && savingsGoalList != null) {
            mListener.setSavingsGoalList(savingsGoalList);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissAllDialogs();
        isViewCreated = false;
        recyclerViewGoals = null;
        emptyStateContainer = null;
        savingsGoalAdapter = null;
    }

    private void setupRecyclerView() {
        if (recyclerViewGoals == null || !isViewCreated) return;
        
        try {
            recyclerViewGoals.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewGoals.setHasFixedSize(true);
            
            if (savingsGoalAdapter == null) {
                savingsGoalAdapter = new SavingsGoalAdapter(savingsGoalList, getContext(), this);
                recyclerViewGoals.setAdapter(savingsGoalAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGoals() {
        if (!isViewCreated || mListener == null) return;
        
        try {
            savingsGoalList = mListener.getSavingsGoalList();
            if (savingsGoalList == null) {
                savingsGoalList = new ArrayList<>();
                mListener.setSavingsGoalList(savingsGoalList);
            }
            
            if (savingsGoalAdapter != null) {
                savingsGoalAdapter.updateGoals(savingsGoalList);
            } else {
                setupRecyclerView();
            }
            
            updateEmptyState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEmptyState() {
        if (!isViewCreated || recyclerViewGoals == null || emptyStateContainer == null) return;
        
        if (savingsGoalList == null || savingsGoalList.isEmpty()) {
            recyclerViewGoals.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerViewGoals.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private void updateAdapter() {
        if (!isViewCreated || recyclerViewGoals == null) return;
        
        try {
            if (savingsGoalAdapter == null) {
                setupRecyclerView();
            } else {
                savingsGoalAdapter.updateGoals(savingsGoalList);
            }
            
            updateEmptyState();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissAllDialogs() {
        if (!isAdded()) return;
        
        try {
            Fragment addDialog = getParentFragmentManager().findFragmentByTag(TAG_ADD_DIALOG);
            if (addDialog != null) {
                ((DialogFragment) addDialog).dismissAllowingStateLoss();
            }
            
            Fragment editDialog = getParentFragmentManager().findFragmentByTag(TAG_EDIT_DIALOG);
            if (editDialog != null) {
                ((DialogFragment) editDialog).dismissAllowingStateLoss();
            }
            
            isDialogShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAddGoalDialog() {
        if (!isAdded() || !isViewCreated) return;
        
        try {
            dismissAllDialogs();
            
            GoalDialog dialog = GoalDialog.newInstance();
            dialog.setOnGoalSetListener((name, targetAmount, position) -> {
                if (!isAdded() || !isViewCreated) return;
                
                SavingsGoal newGoal = new SavingsGoal(name, targetAmount);
                if (savingsGoalList == null) {
                    savingsGoalList = new ArrayList<>();
                }
                savingsGoalList.add(newGoal);
                
                if (mListener != null) {
                    mListener.onGoalAddedOrProgressUpdated();
                    updateAchievements();
                }
                
                if (isViewCreated && recyclerViewGoals != null) {
                    updateAdapter();
                }
                
                isDialogShowing = false;
            });
            
            isDialogShowing = true;
            dialog.show(getParentFragmentManager(), TAG_ADD_DIALOG);
        } catch (Exception e) {
            e.printStackTrace();
            isDialogShowing = false;
        }
    }

    @Override
    public void onProgressUpdated() {
        if (mListener != null) {
            mListener.onGoalAddedOrProgressUpdated();
            updateAchievements();
        }
    }

    @Override
    public void onEditGoal(SavingsGoal goal, int position) {
        if (!isAdded() || !isViewCreated) return;
        
        try {
            dismissAllDialogs();
            
            GoalDialog dialog = GoalDialog.newInstance(goal.getName(), goal.getTargetAmount(), position);
            dialog.setOnGoalSetListener((name, targetAmount, pos) -> {
                if (!isAdded() || !isViewCreated) return;
                
                if (savingsGoalList != null && pos >= 0 && pos < savingsGoalList.size()) {
                    SavingsGoal existingGoal = savingsGoalList.get(pos);
                    existingGoal.setName(name);
                    existingGoal.setTargetAmount(targetAmount);
                    
                    if (mListener != null) {
                        mListener.onGoalAddedOrProgressUpdated();
                        updateAchievements();
                    }
                    
                    if (isViewCreated && recyclerViewGoals != null) {
                        updateAdapter();
                    }
                }
                
                isDialogShowing = false;
            });
            
            isDialogShowing = true;
            dialog.show(getParentFragmentManager(), TAG_EDIT_DIALOG);
        } catch (Exception e) {
            e.printStackTrace();
            isDialogShowing = false;
        }
    }

    @Override
    public void onDeleteGoal(int position) {
        if (!isAdded() || !isViewCreated) return;
        
        if (savingsGoalList != null && position >= 0 && position < savingsGoalList.size()) {
            savingsGoalList.remove(position);
            
            if (mListener != null) {
                mListener.onGoalAddedOrProgressUpdated();
                updateAchievements();
            }
            
            if (isViewCreated && recyclerViewGoals != null) {
                updateAdapter();
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (isViewCreated) {
            loadGoals();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dismissAllDialogs();
        mListener = null;
    }

    public void refreshList() {
        if (isViewCreated) {
            loadGoals();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (recyclerViewGoals != null) {
                        recyclerViewGoals.invalidate();
                        recyclerViewGoals.requestLayout();
                    }
                });
            }
        }
    }

    private void updateAchievements() {
        if (!isAdded() || !isViewCreated) return;
        
        try {
            Fragment achievementsFragment = getParentFragmentManager().findFragmentByTag("achievements");
            if (achievementsFragment instanceof AchievementsFragment) {
                ((AchievementsFragment) achievementsFragment).refreshAchievements();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 