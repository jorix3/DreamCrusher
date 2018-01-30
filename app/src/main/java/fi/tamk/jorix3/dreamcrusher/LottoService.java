package fi.tamk.jorix3.dreamcrusher;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * DreamCrusher
 *
 * @author Jyri Virtaranta jyri.virtaranta@cs.tamk.fi
 * @version 2018.01.30
 * @since 1.8
 */
public class LottoService extends Service {
    private SortedSet<Integer> selectedNumbers;
    private boolean runThread = false;
    private int threadSleepValue = 510;
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        runThread = true;
        
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            MyDebug.print("onStartCommand",
                    "bundle size: " + bundle.size(), 3);
            
            for (String key : keys) {
                if (key.equals("speed")) {
                    threadSleepValue = bundle.getInt(key);
                } else {
                    int value = bundle.getInt(key);
                    selectedNumbers.add(value);
                }
            }
        }
    
        new Thread(() -> {
            int week = 0;
        
            while (runThread) {
                SortedSet<Integer> lottoOfWeek = randomSet(7, 1, 39);
                week++;
                
                if (lottoOfWeek.containsAll(selectedNumbers)) {
                
                    MyDebug.print("calculateLotto", "WON", 4);
                    displayNotification(1,
                            "WON! it only took " + week + " weeks!");
                    runThread = false;
                } else {
                    MyDebug.print("calculateLotto",
                            "week: " + week, 4);
                }
    
                sendBroadcast(week, lottoOfWeek);
                
                try {
                    Thread.sleep(threadSleepValue);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
        return START_STICKY;
    }
    
    public void sendBroadcast(int weeks, SortedSet<Integer> lottoOfWeek) {
        String intentTag = "LottoService";
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(intentTag);
        
        intent.putExtra("weeks", weeks);
        
        int key = 0;
        for (int number : lottoOfWeek) {
            intent.putExtra(String.valueOf(key), number);
            key++;
            MyDebug.print("sendBroadcast", "number to send: " + number, 2);
        }
        
        manager.sendBroadcast(intent);
    }
    
    public void displayNotification(int id, String msg) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("DreamCrusher");
        builder.setContentText(msg);
    
        Notification notification = builder.build();
    
        NotificationManager nManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            nManager.notify(id, notification);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    private SortedSet<Integer> randomSet(int size, int min, int max) {
        SortedSet<Integer> numbers = new TreeSet<>();
        
        while (numbers.size() < size) {
            int multiplier = (max - min) + 1;
            int value = (int)(Math.random() * multiplier + min);
            
            numbers.add(value);
        }
        
        return numbers;
    }
    
    @Override
    public void onCreate() {
        MyDebug.loadMyDebug(this, "LottoService");
        selectedNumbers = new TreeSet<>();
        BroadcastReceiver broadcastListener = new BroadcastReceiver () {
            @Override
            public void onReceive(Context context, Intent intent) {
                MyDebug.loadMyDebug(context, "LottoService:MyBroadCastListener");
                Bundle bundle = intent.getExtras();
            
                if (bundle != null) {
                    Set<String> keys = bundle.keySet();
                
                    for (String key : keys) {
                        if (key.equals("speed")) {
                            threadSleepValue = bundle.getInt(key);
                        }
                    
                        MyDebug.print("onReceive", "" + bundle.getInt(key), 4);
                    }
                }
            }
        };
        
        LocalBroadcastManager.getInstance(this).
                registerReceiver(broadcastListener,
                        new IntentFilter("MainActivity"));
    }
    
    @Override
    public void onDestroy() {
        runThread = false;
    }
}
