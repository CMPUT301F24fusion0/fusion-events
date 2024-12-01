package com.example.fusion0.helpers;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Provides animation on View objects
 * @author Nimi Akinroye
 */
public class AnimationHelper {

    /**
     * Private constructor to prevent instantiation
     * @author Nimi Akinroye
     */
    private AnimationHelper() {
    }

    /**
     * Fades a view in
     * @author Nimi Akinroye
     * @param view the view
     * @param duration how long of the animation
     */
    public static void fadeInView(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

    /**
     * Fades a view out
     * @author Nimi Akinroye
     * @param view view for animation
     * @param duration duration for animation
     */
    public static void fadeOutView(View view, long duration) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(() -> view.setVisibility(View.GONE));
    }

    /**
     * Slides a view from the bottom
     * @author Nimi Akinroye
     * @param view view for animation
     * @param duration duration for animation
     */
    public static void slideInFromBottom(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .setDuration(duration)
                .setListener(null);
    }

    /**
     * Slide view out to the bottom
     * @author Nimi Akinroye
     * @param view view for animation
     * @param duration duration for animation
     */
    public static void slideOutToBottom(View view, long duration) {
        view.animate()
                .translationY(view.getHeight())
                .setDuration(duration)
                .withEndAction(() -> view.setVisibility(View.GONE));
    }

    /**
     * Scales a view into its actual size
     * @author Nimi Akinroye
     * @param view view for animation
     * @param duration duration for animation
     */
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

    /**
     * Scales a view out into no size
     * @author Nimi Akinroye
     * @param view view for animation
     * @param duration duration for animation
     */
    public static void scaleOutView(View view, long duration) {
        view.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(duration)
                .withEndAction(() -> view.setVisibility(View.GONE));
    }

    /**
     * Fades a view in and slides it in
     * @author Nimi Akinroye
     * @param view view for animation
     * @param duration duration for animation
     */
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

    /**
     * Rotates the view
     * @author Nimi Akinroye
     * @param view view for animation
     * @param duration duration for animation
     * @param angle angle to rotate by
     */
    public static void rotateView(View view, float angle, long duration) {
        float currentRotation = view.getRotation();
        float newRotation = currentRotation + angle;

        // Animate to the new rotation
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", currentRotation, newRotation);
        animator.setDuration(duration);
        animator.start();
    }

}