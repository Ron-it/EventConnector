package com.ronit.eventconnector.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.ronit.eventconnector.models.Event;
import com.ronit.eventconnector.databinding.FragmentEventManagementBinding;

public class EventManagementFragment extends Fragment {
    private FragmentEventManagementBinding binding;
    private Event event;  // Change from DatabaseReference to Event
    private ValueEventListener attendanceListener; // Add this field
    private DatabaseReference eventRef; // Add this field

    // Update newInstance to take Event instead of String
    public static EventManagementFragment newInstance(Event event) {
        EventManagementFragment fragment = new EventManagementFragment();
        Bundle args = new Bundle();
        args.putString("eventId", event.getId());
        args.putString("title", event.getTitle());
        args.putString("description", event.getDescription());
        args.putString("location", event.getLocation());
        args.putLong("dateTime", event.getDateTime());
        args.putInt("capacity", event.getCapacity());
        args.putInt("currentAttendance", event.getCurrentAttendance());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventManagementBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            // Create event from arguments
            event = new Event();
            event.setId(getArguments().getString("eventId"));
            event.setTitle(getArguments().getString("title"));
            event.setDescription(getArguments().getString("description"));
            event.setLocation(getArguments().getString("location"));
            event.setDateTime(getArguments().getLong("dateTime"));
            event.setCapacity(getArguments().getInt("capacity"));
            event.setCurrentAttendance(getArguments().getInt("currentAttendance"));

            listenToAttendanceUpdates();
            updateUI();
        }

        setupButtons();
        return binding.getRoot();
    }

    private void updateUI() {
        if (binding != null) {  // Add null check
            binding.eventTitle.setText(event.getTitle());

            // Calculate percentage
            int percentage = event.getCapacity() > 0 ?
                    (event.getCurrentAttendance() * 100) / event.getCapacity() : 0;

            // Update progress circle
            binding.capacityProgress.setProgress(percentage);

            // Update capacity text
            binding.capacityText.setText(String.format("%d/%d Capacity",
                    event.getCurrentAttendance(),
                    event.getCapacity()));
        }
    }


    private void setupButtons() {
        binding.backButton.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        binding.sendNotificationButton.setOnClickListener(v ->
                showNotificationDialog());

        binding.sendNotificationButton.setOnClickListener(v -> {
            Event event = new Event();
            NotificationDialog dialog = NotificationDialog.newInstance(event);
            dialog.show(getParentFragmentManager(), "notification_dialog");
        });

    }

    private void showNotificationDialog() {
        // We'll implement this next
        // This will show a dialog to send notifications to event attendees
    }

    private void listenToAttendanceUpdates() {
        eventRef = FirebaseDatabase.getInstance()
                .getReference("events")
                .child(event.getId());

        // Create the listener
        attendanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer attendance = snapshot.getValue(Integer.class);
                if (attendance != null && binding != null) {  // Add binding null check
                    event.setCurrentAttendance(attendance);
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };

        // Attach the listener
        eventRef.child("currentAttendance").addValueEventListener(attendanceListener);
    }

    @Override
    public void onDestroyView() {
        // Remove the listener when the fragment is destroyed
        if (eventRef != null && attendanceListener != null) {
            eventRef.child("currentAttendance").removeEventListener(attendanceListener);
        }

        super.onDestroyView();
        binding = null;
    }
}