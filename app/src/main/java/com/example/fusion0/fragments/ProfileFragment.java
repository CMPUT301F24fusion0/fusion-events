package com.example.fusion0.fragments;import android.annotation.SuppressLint;import android.content.Context;import android.content.Intent;import android.graphics.Color;import android.graphics.drawable.Drawable;import android.net.Uri;import android.os.Bundle;import android.provider.Settings;import android.util.Log;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.Button;import android.widget.EditText;import android.widget.ImageButton;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import androidx.annotation.NonNull;import androidx.annotation.Nullable;import androidx.appcompat.app.AlertDialog;import androidx.fragment.app.Fragment;import androidx.navigation.Navigation;import com.bumptech.glide.Glide;import com.example.fusion0.R;import com.example.fusion0.helpers.ManageImageProfile;import com.example.fusion0.helpers.ProfileManagement;import com.example.fusion0.helpers.UserFirestore;import com.example.fusion0.models.UserInfo;import com.google.android.material.floatingactionbutton.FloatingActionButton;import java.util.ArrayList;import java.util.Collections;import de.hdodenhof.circleimageview.CircleImageView;public class ProfileFragment extends Fragment {    // Variables for UI components and other classes    private TextView fullName;    private TextView emailAddress;    private TextView phoneNumber;    private EditText editFullName;    private EditText editEmailAddress;    private EditText editPhoneNumber;    // Image related fields    private CircleImageView profileImage;    private ImageView editImage;    private FloatingActionButton editButton;    private Button saveButton;    private Button cancelButton;    // State managers    private ProfileManagement profileManager;    private ManageImageProfile manageImage;    private Uri imageUri;    private ImageButton settingsButton;    private String deviceId;    // Toolbar buttons    private ImageButton homeButton;    private ImageButton cameraButton;    private ImageButton addButton;    private ImageButton favouriteButton;    /**     * Required empty constructor     * @author Nimi Akinroye     */    public ProfileFragment() {        // Required empty public constructor    }    /**     * Inflates the view     * @author Nimi Akinroye     * @param inflater The LayoutInflater object that can be used to inflate     * any views in the fragment,     * @param container If non-null, this is the parent view that the fragment's     * UI should be attached to.  The fragment should not add the view itself,     * but this can be used to generate the LayoutParams of the view.     * @param savedInstanceState If non-null, this fragment is being re-constructed     * from a previous saved state as given here.     *     * @return the inflated view     */    @Override    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {        // Inflate the layout for the fragment        return inflater.inflate(R.layout.fragment_profile, container, false);    }    /**     * Sets up the methods needed for this class     * @author Nimi Akinroye     * @param savedInstanceState If the fragment is being re-created from     * a previous saved state, this is the state.     */    @SuppressLint("HardwareIds")    @Override    public void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);                deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);                profileManager = new ProfileManagement();        manageImage = new ManageImageProfile(requireContext());    }    /**     * Set up the user's profile by obtaining the image and all othe required information.     * @author Nimi Akinroye     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.     * @param savedInstanceState If non-null, this fragment is being re-constructed     * from a previous saved state as given here.     */    @Override    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {        super.onViewCreated(view, savedInstanceState);        Context context = requireContext();        fullName = view.findViewById(R.id.fullName);        emailAddress = view.findViewById(R.id.emailAddress);        phoneNumber = view.findViewById(R.id.phoneNumber);        editFullName = view.findViewById(R.id.editFullName);        editEmailAddress = view.findViewById(R.id.editEmailAddress);        editPhoneNumber = view.findViewById(R.id.editPhoneNumber);        profileImage = view.findViewById(R.id.profileImage);        editImage = view.findViewById(R.id.editImage);        editButton = view.findViewById(R.id.editButton);        saveButton = view.findViewById(R.id.saveButton);        cancelButton = view.findViewById(R.id.cancelButton);        homeButton = view.findViewById(R.id.toolbar_home);        cameraButton = view.findViewById(R.id.toolbar_camera);        addButton = view.findViewById(R.id.toolbar_add);        favouriteButton = view.findViewById(R.id.toolbar_favourite);        settingsButton = view.findViewById(R.id.settingsButton);                profileManager.getUserData(deviceId, new ProfileManagement.UserDataCallback() {            @Override            public void onUserDataReceived(UserInfo user) {                String fullPersonName = user.getFirstName() + " " + user.getLastName();                fullName.setText(fullPersonName);                emailAddress.setText(user.getEmail());                if (user.getPhoneNumber() == null) {                    String noPhoneNumber = "No phone number";                    phoneNumber.setText(noPhoneNumber);                } else {                    phoneNumber.setText(user.getPhoneNumber());                }            }            @Override            public void onDataNotAvailable() {                Toast.makeText(context, "Data not available", Toast.LENGTH_SHORT).show();            }            @Override            public void onError(Exception e) {                Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show();            }        });        manageImage.checkImageExists(new ManageImageProfile.ImageCheckCallback() {            @Override            public void onImageExists() {                // If the image exists, retrieve and load it into the profileImage ImageView                manageImage.getImage(new ManageImageProfile.ImageRetrievedCallback() {                    @Override                    public void onImageRetrieved(Uri uri) {                        Glide.with(context)                                .load(uri)                                .into(profileImage);                    }                    @Override                    public void onFailure(Exception e) {                        Toast.makeText(context, "Error fetching message", Toast.LENGTH_SHORT).show();                    }                });            }            @Override            public void onImageDoesNotExist() {                // Deterministically generate a picture                String fullnameString = fullName.getText().toString();                Drawable deterministicImage = ManageImageProfile.generateArtFromName(context, fullnameString, 100, 100);                profileImage.setImageDrawable(deterministicImage);            }        });                editImage.setOnClickListener(v -> {            AlertDialog.Builder builder = new AlertDialog.Builder(context);            builder.setTitle("Profile Image Options");            String[] options = {"Change Image", "Delete Image"};            builder.setItems(options, (dialog, which) -> {                if (which == 0){                    selectImage();                } else if (which == 1) {                    manageImage.deleteImage(new ManageImageProfile.ImageDeleteCallback() {                        @Override                        public void onSuccess() {                            String fullnameString = fullName.getText().toString();                            Drawable deterministicImage = ManageImageProfile.generateArtFromName(context, fullnameString, 100, 100);                            profileImage.setImageDrawable(deterministicImage);                            Toast.makeText(context, "Image deleted successfully", Toast.LENGTH_SHORT).show();                        }                        @Override                        public void onFailure(Exception e) {                            Log.d("FirebaseStorage", e.toString());                            Toast.makeText(context, "Error deleting image", Toast.LENGTH_SHORT).show();                        }                    });                }            });            AlertDialog dialog = builder.create();            dialog.show();        });        editButton.setOnClickListener(v -> {            // Hide TextViews, show EditTexts for editing, and enable save/cancel buttons            fullName.setVisibility(View.GONE);            emailAddress.setVisibility(View.GONE);            phoneNumber.setVisibility(View.GONE);            editFullName.setVisibility(View.VISIBLE);            editEmailAddress.setVisibility(View.VISIBLE);            editPhoneNumber.setVisibility(View.VISIBLE);            saveButton.setVisibility(View.VISIBLE);            cancelButton.setVisibility(View.VISIBLE);            editButton.setVisibility(View.GONE);            editImage.setVisibility(View.VISIBLE);            // Set save button click behavior to update the profile            saveButton.setOnClickListener(saveView -> {                String newFullName = editFullName.getText().toString();                String newEmailAddress = editEmailAddress.getText().toString();                String newPhoneNumber = editPhoneNumber.getText().toString();                boolean isUpdated = false;                // Assuming we have a UserInfo object to work with                UserInfo currentUser = new UserInfo();                currentUser.setDeviceID(deviceId);                if (!newFullName.trim().isEmpty()) {                    String[] nameParts = newFullName.split(" ", 2);                    if (nameParts.length == 2) {                        UserFirestore.editUser(currentUser, "first name", new ArrayList<String>(Collections.singletonList(nameParts[0])));                        UserFirestore.editUser(currentUser, "last name", new ArrayList<String>(Collections.singletonList(nameParts[1])));                        fullName.setText(newFullName);                        Drawable deterministicImage = ManageImageProfile.generateArtFromName(context, newFullName, 100, 100);                        profileImage.setImageDrawable(deterministicImage);                        isUpdated = true;                    }                }                if (!newEmailAddress.trim().isEmpty()) {                    UserFirestore.editUser(currentUser, "email", new ArrayList<String>(Collections.singletonList(newEmailAddress)));                    emailAddress.setText(newEmailAddress);                    isUpdated = true;                }                if (!newPhoneNumber.trim().isEmpty()) {                    UserFirestore.editUser(currentUser, "phone number", new ArrayList<String>(Collections.singletonList(newPhoneNumber)));                    phoneNumber.setText(newPhoneNumber);                    isUpdated = true;                }                if (isUpdated) {                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show();                } else {                    Toast.makeText(context, "No changes made to the profile", Toast.LENGTH_SHORT).show();                }                toggleViewMode();            });            // Set cancel button click behavior to revert to view mode without saving            cancelButton.setOnClickListener(cancelView -> toggleViewMode());        });        initializeToolbarButtons(view);    }    /**     * Set up the toolbar     * @author Nimi Akinroye     */    private void initializeToolbarButtons(View view) {        homeButton = view.findViewById(R.id.toolbar_home);        cameraButton = view.findViewById(R.id.toolbar_qrscanner );        addButton = view.findViewById(R.id.toolbar_add);        favouriteButton = view.findViewById(R.id.toolbar_favourite);        settingsButton = view.findViewById(R.id.settingsButton);        homeButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_mainFragment));        cameraButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_qrFragment));        addButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_eventFragment));        favouriteButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_favouriteFragment));        settingsButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_settingsFragment));    }    /**     * Determine which fields are available to the user     * @author Nimi Akinroye     */    private void toggleViewMode() {        // Hide EditTexts and show TextViews        fullName.setVisibility(View.VISIBLE);        emailAddress.setVisibility(View.VISIBLE);        phoneNumber.setVisibility(View.VISIBLE);        editFullName.setVisibility(View.GONE);        editEmailAddress.setVisibility(View.GONE);        editPhoneNumber.setVisibility(View.GONE);        saveButton.setVisibility(View.GONE);        cancelButton.setVisibility(View.GONE);        editButton.setVisibility(View.VISIBLE);        editImage.setVisibility(View.GONE);    }    /**     * Starts an intent to select an image from the device gallery.     * @author Nimi Akinroye     */    private void selectImage() {        Intent intent = new Intent();        intent.setType("image/*");        intent.setAction(Intent.ACTION_GET_CONTENT);        startActivityForResult(intent, 100);    }    /**     * Handles the result of the image selection activity. Displays the selected image     * and uploads it to Firebase Storage.     * @author Nimi Akinroye     * @param requestCode The request code for the activity result.     * @param resultCode The result code indicating success or failure.     * @param data The data returned from the activity, including the image URI.     */    @Override    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {        super.onActivityResult(requestCode, resultCode, data);        if (requestCode == 100 && data != null && data.getData() != null) {            imageUri = data.getData();            profileImage.setImageURI(imageUri);            manageImage.uploadImage(imageUri, new ManageImageProfile.ImageUploadCallback() {                @Override                public void onSuccess() {                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();                }                @Override                public void onFailure(Exception e) {                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show();                }            });        }    }    }