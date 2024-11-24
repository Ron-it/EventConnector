package com.ronit.eventconnector.fragments;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ronit.eventconnector.R;
import com.ronit.eventconnector.adapters.EventAdapter;
import com.ronit.eventconnector.databinding.FragmentDiscoverBinding;
import com.ronit.eventconnector.models.Event;
import com.ronit.eventconnector.utils.FilterManager;
import com.ronit.eventconnector.utils.LocationUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiscoverFragment extends Fragment implements OnMapReadyCallback {
    private FragmentDiscoverBinding binding;
    private GoogleMap mMap;
    private DatabaseReference eventsRef;
    private List<Event> eventsList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    private List<Event> filteredEvents = new ArrayList<>(); // To store currently filtered events
    private List<com.google.android.gms.maps.model.Marker> mapMarkers = new ArrayList<>(); // To track markers


    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        if (permissions.containsValue(false)) {
                            // Handle permission denied
                        } else {
                            enableMyLocation();
                            loadNearbyEvents();
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);

        filterManager = new FilterManager();
        binding.filterButton.setOnClickListener(v -> showFilterDialog());

        setupSearchView();
        setupRecyclerView();
        setupFirebase();
        setupViewToggle();
        setupMapView(savedInstanceState);
        checkLocationPermissions();

        return binding.getRoot();
    }

    // Update your existing setupFirebase() method to update the RecyclerView:
    private void setupFirebase() {
        eventsRef = FirebaseDatabase.getInstance().getReference("events");
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    eventsList.clear();
                    for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                        Event event = eventSnapshot.getValue(Event.class);
                        if (event != null && event.getTitle() != null) {  // Add null checks
                            // Debug log
                            Log.d("DiscoverFragment", "Loading event: " + event.getTitle()
                                    + " | Coordinates: " + event.getLatitude() + "," + event.getLongitude());
                            eventsList.add(event);
                        }
                    }
                    filteredEvents = new ArrayList<>(eventsList);
                    eventAdapter.updateEvents(filteredEvents);
                    updateMapMarkers();
                } catch (Exception e) {
                    Log.e("DiscoverFragment", "Error loading events: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DiscoverFragment", "Database error: " + error.getMessage());
                Toast.makeText(requireContext(), "Error loading events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermissions() {
        if (!LocationUtils.hasLocationPermissions(requireContext())) {
            requestPermissionLauncher.launch(LocationUtils.LOCATION_PERMISSIONS);
        } else {
            enableMyLocation();
            loadNearbyEvents();
        }
    }

    private void enableMyLocation() {
        if (mMap != null && LocationUtils.hasLocationPermissions(requireContext())) {
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                // Removed the camera movement to user location
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadNearbyEvents() {
        LocationUtils.getCurrentLocation(requireContext(), location -> {
            if (location != null) {
                eventAdapter.setUserLocation(location);
            }
        });
    }

//    private void addEventMarker(Event event) {
//        LatLng position = new LatLng(event.getLatitude(), event.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(position)
//                .title(event.getTitle())
//                .snippet(dateFormat.format(new Date(event.getDateTime())));
//        mMap.addMarker(markerOptions);
//    }

    private void updateMapMarkers() {
        if (mMap == null) return;

        // Clear existing markers
        for (com.google.android.gms.maps.model.Marker marker : mapMarkers) {
            marker.remove();
        }
        mapMarkers.clear();

        // Add new markers for filtered events
        for (Event event : filteredEvents) {
            LatLng position = new LatLng(event.getLatitude(), event.getLongitude());

            // Debug log to verify coordinates
            Log.d("DiscoverFragment", "Adding marker at: " + event.getLatitude() + ", " + event.getLongitude()
                    + " for event: " + event.getTitle());

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(event.getTitle())
                    .snippet(dateFormat.format(new Date(event.getDateTime())));

            com.google.android.gms.maps.model.Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(event);
                mapMarkers.add(marker);
            }
        }
    }


    private void setupMapView(Bundle savedInstanceState) {
        // Initialize map
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);
    }

    private void setupViewToggle() {
        // Set initial selection
        binding.viewToggleGroup.check(binding.mapViewButton.getId());

        binding.viewToggleGroup.addOnButtonCheckedListener(
                new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                    @Override
                    public void onButtonChecked(MaterialButtonToggleGroup group,
                                                int checkedId,
                                                boolean isChecked) {
                        if (isChecked) {
                            if (checkedId == binding.mapViewButton.getId()) {
                                binding.mapView.setVisibility(View.VISIBLE);
                                binding.eventsRecyclerView.setVisibility(View.GONE);
                            } else if (checkedId == binding.listViewButton.getId()) {
                                binding.mapView.setVisibility(View.GONE);
                                binding.eventsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set default location to UBCO (49.9401° N, 119.3960° W)
        LatLng ubco = new LatLng(49.9401, -119.3960);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubco, 15f));

        // Customize map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (!eventsList.isEmpty()) {
                filteredEvents = new ArrayList<>(eventsList);
                updateMapMarkers();
            }

            // Show info window on marker click
            mMap.setOnMarkerClickListener(marker -> {
                marker.showInfoWindow();
                return true;
            });

            // Open details when info window is clicked
            mMap.setOnInfoWindowClickListener(marker -> {
                Event event = (Event) marker.getTag();
                if (event != null) {
                    openEventDetails(event);
                }
            });

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Add this helper method to open event details
    private void openEventDetails(Event event) {
        LocationUtils.getCurrentLocation(requireContext(), location -> {
            double distance = location != null ?
                    LocationUtils.calculateDistance(
                            location.getLatitude(),
                            location.getLongitude(),
                            event.getLatitude(),
                            event.getLongitude()
                    ) : 0;

            ViewEventFragment viewEventFragment = ViewEventFragment.newInstance(event, distance);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, viewEventFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private EventAdapter eventAdapter;

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter((event, distance) -> {  // Add distance parameter here
            ViewEventFragment viewEventFragment = ViewEventFragment.newInstance(event, distance);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, viewEventFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.eventsRecyclerView.setAdapter(eventAdapter);
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private FilterManager filterManager;

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_filter_events, null);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        Spinner eventTypeSpinner = dialogView.findViewById(R.id.eventTypeSpinner);
        Spinner distanceSpinner = dialogView.findViewById(R.id.distanceSpinner);

        // Setup spinners
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                FilterManager.EVENT_TYPES
        );

        ArrayAdapter<String> distanceAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                FilterManager.DISTANCE_OPTIONS
        );

        eventTypeSpinner.setAdapter(eventTypeAdapter);
        distanceSpinner.setAdapter(distanceAdapter);

        // Handle button clicks
        dialogView.findViewById(R.id.clearButton).setOnClickListener(v -> {
            filterManager.clearFilters();
            applyFilters();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.applyButton).setOnClickListener(v -> {
            filterManager.setEventType(eventTypeSpinner.getSelectedItem().toString());
            filterManager.setDistance(distanceSpinner.getSelectedItem().toString());
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        LocationUtils.getCurrentLocation(requireContext(), location -> {
            if (location != null) {
                // Get filtered events
                filteredEvents = filterManager.applyFilters(
                        eventsList,
                        location.getLatitude(),
                        location.getLongitude()
                );

                // Update RecyclerView
                eventAdapter.updateEvents(filteredEvents);

                // Update map markers
                updateMapMarkers();
            }
        });
    }

    private void setupSearchView() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterManager.setSearchQuery(s.toString());
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    // MapView lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}