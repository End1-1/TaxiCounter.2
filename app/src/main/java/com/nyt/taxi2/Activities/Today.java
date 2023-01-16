package com.nyt.taxi2.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Interfaces.WorkMenuInterface;
import com.nyt.taxi2.Kalman.Services.KalmanLocationService;
import com.nyt.taxi2.Model.GCarClasses;
import com.nyt.taxi2.Model.GInitialInfo;
import com.nyt.taxi2.Model.GObject;
import com.nyt.taxi2.Model.GOrderDayInfo;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Services.FileLogger;
import com.nyt.taxi2.Services.LocationListenerService;
import com.nyt.taxi2.Services.WebRequest;
import com.nyt.taxi2.Services.WebSocketHttps;
import com.nyt.taxi2.Utils.ResizeWithAnimation;
import com.nyt.taxi2.Utils.UConfig;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Utils.UText;
import com.nyt.taxi2.Web.WQDayOrdersInfo;
import com.nyt.taxi2.Web.WebInitialInfo;
import com.nyt.taxi2.Web.WebLogout;
import com.nyt.taxi2.Web.WebQuery;
import com.nyt.taxi2.Web.WebResponse;
import com.nyt.taxi2.Web.WebToggleCarClass;
import com.nyt.taxi2.Web.WebToggleCarOption;
import com.nyt.taxi2.databinding.ActivityTodayBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Today extends BaseActivity implements WorkMenuInterface {

    private ActivityTodayBinding bind;

    GCarClasses mCarClasses;
    CarTypeAdapter mCarTypeAdapter = new CarTypeAdapter();
    RecyclerView rvCartType;
    private MenuAdapter mMenuAdapter;
    private RecyclerView mRecycleView;

    public Today() {
        super();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!UPref.getString("order_event").isEmpty()) {
            finish();
            return;
        }
        WebInitialInfo webInitialInfo = new WebInitialInfo(mWebResponse);
        webInitialInfo.request();
        WQDayOrdersInfo dayOrdersInfo = new WQDayOrdersInfo(mWebResponse);
        dayOrdersInfo.request();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bind.imgProfile.setImageBitmap(ProfileActivity.getProfileImage());
        bind.tvDriverName.setText(UPref.getString("driver_fullname"));
        bind.tvCity.setText(UPref.getString("driver_city"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityTodayBinding.inflate(getLayoutInflater());
        View view = bind.getRoot();
        setContentView(view);
        findViewById(R.id.btnSetup).setOnClickListener(this);
        findViewById(R.id.btnClose).setOnClickListener(this);
        findViewById(R.id.imgProfile).setOnClickListener(this);
        bind.btnHistory.setOnClickListener(this);
        bind.btnGoOffline.setOnClickListener(this);
        bind.btnCloseApp.setOnClickListener(this);
        bind.btnNotification.setOnClickListener(this);
        bind.tvDriverName.setText(UPref.getString("driver_name"));
        mCarClasses = GObject.restoreFromPref("carclasses", GCarClasses.class);
        rvCartType = findViewById(R.id.rvCarType);
        rvCartType.setLayoutManager(new GridLayoutManager(this, 1));
        rvCartType.setAdapter(mCarTypeAdapter);
        rvCartType.setNestedScrollingEnabled(false);
        mMenuAdapter = new MenuAdapter();
        mRecycleView = findViewById(R.id.rvMenu);
        //mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setAdapter(mMenuAdapter);
        mRecycleView.setNestedScrollingEnabled(false);

        bind.rvCarOption.setLayoutManager(new LinearLayoutManager(this));
        bind.rvCarOption.setAdapter(new CarOptionAdapter());
        bind.rvCarOption.setNestedScrollingEnabled(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        close();
    }

    private void close() {
        mCarClasses.saveToPref("carclasses");
        finish();
//        Intent intent = new Intent(Today.this, Workspace.class);
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
    }

    @Override
    public void handleClick(int id) {
        super.handleClick(id);
        switch (id) {
            case R.id.btnSetup:
//                Intent intent = new Intent(this, WorkMenuActivity.class);
//                startActivity(intent);
                break;
            case R.id.btnClose: {
                close();
                break;
            }
            case R.id.btnNotification:
                Intent intentChat = new Intent(this, ChatActivity.class);
                intentChat.putExtra("info", true);
                startActivity(intentChat);
                break;
            case R.id.imgProfile:
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.btnHistory:
                Intent historyIntent = new Intent(this, OrdersHistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.btnCloseApp:
                UDialog.alertDialogWithButtonTitles(this, R.string.Empty,
                        getString(R.string.CloseAppQuestion),
                        getString(R.string.YES),
                        getString(R.string.NO), new DialogInterface(){

                    @Override
                    public void cancel() {

                    }

                    @Override
                    public void dismiss() {
                        FileLogger.write("Application quit");
                        WebLogout wl = new WebLogout(new WebResponse() {
                            @Override
                            public void webResponse(int code, int webResponse, String s) {
                                Intent i3  = new Intent(Today.this, WebSocketHttps.class);
                                i3.putExtra("cmd", 1);
                                Today.this.startService(i3);

                                UPref.setBearerKey("");
                                UPref.setBoolean("finish", true);
                                Today.this.finish();
                            }
                        });
                        wl.request();

                        /*
                        Intent i1  = new Intent(Today.this, KalmanLocationService.class);
                        i1.putExtra("cmd", 1);
                        startService(i1);

                        Intent i2  = new Intent(Today.this, LocationListenerService.class);
                        i2.putExtra("cmd", 1);
                        startService(i2);
                         */

//                        Intent i3  = new Intent(Today.this, WebSocketHttps.class);
//                        i3.putExtra("cmd", 1);
//                        startService(i3);
//
//                        UPref.setBearerKey("");
//                        UPref.setBoolean("finish", true);
//                        finish();
                    }
                });
                break;
            case R.id.btnGoOffline:
                UDialog.alertDialogWithButtonTitles(this, R.string.Empty, getString(R.string.QuestionGoOffline),
                        getString(R.string.YES), getString(R.string.NO),
                        new DialogInterface() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void dismiss() {
                                createProgressDialog(R.string.Empty, R.string.Wait);
                                WebRequest.create("/api/driver/order_ready", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                                    @Override
                                    public void httpRespone(int httpReponseCode, String data) {
                                        hideProgressDialog();
                                        if (httpReponseCode == -1) {
                                            UDialog.alertError(Today.this, R.string.MissingInternet);
                                        } else if (httpReponseCode > 299) {
                                            UDialog.alertError(Today.this, data);
                                        } else {
                                            finish();
                                        }
                                    }
                                })
                                        .setParameter("ready", Integer.toString(0))
                                    .setParameter("lat", UText.valueOf(UPref.lastPoint().lat))
                                    .setParameter("lut", UText.valueOf(UPref.lastPoint().lut))
                                    .request();
                            }
                        });
                break;
        }
    }

    @Override
    public void click(int action) {
        switch (action) {
        case mclProfile:
            finish();
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            break;
        }
    }

    public class MenuAdapterOption {
        int mAction;
        int mImage;
        String mTitle;
    }


    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            if (code > 299) {
                return;
            }
            switch (code) {
                case mResponseDayOrdersInfo:
                    GOrderDayInfo orderDayInfo = GOrderDayInfo.parse(s, GOrderDayInfo.class);
                    bind.tvActivity.setText(UText.valueOf(orderDayInfo.assessment));
                    bind.tvRating.setText(String.valueOf(orderDayInfo.rating));
                    bind.tvAmount.setText(UText.valueOf(orderDayInfo.days_cost));
                    bind.tvOrders.setText(String.format("%s", String.valueOf(orderDayInfo.days_orders)));
                    break;
                case mResponseToggleCarClass:
                    break;
                case mResponseInitialInfo:
                    GInitialInfo info = GInitialInfo.parse(s, GInitialInfo.class);
                    bind.txtDistance.setText(String.format("%.1f", info.distance));
                    bind.tvBalance.setText(String.format("%.0f", info.balance));
                    System.out.println(s);
                    break;
            }
        }
    };


    public class CarTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView tvName;
            TextView tvPrice;
            ImageView ivCheck;
            View vCheck;

            public VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvPrice = v.findViewById(R.id.tvPrice);
                ivCheck = v.findViewById(R.id.imgCheck);
                vCheck = v.findViewById(R.id.vCheck);
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int i = getAdapterPosition();
                GCarClasses.CarClass f = Today.this.mCarClasses.classes.get(i);
                f.selected = !f.selected;
                notifyDataSetChanged();
                if (!createProgressDialog(R.string.Empty, R.string.Wait)) {
                    return;
                }
                WebToggleCarClass cc = new WebToggleCarClass(f.class_id,mWebResponse);
                cc.request();
            }

            public void bind(int i) {
                GCarClasses.CarClass f = Today.this.mCarClasses.classes.get(i);
                tvName.setText(f.name);
                tvPrice.setText(String.format("%.0f", f.min_price));
                tvPrice.setVisibility(View.GONE);
                tvPrice.setVisibility(View.GONE);
                if (f.selected) {
                    ivCheck.setBackground(getDrawable(R.drawable.ok_active));
                    itemView.post(new Runnable() {
                        @Override
                        public void run() {
                            int width = itemView.getWidth() - 15;
                            ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, width);
                            anim.setDuration(400);
                            vCheck.startAnimation(anim);
                        }
                    });
                } else {
                    ivCheck.setBackground(getDrawable(R.drawable.ok_inactive));
                    ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, 140);
                    anim.setDuration(400);
                    vCheck.startAnimation(anim);
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Today.CarTypeAdapter.VH(Today.this.getLayoutInflater().inflate(R.layout.item_car_type, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            VH vh = (VH) viewHolder;
            vh.bind(i);
        }

        @Override
        public int getItemCount() {
            return Today.this.mCarClasses.classes.size();
        }
    }

    public class CarOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView tvName;
            TextView tvPrice;
            ImageView ivCheck;
            View vCheck;

            public VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvPrice = v.findViewById(R.id.tvPrice);
                ivCheck = v.findViewById(R.id.imgCheck);
                vCheck = v.findViewById(R.id.vCheck);
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int i = getAdapterPosition();
                GCarClasses.CarOption f = Today.this.mCarClasses.options.get(i);
                f.selected = !f.selected;
                notifyDataSetChanged();
                if (!createProgressDialog(R.string.Empty, R.string.Wait)) {
                    return;
                }
                WebToggleCarOption cc = new WebToggleCarOption(f.id,mWebResponse);
                cc.request();
            }

            public void bind(int i) {
                GCarClasses.CarOption f = Today.this.mCarClasses.options.get(i);
                tvName.setText(f.option);
                tvPrice.setText(String.format("%.0f", f.price));
                if (f.selected) {
                    ivCheck.setBackground(getDrawable(R.drawable.ok_active));
                    itemView.post(new Runnable() {
                        @Override
                        public void run() {
                            int width = itemView.getWidth() - 15;
                            ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, width);
                            anim.setDuration(400);
                            vCheck.startAnimation(anim);
                        }
                    });
                } else {
                    ivCheck.setBackground(getDrawable(R.drawable.ok_inactive));
                    ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, 140);
                    anim.setDuration(400);
                    vCheck.startAnimation(anim);
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new Today.CarOptionAdapter.VH(Today.this.getLayoutInflater().inflate(R.layout.item_car_type, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            VH vh = (VH) viewHolder;
            vh.bind(i);
        }

        @Override
        public int getItemCount() {
            return Today.this.mCarClasses.options.size();
        }
    }

    private class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class MenuAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            int mAction;
            private ImageView mImageView;
            private TextView mTextView;
            private Switch mSwitch;

            public MenuAdapterHolder(@NonNull View v) {
                super(v);
                mImageView = v.findViewById(R.id.img);
                mTextView = v.findViewById(R.id.tvCaption);
                mSwitch = v.findViewById(R.id.idSwitch);
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Today.this.click(mAction);
            }

            public void bind(int i) {
                MenuAdapterOption mao = mData.get(i);
                mAction = mao.mAction;
                mTextView.setText(mao.mTitle);
                //mImageView.setImageDrawable(getDrawable(mao.mImage));
                switch (i) {
                    case mclProfile:
                        mSwitch.setVisibility(View.GONE);
                        break;
                    case mclNavigator:
                        mSwitch.setChecked(UPref.getBoolean("navigator_on"));
                        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                UPref.setBoolean("navigator_on", isChecked);
                            }
                        });
                        break;
                    case mclNavigatorNightMode:
                        mSwitch.setChecked(UPref.getBoolean("navigator_nightmode"));
                        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                UPref.setBoolean("navigator_nightmode", isChecked);
                            }
                        });
                        break;
                    case mclLandscapeOrientation:
                        mSwitch.setChecked(UPref.getBoolean("display_landscape"));
                        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                UPref.setBoolean("display_landscape", isChecked);
                                Intent intent = new Intent(Today.this, Workspace.class);
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Today.this.startActivity(intent);
                            }
                        });
                        break;
                }
            }
        }

        private ArrayList<MenuAdapterOption> mData = new ArrayList<>();

        public MenuAdapter() {
            MenuAdapterOption ma = new MenuAdapterOption();
            ma.mAction = mclProfile;
            ma.mTitle = Today.this.getString(R.string.Profile);
            ma.mImage = R.drawable.roza;
            mData.add(ma);
            ma = new MenuAdapterOption();
            ma.mAction = mclNavigator;
            ma.mTitle = getString(R.string.Navigator);
            ma.mImage = R.drawable.navigator;
            mData.add(ma);
            ma = new MenuAdapterOption();
            ma.mAction = mclNavigatorNightMode;
            ma.mTitle = getString(R.string.NavigatorNightMode);
            ma.mImage = R.drawable.night_mode;
            mData.add(ma);
            ma = new MenuAdapterOption();
            ma.mAction = mclLandscapeOrientation;
            ma.mTitle = getString(R.string.LangscapeOrientation);
            ma.mImage = R.drawable.landscape;
            mData.add(ma);
//            ma = new MenuAdapterOption();
//            ma.mAction = mclCloseSession;
//            ma.mTitle = getString(R.string.CloseSession);
//            ma.mImage = R.drawable.logout;
//            mData.add(ma);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MenuAdapter.MenuAdapterHolder(getLayoutInflater().inflate(R.layout.item_work_menu_navigator, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((MenuAdapter.MenuAdapterHolder) viewHolder).bind(i);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }
}
