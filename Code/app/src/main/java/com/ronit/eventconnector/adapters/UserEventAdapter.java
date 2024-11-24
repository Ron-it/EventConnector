package com.ronit.eventconnector.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ronit.eventconnector.databinding.ItemUserEventBinding;
import com.ronit.eventconnector.models.Event;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserEventAdapter extends RecyclerView.Adapter<UserEventAdapter.EventViewHolder> {
    private List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public void updateEvents(List<Event> myEvents) {
        this.events = myEvents;
        notifyDataSetChanged();
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public UserEventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserEventBinding binding = ItemUserEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserEventBinding binding;

        EventViewHolder(ItemUserEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Event event) {
            binding.eventTitle.setText(event.getTitle());
            binding.eventDateTime.setText(dateFormat.format(new Date(event.getDateTime())));
            binding.eventLocation.setText(event.getLocation());

            // Listen for real-time attendance updates
            DatabaseReference eventRef = FirebaseDatabase.getInstance()
                    .getReference("events")
                    .child(event.getId());

            eventRef.child("currentAttendance").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer currentAttendance = snapshot.getValue(Integer.class);
                    if (currentAttendance != null) {
                        // Update the progress and text
                        int capacity = event.getCapacity();
                        int percentage = capacity > 0 ? (currentAttendance * 100) / capacity : 0;

                        binding.capacityProgress.setProgress(percentage);
                        binding.capacityText.setText(String.format("%d/%d Capacity",
                                currentAttendance, capacity));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if needed
                }
            });

            binding.getRoot().setOnClickListener(v -> listener.onEventClick(event));
        }
    }
}