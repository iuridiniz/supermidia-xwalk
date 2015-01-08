package tv.supermidia.supermidia;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import org.xwalk.core.XWalkView;

/**
 * Created by iuri on 06/01/15.
 */
public class myXWalkView extends XWalkView {

    public myXWalkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public myXWalkView(Context context, Activity activity) {
        super(context, activity);
    }

    /*@Override
    public void pauseTimers() {

    }*/

    /*@Override
    public void resumeTimers() {

    }*/
}
