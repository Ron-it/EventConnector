package com.ronit.eventconnector.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ronit.eventconnector.databinding.ItemReviewBinding;
import com.ronit.eventconnector.models.Review;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        // Sort reviews by timestamp, most recent first
        Collections.sort(this.reviews, (r1, r2) ->
                Long.compare(r2.getTimestamp(), r1.getTimestamp()));
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemReviewBinding binding;

        ReviewViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Review review) {
            // Set reviewer name (or Anonymous)
            binding.reviewerName.setText(review.getReviewerName());

            // Set rating
            binding.ratingBar.setRating(review.getRating());

            // Set comment
            binding.reviewComment.setText(review.getComment());

            // Handle image
            if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
                binding.reviewImage.setVisibility(View.VISIBLE);
                // Load image using Glide
                Glide.with(binding.reviewImage.getContext())
                        .load(review.getImageUrl())
                        .centerCrop()
                        .into(binding.reviewImage);
            } else {
                binding.reviewImage.setVisibility(View.GONE);
            }

            // Add timestamp below the name
            binding.reviewTimestamp.setText(dateFormat.format(new Date(review.getTimestamp())));
        }
    }
}