package com.ronit.eventconnector.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Event {
    private String id;
    private String title;
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private long dateTime;
    private String eventType;
    private int capacity;
    private int currentAttendance;
    private String creatorId;
    private double cost;
    private String imageUrl;  // Added for future image support
    private boolean isLocallyCreated = false;

    // Required for Firebase
    public Event() {
    }

    public Event(String id, String title, String description, String location,
                 double latitude, double longitude, long dateTime,
                 String eventType, int capacity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
        this.eventType = eventType;
        this.capacity = capacity;
        this.currentAttendance = 0;
    }

    // Existing getters and setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getCurrentAttendance() { return currentAttendance; }
    public void setCurrentAttendance(int currentAttendance) {
        this.currentAttendance = currentAttendance;
    }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    // New getters and setters for cost
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    // New getters and setters for imageUrl
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isLocallyCreated() { return isLocallyCreated; }
    public void setLocallyCreated(boolean locallyCreated) {
        this.isLocallyCreated = locallyCreated;
    }
}