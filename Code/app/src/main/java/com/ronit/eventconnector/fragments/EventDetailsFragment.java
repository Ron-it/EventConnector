package com.ronit.eventconnector.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.annotations.Nullable;
import com.ronit.eventconnector.R;
import com.ronit.eventconnector.databinding.FragmentEventDetailsBinding;
import com.ronit.eventconnector.models.Event;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EventDetailsFragment extends Fragment {
    private FragmentEventDetailsBinding binding;
    private Event eventData;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;



    private void setupImageUpload() {
        binding.imageUploadCard.setOnClickListener(v -> {
            openImagePicker();
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }


    public static EventDetailsFragment newInstance(Event event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("title", event.getTitle());
        args.putString("location", event.getLocation());
        args.putLong("dateTime", event.getDateTime());
        args.putString("eventType", event.getEventType());
        args.putDouble("latitude", event.getLatitude());   // Make sure these
        args.putDouble("longitude", event.getLongitude()); // are included

        // Debug log
        Log.d("EventDetailsFragment", "Saving coordinates to args: " +
                event.getLatitude() + ", " + event.getLongitude());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            double lat = getArguments().getDouble("latitude");
            double lng = getArguments().getDouble("longitude");

            // Debug log
            Log.d("EventDetailsFragment", "Retrieved coordinates from args: " + lat + ", " + lng);

            eventData = new Event();
            eventData.setTitle(getArguments().getString("title"));
            eventData.setLocation(getArguments().getString("location"));
            eventData.setDateTime(getArguments().getLong("dateTime"));
            eventData.setEventType(getArguments().getString("eventType"));
            eventData.setLatitude(lat);
            eventData.setLongitude(lng);
        }

        setupButtons();
        return binding.getRoot();
    }

    private void setupButtons() {
        binding.backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.nextButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveEventDetails();
                navigateToPreview();
            }
        });

        binding.imageUploadCard.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

    }

    private boolean validateInputs() {
        boolean isValid = true;

        String description = binding.descriptionInput.getText().toString().trim();
        if (description.isEmpty()) {
            binding.descriptionLayout.setError("Description is required");
            isValid = false;
        } else {
            binding.descriptionLayout.setError(null);
        }

        String cost = binding.costInput.getText().toString().trim();
        if (cost.isEmpty()) {
            binding.costLayout.setError("Cost is required");
            isValid = false;
        } else {
            binding.costLayout.setError(null);
        }

        return isValid;
    }



    private void saveEventDetails() {
        eventData.setDescription(binding.descriptionInput.getText().toString().trim());
        eventData.setCost(Double.parseDouble(binding.costInput.getText().toString().trim()));

        String capacityStr = binding.capacityInput.getText().toString().trim();
        if (!capacityStr.isEmpty()) {
            eventData.setCapacity(Integer.parseInt(capacityStr));
        }

        // Handle image upload to Firebase Storage
        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri);
        } else {
            navigateToPreview();
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("event_images/" + UUID.randomUUID().toString());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the image URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        eventData.setImageUrl(uri.toString());
                        // Show success message
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToPreview() {
        EventPreviewFragment previewFragment = EventPreviewFragment.newInstance(eventData);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, previewFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}