package com.wesaphzt.privatelock;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.color.DynamicColors;
import com.wesaphzt.privatelock.animation.Circle;
import com.wesaphzt.privatelock.animation.CircleAngleAnimation;
import com.wesaphzt.privatelock.fragments.FragmentAbout;
import com.wesaphzt.privatelock.fragments.FragmentDonate;
import com.wesaphzt.privatelock.fragments.FragmentSettings;
import com.wesaphzt.privatelock.receivers.DeviceAdminReceiver;
import com.wesaphzt.privatelock.service.LockService;
import com.wesaphzt.privatelock.widget.LockWidgetProvider;

import java.util.Locale;

import static com.wesaphzt.privatelock.service.LockService.CHANNEL_ID;
import static com.wesaphzt.privatelock.service.LockService.DEFAULT_SENSITIVITY;
import static com.wesaphzt.privatelock.service.LockService.activeListener;
import static com.wesaphzt.privatelock.service.LockService.disabled;

public class MainActivity extends AppCompatActivity {

    private Context context;

    private int mSensitivity;
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private static SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private SensorEventListener mActiveListener;

    //DevicePolicyManager
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    private TextView tvSensitivityActualValue;

    private SharedPreferences prefs;
    public static final String PREFS_THRESHOLD = "THRESHOLD";

    private CountDownTimer cdTimer;
    private final int cdTimerLength = 1500;
    private boolean isRunning = false;
    private boolean isHit = false;

    //stats
    private TextView tvLastBreachValue;
    private TextView tvAvgBreachValue;
    private TextView tvHighestBreachValue;

    private int triggerCount = 0;
    private float avgBreachValueTotal = 0;
    private double highestBreach = 0;

    private Circle circle;
    private Circle circle_bg;
    //circle color
    private final int circleDefaultR = 88; private final int circleDefaultG = 186; private final int circleDefaultB = 255;
    int animationDuration = 220;

