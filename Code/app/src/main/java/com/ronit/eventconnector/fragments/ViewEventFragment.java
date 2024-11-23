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
        args.putString("eventId", event.getId());
        args.putString("title", event.getTitle());
        args.putString("location", event.getLocation());
        args.putLong("dateTime", event.getDateTime());
        args.putString("description", event.getDescription());
        args.putDouble("latitude", event.getLatitude());
        args.putDouble("longitude", event.getLongitude());
        args.putDouble("distance", distance);  // Add this line
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
            // Load event data
            binding.eventTitle.setText(getArguments().getString("title"));
            binding.eventLocation.setText(getArguments().getString("location"));
            binding.eventDateTime.setText(dateFormat.format(new Date(getArguments().getLong("dateTime"))));

            // Set distance (you might want to calculate this based on user's location)
            binding.eventDistance.setText(String.format("%s km away",
                    getArguments().getDouble("distance", 2.5)));
        }
    }

    private void setupButtons() {
        binding.backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        binding.rsvpButton.setOnClickListener(v -> {
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
            Toast.makeText(requireContext(), "Opening reviews...", Toast.LENGTH_SHORT).show();
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}