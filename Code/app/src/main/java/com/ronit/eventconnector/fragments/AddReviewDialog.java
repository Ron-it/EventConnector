package com.ronit.eventconnector.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ronit.eventconnector.databinding.DialogAddReviewBinding;
import com.ronit.eventconnector.models.Review;

import java.util.UUID;

public class AddReviewDialog extends DialogFragment {
    private DialogAddReviewBinding binding;
    private String eventId;
    private Uri selectedImageUri;

    public static AddReviewDialog newInstance(String eventId) {
        AddReviewDialog dialog = new AddReviewDialog();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogAddReviewBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        setupButtons();
        setupImageUpload();
        setupAnonymousCheckbox();

        return binding.getRoot();
    }

    private void setupButtons() {
        binding.cancelButton.setOnClickListener(v -> dismiss());

        binding.submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                submitReview();
            }
        });
    }

    private void setupImageUpload() {
        binding.imageUploadCard.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        });
    }

    private void setupAnonymousCheckbox() {
        binding.anonymousCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.nameLayout.setEnabled(!isChecked);
            if (isChecked) {
                binding.nameInput.setText("");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            binding.uploadedImage.setVisibility(View.VISIBLE);
            binding.uploadedImage.setImageURI(selectedImageUri);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (!binding.anonymousCheckbox.isChecked() &&
                binding.nameInput.getText().toString().trim().isEmpty()) {
            binding.nameLayout.setError("Please enter your name or select anonymous");
            isValid = false;
        } else {
            binding.nameLayout.setError(null);
        }

        if (binding.ratingBar.getRating() == 0) {
            Toast.makeText(requireContext(), "Please add a rating", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (binding.commentInput.getText().toString().trim().isEmpty()) {
            binding.commentLayout.setError("Please add a comment");
            isValid = false;
        } else {
            binding.commentLayout.setError(null);
        }

        return isValid;
    }

    private void submitReview() {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                .getReference("reviews")
                .child(eventId);

        String reviewId = reviewsRef.push().getKey();
        if (reviewId == null) return;

        String name = binding.anonymousCheckbox.isChecked() ?
                "Anonymous" : binding.nameInput.getText().toString().trim();

        Review review = new Review(
                eventId,
                name,
                binding.commentInput.getText().toString().trim(),
                binding.ratingBar.getRating(),
                binding.anonymousCheckbox.isChecked()
        );
        review.setId(reviewId);

        if (selectedImageUri != null) {
            uploadImageAndSaveReview(review);
        } else {
            saveReview(review);
        }
    }

    private void uploadImageAndSaveReview(Review review) {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("review_images")
                .child(UUID.randomUUID().toString());

        binding.submitButton.setEnabled(false);
        binding.submitButton.setText("Uploading...");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        review.setImageUrl(uri.toString());
                        saveReview(review);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to upload image", Toast.LENGTH_SHORT).show();
                    binding.submitButton.setEnabled(true);
                    binding.submitButton.setText("Submit");
                });
    }

    private void saveReview(Review review) {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance()
                .getReference("reviews")
                .child(eventId)
                .child(review.getId());

        reviewsRef.setValue(review)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(),
                            "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to submit review", Toast.LENGTH_SHORT).show();
                    binding.submitButton.setEnabled(true);
                    binding.submitButton.setText("Submit");
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}