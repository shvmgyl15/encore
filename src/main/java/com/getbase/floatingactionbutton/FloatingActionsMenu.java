package com.getbase.floatingactionbutton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.fastbootmobile.encore.app.R;

public class FloatingActionsMenu extends ViewGroup {
  private static final int ANIMATION_DURATION = 300;
  private static final float COLLAPSED_PLUS_ROTATION = 0f;
  private static final float EXPANDED_PLUS_ROTATION = 90f + 45f;

  private int mAddButtonPlusColor;
  private int mAddButtonColorNormal;
  private int mAddButtonColorPressed;

  private int mButtonSpacing;

  private boolean mExpanded;

  private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
  private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
  private AddFloatingActionButton mAddButton;
  private RotatingDrawable mRotatingDrawable;

  public FloatingActionsMenu(Context context) {
    this(context, null);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attributeSet) {
    mAddButtonPlusColor = getColor(android.R.color.white);
    mAddButtonColorNormal = getColor(android.R.color.holo_blue_dark);
    mAddButtonColorPressed = getColor(android.R.color.holo_blue_light);

    mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing) - getResources().getDimension(R.dimen.fab_shadow_radius) - getResources().getDimension(R.dimen.fab_shadow_offset));

    if (attributeSet != null) {
      TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
      if (attr != null) {
        try {
          mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.color.white));
          mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
          mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.color.holo_blue_light));
        } finally {
          attr.recycle();
        }
      }
    }

    createAddButton(context);
  }

  private static class RotatingDrawable extends LayerDrawable {
    public RotatingDrawable(Drawable drawable) {
      super(new Drawable[] { drawable });
    }

    private float mRotation;

    @SuppressWarnings("UnusedDeclaration")
    public float getRotation() {
      return mRotation;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setRotation(float rotation) {
      mRotation = rotation;
      invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
      canvas.save();
      canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
      super.draw(canvas);
      canvas.restore();
    }
  }

  private void createAddButton(Context context) {
    mAddButton = new AddFloatingActionButton(context) {
      @Override
      void updateBackground() {
        mPlusColor = mAddButtonPlusColor;
        mColorNormal = mAddButtonColorNormal;
        mColorPressed = mAddButtonColorPressed;
        super.updateBackground();
      }

      @Override
      Drawable getIconDrawable() {
        final RotatingDrawable rotatingDrawable = new RotatingDrawable(super.getIconDrawable());
        mRotatingDrawable = rotatingDrawable;

        final OvershootInterpolator interpolator = new OvershootInterpolator();

        final ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", EXPANDED_PLUS_ROTATION, COLLAPSED_PLUS_ROTATION);
        final ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", COLLAPSED_PLUS_ROTATION, EXPANDED_PLUS_ROTATION);

        collapseAnimator.setInterpolator(interpolator);
        expandAnimator.setInterpolator(interpolator);

        mExpandAnimation.play(expandAnimator);
        mCollapseAnimation.play(collapseAnimator);

        return rotatingDrawable;
      }
    };

    mAddButton.setId(R.id.fab_expand_menu_button);
    mAddButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        toggle();
      }
    });

    addView(mAddButton, super.generateDefaultLayoutParams());
  }

  private int getColor(@ColorRes int id) {
    return getResources().getColor(id);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureChildren(widthMeasureSpec, heightMeasureSpec);

    int width = 0;
    int height = 0;

    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);

      width = Math.max(width, child.getMeasuredWidth());
      height += child.getMeasuredHeight();
    }

    height += mButtonSpacing * (getChildCount() - 1);
    height = height * 12 / 10; // for overshoot

    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int addButtonY = b - t - mAddButton.getMeasuredHeight();
    mAddButton.layout(0, addButtonY, mAddButton.getMeasuredWidth(), addButtonY + mAddButton.getMeasuredHeight());

    int bottomY = addButtonY - mButtonSpacing;

    for (int i = getChildCount() - 1; i >= 0; i--) {
      final View child = getChildAt(i);

      if (child == mAddButton) continue;

      int childY = bottomY - child.getMeasuredHeight();
      child.layout(0, childY, child.getMeasuredWidth(), childY + child.getMeasuredHeight());

      float collapsedTranslation = addButtonY - childY;
      float expandedTranslation = 0f;

      child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
      child.setAlpha(mExpanded ? 1f : 0f);

      LayoutParams params = (LayoutParams) child.getLayoutParams();
      params.mCollapseY.setFloatValues(expandedTranslation, collapsedTranslation);
      params.mExpandY.setFloatValues(collapsedTranslation, expandedTranslation);
      params.setAnimationsTarget(child);

      bottomY = childY - mButtonSpacing;
    }
  }

  @Override
  protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(super.generateDefaultLayoutParams());
  }

  @Override
  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(super.generateLayoutParams(attrs));
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(super.generateLayoutParams(p));
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return super.checkLayoutParams(p);
  }

  private static Interpolator sExpandInterpolator = new OvershootInterpolator();
  private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
  private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

  private class LayoutParams extends ViewGroup.LayoutParams {

    private ObjectAnimator mExpandY = new ObjectAnimator();
    private ObjectAnimator mExpandAlpha = new ObjectAnimator();
    private ObjectAnimator mCollapseY = new ObjectAnimator();
    private ObjectAnimator mCollapseAlpha = new ObjectAnimator();

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);

      mExpandY.setInterpolator(sExpandInterpolator);
      mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
      mCollapseY.setInterpolator(sCollapseInterpolator);
      mCollapseAlpha.setInterpolator(sCollapseInterpolator);

      mCollapseAlpha.setProperty(View.ALPHA);
      mCollapseAlpha.setFloatValues(1f, 0f);

      mExpandAlpha.setProperty(View.ALPHA);
      mExpandAlpha.setFloatValues(0f, 1f);

      mCollapseY.setProperty(View.TRANSLATION_Y);
      mExpandY.setProperty(View.TRANSLATION_Y);

      mExpandAnimation.play(mExpandAlpha);
      mExpandAnimation.play(mExpandY);

      mCollapseAnimation.play(mCollapseAlpha);
      mCollapseAnimation.play(mCollapseY);
    }

    public void setAnimationsTarget(View view) {
      mCollapseAlpha.setTarget(view);
      mCollapseY.setTarget(view);
      mExpandAlpha.setTarget(view);
      mExpandY.setTarget(view);
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    bringChildToFront(mAddButton);
  }

  public void collapse() {
    if (mExpanded) {
      mExpanded = false;
      mCollapseAnimation.start();
      mExpandAnimation.cancel();
    }
  }

  public void toggle() {
    if (mExpanded) {
      collapse();
    } else {
      expand();
    }
  }

  public void expand() {
    if (!mExpanded) {
      mExpanded = true;
      mCollapseAnimation.cancel();
      mExpandAnimation.start();
    }
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.mExpanded = mExpanded;

    return savedState;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      SavedState savedState = (SavedState) state;
      mExpanded = savedState.mExpanded;

      if (mRotatingDrawable != null) {
        mRotatingDrawable.setRotation(mExpanded ? EXPANDED_PLUS_ROTATION : COLLAPSED_PLUS_ROTATION);
      }

      super.onRestoreInstanceState(savedState.getSuperState());
    } else {
      super.onRestoreInstanceState(state);
    }
  }

  public static class SavedState extends BaseSavedState {
    public boolean mExpanded;

    public SavedState(Parcelable parcel) {
      super(parcel);
    }

    private SavedState(Parcel in) {
      super(in);
      mExpanded = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(mExpanded ? 1 : 0);
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

      @Override
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }
}
