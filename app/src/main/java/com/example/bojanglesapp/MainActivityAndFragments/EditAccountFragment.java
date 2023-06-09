// Bojangles Application
// BoBerry Biscuits - Group 16
// ITCS 6112 - 051
// Stephanie Karp, Wes Wotring, Jason Ellerbeck

package com.example.bojanglesapp.MainActivityAndFragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bojanglesapp.R;
import com.example.bojanglesapp.databinding.FragmentEditAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditAccountFragment extends Fragment {
    FragmentEditAccountBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(R.string.edit_account_label);

        binding.buttonSubmitEdit.setOnClickListener(v -> {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();

            String email = binding.editTextAccountEmail.getText().toString();
            String password = binding.editTextAccountPassword.getText().toString();
            String payment = binding.editTextAccountPayment.getText().toString();

            // if nothing is entered, go to view account
            if (email.isEmpty() && password.isEmpty() && payment.isEmpty()) {
                Log.d("demo", "no info entered");
                mListener.goToViewAccount();
            } else {
                // if new email is entered, update it
                if (email.isEmpty()) {
                    Log.d("demo", "no email entered");
                } else {
                    firebaseUser.updateEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("demo", "User email address updated.");
                                } else {
                                    Log.d("demo", "User email NOT updated.");
                                }
                            });
                }

                //if new password is entered, update it
                if (password.isEmpty()) {
                    Log.d("demo", "no password entered");
                } else {
                    firebaseUser.updatePassword(password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d("demo", "User password updated.");
                                } else {
                                    Log.d("demo", "User password NOT updated.");
                                }
                            });
                }

                // if new payment is entered, update it
                if (payment.isEmpty()) {
                    Log.d("demo", "no payment entered");
                } else {
                    DocumentReference userRef = firebaseFirestore.collection("Users").document(firebaseUser.getUid());
                    userRef
                            .update("Payment", payment)
                            .addOnSuccessListener(unused -> Log.d("demo", "User payment updated."))
                            .addOnFailureListener(e -> Log.d("demo", "Error updating payment.", e));
                }
            }
            // after updating the user, go to view account
            mListener.goToViewAccount();
        });
        binding.buttonLogout.setOnClickListener(v -> mListener.logout());
    }

    EditAccountFragment.EditAccountListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (EditAccountListener) context;
    }
    interface EditAccountListener {
        void goToViewAccount();
        void logout();
    }
}