    CircleAngleAnimation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        getFirstRun();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. This solution sets only the
            // bottom, left, and right dimensions, but you can apply whichever insets are
            // appropriate to your layout. You can also update the view padding if that's
            // more appropriate.
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.topMargin = insets.top;
            mlp.rightMargin = insets.right;
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.last_card), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as a margin to the view. This solution sets only the
            // bottom, left, and right dimensions, but you can apply whichever insets are
            // appropriate to your layout. You can also update the view padding if that's
            // more appropriate.
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.bottomMargin = insets.bottom;
            v.setLayoutParams(mlp);

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            return WindowInsetsCompat.CONSUMED;
        });


        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        //toggle back arrow on back stack change
                        if (getSupportActionBar() != null) {
                            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            } else {
                                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                                //set title
                                MainActivity.this.setTitle(R.string.app_name);
                            }
                        }
                    }
                });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getSupportFragmentManager().getBackStackEntryCount() > 0){
                    getSupportFragmentManager().popBackStack();
                }
            }
        });

        //animation
        circle = findViewById(R.id.circle);
        circle_bg = findViewById(R.id.circle_bg);

        final RelativeLayout relativeLayout = findViewById(R.id.content_main);
        final RelativeLayout rlCircle = findViewById(R.id.rlCircle);

        //scale height/width of animation
        relativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        //only want to do this once
                        relativeLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        circle.setRect((circle.getHeight() / 2), (circle.getHeight()) / 2);
                        circle_bg.setRect((circle_bg.getHeight() / 2), (circle_bg.getHeight()) / 2);

                        circle.setX((rlCircle.getWidth() - circle.getRect()) / 2);
                        circle_bg.setX((rlCircle.getWidth() - circle.getRect()) / 2);
                    }
                });


        circle.setColor(circleDefaultR, circleDefaultG, circleDefaultB);
        //set background circle
        CircleAngleAnimation animation = new CircleAngleAnimation(circle_bg, 360);
        //initial animation
        animation.setDuration(500);
        //circle bg color
        int circleBgR = 240;
        int circleBgG = 240;
        int circleBgB = 240;
        circle_bg.setColor(circleBgR, circleBgG, circleBgB);

        circle_bg.startAnimation(animation);

        //stats
        tvLastBreachValue = findViewById(R.id.tvLastBreachValue);
        tvAvgBreachValue = findViewById(R.id.tvAvgBreachValue);
        tvHighestBreachValue = findViewById(R.id.tvHighestBreachValue);

        //shared prefs
        try {
            mSensitivity = prefs.getInt(PREFS_THRESHOLD, DEFAULT_SENSITIVITY);
        } catch (Exception e) {
            Toast.makeText(context, "Unable to retrieve threshold", Toast.LENGTH_LONG).show();
        }

        //timer when lock hit
        cdTimer = new CountDownTimer(cdTimerLength, 1000) {

            public void onTick(long millisUntilFinished) {
                isRunning = true;
                isHit = true;
            }
            public void onFinish() {
                isRunning = false;
                isHit = false;
                circle.setColor(circleDefaultR, circleDefaultG, circleDefaultB);
                //reset stat variables
                triggerCount = 0;
                avgBreachValueTotal = 0;
                highestBreach = 0;
            }
        };

        //prevent lock animation artifacts
        mInitialized = false;

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //sensor listener
        setSensorListener();
        mSensorManager.registerListener(mActiveListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //dpm
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(this, DeviceAdminReceiver.class);

        tvSensitivityActualValue = findViewById(R.id.tvSensitivityActualValue);
        tvSensitivityActualValue.setText(getString(R.string.sensitivity_value, Integer.toString(mSensitivity)));

        //seek bar
        SeekBar sbSensitivity = findViewById(R.id.sbSensitivity);
        sbSensitivity.setProgress(mSensitivity);
        sbSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSensitivity = progress;
                tvSensitivityActualValue.setText(getString(R.string.sensitivity_value, Integer.toString(mSensitivity)));

                //submit to shared prefs
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PREFS_THRESHOLD, progress).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {  }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {  }
        });
    }

    private void startLockService(Intent intent) {
        //check android api
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            minimizeApp();
        } else {
            context.startService(intent);
            minimizeApp();
        }
    }

    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void requestDeviceAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //launch service if permission granted
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if(resultCode == Activity.RESULT_OK) {
                startServicePrep();
            }
        }
    }

    //determine if we are an active admin
    private boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdmin);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mActiveListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //recreate menu to set start/stop again
        invalidateOptionsMenu();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mActiveListener);
    }

    private void setSensorListener() {
        mActiveListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

            @Override
            public void onSensorChanged(SensorEvent event) {
                sensorCalc(event);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate actionbar menu
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    private void startServicePrep() {
        //stop this listener
        mSensorManager.unregisterListener(mActiveListener);

        //start service intent
        Intent startIntent  = new Intent(context, LockService.class);
        startIntent.setAction(LockService.ACTION_START_FOREGROUND_SERVICE);

        startLockService(startIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.action_start) {
            if (isActiveAdmin()) {
                startServicePrep();
            } else {
                requestDeviceAdmin();
            }
        } else if (id == R.id.action_stop) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.deleteNotificationChannel(CHANNEL_ID);
                } catch (java.lang.SecurityException e) {
                    // to be expected tbh
                }

                disabled = true;
                mSensorManager.unregisterListener(activeListener);

                LockWidgetProvider lockWidgetProvider = new LockWidgetProvider();
                lockWidgetProvider.setWidgetStop(context);

                invalidateOptionsMenu();
            } else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(LockService.NOTIFICATION_ID);

                disabled = true;
                mSensorManager.unregisterListener(activeListener);

                LockWidgetProvider lockWidgetProvider = new LockWidgetProvider();
                lockWidgetProvider.setWidgetStop(context);
            }
        } else if (id == R.id.action_settings) {
            fragment = new FragmentSettings();
        } else if (id == R.id.action_donate) {
            fragment = new FragmentDonate();
        } else if (id == R.id.action_show_intro) {
            Intent myIntent = new Intent(this, IntroActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.action_about) {
            fragment = new FragmentAbout();
        }

        //add fragment
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
            fragmentTransaction.replace(R.id.content_main, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem action_start = menu.findItem(R.id.action_start);
        MenuItem action_stop = menu.findItem(R.id.action_stop);

        setMenuStatus(action_start, action_stop);

        return true;
    }

    public void setMenuStatus(MenuItem action_start, MenuItem action_stop) {
        if (disabled) {
            action_start.setEnabled(true);
            action_stop.setEnabled(false);
        } else {
            //enabled
            action_start.setEnabled(false);
            action_stop.setEnabled(true);
        }
    }

    private void getFirstRun() {
        //get current version code
        int DOESNT_EXIST = -1;
        int currentVersionCode = DOESNT_EXIST;//BuildConfig.VERSION_CODE;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //get saved version code
        int savedVersionCode = DOESNT_EXIST;
        //first run
        String PREF_VERSION_CODE_KEY = "VERSION_CODE";
        try {
            savedVersionCode = this.prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //check first run
        //noinspection StatementWithEmptyBody
        if (currentVersionCode == savedVersionCode) {
            //normal run
        } else if (savedVersionCode == DOESNT_EXIST) {
            //first run
            Intent myIntent = new Intent(this, IntroActivity.class);
            this.startActivity(myIntent);
        }
        //update shared prefs with current version code
        this.prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    private void sensorCalc(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;

            mInitialized = true;
        } else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);

            float NOISE = (float) 2.0;
            if (deltaX < NOISE) deltaX = (float) 0.0;
            if (deltaY < NOISE) deltaY = (float) 0.0;
            if (deltaZ < NOISE) deltaZ = (float) 0.0;

            mLastX = x;
            mLastY = y;
            mLastZ = z;

            float total = (float) Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ));

            int calculatedAngleInt = 0;

            if (total >= mSensitivity) {
                //lock screen threshold hit

                stats(total);

                if(!isHit) {
                    CircleAngleAnimation anim = new CircleAngleAnimation(circle, 360);
                    //circle lock color
                    int circleLockR = 88;
                    int circleLockG = 255;
                    int circleLockB = 135;
                    circle.setColor(circleLockR, circleLockG, circleLockB);
                    anim.setDuration(animationDuration);
                    //set lock color
                    circle.startAnimation(anim);

                    isHit = true;
                    cdTimer.start();
                } else {
                    if(!isRunning) {
                        isRunning = true;
                    }
                }
            } else {
                if(isRunning)
                    return;

                if(isHit)
                    return;

                calculatedAngleInt = Math.round((total / mSensitivity) * 360);

                animation = new CircleAngleAnimation(circle, calculatedAngleInt);
                animation.setDuration(animationDuration);
                circle.startAnimation(animation);
            }
        }
    }

    private void stats(float total) {
        tvLastBreachValue.setText(String.format(Locale.ENGLISH, "%.1f", (double) total));

        if(total == 0 || total > highestBreach) {
            highestBreach = total;
        }
        tvHighestBreachValue.setText(String.format(Locale.ENGLISH, "%.1f", highestBreach));

        triggerCount += 1;
        avgBreachValueTotal += total;
        tvAvgBreachValue.setText(String.format(Locale.ENGLISH, "%.1f", avgBreachValueTotal / triggerCount));
    }
}
