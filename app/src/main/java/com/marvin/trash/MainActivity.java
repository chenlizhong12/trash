package com.marvin.trash;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private FragmentManager mFragmentManager;
    private DiscoverFragment mDiscoverFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //申请相机权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        initView();
        if (mDiscoverFragment != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            hideFragment(ft);
        }
        if (mDiscoverFragment == null) {
            setTabSelection(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setTabSelection(0);
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void initView() {
        FrameLayout fl = findViewById(R.id.fl);
        Button discover = findViewById(R.id.discover);
        fl.setOnClickListener(this);
        discover.setOnClickListener(this);
        mFragmentManager = getSupportFragmentManager();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.discover:
                setTabSelection(0);
                break;
            default:
                break;
        }
    }

    private void setTabSelection(int index) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        hideFragment(ft);
        switch (index) {
            case 0:
                if (mDiscoverFragment == null) {
                    mDiscoverFragment = new DiscoverFragment();
                    ft.add(R.id.fl, mDiscoverFragment);
                } else {
                    ft.show(mDiscoverFragment);
                }

                break;
            default:
                break;
        }
        ft.commit();
    }

    //用于隐藏fragment
    private void hideFragment(FragmentTransaction ft) {
        if (mDiscoverFragment != null) {
            ft.hide(mDiscoverFragment);
        }
    }
}
