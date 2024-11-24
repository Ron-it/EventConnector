package com.ronit.eventconnector.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

import com.ronit.eventconnector.R;
import com.ronit.eventconnector.databinding.DialogNotificationBinding;
import com.ronit.eventconnector.models.Event;

public class NotificationDialog extends DialogFragment {

    private DialogNotificationBinding binding;
    private static final String CHANNEL_ID = "local_notification_channel";

    public static NotificationDialog newInstance(Event event) {
        NotificationDialog dialog = new NotificationDialog();
        Bundle args = new Bundle();

        // Pass event data to the dialog
        args.putString("eventId", event.getId());
        args.putString("eventTitle", event.getTitle());
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DialogNotificationBinding.inflate(inflater, container, false);

        setupSpinner(); // Add notification type options
        setupButtons(); // Handle button clicks

        return binding.getRoot();
    }

    private void setupSpinner() {
        // Predefined options for the Notification Type spinner
        String[] notificationTypes = {"Reminder", "General Notification"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                notificationTypes
        );
        binding.notificationTypeSpinner.setAdapter(adapter);
    }

    private void setupButtons() {
        // Cancel button dismisses the dialog
        binding.cancelButton.setOnClickListener(view -> dismiss());

        // Send button sends local notification
        binding.sendButton.setOnClickListener(view -> {
            String message = binding.messageInput.getText().toString();
            String notificationType = binding.notificationTypeSpinner.getSelectedItem().toString();

            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                return; // Stop if message is empty
            }

            showLocalNotification(notificationType, message);
            showToast();
        });
    }

    private void showLocalNotification(String type, String message) {
        // Get the Notification Manager
        NotificationManager notificationManager = (NotificationManager) requireContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Step 1: Create Notification Channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Event Notifications";
            String channelDescription = "Notifications for event updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    importance
            );
            channel.setDescription(channelDescription);

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel);
        }

        // Step 2: Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ensure this drawable resource exists
                .setContentTitle(type) // Set notification title (e.g., "Reminder")
                .setContentText(message) // Set the notification content
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Make the notification high priority
                .setAutoCancel(true); // Dismiss notification when clicked

        // Step 3: Show the Notification
        int notificationId = (int) System.currentTimeMillis(); // Use unique ID to avoid overwriting
        notificationManager.notify(notificationId, builder.build());
    }

    private void showToast() {
        // Show Toast message
        Toast.makeText(requireContext(), "Notification sent successfully", Toast.LENGTH_SHORT).show();
    }
}