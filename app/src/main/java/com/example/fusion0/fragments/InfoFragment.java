package com.example.fusion0.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.fusion0.R;
import com.example.fusion0.helpers.AddEventHelper;
import com.example.fusion0.helpers.EventFirebase;
import com.example.fusion0.models.AddEventViewModel;
import com.example.fusion0.models.OrganizerInfo;
import com.example.fusion0.models.SimpleTextWatcher;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Aid with the creation of events
 * @author Simon Haile
 */
public class InfoFragment extends Fragment {

    private final String TAG = "InfoFragment";
    private TextView dateRequirementsTextView,registrationDateRequirementsTextView, startDateTextView, startTimeTextView, endDateTextView, endTimeTextView, registrationDateTextView;
    private Calendar startDateCalendar , registrationDateCalendar;

    private EditText eventName, description, capacity, lotteryCapacity;

    private Button addEventButton;
    private ImageView uploadedImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private Date startDate;
    private Date endDate;
    private Date registrationDate;
    private String eventPoster;
    private Uri eventPosterUri;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private OrganizerInfo organizer;
    private AddEventViewModel viewModel;

    private String deviceID;
    private ImageButton profileButton;
    private ImageButton addButton;
    private ImageButton homeButton;
    private ImageButton scannerButton;
    private ImageButton favouriteButton;

    private TextView homeTextView;
    private TextView scannerTextView;
    private TextView addTextView;
    private TextView searchTextView;
    private TextView profileTextView;

    private LottieAnimationView loadingSpinner;
    private EventFirebase eventFirebase = new EventFirebase();

    /**
     * Required empty public constructor
     * @author Nimi Akinroye
     */
    public InfoFragment() {
    }

