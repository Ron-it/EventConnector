package com.ronit.eventconnector.utils;

import com.ronit.eventconnector.models.Event;
import java.util.ArrayList;
import java.util.List;

public class FilterManager {
    public static final String[] EVENT_TYPES = {
            "All Events",
            "Concerts",
            "Parties",
            "Activities",
            "Company/Events"
    };

    public static final String[] DISTANCE_OPTIONS = {
            "Any Distance",
            "< 1 km",
            "1-5 km",
            "5+ km"
    };

    private String selectedEventType = EVENT_TYPES[0];
    private String selectedDistance = DISTANCE_OPTIONS[0];
    private String searchQuery = "";

    public void setEventType(String eventType) {
        this.selectedEventType = eventType;
    }

    public void setDistance(String distance) {
        this.selectedDistance = distance;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase().trim();
    }

    public void clearFilters() {
        selectedEventType = EVENT_TYPES[0];
        selectedDistance = DISTANCE_OPTIONS[0];
        searchQuery = "";
    }

    public List<Event> applyFilters(List<Event> events, double userLat, double userLng) {
        List<Event> filteredEvents = new ArrayList<>();

        for (Event event : events) {
            if (matchesFilters(event, userLat, userLng) && matchesSearch(event)) {
                filteredEvents.add(event);
            }
        }
        return filteredEvents;
    }

    private boolean matchesSearch(Event event) {
        if (searchQuery.isEmpty()) return true;

        return event.getTitle().toLowerCase().contains(searchQuery) ||
                event.getLocation().toLowerCase().contains(searchQuery) ||
                event.getDescription().toLowerCase().contains(searchQuery);
    }

    private boolean matchesFilters(Event event, double userLat, double userLng) {
        // Check event type
        if (!selectedEventType.equals(EVENT_TYPES[0]) &&
                !event.getEventType().equals(selectedEventType)) {
            return false;
        }

        // Check distance
        if (!selectedDistance.equals(DISTANCE_OPTIONS[0])) {
            double distance = LocationUtils.calculateDistance(
                    userLat, userLng,
                    event.getLatitude(), event.getLongitude()
            );

            switch (selectedDistance) {
                case "< 1 km":
                    if (distance >= 1) return false;
                    break;
                case "1-5 km":
                    if (distance < 1 || distance > 5) return false;
                    break;
                case "5+ km":
                    if (distance <= 5) return false;
                    break;
            }
        }

        return true;
    }
}