package com.example.fusion0;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventActivity extends AppCompatActivity{
    private EventInfo eventInfo;
    private FacilitiesInfo facilitiesInfo;

    private EditText eventName;

    private EditText description;


    private Calendar startDateCalendar;

    private TextView dateRequirementsTextView;
    private TextView startDateTextView;
    private TextView startTimeTextView;
    private TextView endDateTextView;
    private TextView endTimeTextView;

    private EditText capacity;

    private Button addButton;
    private Button exitButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        eventName = findViewById(R.id.EventName);
        description = findViewById(R.id.Description);
        dateRequirementsTextView = findViewById(R.id.date_requirements_text);
        startDateTextView = findViewById(R.id.start_date_text);
        startTimeTextView = findViewById(R.id.start_time_text);
        endDateTextView = findViewById(R.id.end_date_text);
        endTimeTextView = findViewById(R.id.end_time_text);

        capacity = findViewById(R.id.Capacity);
        addButton = findViewById(R.id.add_button);
        exitButton = findViewById(R.id.exit_button);


        StartDateButtonHandling();
        EndDateButtonHandling();
        AddEvent();
        ExitButtonHandling();
    }

    private void StartDateButtonHandling() {
        Button startDateButton = findViewById(R.id.start_date_button);
        TextView startDateTextView = findViewById(R.id.start_date_text);
        TextView startTimeTextView = findViewById(R.id.start_time_text);

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(EventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        startDateCalendar = Calendar.getInstance();
                        startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                        Calendar currentDate = Calendar.getInstance();

                        if (startDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("Date Must Be Today or Later.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                            startDateTextView.setVisibility(View.INVISIBLE);
                            startDateCalendar = null;
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            startDateTextView.setText(selectedDate);
                            startDateTextView.setVisibility(View.VISIBLE);
                            dateRequirementsTextView.setVisibility(View.INVISIBLE);

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    startDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    // Check if start time is before current time
                                    Calendar currentTime = Calendar.getInstance();
                                    if (startDateCalendar.before(currentTime)) {
                                        dateRequirementsTextView.setText("Start Time Must Be Now or Later.");
                                        dateRequirementsTextView.setVisibility(View.VISIBLE);
                                        startTimeTextView.setVisibility(View.INVISIBLE);
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        startTimeTextView.setText(selectedTime);
                                        startTimeTextView.setVisibility(View.VISIBLE);
                                        dateRequirementsTextView.setVisibility(View.INVISIBLE);
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

    private void EndDateButtonHandling() {
        Button endDateButton = findViewById(R.id.end_date_button);
        TextView endDateTextView = findViewById(R.id.end_date_text);
        TextView endTimeTextView = findViewById(R.id.end_time_text);


        // End Date Button
        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(EventActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                            endDateTextView.setVisibility(View.INVISIBLE);
                            endTimeTextView.setVisibility(View.INVISIBLE);
                        } else if (endDateCalendar.before(currentDate)) {
                            dateRequirementsTextView.setText("End Date Must Be Today or Later.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                            endDateTextView.setVisibility(View.INVISIBLE);
                            endTimeTextView.setVisibility(View.INVISIBLE);
                        } else if (endDateCalendar.before(startDateCalendar)) {
                            endDateTextView.setVisibility(View.INVISIBLE);
                            endTimeTextView.setVisibility(View.INVISIBLE);
                            dateRequirementsTextView.setText("End Date Must Be On or After Start Date.");
                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                        } else {
                            String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                            endDateTextView.setText(selectedDate);
                            endDateTextView.setVisibility(View.VISIBLE);
                            dateRequirementsTextView.setVisibility(View.INVISIBLE);

                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            TimePickerDialog timePickerDialog = new TimePickerDialog(EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                    endDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                    endDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                    // Check if end time is after or equal to start time
                                    if (endDateCalendar.before(startDateCalendar)) {
                                        dateRequirementsTextView.setText("End Time Must Be After Start Time.");
                                        dateRequirementsTextView.setVisibility(View.VISIBLE);
                                        endTimeTextView.setVisibility(View.INVISIBLE);
                                    } else {
                                        String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                        endTimeTextView.setText(selectedTime);
                                        endTimeTextView.setVisibility(View.VISIBLE);
                                        dateRequirementsTextView.setVisibility(View.INVISIBLE);
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

import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

    public class EventActivity extends AppCompatActivity, EventInfo {
        private EditText eventName;

        private EditText description;


        private Calendar startDateCalendar;

        private TextView dateRequirementsTextView;
        private TextView startDateTextView;
        private TextView startTimeTextView;
        private TextView endDateTextView;
        private TextView endTimeTextView;

        private EditText capacity;

        private Button addButton;
        private Button exitButton;


        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_event);

            eventName = findViewById(R.id.EventName);
            description = findViewById(R.id.Description);
            dateRequirementsTextView = findViewById(R.id.date_requirements_text);
            startDateTextView = findViewById(R.id.start_date_text);
            startTimeTextView = findViewById(R.id.start_time_text);
            endDateTextView = findViewById(R.id.end_date_text);
            endTimeTextView = findViewById(R.id.end_time_text);

            capacity = findViewById(R.id.Capacity);
            addButton = findViewById(R.id.add_button);
            exitButton = findViewById(R.id.exit_button);


            StartDateButtonHandling();
            EndDateButtonHandling();
            AddEvent();
            ExitButtonHandling();
        }

        private void StartDateButtonHandling() {
            Button startDateButton = findViewById(R.id.start_date_button);
            TextView startDateTextView = findViewById(R.id.start_date_text);
            TextView startTimeTextView = findViewById(R.id.start_time_text);

            startDateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog dialog = new DatePickerDialog(com.example.fusion0.EventActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                            startDateCalendar = Calendar.getInstance();
                            startDateCalendar.set(selectedYear, selectedMonth, selectedDay);
                            Calendar currentDate = Calendar.getInstance();

                            if (startDateCalendar.before(currentDate)) {
                                dateRequirementsTextView.setText("Date Must Be Today or Later.");
                                dateRequirementsTextView.setVisibility(View.VISIBLE);
                                startDateTextView.setVisibility(View.INVISIBLE);
                                startDateCalendar = null;
                            } else {
                                String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                                startDateTextView.setText(selectedDate);
                                startDateTextView.setVisibility(View.VISIBLE);
                                dateRequirementsTextView.setVisibility(View.INVISIBLE);

                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                int minute = calendar.get(Calendar.MINUTE);
                                TimePickerDialog timePickerDialog = new TimePickerDialog(com.example.fusion0.EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        startDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                        startDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                        // Check if start time is before current time
                                        Calendar currentTime = Calendar.getInstance();
                                        if (startDateCalendar.before(currentTime)) {
                                            dateRequirementsTextView.setText("Start Time Must Be Now or Later.");
                                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                                            startTimeTextView.setVisibility(View.INVISIBLE);
                                        } else {
                                            String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                            startTimeTextView.setText(selectedTime);
                                            startTimeTextView.setVisibility(View.VISIBLE);
                                            dateRequirementsTextView.setVisibility(View.INVISIBLE);
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

        private void EndDateButtonHandling() {
            Button endDateButton = findViewById(R.id.end_date_button);
            TextView endDateTextView = findViewById(R.id.end_date_text);
            TextView endTimeTextView = findViewById(R.id.end_time_text);


            // End Date Button
            endDateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog dialog = new DatePickerDialog(com.example.fusion0.EventActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                                dateRequirementsTextView.setVisibility(View.VISIBLE);
                                endDateTextView.setVisibility(View.INVISIBLE);
                                endTimeTextView.setVisibility(View.INVISIBLE);
                            } else if (endDateCalendar.before(currentDate)) {
                                dateRequirementsTextView.setText("End Date Must Be Today or Later.");
                                dateRequirementsTextView.setVisibility(View.VISIBLE);
                                endDateTextView.setVisibility(View.INVISIBLE);
                                endTimeTextView.setVisibility(View.INVISIBLE);
                            } else if (endDateCalendar.before(startDateCalendar)) {
                                endDateTextView.setVisibility(View.INVISIBLE);
                                endTimeTextView.setVisibility(View.INVISIBLE);
                                dateRequirementsTextView.setText("End Date Must Be On or After Start Date.");
                                dateRequirementsTextView.setVisibility(View.VISIBLE);
                            } else {
                                String selectedDate = String.format(Locale.US, "%d/%d/%d", selectedMonth + 1, selectedDay, selectedYear);
                                endDateTextView.setText(selectedDate);
                                endDateTextView.setVisibility(View.VISIBLE);
                                dateRequirementsTextView.setVisibility(View.INVISIBLE);

                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                int minute = calendar.get(Calendar.MINUTE);
                                TimePickerDialog timePickerDialog = new TimePickerDialog(com.example.fusion0.EventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                        endDateCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                        endDateCalendar.set(Calendar.MINUTE, selectedMinute);

                                        // Check if end time is after or equal to start time
                                        if (endDateCalendar.before(startDateCalendar)) {
                                            dateRequirementsTextView.setText("End Time Must Be After Start Time.");
                                            dateRequirementsTextView.setVisibility(View.VISIBLE);
                                            endTimeTextView.setVisibility(View.INVISIBLE);
                                        } else {
                                            String selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
                                            endTimeTextView.setText(selectedTime);
                                            endTimeTextView.setVisibility(View.VISIBLE);
                                            dateRequirementsTextView.setVisibility(View.INVISIBLE);
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


        private void setDateRequirements(String message, TextView textView, boolean hideOtherTextViews) {
            dateRequirementsTextView.setText(message);
            dateRequirementsTextView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
            if (hideOtherTextViews) {
                startTimeTextView.setVisibility(View.INVISIBLE);
                endTimeTextView.setVisibility(View.INVISIBLE);
            }
        }

        private void AddEvent(){
            EventInfo eventInfo = new EventInfo(organizer, eventName,  address, facilityName, capacity, description, startDate, endDate, startTime, endTime, qrCode);
        }

        private void ExitButtonHandling() {
            exitButton.setOnClickListener(v -> finish());
        }
    }

    private void setDateRequirements(String message, TextView textView, boolean hideOtherTextViews) {
        dateRequirementsTextView.setText(message);
        dateRequirementsTextView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);
        if (hideOtherTextViews) {
            startTimeTextView.setVisibility(View.INVISIBLE);
            endTimeTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void AddEvent(){
    }

    private void ExitButtonHandling() {
        exitButton.setOnClickListener(v -> finish());
    }
}
