package com.ronit.eventconnector.models;

public class Review {
    private String id;
    private String eventId;
    private String reviewerName;
    private String comment;
    private float rating;
    private long timestamp;
    private String imageUrl;
    private boolean isAnonymous;

    // Required for Firebase
    public Review() {}

    public Review(String eventId, String reviewerName, String comment, float rating, boolean isAnonymous) {
        this.eventId = eventId;
        this.reviewerName = reviewerName;
        this.comment = comment;
        this.rating = rating;
        this.isAnonymous = isAnonymous;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getReviewerName() {
        return isAnonymous ? "Anonymous" : reviewerName;
    }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
}