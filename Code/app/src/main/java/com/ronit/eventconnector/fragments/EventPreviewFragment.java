package com.ronit.eventconnector.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ronit.eventconnector.R;
import com.ronit.eventconnector.adapters.EventAdapter;
import com.ronit.eventconnector.databinding.FragmentEventPreviewBinding;
import com.ronit.eventconnector.models.Event;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventPreviewFragment extends Fragment {
    private FragmentEventPreviewBinding binding;
    private Event event;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, h:mm a", Locale.getDefault());

    public static EventPreviewFragment newInstance(Event event) {
        EventPreviewFragment fragment = new EventPreviewFragment();
        Bundle args = new Bundle();
        // Add all event data to arguments
        args.putString("title", event.getTitle());
        args.putString("description", event.getDescription());
        args.putString("location", event.getLocation());
        args.putLong("dateTime", event.getDateTime());
        args.putString("eventType", event.getEventType());
        args.putDouble("cost", event.getCost());
        args.putInt("capacity", event.getCapacity());
        args.putDouble("latitude", event.getLatitude());   // Add these
        args.putDouble("longitude", event.getLongitude()); // two lines

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventPreviewBinding.inflate(inflater, container, false);

        loadEventData();
        setupButtons();
        return binding.getRoot();
    }

    private void loadEventData() {
        if (getArguments() != null) {
            event = new Event();
            event.setTitle(getArguments().getString("title"));
            event.setDescription(getArguments().getString("description"));
            event.setLocation(getArguments().getString("location"));
            event.setDateTime(getArguments().getLong("dateTime"));
            event.setEventType(getArguments().getString("eventType"));
            event.setCost(getArguments().getDouble("cost"));
            event.setCapacity(getArguments().getInt("capacity"));
            event.setLatitude(getArguments().getDouble("latitude"));   // Add these
            event.setLongitude(getArguments().getDouble("longitude")); // two lines

            // Add debug logging
            Log.d("EventPreviewFragment", "Loaded coordinates: " +
                    event.getLatitude() + ", " + event.getLongitude());

            // Update UI with event data
            binding.eventTitlePreview.setText(event.getTitle());
            binding.eventDateTimePreview.setText(dateFormat.format(new Date(event.getDateTime())));
            binding.eventLocationPreview.setText(event.getLocation());
            binding.eventDescriptionPreview.setText(event.getDescription());
        }
    }

    private void setupButtons() {
        binding.editButton.setOnClickListener(v -> {
            // Go back to edit the event
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.publishButton.setOnClickListener(v -> {
            publishEvent();
        });
    }

    private void publishEvent() {
        try {
            event.setLocallyCreated(true);
            event.setCurrentAttendance(0);  // Initialize attendance

            // Debug log
            Log.d("EventPreviewFragment", "Publishing event: " + event.getTitle()
                    + " with coordinates: " + event.getLatitude() + "," + event.getLongitude());

            DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
            String eventId = eventsRef.push().getKey();

            if (eventId != null) {
                event.setId(eventId);
                eventsRef.child(eventId).setValue(event)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("EventPreviewFragment", "Event published successfully");
                            Toast.makeText(requireContext(), "Event published successfully!",
                                    Toast.LENGTH_SHORT).show();

                            // Navigate back to discover fragment
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, new DiscoverFragment())
                                    .commit();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("EventPreviewFragment", "Failed to publish event: " + e.getMessage());
                            Toast.makeText(requireContext(),
                                    "Failed to publish event: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            Log.e("EventPreviewFragment", "Error in publishEvent: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(),
                    "Error publishing event: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}