    /**
     * Initialize variables
     * @author Nimi Akinroye
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @SuppressLint("HardwareIds")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceID = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    /**
     * Inflate the view
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);  // Change this later
    }

    /**
     * Do everything except facilities and geolocation
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AddEventViewModel.class);
        AddEventHelper helper = viewModel.getHelper();

        helper.setDeviceID(deviceID);

        Context infoContext = requireContext();
        Activity activity = requireActivity();

        eventName = view.findViewById(R.id.EventName);
        uploadedImageView = view.findViewById(R.id.uploaded_image_view);
        description = view.findViewById(R.id.Description);
        dateRequirementsTextView = view.findViewById(R.id.date_requirements_text);
        registrationDateRequirementsTextView =view.findViewById(R.id.registrationDateRequirementsTextView);
        startDateTextView = view.findViewById(R.id.start_date_text);
        startTimeTextView = view.findViewById(R.id.start_time_text);
        endDateTextView = view.findViewById(R.id.end_date_text);
        endTimeTextView = view.findViewById(R.id.end_time_text);
        capacity = view.findViewById(R.id.Capacity);
        lotteryCapacity =view.findViewById(R.id.lotteryCapacity);
        addEventButton = view.findViewById(R.id.add_button);


        registrationDateTextView = view.findViewById(R.id.registration_date_text);
        startDateTextView = view.findViewById(R.id.start_date_text);
        startTimeTextView = view.findViewById(R.id.start_time_text);
        startDateTextView = view.findViewById(R.id.start_date_text);
        startTimeTextView = view.findViewById(R.id.start_time_text);



        setupTextListeners(helper);

        // Main Processes
        validateOrganizer(infoContext);
        uploadPoster(view, infoContext);
        startDateButtonHandling(view, infoContext);
        endDateButtonHandling(view, infoContext);
        registrationDateButtonHandling(view, infoContext);
    }

    /**
     * @author Simon Haile
     * Validates and retrieves the organizer associated with the current device ID.
     * This method checks if an organizer already exists in the database based on the device ID.
     * If the organizer is found, it is assigned to the `organizer` variable.
     * If no organizer is found, a new `OrganizerInfo` object is created and added to the database.
     */
    private void validateOrganizer(Context context) {
        eventFirebase.findOrganizer(deviceID, new EventFirebase.OrganizerCallback() {
            @Override
            public void onSuccess(OrganizerInfo organizerInfo) {
                if (organizerInfo == null) {
                    organizer = new OrganizerInfo(deviceID);
                    eventFirebase.addOrganizer(organizer);
                    viewModel.getHelper().setOrganizer(organizer);
                } else {
                    organizer = organizerInfo;
                    viewModel.getHelper().setOrganizer(organizer);
                }

                // Can be handled elsewhere
                // I can make it return the organizer
//                handleFacility(organizer, context);
            }
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Error fetching organizer: " + error);
            }
        });
    }

    /**
     * @author Simon Haile
     * Initializes the image upload process by setting up an image picker and handling the image
     * upload to Firebase Storage.
     * This method sets up an `ActivityResultLauncher` to handle the image selection from the device.
     * When an image is selected, it is displayed in an `ImageView` (`eventPosterImageView`).
     * The image is then uploaded to Firebase Storage under the "event_posters"
     * directory, with a unique file name generated using `UUID`. Once the upload is successful,
     * the download URL for the uploaded image
     * is stored for later use.
     */
    private void uploadPoster(View view, Context context) {
        ImageButton uploadImageButton = view.findViewById(R.id.upload_image_button);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        Uri destinationUri = Uri.fromFile(new File(context.getCacheDir(), "cropped_image.jpg"));

                        assert imageUri != null;
                        UCrop.of(imageUri, destinationUri)
                                .withAspectRatio(9, 16)
                                .withMaxResultSize(800, 800)
                                .start(context, this);
                    }
                }

        );

        uploadImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    /**
     * Checks results after coming back from another activity
     * @author Simon Haile
     * @param requestCode see if the activity we came back from was correct
     * @param resultCode whether the activity finished correctly
     * @param data the data obtained from the activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loadingSpinner = requireView().findViewById(R.id.loadingSpinner);
        loadingSpinner.playAnimation();
        loadingSpinner.setVisibility(View.VISIBLE);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                uploadedImageView.setVisibility(View.VISIBLE);

                StorageReference imageRef = storageRef.child("event_posters/" + UUID.randomUUID().toString() + ".jpg");

                imageRef.putFile(resultUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                eventPoster = uri.toString();
                                viewModel.getHelper().setEventPoster(eventPoster);

                                Glide.with(requireContext())
                                        .load(resultUri)
                                        .centerCrop()
                                        .into(uploadedImageView);
                                loadingSpinner.cancelAnimation();
                                loadingSpinner.setVisibility(View.GONE);

                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting download URL", e);
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Upload failed", e);
                        });
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Log.e(TAG, "Crop error", cropError);
            }
        }
    }

    /**
     * @author Simon Haile
     * Handles the user interaction with the Start Date button. This method sets up a date picker dialog for selecting
     * the start date and time. It validates the selected date and time to ensure they are not in the past, and displays
     * appropriate error messages when necessary.
     */
    private void startDateButtonHandling(View view, Context context) {
        ConstraintLayout startDateButton = view.findViewById(R.id.start_date_button);
        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        startDateCalendar = Calendar.getInstance();
                        startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                        Calendar currentDate = Calendar.getInstance();
                        currentDate.add(Calendar.DAY_OF_YEAR, 1); // increments current date to tomorrow
                        if (startDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("Date Must Be After Today.");
                            showWithTransition(dateRequirementsTextView);
                            startDateTextView.setText("MM / DD / YYYY");
                            startTimeTextView.setText("HH / MM");
                            startDateTextView.setTextColor(getResources().getColor(R.color.red));
                            startDateCalendar = null;
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            startDateTextView.setText(selectedDate);
                            startDateTextView.setTextColor(getResources().getColor(R.color.black));
                            startDate = startDateCalendar.getTime();

                            viewModel.getHelper().setStartDate(startDate);


                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    startDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    Calendar currentTime = Calendar.getInstance();
                                    if (startDateCalendar.before(currentTime)) {
                                        dateRequirementsTextView.setText("Start Time Must Be Now or Later.");
                                        showWithTransition(dateRequirementsTextView);
                                        startDateTextView.setText("MM / DD / YYYY");
                                        startTimeTextView.setText("HH / MM");
                                        startTimeTextView.setTextColor(getResources().getColor(R.color.red));
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        startTimeTextView.setText(selectedTime);
                                        startTimeTextView.setTextColor(getResources().getColor(R.color.black));
                                    }
                                }
                            }, hour, minute, true);
                            timePickerDialog.show();
                        }
                    }
                }, year, month, day);
                dialog.show();
            }
        });
    }

    /**
     * @author Simon Haile
     * Handles the user interaction with the End Date button. This method sets up a date picker dialog for selecting
     * the end date and time. It ensures that the end date is not earlier than the start date and that the selected
     * end time is not before the start time.
     */
    private void endDateButtonHandling(View view, Context context) {
        ConstraintLayout endDateButton = view.findViewById(R.id.end_date_button);
        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        Calendar endDateCalendar = Calendar.getInstance();
                        endDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                        // Copy time from startDateCalendar if the dates are the same
                        if (startDateCalendar != null &&
                                endDateCalendar.get(Calendar.YEAR) == startDateCalendar.get(Calendar.YEAR) &&
                                endDateCalendar.get(Calendar.MONTH) == startDateCalendar.get(Calendar.MONTH) &&
                                endDateCalendar.get(Calendar.DAY_OF_MONTH) == startDateCalendar.get(Calendar.DAY_OF_MONTH)) {
                            // Set the end time to the start time
                            endDateCalendar.set(Calendar.HOUR_OF_DAY, startDateCalendar.get(Calendar.HOUR_OF_DAY));
                            endDateCalendar.set(Calendar.MINUTE, startDateCalendar.get(Calendar.MINUTE));
                        }
                        Calendar currentDate = Calendar.getInstance();
                        if (startDateCalendar == null) {
                            dateRequirementsTextView.setText("Please select a Start Date first.");
                            showWithTransition(dateRequirementsTextView);
                            endDateTextView.setText("MM / DD / YYYY");
                            endTimeTextView.setText("HH / MM");
                            startDateTextView.setTextColor(getResources().getColor(R.color.red));
                            startTimeTextView.setTextColor(getResources().getColor(R.color.red));
                        } else if (endDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("End Date Must Be Today or Later.");
                            showWithTransition(dateRequirementsTextView);
                            endDateTextView.setText("MM / DD / YYYY");
                            endTimeTextView.setText("HH / MM");
                            endDateTextView.setTextColor(getResources().getColor(R.color.red));
                        } else if (endDateCalendar.before(startDateCalendar)) {
                            dateRequirementsTextView.setText("End Date Must Be On or After Start Date.");
                            showWithTransition(dateRequirementsTextView);
                            endDateTextView.setText("MM / DD / YYYY");
                            endTimeTextView.setText("HH / MM");
                            endDateTextView.setTextColor(getResources().getColor(R.color.red));
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            endDateTextView.setText(selectedDate);
                            endDateTextView.setTextColor(getResources().getColor(R.color.black));

                            endDate = endDateCalendar.getTime();
                            viewModel.getHelper().setEndDate(endDate);

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    endDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    endDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    if (endDateCalendar.before(startDateCalendar)) {
                                        dateRequirementsTextView.setText("End Time Must Be After Start Time.");
                                        showWithTransition(dateRequirementsTextView);
                                        endDateTextView.setText("MM / DD / YYYY");
                                        endTimeTextView.setText("HH / MM");
                                        endTimeTextView.setTextColor(getResources().getColor(R.color.red));
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        endTimeTextView.setText(selectedTime);
                                        endTimeTextView.setTextColor(getResources().getColor(R.color.black));
                                    }
                                }
                            }, hour, minute, true);
                            timePickerDialog.show();
                        }
                    }
                }, year, month, day);
                dialog.show();
            }
        });
    }

    /**
     * Allows for organizer to select dates
     * @author Simon Haile
     * @param view the view
     * @param context the context
     */
    private void registrationDateButtonHandling(View view, Context context){
        ConstraintLayout registrationDateButton = view.findViewById(R.id.registration_date_button);
        registrationDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) {
                    registrationDateCalendar = Calendar.getInstance();
                    registrationDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                    Calendar currentDate = Calendar.getInstance();
                    if (startDate != null){
                        if (registrationDateCalendar.before(currentDate)) {
                            registrationDateRequirementsTextView.setText("Deadline Cannot Be Before Today.");
                            showWithTransition(registrationDateRequirementsTextView);
                            registrationDateTextView.setText("MM / DD / YYYY");
                            registrationDateTextView.setTextColor(getResources().getColor(R.color.red));
                            registrationDateCalendar = null;
                        }else if (startDateCalendar.before(registrationDateCalendar)) {
                            registrationDateRequirementsTextView.setText("RegistrationFragment deadline must be before the event start date.");
                            showWithTransition(registrationDateRequirementsTextView);
                            registrationDateTextView.setText("MM / DD / YYYY");
                            registrationDateTextView.setTextColor(getResources().getColor(R.color.red));
                            registrationDateCalendar = null;
                        }else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            registrationDateTextView.setText(selectedDate);
                            registrationDateTextView.setTextColor(getResources().getColor(R.color.black));
                            registrationDate = registrationDateCalendar.getTime();
                            viewModel.getHelper().setRegistrationDate(registrationDate);
                        }
                    }else{
                        registrationDateRequirementsTextView.setText("Please Select Start Date.");
                        showWithTransition(registrationDateRequirementsTextView);
                        registrationDateTextView.setText("MM / DD / YYYY");
                        registrationDateTextView.setTextColor(getResources().getColor(R.color.red));
                        registrationDateCalendar = null;
                    }

                }
            }, year, month, day);
            dialog.show();
        });
    }

    private void setupTextListeners(AddEventHelper helper) {

        // Event Name Listener
        eventName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                helper.setEventName(s.toString());
            }
        });

        // Description Listener
        description.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                helper.setDescription(s.toString());
            }
        });

        // Capacity Listener
        capacity.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                helper.setWaitlistCapacity(s.toString());
            }
        });

        // Lottery Capacity Listener
        lotteryCapacity.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                helper.setLotteryCapacity(s.toString());
            }
        });

        // Start Time Listener
        startTimeTextView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                helper.setStartTime(s.toString());
            }
        });


        // End Time Listener
        endTimeTextView.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                helper.setEndTime(s.toString());
            }
        });

    }

    public void showWithTransition(View view) {
        // Fade in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500); // Duration for fade-in (500ms)
        fadeIn.setFillAfter(true);

        // Fade out animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500); // Duration for fade-out (500ms)
        fadeOut.setFillAfter(true);

        // Make the view visible and start fade-in
        view.setVisibility(View.VISIBLE);
        view.startAnimation(fadeIn);

        // Delay for 4 seconds, then start fade-out and hide the view
        new Handler().postDelayed(() -> {
            view.startAnimation(fadeOut);
            new Handler().postDelayed(() -> view.setVisibility(View.GONE), fadeOut.getDuration());
        }, 4000);
    }
}