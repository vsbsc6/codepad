package net.chittu.codepad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;


public class CodeView extends AppCompatEditText {

    private static final int HIGHLIGHTER_COLOR = 0x88f3f315;
    private Rect mCurrentLineBounds;
    private Paint mHighlightPaint;
    private LineView mLineView;
    private int mCurrentLine = 0;

    public CodeView(Context context) {
        this(context, null);
    }

    public CodeView(Context context, AttributeSet a) {
        super(context, a);

        mCurrentLineBounds = new Rect();
        mHighlightPaint = new Paint();
        mHighlightPaint.setColor(HIGHLIGHTER_COLOR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int currentLine = getLayout().getLineForOffset(getSelectionStart());
        getLineBounds(currentLine, mCurrentLineBounds);
        canvas.drawRect(mCurrentLineBounds, mHighlightPaint);
        if(mLineView != null){
            mLineView.rewriteLineNumbers();
            if(mCurrentLine != currentLine){
                mCurrentLine = currentLine;
                mLineView.invalidate();
            }

        }
        super.onDraw(canvas);
    }

    public void setLineView(LineView lineView){
        mLineView = lineView;
        mLineView.setCodeView(this);
    }

    public Rect getCurrentLineBounds(){
            return mCurrentLineBounds;
    }

    public int getLinesCount(){
        return getLayout().getLineCount();
    }


}
