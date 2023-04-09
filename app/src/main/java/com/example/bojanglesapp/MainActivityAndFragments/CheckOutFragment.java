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

import com.example.bojanglesapp.Objects.Order;
import com.example.bojanglesapp.R;
import com.example.bojanglesapp.Objects.ShoppingCart;
import com.example.bojanglesapp.databinding.FragmentCheckOutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.RoundingMode;
import java.text.DecimalFormat;


public class CheckOutFragment extends Fragment {

    private static final String ARG_CART = "cart";
    ShoppingCart sCart;
    double total, tax, subtotal, points;
    FragmentCheckOutBinding binding;
    final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    DocumentReference docRef;
    Order order = new Order();

    public CheckOutFragment() {
        // Required empty public constructor
    }

    public static CheckOutFragment newInstance(ShoppingCart sCart) {
        CheckOutFragment fragment = new CheckOutFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CART, sCart);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Entering Checkout");

        if (getArguments() != null) {
            System.out.println("Entering Checkout pt2 - argument?");
            this.sCart = (ShoppingCart) getArguments().getSerializable(ARG_CART);
            this.total = sCart.getTotal();
            this.tax = sCart.getTax();
            this.subtotal = sCart.getSubtotal();
            this.points = sCart.getPoints();

            System.out.println("Inside CheckOutFragment - sCart: " + this.sCart);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCheckOutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.check_out_label);

        docRef = firebaseFirestore.collection("Users").document(firebaseUser.getUid());

        docRef.get().addOnCompleteListener(task -> {
            if(task.getResult().exists()){
                System.out.println("Inside docRef for assignments");

                //BIG PROBLEM:
                /*this whole docRef jazz is returning a promise, meaning that code below
                this "function" (i.e. code after ~line 118) will be executed prior to this information
                being gathered from database. This means that the button can be pressed aswell before
                finishing.
                SOLUTION 1:
                    Find Java packages to handle asynchronous code via async, await functions..
                SOLUTION 2 (easier?):
                    Only utilize order within this if statement (this is limiting). This would
                    include the button

                If you have not seen this before, it is tricky and annoying. I'm leaving the
                prints to show you how/why this is happening. Cart is here tho.
                */
                String nameResult = task.getResult().getString("displayName");
                String emailResult = task.getResult().getString("Email");
                String paymentResult = task.getResult().getString("Payment");

                binding.accountNameTextViewAccount.setText(nameResult);
                binding.textViewUserEmail.setText(emailResult);
                binding.textViewUserPayment.setText(paymentResult);

                order.setCustomerName(nameResult);
                order.setCustomerEmail(emailResult);
                order.setCustomerPayment(paymentResult);
                order.setCart(sCart);
                order.setPointsGained(points);

                System.out.println("Inside DocRef Order: " + order);
                System.out.println("Inside DocRef Order.getCart: " + order.getCart());
            } else {
                Log.d("whatever", "get failed with", task.getException());
            }

        });

        DecimalFormat dfDecimal = new DecimalFormat("0.00");
        DecimalFormat dfNoDecimal = new DecimalFormat("#");
        dfDecimal.setRoundingMode(RoundingMode.DOWN);

        binding.textViewSubtotal.setText(dfDecimal.format(subtotal));
        binding.textViewTax.setText(dfDecimal.format(tax));
        binding.textViewTotal.setText(dfDecimal.format(total));

        binding.textViewUserPoints.setText(dfNoDecimal.format(points));

        System.out.println("Order outside docRef: " + order);
        System.out.println("Order.getCart() outside docRef: " + order.getCart());

        binding.buttonCheckOut.setOnClickListener(v -> {
            order.setOrderedAt();
            cListener.placeOrder(order);
        });

    }

    CheckOutListener cListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        cListener = (CheckOutListener) context;
    }

    interface CheckOutListener {
        void placeOrder(Order order);
    }
}