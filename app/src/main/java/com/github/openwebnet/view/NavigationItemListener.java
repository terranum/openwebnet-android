package com.github.openwebnet.view;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.openwebnet.R;
import com.github.openwebnet.component.Injector;
import com.github.openwebnet.service.EnvironmentService;
import com.github.openwebnet.view.device.DeviceListFragment;
import com.github.openwebnet.view.settings.SettingsFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.github.openwebnet.view.device.DeviceListFragment.ARG_ENVIRONMENT;

public class NavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {

    private static final Logger log = LoggerFactory.getLogger(NavigationItemListener.class);

    @Inject
    EnvironmentService environmentService;

    @Bind(R.id.floatingActionsMenuMain)
    FloatingActionsMenu floatingActionsMenuMain;

    @BindString(R.string.app_name)
    String labelApplicationName;
    @BindString(R.string.activity_settings)
    String labelSettings;
    @BindString(R.string.validation_required)
    String labelValidationRequired;
    @BindString(R.string.error_add_environment)
    String labelErrorAddEnvironment;

    private final AppCompatActivity mActivity;
    private final DrawerLayout mDrawerLayout;

    public NavigationItemListener(AppCompatActivity activity, DrawerLayout drawerLayout) {
        Injector.getApplicationComponent().inject(this);
        ButterKnife.bind(this, activity);

        this.mActivity = activity;
        this.mDrawerLayout = drawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        log.debug("MENU selected [id={}]", id);

        switch (id) {
            case R.id.nav_favourite:
                mActivity.getSupportActionBar().setTitle(labelApplicationName);
                log.debug("TODO favourite");
                break;
            case R.id.nav_add:
                showDialogAddEnvironment();
                break;
            case R.id.nav_settings:
                mActivity.getSupportActionBar().setTitle(labelSettings);
                showSettings();
                break;
            default:
                mActivity.getSupportActionBar().setTitle(item.getTitle());
                showEnvironment(id);
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showEnvironment(Integer id) {
        floatingActionsMenuMain.setVisibility(View.VISIBLE);
        removeFragment();

        Fragment fragment = new DeviceListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ENVIRONMENT, id);
        fragment.setArguments(args);

        mActivity.getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_frame, fragment)
            //.addToBackStack(null)
            .commit();
    }

    private void showSnackbar(String message) {
        Snackbar.make(floatingActionsMenuMain, message, Snackbar.LENGTH_LONG).show();
    }

    private void showDialogAddEnvironment() {
        View layout = LayoutInflater.from(mActivity).inflate(R.layout.dialog_environment, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
            .setView(layout)
            .setTitle(R.string.dialog_add_environment_title)
            .setPositiveButton(R.string.button_add, null)
            .setNegativeButton(android.R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener(v -> {
                EditText name = (EditText) layout.findViewById(R.id.editTextDialogEnvironmentName);
                if (TextUtils.isEmpty(name.getText())) {
                    name.setError(labelValidationRequired);
                } else {
                    addEnvironment(name.getText().toString());
                    dialog.dismiss();
                }
            });
    }

    private void addEnvironment(String name) {
        environmentService.add(name)
            .subscribe(id -> {
                // calls onPrepareOptionsMenu(): reload menu
                mActivity.invalidateOptionsMenu();
                mDrawerLayout.openDrawer(GravityCompat.START);
            },
            throwable -> {
                showSnackbar(labelErrorAddEnvironment);
            });
    }

    private void showSettings() {
        floatingActionsMenuMain.setVisibility(View.INVISIBLE);
        removeCompactFragment();

        // refactor with android.support.v4 when will be stable
        mActivity.getFragmentManager()
            .beginTransaction()
            .replace(R.id.content_frame, new SettingsFragment())
            .commit();
    }

    private void removeFragment() {
        android.app.Fragment fragment = mActivity.getFragmentManager()
            .findFragmentById(R.id.content_frame);
        if (fragment != null) {
            mActivity.getFragmentManager()
                .beginTransaction()
                .remove(fragment).commit();
        }
    }

    private void removeCompactFragment() {
        Fragment compactFragment = mActivity.getSupportFragmentManager()
            .findFragmentById(R.id.content_frame);
        if (compactFragment != null) {
            mActivity.getSupportFragmentManager()
                .beginTransaction()
                .remove(compactFragment).commit();
        }
    }
}
