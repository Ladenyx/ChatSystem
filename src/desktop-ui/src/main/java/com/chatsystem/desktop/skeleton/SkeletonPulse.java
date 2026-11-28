package com.chatsystem.desktop.skeleton;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.util.Duration;

/** Manages a subtle opacity pulse on skeleton containers while honoring {@code reducedMotion} and scene lifecycle. */
public final class SkeletonPulse implements AutoCloseable {
    private static final Duration PULSE_DURATION = Duration.millis(1100);
    private static final double MIN_OPACITY = 0.55;

    private final Node target;
    private final ObservableBooleanValue reducedMotion;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private FadeTransition transition;

    private final ChangeListener<Scene> sceneListener = (obs, oldScene, newScene) -> refreshAnimation();
    private final ChangeListener<Boolean> visibleListener = (obs, wasVisible, isVisible) -> refreshAnimation();
    private final ChangeListener<Boolean> motionListener = (obs, wasReduced, isReduced) -> refreshAnimation();
    private final WeakChangeListener<Boolean> weakMotionListener = new WeakChangeListener<>(motionListener);

    private SkeletonPulse(Node target, ObservableBooleanValue reducedMotion) {
        this.target = Objects.requireNonNull(target, "target");
        this.reducedMotion = reducedMotion;

        target.sceneProperty().addListener(sceneListener);
        target.visibleProperty().addListener(visibleListener);
        if (reducedMotion != null) {
            reducedMotion.addListener(weakMotionListener);
        }
        refreshAnimation();
    }

    public static SkeletonPulse attach(Node target, ObservableBooleanValue reducedMotion) {
        return new SkeletonPulse(target, reducedMotion);
    }

    private void refreshAnimation() {
        if (closed.get()) {
            stopAndResetOpacity();
            return;
        }
        boolean motionDisabled = reducedMotion != null && reducedMotion.get();
        boolean activeOnScreen = target.getScene() != null && target.isVisible() && !motionDisabled;
        if (activeOnScreen) {
            if (transition == null) {
                transition = createTransition();
            }
            if (transition.getStatus() != Animation.Status.RUNNING) {
                transition.playFromStart();
            }
        } else {
            stopAndResetOpacity();
        }
    }

    private FadeTransition createTransition() {
        FadeTransition fade = new FadeTransition(PULSE_DURATION, target);
        fade.setFromValue(1.0);
        fade.setToValue(MIN_OPACITY);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);
        return fade;
    }

    private void stopAndResetOpacity() {
        if (transition != null) {
            transition.stop();
            transition = null;
        }
        target.setOpacity(1.0);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        target.sceneProperty().removeListener(sceneListener);
        target.visibleProperty().removeListener(visibleListener);
        if (reducedMotion != null) {
            reducedMotion.removeListener(weakMotionListener);
        }
        stopAndResetOpacity();
    }
}
