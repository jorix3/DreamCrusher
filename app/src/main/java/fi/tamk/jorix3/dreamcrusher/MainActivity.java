package fi.tamk.jorix3.dreamcrusher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    private SortedSet<Integer> selectedNumbers;
    private int threadSleepValue;
    private boolean isLottoRunning = false;
    private int lvl = 7;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyDebug.loadMyDebug(this, "MainActivity");
        selectedNumbers = new TreeSet<>();
        BroadcastReceiver broadcastListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MyDebug.loadMyDebug(context, "MainActivity:MyBroadCastListener");
                Bundle bundle = intent.getExtras();
            
                if (bundle != null) {
                    Set<String> keys = bundle.keySet();
                
                    resetButtonStyles();
                
                    for (String key : keys) {
                        if (key.equals("weeks")) {
                            TextView textView = findViewById(R.id.notifications);
                            String text = "Weeks passed: " + bundle.getInt(key);
                            textView.setText(text);
                        } else {
                            int value = bundle.getInt(key);
                            Button button = findViewById(getResources().
                                    getIdentifier("B" + value, "id",
                                            context.getPackageName()));
                        
                            button.setBackgroundResource(android.R.drawable.btn_star_big_on);
                        }
                    
                        MyDebug.print("onReceive",
                                "" + bundle.getInt(key), 4);
                    }
                }
            }
        };

        threadSleepValue = 510;
        LocalBroadcastManager.getInstance(this).
                registerReceiver(broadcastListener,
                        new IntentFilter("LottoService"));
        resetButtonStyles();
    }
    
    public void sendBroadcast() {
        String intentTag = "MainActivity";
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(intentTag);
        
        intent.putExtra("speed", threadSleepValue);
        manager.sendBroadcast(intent);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem plus = menu.findItem(R.id.plus);
        MenuItem minus = menu.findItem(R.id.minus);
        
        plus.setEnabled(true);
        minus.setEnabled(true);
        
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        switch (item.getItemId()) {
            case (R.id.plus):
                Toast.makeText(this, "+", Toast.LENGTH_SHORT).show();
                
                if (threadSleepValue > 10) {
                    threadSleepValue = threadSleepValue - 100;
                    sendBroadcast();
                }
                return true;
                
            case (R.id.minus):
                Toast.makeText(this, "-", Toast.LENGTH_SHORT).show();
                
                if (threadSleepValue < 5000) {
                    threadSleepValue = threadSleepValue + 100;
                    sendBroadcast();
                }
                return true;
                
            case (R.id.lvl7):
                lvl = 7;
                resetGame();
                return true;
    
            case (R.id.lvl6):
                lvl = 6;
                resetGame();
                return true;
    
            case (R.id.lvl5):
                lvl = 5;
                resetGame();
                return true;
        }
        
        return false;
    }
    
    public void resetGame() {
        Intent lottoIntent = new Intent(this, LottoService.class);
        Button button = findViewById(R.id.lucky_button);
        isLottoRunning = false;
        selectedNumbers = new TreeSet<>();
        threadSleepValue = 510;
    
        stopService(lottoIntent);
        button.setText("I Feel Lucky");
        
        resetButtonStyles();
        resetButtonTextColors();
    }
    
    public void resetButtonStyles() {
        for (int i = 0; i < 39; i++) {
            int value = i + 1;
            Button button = findViewById(getResources().
                    getIdentifier("B" + value, "id",
                            this.getPackageName()));
        
            button.setBackgroundResource(android.R.drawable.btn_default);
        }
    }
    
    public void resetButtonTextColors() {
        for (int i = 0; i < 39; i++) {
            int value = i + 1;
            Button button = findViewById(getResources().
                    getIdentifier("B" + value, "id",
                            this.getPackageName()));
        
            button.setTextColor(Color.BLACK);
        }
    }
    
    public void selectNumber(View v) {
        Button button = (Button) v;
        int value;
        
        try {
            value = Integer.parseInt(button.getText().toString());
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            MyDebug.print("selectNumber", "Error getting button number", 2);
            return;
        }
    
        if (selectedNumbers.contains(value)) {
            selectedNumbers.remove(value);
            button.setTextColor(Color.BLACK);
            MyDebug.print("selectNumber", "removed " + value, 2);
        } else if (selectedNumbers.size() < lvl) {
            selectedNumbers.add(value);
            button.setTextColor(Color.rgb(0, 128, 0));
            MyDebug.print("selectNumber", "added " + value, 2);
        }
    }
    
    public void iFeelLucky(View v) {
        for (int i : selectedNumbers) {
            MyDebug.print("iFeelLucky", "current numbers: " + i, 2);
        }
        
        Intent lottoIntent = new Intent(this, LottoService.class);
        Button button = findViewById(R.id.lucky_button);
        
        if (!isLottoRunning && selectedNumbers.size() == lvl) {
            int key = 0;
            lottoIntent.putExtra("speed", threadSleepValue);
            
            for (int number : selectedNumbers) {
                lottoIntent.putExtra(String.valueOf(key), number);
                key++;
            }
    
            startService(lottoIntent);
            button.setText("I Give Up");
            isLottoRunning = true;
        } else {
            stopService(lottoIntent);
            button.setText("I Feel Lucky");
            isLottoRunning = false;
        }
    }
}
