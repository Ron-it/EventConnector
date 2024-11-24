package com.ronit.eventconnector.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ronit.eventconnector.R;
import com.ronit.eventconnector.databinding.FragmentViewEventBinding;
import com.ronit.eventconnector.models.Event;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewEventFragment extends Fragment {
    private FragmentViewEventBinding binding;
    private Event event;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, h:mm a", Locale.getDefault());

    public static ViewEventFragment newInstance(Event event, double distance) {
        ViewEventFragment fragment = new ViewEventFragment();
        Bundle args = new Bundle();
        args.putString("id", event.getId());  // Add this line
        args.putString("title", event.getTitle());
        args.putString("description", event.getDescription());
        args.putString("location", event.getLocation());
        args.putLong("dateTime", event.getDateTime());
        args.putString("eventType", event.getEventType());
        args.putDouble("latitude", event.getLatitude());
        args.putDouble("longitude", event.getLongitude());
        args.putDouble("distance", distance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewEventBinding.inflate(inflater, container, false);

        loadEventData();
        setupButtons();

        return binding.getRoot();
    }

    private void loadEventData() {
        if (getArguments() != null) {
            // Initialize the event object
            event = new Event();
            event.setId(getArguments().getString("id")); // Add this to newInstance args
            event.setTitle(getArguments().getString("title"));
            event.setDescription(getArguments().getString("description"));
            event.setLocation(getArguments().getString("location"));
            event.setDateTime(getArguments().getLong("dateTime"));
            event.setEventType(getArguments().getString("eventType"));
            event.setLatitude(getArguments().getDouble("latitude"));
            event.setLongitude(getArguments().getDouble("longitude"));

            // Update UI
            binding.eventTitle.setText(event.getTitle());
            binding.eventLocation.setText(event.getLocation());
            binding.eventDateTime.setText(dateFormat.format(new Date(event.getDateTime())));
            binding.eventDistance.setText(String.format("%s km away",
                    getArguments().getDouble("distance", 2.5)));
        }
    }

    private void setupButtons() {
        binding.backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        binding.rsvpButton.setOnClickListener(v -> {
            incrementAttendance();
            binding.successMessage.setVisibility(View.VISIBLE);
            binding.rsvpButton.setEnabled(false);
        });

        binding.directionsButton.setOnClickListener(v -> {
            if (getArguments() != null) {
                double latitude = getArguments().getDouble("latitude");
                double longitude = getArguments().getDouble("longitude");
                String location = getArguments().getString("location", "");

                // Create a URI for Google Maps directions
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude + "&mode=d");

                // Create an Intent to open Google Maps
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Check if Google Maps is installed
                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // If Google Maps isn't installed, open in browser
                    Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                            latitude + "," + longitude);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                    startActivity(browserIntent);
                }
            }
        });

        binding.viewReviewsButton.setOnClickListener(v -> {
            ReviewsFragment fragment = ReviewsFragment.newInstance(event.getId());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void incrementAttendance() {
        if (event == null || event.getId() == null) {
            Toast.makeText(requireContext(), "Error: Event data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference eventRef = FirebaseDatabase.getInstance()
                .getReference("events")
                .child(event.getId());

        // Get the current attendance
        eventRef.child("currentAttendance").get().addOnSuccessListener(snapshot -> {
            Integer currentAttendance = snapshot.getValue(Integer.class);
            if (currentAttendance == null) currentAttendance = 0;

            int newAttendance = currentAttendance + 1;

            // Update the attendance
            eventRef.child("currentAttendance").setValue(newAttendance)
                    .addOnSuccessListener(aVoid -> {
                        // Disable the RSVP button
                        binding.rsvpButton.setEnabled(false);
                        binding.rsvpButton.setText("RSVP Confirmed");

                        // Show success message
                        binding.successMessage.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Failed to RSVP", Toast.LENGTH_SHORT).show());
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}