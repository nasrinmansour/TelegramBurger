package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;

/**
 * Created by Morteza on 2016/03/19.
 */
public class VideoEditorFrameLayoutDrawer extends  SizeNotifierFrameLayoutPhoto{
    private PhotoViewerCaptionEnterView captionEditText;
    private Runnable onBackPressedRunnable;

    public VideoEditorFrameLayoutDrawer(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
    }
    public VideoEditorFrameLayoutDrawer(Context context, AttributeSet attributeSet,int styleAttr){
        super(context, attributeSet, styleAttr);
    }
    public VideoEditorFrameLayoutDrawer(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        if (heightSize > AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight) {
            heightSize = AndroidUtilities.displaySize.y - AndroidUtilities.statusBarHeight;
        }

        setMeasuredDimension(widthSize, heightSize);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (captionEditText.isPopupView(child)) {
                child.measure(View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, View.MeasureSpec.EXACTLY));
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
        }
    }

//    @Override
    protected void onLayout2(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed,l,t,r,b);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();

        int paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20) && ( captionEditText.isKeyboardVisible() || captionEditText.isPopupShowing() ) ? captionEditText.getEmojiPadding() : 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();

            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();

            int childLeft;
            int childTop;

            int gravity = lp.gravity;
            if (gravity == -1) {
                gravity = Gravity.TOP | Gravity.LEFT;
            }

            final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                    break;
                case Gravity.RIGHT:
                    childLeft = r - width - lp.rightMargin;
                    break;
                case Gravity.LEFT:
                default:
                    childLeft = lp.leftMargin;
            }

            switch (verticalGravity) {
                case Gravity.TOP:
                    childTop = lp.topMargin;
                    break;
                case Gravity.CENTER_VERTICAL:
                    childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                    break;
                case Gravity.BOTTOM:
                    childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                    break;
                default:
                    childTop = lp.topMargin;
            }

             if (child == captionEditText) {
                 childTop=getHeight()-captionEditText.getHeight();
                if (captionEditText.isPopupShowing()) {
//                    captionEditText.getEmojiView().getHeight();
//                    EmojiView.this.getMeasuredHeight()
                    childTop -= AndroidUtilities.dp(310);
                }
                if (!captionEditText.isPopupShowing() && !captionEditText.isKeyboardVisible() && captionEditText.getEmojiPadding() == 0) {
//                    childTop -= AndroidUtilities.dp(400);
                }
            }else if (captionEditText.isPopupView(child)) {
                childTop = captionEditText.getBottom();
             }
            child.layout(childLeft, childTop, childLeft + width,childTop + height);
        }

        notifyHeightChanged();
    }

    public void setCaptionEditText(PhotoViewerCaptionEnterView captionEditText, Runnable runnable) {
        this.captionEditText = captionEditText;
        this.onBackPressedRunnable=runnable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        getVideoEditorActivity().onDraw(canvas);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(onBackPressedRunnable!=null && !captionEditText.isPopupShowing()){onBackPressedRunnable.run();}
        return super.dispatchKeyEventPreIme(event);
    }
}
