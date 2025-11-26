package ai.tech5.pheonix.capture.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ai.tech5.ui.R;


public class GraphicOverlay extends View {

    private final Paint m_borderPaint;
    private final Paint m_transparentViewPaint;

    private int cameraPosition = -1;


    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        m_borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_borderPaint.setStyle(Paint.Style.STROKE);
        m_borderPaint.setStrokeWidth(15);
        m_borderPaint.setColor(context.getResources().getColor(R.color.border_color_fail));


        m_transparentViewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        m_transparentViewPaint.setStyle(Paint.Style.FILL);
        m_transparentViewPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


    }


    public void init(int cameraPos) {

        cameraPosition = cameraPos;

    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (cameraPosition == -1) {
            return;
        }


        int xc = this.getWidth() / 2;
        int yc = this.getHeight() / 2;


        canvas.drawColor(getResources().getColor(R.color.overlay_color));
        if (cameraPosition == 0) {
            canvas.drawCircle(xc, yc, this.getWidth() * 0.44f, m_transparentViewPaint);
            canvas.drawCircle(xc, yc, this.getWidth() * 0.44f, m_borderPaint);
        } else {
            canvas.drawCircle(xc, yc, this.getWidth() * 0.35f, m_transparentViewPaint);
            canvas.drawCircle(xc, yc, this.getWidth() * 0.35f, m_borderPaint);
        }


    }

    public void drawBorder(int color) {
        m_borderPaint.setColor(color);

        invalidate();
    }
}
