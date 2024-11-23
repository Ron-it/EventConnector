package com.ronit.eventconnector.adapters;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ronit.eventconnector.R;
import com.ronit.eventconnector.models.Event;
import com.ronit.eventconnector.utils.LocationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    private OnEventClickListener listener;
    private Location userLocation;

    // Add this method to update user location
    public void setUserLocation(Location location) {
        this.userLocation = location;
        notifyDataSetChanged(); // Refresh distances
    }

    public interface OnEventClickListener {
        void onEventClick(Event event, double distance);
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView locationText;
        private final TextView dateText;
        private final TextView distanceText;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.eventTitle);
            locationText = itemView.findViewById(R.id.eventLocation);
            dateText = itemView.findViewById(R.id.eventDate);
            distanceText = itemView.findViewById(R.id.eventDistance);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Event event = events.get(position);
                    // Calculate distance using the stored userLocation
                    double distance = 0;
                    if (userLocation != null) {
                        distance = LocationUtils.calculateDistance(
                                userLocation.getLatitude(),
                                userLocation.getLongitude(),
                                event.getLatitude(),
                                event.getLongitude()
                        );
                    }
                    listener.onEventClick(event, distance);
                }
            });
        }

        public void bind(Event event) {
            titleText.setText(event.getTitle());
            locationText.setText(event.getLocation());
            dateText.setText(dateFormat.format(new Date(event.getDateTime())));

            // Calculate and display distance if user location is available
            if (userLocation != null) {
                double distance = LocationUtils.calculateDistance(
                        userLocation.getLatitude(), userLocation.getLongitude(),
                        event.getLatitude(), event.getLongitude()
                );
                distanceText.setText(LocationUtils.formatDistance(distance));
            } else {
                distanceText.setText("Distance unavailable");
            }
        }
    }
}