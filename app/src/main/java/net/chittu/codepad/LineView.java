package net.chittu.codepad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;


public class LineView extends AppCompatEditText {
    private static final int HIGHLIGHTER_COLOR = 0x88cccccc;
    private Paint mHighlightPaint;
    private CodeView mCodeView;
    private int mLinesCount = 1;

    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, AttributeSet a) {
        super(context, a);
        mHighlightPaint = new Paint();
        mHighlightPaint.setColor(HIGHLIGHTER_COLOR);
        setText("1 ");
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(mCodeView != null)
            canvas.drawRect(mCodeView.getCurrentLineBounds(), mHighlightPaint);
        super.onDraw(canvas);
    }

    public void setCodeView(CodeView codeView){
        mCodeView = codeView;
    }

    public void rewriteLineNumbers(){
        int currentTotalLines = mCodeView.getLinesCount();

        if(currentTotalLines > mLinesCount) {
            for (int i = mLinesCount + 1; i <= currentTotalLines; i++) {
                append("\n" + i);
                append(" ");
            }
            mLinesCount = currentTotalLines;
        }
        else if(currentTotalLines < mLinesCount) {
            getText().delete(getLayout().getLineStart(currentTotalLines) - 1, getLayout().getLineEnd(mLinesCount - 1));
            mLinesCount = currentTotalLines;
        }
    }
}
