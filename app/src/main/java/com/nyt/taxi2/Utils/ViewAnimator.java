package com.nyt.taxi2.Utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;

public class ViewAnimator {
    private static View view;
    private static int currentHeight;
    private static int newHeight;
    private static int currentWidth;
    private static int newWidth;

    /**
     * @param v1 View to hide
     *
     * @param v2 View to show
    */
    public static void animatePosition(View v1, final View v2) {
        if (v1 != null) {
            TranslateAnimation ta = new TranslateAnimation(0, 0, 0, v1.getHeight());
            ta.setDuration(500);
            ta.setFillAfter(true);
            ta.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override
                public void onAnimationStart(android.view.animation.Animation animation) {

                }

                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {
                    if (v2 != null) {
                        TranslateAnimation ta2 = new TranslateAnimation(0, 0, v2.getHeight(), 0);
                        ta2.setDuration(500);
                        ta2.setFillAfter(true);
                        ta2.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(android.view.animation.Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(android.view.animation.Animation animation) {

                            }

                            @Override
                            public void onAnimationRepeat(android.view.animation.Animation animation) {

                            }
                        });
                        v2.startAnimation(ta2);
                    }
                }

                @Override
                public void onAnimationRepeat(android.view.animation.Animation animation) {

                }
            });
            v1.startAnimation(ta);
        } else if (v2 != null) {
            TranslateAnimation ta2 = new TranslateAnimation(0, 0, v2.getHeight(), 0);
            ta2.setDuration(500);
            ta2.setFillAfter(true);
            ta2.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override
                public void onAnimationStart(android.view.animation.Animation animation) {

                }

                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {

                }

                @Override
                public void onAnimationRepeat(android.view.animation.Animation animation) {

                }
            });
            v2.startAnimation(ta2);
        }
    }

    public static void animateHeight(final View view, int currentHeight, int newHeight,final  ViewAnimatorEnd vanEnd) {
//        ViewAnimator.view = view;
//        ViewAnimator.currentHeight = currentHeight;
//        ViewAnimator.newHeight = newHeight;
//
//        ValueAnimator slideAnimator = ValueAnimator
//                .ofInt(currentHeight, newHeight)
//                .setDuration(500);
//
//        /* We use an update listener which listens to each tick
//         * and manually updates the height of the view  */
//
//        slideAnimator.addUpdateListener(animation1 -> {
//            Integer value = (Integer) animation1.getAnimatedValue();
//            view.getLayoutParams().height = value.intValue();
//            view.requestLayout();
//        });
//
//        /*  We use an animationSet to play the animation  */
//
//        AnimatorSet animationSet = new AnimatorSet();
//        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
//        animationSet.play(slideAnimator);
//        animationSet.start();

        ValueAnimator anim = ValueAnimator.ofInt(currentHeight, newHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = val;
                view.setLayoutParams(layoutParams);
            }
        });
        anim.addListener(new ValueAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (vanEnd != null) {
                    vanEnd.end();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(500);
        anim.start();
    }

    public static void animateWidth(final View view, int currentWidth, int newWidth, final  ViewAnimatorEnd vanEnd) {
//        ViewAnimator.view = view;
//        ViewAnimator.currentHeight = currentHeight;
//        ViewAnimator.newHeight = newHeight;
//
//        ValueAnimator slideAnimator = ValueAnimator
//                .ofInt(currentHeight, newHeight)
//                .setDuration(500);
//
//        /* We use an update listener which listens to each tick
//         * and manually updates the height of the view  */
//
//        slideAnimator.addUpdateListener(animation1 -> {
//            Integer value = (Integer) animation1.getAnimatedValue();
//            view.getLayoutParams().height = value.intValue();
//            view.requestLayout();
//        });
//
//        /*  We use an animationSet to play the animation  */
//
//        AnimatorSet animationSet = new AnimatorSet();
//        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
//        animationSet.play(slideAnimator);
//        animationSet.start();

        ValueAnimator anim = ValueAnimator.ofInt(currentWidth, newWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = val;
                view.setLayoutParams(layoutParams);
            }
        });
        anim.addListener(new ValueAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (vanEnd != null) {
                    vanEnd.end();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(500);
        anim.start();
    }

    public interface ViewAnimatorEnd {
        void end();
    }

//        ObjectAnimator animator = ObjectAnimator.ofInt(view, new HeightProperty(), currentHeight, newHeight);
//        animator.setDuration(500);
//        animator.setInterpolator(new DecelerateInterpolator());
//        animator.start();
//    }
//
//    static class HeightProperty extends Property<View, Integer> {
//
//        public HeightProperty() {
//            super(Integer.class, "height");
//        }
//
//        @Override public Integer get(View view) {
//            return view.getHeight();
//        }
//
//        @Override public void set(View view, Integer value) {
//            view.getLayoutParams().height = value;
//            view.setLayoutParams(view.getLayoutParams());
//        }
//    }

}
