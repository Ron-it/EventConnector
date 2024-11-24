package com.ronit.eventconnector.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ronit.eventconnector.models.Event;
import com.ronit.eventconnector.databinding.FragmentEventManagementBinding;

public class EventManagementFragment extends Fragment {
    private FragmentEventManagementBinding binding;
    private Event event;  // Change from DatabaseReference to Event

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

            updateUI();
        }

        setupButtons();
        return binding.getRoot();
    }

    private void updateUI() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}