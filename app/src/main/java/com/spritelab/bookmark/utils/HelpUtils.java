package com.spritelab.bookmark.utils;

import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class HelpUtils {
    public static boolean isBtnCoolDown = false;

    public static void btnCoolDown(int timeMseconds) {
        isBtnCoolDown = true;
        new CountDownTimer(timeMseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish() {
                isBtnCoolDown = false;
            }
        }.start();
    }

    public static void setupDropAnimation(View view, boolean btnCoolDown, Runnable onClickAction) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private boolean isPressed = false;
            private boolean isInside = true;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        isInside = true;
                        startX = event.getX();
                        startY = event.getY();
                        animateLiquidDrop(v);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (isPressed) {
                            float currentX = event.getX();
                            float currentY = event.getY();

                            isInside = currentX >= 0 && currentX <= v.getWidth() &&
                                    currentY >= 0 && currentY <= v.getHeight();

                            float deltaX = (currentX - startX) / v.getWidth();
                            float deltaY = (currentY - startY) / v.getHeight();

                            deltaX = Math.max(-0.3f, Math.min(0.3f, deltaX));
                            deltaY = Math.max(-0.3f, Math.min(0.3f, deltaY));

                            float rotationX = deltaY * 15f;
                            float rotationY = -deltaX * 15f;

                            float scaleX = 0.9f + Math.abs(deltaX) * 0.15f;
                            float scaleY = 0.95f + Math.abs(deltaY) * 0.1f;

                            scaleX = Math.max(0.85f, Math.min(1.0f, scaleX));
                            scaleY = Math.max(0.9f, Math.min(1.05f, scaleY));

                            v.setScaleX(scaleX);
                            v.setScaleY(scaleY);
                            v.setRotationX(rotationX);
                            v.setRotationY(rotationY);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (isPressed) {
                            isPressed = false;
                            if (isInside) {
                                animateReleaseWithEffect(v);
                                if (onClickAction != null) {
                                    if (isBtnCoolDown) return false;
                                    v.postDelayed(onClickAction, 200);
                                    if (btnCoolDown) btnCoolDown(3000);
                                }
                            } else {
                                animateReturnToNormal(v);
                            }
                        }
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        if (isPressed) {
                            isPressed = false;
                            animateReturnToNormal(v);
                        }
                        break;
                }
                return true;
            }
        });
    }

    public static void animateLiquidDrop(View view) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.95f)
                .rotationX(5f)
                .rotationY(3f)
                .setDuration(70)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    public static void animateReturnToNormal(View view) {
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotationX(0f)
                .rotationY(0f)
                .setDuration(150)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void animateReleaseWithEffect(View view) {
        view.animate()
                .scaleX(1.02f)
                .scaleY(1.06f)
                .rotationX(-3f)
                .rotationY(-2f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .rotationX(0f)
                                .rotationY(0f)
                                .setDuration(130)
                                .setInterpolator(new OvershootInterpolator(1.3f))
                                .start();
                    }
                })
                .start();
    }
}
