package com.ronit.eventconnector.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ronit.eventconnector.R;
import com.ronit.eventconnector.adapters.UserEventAdapter;
import com.ronit.eventconnector.databinding.FragmentProfileBinding;
import com.ronit.eventconnector.models.Event;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private UserEventAdapter adapter;
    private DatabaseReference eventsRef;
    private ValueEventListener eventListener;  // Add this field

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        setupRecyclerView();
        loadMyEvents();

        return binding.getRoot();
    }

    private void loadMyEvents() {
        eventsRef = FirebaseDatabase.getInstance().getReference("events");

        // Create the listener
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding != null) {  // Add null check for binding
                    List<Event> myEvents = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                        Event event = eventSnapshot.getValue(Event.class);
                        if (event != null) {
                            myEvents.add(event);
                        }
                    }
                    adapter.updateEvents(myEvents);
                    updateEmptyView(myEvents.isEmpty());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (binding != null) {  // Add null check for binding
                    Toast.makeText(requireContext(),
                            "Error loading events: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        // Attach the listener
        eventsRef.orderByChild("locallyCreated").equalTo(true)
                .addValueEventListener(eventListener);
    }

    private void updateEmptyView(boolean isEmpty) {
        if (binding != null) {  // Add null check for binding
            binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.userEventsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new UserEventAdapter(event -> {
            // Navigate to event management
            EventManagementFragment fragment = EventManagementFragment.newInstance(event);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.userEventsRecyclerView.setAdapter(adapter);
        binding.userEventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onDestroyView() {
        // Remove the listener when the fragment is destroyed
        if (eventsRef != null && eventListener != null) {
            eventsRef.orderByChild("locallyCreated").equalTo(true)
                    .removeEventListener(eventListener);
        }

        super.onDestroyView();
        binding = null;
    }
}