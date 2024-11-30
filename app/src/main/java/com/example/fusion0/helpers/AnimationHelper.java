package com.example.fusion0.helpers;

import android.animation.ObjectAnimator;
import android.view.View;

public class AnimationHelper {

    private AnimationHelper() {
        // Private constructor to prevent instantiation
    }

    public static void fadeInView(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

    public static void fadeOutView(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(() -> view.setVisibility(View.GONE));
    }

    public static void slideInFromBottom(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .setDuration(duration)
                .setListener(null);
    }

    public static void slideOutToBottom(View view, long duration) {
        view.animate()
                .translationY(view.getHeight())
                .setDuration(duration)
                .withEndAction(() -> view.setVisibility(View.GONE));
    }

    public static void scaleInView(View view, long duration) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setListener(null);
    }

    public static void scaleOutView(View view, long duration) {
        view.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(duration)
                .withEndAction(() -> view.setVisibility(View.GONE));
    }

    public static void fadeSlideIn(View view, long duration) {
        view.setAlpha(0f);
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(duration)
                .setListener(null);
    }

    public static void rotateView(View view, float angle, long duration) {
        float currentRotation = view.getRotation();
        float newRotation = currentRotation + angle;

        // Animate to the new rotation
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", currentRotation, newRotation);
        animator.setDuration(duration);
        animator.start();
    }

}