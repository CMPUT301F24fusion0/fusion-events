package com.example.fusion0;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.fusion0.helpers.ManageImageProfile;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test class for the ManageImageProfile class.
 * This class tests the image upload, deletion, existence check,
 * and retrieval functionality in Firebase Storage.
 * @author Nimi Akinroye
 */
@RunWith(AndroidJUnit4.class)
public class ManageImageProfileTest {

    private ManageImageProfile manageImageProfile;
    private StorageReference storageReference;
    private Uri testImageUri;

    /**
     * Sets up the test environment before each test method.
     * Initializes the ManageImageProfile instance and a test image URI.
     */
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        manageImageProfile = new ManageImageProfile(context);
        storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_images/test_image.jpg");

        // Create a sample image URI for testing
        String imageUriString = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888),
                "testImage",
                "testDescription"
        );

        if (imageUriString == null) {
            fail("Failed to create test image: insertImage returned null");
        } else {
            testImageUri = Uri.parse(imageUriString);
        }
    }

    /**
     * Tests the successful upload of an image to Firebase Storage.
     * Verifies that the image can be retrieved after uploading.
     */
    @Test
    public void testUploadImage_Success() {
        manageImageProfile.uploadImage(testImageUri, new ManageImageProfile.ImageUploadCallback() {
            @Override
            public void onSuccess() {
                storageReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> assertTrue("Image upload succeeded", true))
                        .addOnFailureListener(e -> fail("Image not found in Firebase: " + e.getMessage()));
            }

            @Override
            public void onFailure(Exception e) {
                fail("Image upload failed: " + e.getMessage());
            }
        });
    }

    /**
     * Tests the successful deletion of an image from Firebase Storage.
     * Verifies that the image cannot be found after deletion.
     */
    @Test
    public void testDeleteImage_Success() {
        manageImageProfile.uploadImage(testImageUri, new ManageImageProfile.ImageUploadCallback() {
            @Override
            public void onSuccess() {
                manageImageProfile.deleteImage(new ManageImageProfile.ImageDeleteCallback() {
                    @Override
                    public void onSuccess() {
                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(uri -> fail("Image still exists after delete"))
                                .addOnFailureListener(e -> assertTrue("Image successfully deleted", true));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Image delete failed: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Initial upload failed: " + e.getMessage());
            }
        });
    }

    /**
     * Tests the existence check for an uploaded image in Firebase Storage.
     * Verifies that the image is found after uploading.
     */
    @Test
    public void testCheckImageExists() {
        manageImageProfile.uploadImage(testImageUri, new ManageImageProfile.ImageUploadCallback() {
            @Override
            public void onSuccess() {
                manageImageProfile.checkImageExists(new ManageImageProfile.ImageCheckCallback() {
                    @Override
                    public void onImageExists() {
                        assertTrue("Image exists in Firebase Storage", true);
                    }

                    @Override
                    public void onImageDoesNotExist() {
                        fail("Image does not exist in Firebase Storage");
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Image upload failed: " + e.getMessage());
            }
        });
    }

    /**
     * Tests the retrieval of an uploaded image URI from Firebase Storage.
     * Verifies that the retrieved URI is not null.
     */
    @Test
    public void testGetImage_Success() {
        manageImageProfile.uploadImage(testImageUri, new ManageImageProfile.ImageUploadCallback() {
            @Override
            public void onSuccess() {
                manageImageProfile.getImage(new ManageImageProfile.ImageRetrievedCallback() {
                    @Override
                    public void onImageRetrieved(Uri uri) {
                        assertNotNull("Image URI retrieved successfully", uri);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        fail("Failed to retrieve image URI: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                fail("Image upload failed: " + e.getMessage());
            }
        });
    }

    /**
     * Tests the failure of uploading an image with an invalid URI.
     * Verifies that the upload fails as expected.
     */
    @Test
    public void testUploadImage_Failure() {
        Uri invalidURI = Uri.parse("");

        manageImageProfile.uploadImage(invalidURI, new ManageImageProfile.ImageUploadCallback() {
            @Override
            public void onSuccess() {
                fail("Upload should not succeed with an invalid URI");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue("Image upload failed as expected with invalid URI", true);
            }
        });
    }

    /**
     * Tests the failure of deleting a non-existent image from Firebase Storage.
     * Verifies that the delete operation fails as expected.
     */
    @Test
    public void testDeleteImage_Failure() {
        manageImageProfile.deleteImage(new ManageImageProfile.ImageDeleteCallback() {
            @Override
            public void onSuccess() {
                fail("Delete should not succeed for a non-existent image");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue("Image delete failed as expected for a non-existent image", true);
            }
        });
    }

    /**
     * Tests checking the non-existence of an image in Firebase Storage.
     * Verifies that the image does not exist.
     */
    @Test
    public void testCheckImageDoesNotExist() {
        manageImageProfile.checkImageExists(new ManageImageProfile.ImageCheckCallback() {
            @Override
            public void onImageExists() {
                fail("Image check should not indicate existence for a non-existent image");
            }

            @Override
            public void onImageDoesNotExist() {
                assertTrue("Correctly identified that the image does not exist", true);
            }
        });
    }

    /**
     * Tests the failure of retrieving a non-existent image URI from Firebase Storage.
     * Verifies that the retrieval operation fails as expected.
     */
    @Test
    public void testGetImage_Failure() {
        manageImageProfile.getImage(new ManageImageProfile.ImageRetrievedCallback() {
            @Override
            public void onImageRetrieved(Uri uri) {
                fail("Image retrieval should not succeed for a non-existent image");
            }

            @Override
            public void onFailure(Exception e) {
                assertTrue("Failed to retrieve image as expected for a non-existent image", true);
            }
        });
    }

    /**
     * Cleans up by deleting the test image from Firebase Storage after each test.
     */
    @After
    public void tearDown() {
        if (storageReference != null) {
            storageReference.delete()
                    .addOnSuccessListener(aVoid -> Log.d("ManageImageProfileTest", "Test image deleted successfully"))
                    .addOnFailureListener(e -> Log.e("ManageImageProfileTest", "Failed to delete test image: " + e.getMessage()));
        }
    }
}