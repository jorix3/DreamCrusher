package fi.tamk.jorix3.dreamcrusher;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * MyApplication2
 *
 * @author      Jyri Virtaranta jyri.virtaranta@cs.tamk.fi
 * @version     2018.01.08
 * @since       1.8
 */

public class MyDebug {
    private static int level = 1;
    private static boolean showInUI = false;
    private static Context host;
    private static String className;
    
    public static void loadMyDebug(Context host, String className) {
        MyDebug.className = className;
        MyDebug.host = host;
        showInUI = host.getResources().getBoolean(R.bool.show_in_ui);
        
        try {
            level = host.getResources().getInteger(R.integer.debug_level);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    public static void print(String methodName, String message, int lvl) {
        if (BuildConfig.DEBUG && lvl <= level) {
            if (showInUI) {
                Toast.makeText(host, message, Toast.LENGTH_SHORT).show();
            } else {
                Log.d(className + ": " + methodName, message);
            }
        }
    }
}
