/**
 * Created by nhancao on 10/21/16.
 */
package com.nhancv.appmanager;

import android.app.ActivityManager.TaskDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.nhancv.appmanager.models.AppResources;

import java.util.ArrayList;
import java.util.Collection;

import timber.log.Timber;

public abstract class BaseActivity extends AppCompatActivity {
    public static final String ACTION_REQUEST_PERMISSION = "action_request_permission";
    public static final String EXTRA_PERMISSIONS = "extra_permissions";
    private static final int REQ_PERMISSIONS = 1893;
    public final BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received intent: %s", intent);
            if (intent == null) {
                return;
            }
            final String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            switch (action) {
                case ACTION_REQUEST_PERMISSION: {
                    final ArrayList<String> permissions = intent.getStringArrayListExtra(EXTRA_PERMISSIONS);
                    if (permissions != null && permissions.size() > 0) {
                        requestPermissions(permissions);
                    }
                    break;
                }
            }
        }
    };
    public IntentFilter mLocalIntentFilter;
    protected NavigationView mNavigationView;
    protected MenuItem mPreviousMenuItem;

    public static boolean isGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    protected void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ViewCompat.setElevation(toolbar, 4.0f);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final AppResources res = AppResources.get(this);
            // WTF! IRIS506Q android version "unknown"
            try {
                getWindow().setStatusBarColor(res.getPrimaryColor());
            } catch (Exception e) {
                Log.e("BaseActivity", "get a stone and throw it at your device vendor", e);
            }

            // color recents tab
            final Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            final TaskDescription description = new TaskDescription(String.valueOf(getTitle()), appIcon, res.getAccentColor());
            setTaskDescription(description);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver, createIntentFilter());
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Searches and checks the {@link MenuItem} if found
     **/
    public void checkMenuItem(final int menuId) {
        final MenuItem menuItem = findMenuItem(menuId);
        if (menuItem != null) {
            checkMenuItem(menuItem);
        }
    }

    /**
     * Checks the {@link MenuItem}
     **/
    public void checkMenuItem(@NonNull final MenuItem menuItem) {
        // to work around a bug in the design library we need to store the menu item and
        // uncheck it manually everytime we check a new item
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        menuItem.setChecked(true);
        mPreviousMenuItem = menuItem;

        if (mNavigationView != null) {
            mNavigationView.invalidate();
        }
    }

    @Nullable
    public MenuItem findMenuItem(final int menuId) {
        return findMenuItem(mNavigationView, menuId);
    }

    @Nullable
    public MenuItem findMenuItem(@Nullable NavigationView navigationView, final int menuId) {
        if (navigationView == null || navigationView.getMenu() == null) {
            return null;
        }
        return navigationView.getMenu().findItem(menuId);
    }

    public boolean isGranted(String permission) {
        return isGranted(this, permission);
    }

    public void requestPermissions(Collection<String> permissions) {
        final String[] permissionsToRequest = permissions.toArray(new String[permissions.size()]);
        ActivityCompat.requestPermissions(this, permissionsToRequest, REQ_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSIONS: {
                boolean grantedAll = true;
                if (grantResults.length > 0) {
                    for (final int result : grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            grantedAll = false;
                        }
                    }
                } else {
                    grantedAll = false;
                }
                Timber.d("Granted all permissions: %s", grantedAll);
                // TODO: show warning?
                return;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public IntentFilter createIntentFilter() {
        if (mLocalIntentFilter == null) {
            mLocalIntentFilter = new IntentFilter();
            mLocalIntentFilter.addAction(BaseActivity.ACTION_REQUEST_PERMISSION);
        }
        return mLocalIntentFilter;
    }
}
