package com.ronit.eventconnector.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ronit.eventconnector.adapters.ReviewAdapter;
import com.ronit.eventconnector.databinding.FragmentReviewsBinding;
import com.ronit.eventconnector.models.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewsFragment extends Fragment {
    private FragmentReviewsBinding binding;
    private ReviewAdapter adapter;
    private String eventId;
    private DatabaseReference reviewsRef;
    private ValueEventListener reviewsListener;

    public static ReviewsFragment newInstance(String eventId) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReviewsBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            setupRecyclerView();
            loadReviews();
            setupButtons();
        }

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new ReviewAdapter();
        binding.reviewsRecyclerView.setAdapter(adapter);
        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadReviews() {
        reviewsRef = FirebaseDatabase.getInstance()
                .getReference("reviews")
                .child(eventId);

        reviewsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Review> reviews = new ArrayList<>();
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        reviews.add(review);
                    }
                }
                adapter.setReviews(reviews);
                updateEmptyView(reviews.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(),
                        "Error loading reviews: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        reviewsRef.addValueEventListener(reviewsListener);
    }

    private void updateEmptyView(boolean isEmpty) {
        if (binding != null) {
            binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.reviewsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void setupButtons() {
        binding.backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        binding.addReviewFab.setOnClickListener(v ->
                showAddReviewDialog());
    }

    private void showAddReviewDialog() {
        AddReviewDialog dialog = AddReviewDialog.newInstance(eventId);
        dialog.show(getChildFragmentManager(), "add_review");
    }

    @Override
    public void onDestroyView() {
        if (reviewsRef != null && reviewsListener != null) {
            reviewsRef.removeEventListener(reviewsListener);
        }
        super.onDestroyView();
        binding = null;
    }
}