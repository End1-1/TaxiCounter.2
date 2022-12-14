package com.nyt.taxi.Fragments;

import android.content.Intent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.nyt.taxi.Activities.SelectAddressActivity;
import com.nyt.taxi.Activities.Workspace;
import com.nyt.taxi.Kalman.Services.ServicesHelper;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WQSelectRouteHomeWork;
import com.nyt.taxi.Web.WebResponse;


public class LLWorkspaceIntro extends LLRoot
        implements View.OnClickListener,
        WebResponse {


    private Workspace mWorkspace;
    public static boolean mToHome = false;
    public static boolean mToWork = false;
    public boolean mShowForm = false;

    private ImageButton editToHome;
    private ImageButton editToWork;
    private Button btnGoOnline;
    private Switch swHome;
    private Switch swOffice;
    private LinearLayout orderByRoute;
    private ConstraintLayout flMain;
    private ConstraintLayout clOnline;
    private ImageView imgDirection;
    private TextView tohome;
    private TextView towork;
    private ConstraintLayout clMain;

    public LLWorkspaceIntro(Workspace w) {
        mWorkspace = w;

        editToHome = w.findViewById(R.id.editToHome);
        editToWork = w.findViewById(R.id.editToHome);
        btnGoOnline = w.findViewById(R.id.btnGoOnline);
        swHome = w.findViewById(R.id.swHome);
        swOffice = w.findViewById(R.id.swOffice);
        orderByRoute = w.findViewById(R.id.orderByRoute);
        flMain = w.findViewById(R.id.flMain);
        clOnline = w.findViewById(R.id.clOnline);
        imgDirection = w.findViewById(R.id.imgDirection);
        tohome = w.findViewById(R.id.tohome);
        towork = w.findViewById(R.id.towork);
        clMain = w.findViewById(R.id.clMain);

        editToHome.setOnClickListener(this);
        editToWork.setOnClickListener(this);
        orderByRoute.setOnClickListener(this);
        btnGoOnline.setOnClickListener(this);
        swHome.setOnClickListener(this);
        swOffice.setOnClickListener(this);

        mMoveView = flMain;
        flMain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //flMain.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //flMain.setY(flMain.getHeight() - orderByRoute.getHeight() - clOnline.getHeight() - 48);
                //moveTo(0);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tohome:
                if (ServicesHelper.getLocationService() == null) {
                    return;
                }
                if (ServicesHelper.getLocationService().getLastLocation() == null) {
                    return;
                }
                if (UPref.getInt("HOME_address_id") == 0) {
                    Intent i = new Intent(mWorkspace, SelectAddressActivity.class);
                    i.putExtra("target", "HOME");
                    i.putExtra("nosuggest", true);
                    mWorkspace.startActivity(i);
                } else {
                    mToWork = false;
                    mToHome = !mToHome;
                    WQSelectRouteHomeWork selectRouteHomeWork = new WQSelectRouteHomeWork("HOME", this);
                    selectRouteHomeWork.request();
                }
                updateButtons();
                break;
            case R.id.towork:
                if (ServicesHelper.getLocationService() == null) {
                    return;
                }
                if (ServicesHelper.getLocationService().getLastLocation() == null) {
                    return;
                }
                if (UPref.getInt("WORK_address_id") == 0) {
                    Intent i = new Intent(mWorkspace, SelectAddressActivity.class);
                    i.putExtra("target", "WORK");
                    i.putExtra("nosuggest", true);
                    mWorkspace.startActivity(i);
                } else {
                    mToHome = false;
                    mToWork = !mToWork;
                    WQSelectRouteHomeWork selectRouteHomeWork = new WQSelectRouteHomeWork("WORK", this);
                    selectRouteHomeWork.request();
                }
                updateButtons();
                break;
            case R.id.editToHome:
            {
                Intent i = new Intent(mWorkspace, SelectAddressActivity.class);
                i.putExtra("target", "HOME");
                i.putExtra("nosuggest", true);
                mWorkspace.startActivity(i);
            }
            break;
            case R.id.editToWork:
            {
                Intent i = new Intent(mWorkspace, SelectAddressActivity.class);
                i.putExtra("target", "HOME");
                i.putExtra("nosuggest", true);
                mWorkspace.startActivity(i);
            }
            break;
            case R.id.orderByRoute:
                mShowForm = !mShowForm;
                int imgid = mShowForm ? R.drawable.downward : R.drawable.upward;
                imgDirection.setImageDrawable(mWorkspace.getDrawable(imgid));
                if (mShowForm) {
                    moveTo(flMain.getHeight() - clOnline.getHeight() - clMain.getHeight());
                } else {
                    moveTo(flMain.getHeight() - orderByRoute.getHeight() - clOnline.getHeight() - 48);
                }
                break;
            case R.id.btnGoOnline:
                mWorkspace.goOnline();
                break;
            case R.id.swHome: {
                mToWork = false;
                mToHome = swHome.isChecked();
                if (swOffice.isChecked()) {
                    swOffice.setChecked(false);
                    WQSelectRouteHomeWork selectRouteHomeWork = new WQSelectRouteHomeWork("WORK", this);
                    selectRouteHomeWork.request();
                }
                WQSelectRouteHomeWork selectRouteHomeWork = new WQSelectRouteHomeWork("HOME", this);
                selectRouteHomeWork.request();
                break;
            }
            case R.id.swOffice: {
                mToHome = false;
                mToWork = swOffice.isChecked();
                if (swHome.isChecked()) {
                    swHome.setChecked(false);
                    WQSelectRouteHomeWork selectRouteHomeWork = new WQSelectRouteHomeWork("HOME", this);
                    selectRouteHomeWork.request();
                }
                WQSelectRouteHomeWork selectRouteHomeWork = new WQSelectRouteHomeWork("WORK", this);
                selectRouteHomeWork.request();
                break;
            }
        }
    }

    private void updateButtons() {
        int bg = mToHome ? R.drawable.btn_roundgreenborder : R.drawable.btn_roundgreen;
        tohome.setBackground(mWorkspace.getDrawable(bg));
        bg = mToWork ? R.drawable.btn_roundgreenborder : R.drawable.btn_roundgreen;
        towork.setBackground(mWorkspace.getDrawable(bg));
    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        if (code > 299) {
            return;
        }
    }
}
