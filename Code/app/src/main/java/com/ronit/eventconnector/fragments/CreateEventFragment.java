package com.ronit.eventconnector.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.annotations.Nullable;
import com.ronit.eventconnector.R;
import com.ronit.eventconnector.databinding.FragmentCreateEventBinding;
import com.ronit.eventconnector.models.Event;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateEventFragment extends Fragment {
    private FragmentCreateEventBinding binding;
    private Calendar selectedDateTime = Calendar.getInstance();
    private static final String[] CATEGORIES = new String[]{
            "Concerts", "Parties", "Activities", "Company/Events"
    };
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(inflater, container, false);

        // Initialize Places if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }

        setupCategoryDropdown();
        setupDateTimePicker();
        setupButtons();
        setupLocationInput();  // This line is missing

        return binding.getRoot();
    }

    private LatLng selectedLatLng;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.d("PlacesAPI", "Place selected: " + place.getName());

                    // Get all available fields
                    Log.d("PlacesAPI", "Fields available: " + place.getAddress() +
                            ", LatLng: " + place.getLatLng());

                    selectedLatLng = place.getLatLng();
                    if (selectedLatLng != null) {
                        Log.d("PlacesAPI", "Coordinates: " + selectedLatLng.latitude +
                                ", " + selectedLatLng.longitude);
                        binding.locationInput.setText(place.getAddress());
                    } else {
                        Log.e("PlacesAPI", "LatLng is null");
                        Toast.makeText(requireContext(),
                                "Error: Could not get location coordinates",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("PlacesAPI", "Error getting place: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupLocationInput() {
        // Initialize Places if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }

        binding.locationInput.setFocusable(false);
        binding.locationInput.setOnClickListener(v -> {
            // Specify the fields to return
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG  // Make sure this is included
            );

            try {
                // Start the autocomplete intent
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(requireContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,  // Changed this line
                CATEGORIES
        );

        AutoCompleteTextView categoryInput = binding.categoryInput;
        categoryInput.setAdapter(adapter);
    }

    private void setupDateTimePicker() {
        binding.dateTimeInput.setOnClickListener(v -> showDateTimePicker());
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    showTimePicker();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        binding.dateTimeInput.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void setupButtons() {
        binding.closeButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.nextButton.setOnClickListener(v -> {
            if (validateInputs()) {
                navigateToEventDetails();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (binding.eventTitleInput.getText().toString().trim().isEmpty()) {
            binding.eventTitleLayout.setError("Title is required");
            isValid = false;
        } else {
            binding.eventTitleLayout.setError(null);
        }

        if (binding.dateTimeInput.getText().toString().trim().isEmpty()) {
            binding.dateTimeLayout.setError("Date & Time is required");
            isValid = false;
        } else {
            binding.dateTimeLayout.setError(null);
        }

        if (binding.locationInput.getText().toString().trim().isEmpty()) {
            binding.locationLayout.setError("Location is required");
            isValid = false;
        } else {
            binding.locationLayout.setError(null);
        }

        if (binding.categoryInput.getText().toString().trim().isEmpty()) {
            binding.categoryLayout.setError("Category is required");
            isValid = false;
        } else {
            binding.categoryLayout.setError(null);
        }

        return isValid;
    }

    private void navigateToEventDetails() {
        if (selectedLatLng == null) {
            Toast.makeText(requireContext(), "Please select a valid location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug log
        Log.d("CreateEventFragment", "Creating event with coordinates: " +
                selectedLatLng.latitude + ", " + selectedLatLng.longitude);

        Event event = new Event();
        event.setTitle(binding.eventTitleInput.getText().toString().trim());
        event.setLocation(binding.locationInput.getText().toString().trim());
        event.setDateTime(selectedDateTime.getTimeInMillis());
        event.setEventType(binding.categoryInput.getText().toString().trim());
        event.setLatitude(selectedLatLng.latitude);
        event.setLongitude(selectedLatLng.longitude);

        // Debug log
        Log.d("CreateEventFragment", "Event coordinates before navigation: " +
                event.getLatitude() + ", " + event.getLongitude());

        EventDetailsFragment detailsFragment = EventDetailsFragment.newInstance(event);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}