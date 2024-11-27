package com.example.fusion0;

import static org.mockito.Mockito.*;

import com.example.fusion0.helpers.UserFirestore;
import com.example.fusion0.models.UserInfo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;

public class UserFirebaseTest {
    private CollectionReference mockedCollectionRef;
    private DocumentReference mockedDocRef;
    private UserFirestore userFirestore;
    private UserInfo userInfo;

    /**
     * Setup the required mocks
     * @author Sehej Brar
     */
    @Before
    public void initialize() {
        FirebaseFirestore mockedFirestore = mock(FirebaseFirestore.class);
        mockedCollectionRef = mock(CollectionReference.class);
        mockedDocRef = mock(DocumentReference.class);
        userInfo = mock(UserInfo.class);

        // Mock Firestore behavior
        when(mockedFirestore.collection("users")).thenReturn(mockedCollectionRef);
        when(mockedCollectionRef.document(anyString())).thenReturn(mockedDocRef);
        when(userInfo.getDeviceID()).thenReturn("testDeviceID");
        when(userInfo.user()).thenReturn(new HashMap<>());

        // Initialize UserFirestore with mocked Firestore
        userFirestore = new UserFirestore(mockedFirestore);
    }

    /**
     * Add the user using the userFirestore.addUser() method
     * @author Sehej Brar
     */
    @Test
    public void testAddUser() {
        Task<Void> mockTask = mock(Task.class); // returned by .set

        when(mockedDocRef.set(any(HashMap.class))).thenReturn(mockTask);

        // Lambda function to change the default listener to our own listener
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class))).thenAnswer(invocation -> {
            // Return mockTask which tells if it was a success or not
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockTask;
        });

        // now we can call our method
        Runnable runnable = mock(Runnable.class);
        userFirestore.addUser(userInfo, runnable);

        // verify that it worked by checking that there exists a document with our id
        verify(mockedCollectionRef).document("testDeviceID");
        verify(mockedDocRef).set(any(HashMap.class));
        verify(runnable).run();
    }

}
