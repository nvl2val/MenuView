package com.ubux.quadbubblemenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.widget.ImageView;


class CircleImageView extends ImageView {

    private static final String TAG = CircleImageView.class.getSimpleName();

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleImageView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int radiusMeasureSpec = widthMeasureSpec;
        super.onMeasure(radiusMeasureSpec, radiusMeasureSpec);
        int radiusMeasureSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(radiusMeasureSize, radiusMeasureSize);
    }

    @Override
    public void draw(Canvas viewCanvas) {
        final int EDGE_SIZE = viewCanvas.getWidth();

        // Draw this View's things.
        Bitmap fgBm = Bitmap.createBitmap(EDGE_SIZE, EDGE_SIZE, Bitmap.Config.ARGB_8888);
        Canvas fgCanvas = new Canvas(fgBm);
        super.draw(fgCanvas);

        // Transfer to a special shape.
        Bitmap shapedBm = Bitmap.createBitmap(EDGE_SIZE, EDGE_SIZE, Bitmap.Config.ARGB_8888);
        Canvas shapedCanvas = new Canvas(shapedBm);
        shapedCanvas.drawCircle(EDGE_SIZE/2, EDGE_SIZE/2, EDGE_SIZE/2, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        shapedCanvas.drawBitmap(fgBm, 0, 0, mPaint);
        mPaint.setXfermode(null);

        // Move drawn things to View's canvas.
        viewCanvas.drawBitmap(shapedBm, 0, 0, mPaint);
        fgBm.recycle();
        shapedBm.recycle();
    }
}
