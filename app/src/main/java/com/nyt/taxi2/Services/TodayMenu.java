package com.nyt.taxi2.Services;

import static com.nyt.taxi2.Interfaces.WorkMenuInterface.mclLandscapeOrientation;
import static com.nyt.taxi2.Interfaces.WorkMenuInterface.mclNavigator;
import static com.nyt.taxi2.Interfaces.WorkMenuInterface.mclNavigatorNightMode;
import static com.nyt.taxi2.Interfaces.WorkMenuInterface.mclProfile;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi2.Activities.OrdersHistoryActivity;
import com.nyt.taxi2.Activities.ProfileActivity;
import com.nyt.taxi2.Activities.Workspace;
import com.nyt.taxi2.Model.GCarClasses;
import com.nyt.taxi2.Model.GInitialInfo;
import com.nyt.taxi2.Model.GObject;
import com.nyt.taxi2.Model.GOrderDayInfo;
import com.nyt.taxi2.R;
import com.nyt.taxi2.Utils.ResizeWithAnimation;
import com.nyt.taxi2.Utils.UDialog;
import com.nyt.taxi2.Utils.UPref;
import com.nyt.taxi2.Utils.UText;
import com.nyt.taxi2.Web.WQDayOrdersInfo;
import com.nyt.taxi2.Web.WebInitialInfo;
import com.nyt.taxi2.Web.WebLogout;
import com.nyt.taxi2.Web.WebResponse;
import com.nyt.taxi2.Web.WebToggleCarClass;
import com.nyt.taxi2.Web.WebToggleCarOption;

import java.util.ArrayList;

public class TodayMenu implements View.OnClickListener {
    GCarClasses mCarClasses;
    CarTypeAdapter mCarTypeAdapter = new CarTypeAdapter();
    RecyclerView rvCartType;
    private MenuAdapter mMenuAdapter;
    private RecyclerView mRecycleView;
    private Workspace mWorkspace;

    private TextView tvCity;
    private TextView tvDriverName;
    private ImageView imgProfileLMenu;
    private ImageButton btnClose;
    private ImageButton btnSetup;
    private Button btnHistory;
    private Button btnGoOffline;
    private Button btnCloseApp;
    private RecyclerView rvCarOption;
    private TextView tvAmountLMenu;
    private TextView tvActivity;
    private TextView tvRating;
    private TextView tvDistance;
    private TextView tvBalance;

    public TodayMenu(Workspace ws) {
        mWorkspace = ws;
        WebInitialInfo webInitialInfo = new WebInitialInfo(mWebResponse);
        webInitialInfo.request();
        WQDayOrdersInfo dayOrdersInfo = new WQDayOrdersInfo(mWebResponse);
        dayOrdersInfo.request();

        imgProfileLMenu = ws.findViewById(R.id.imgProfileLMenu);
        imgProfileLMenu.setImageBitmap(ProfileActivity.getProfileImage());
        imgProfileLMenu.setOnClickListener(this);
        tvDriverName = ws.findViewById(R.id.tvDriverName);
        tvDriverName.setText(UPref.getString("driver_fullname"));
        tvCity = ws.findViewById(R.id.tvCity);
        tvCity.setText(UPref.getString("driver_city"));
        btnClose = ws.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        btnSetup = ws.findViewById(R.id.btnSetup);
        btnSetup.setOnClickListener(this);
        btnHistory = ws.findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(this);
        btnGoOffline = ws.findViewById(R.id.btnGoOffline);
        btnGoOffline.setOnClickListener(this);
        btnCloseApp = ws.findViewById(R.id.btnCloseApp);
        btnCloseApp.setOnClickListener(this);
        mCarClasses = GObject.restoreFromPref("carclasses", GCarClasses.class);
        rvCartType = ws.findViewById(R.id.rvCarType);
        rvCartType.setLayoutManager(new GridLayoutManager(mWorkspace, 1));
        rvCartType.setAdapter(mCarTypeAdapter);
        rvCartType.setNestedScrollingEnabled(false);
        mMenuAdapter = new MenuAdapter();
        mRecycleView = ws.findViewById(R.id.rvMenu);
        mRecycleView.setLayoutManager(new LinearLayoutManager(mWorkspace));
        mRecycleView.setAdapter(mMenuAdapter);
        mRecycleView.setNestedScrollingEnabled(false);
        rvCarOption = ws.findViewById(R.id.rvCarOption);
        rvCarOption.setLayoutManager(new LinearLayoutManager(mWorkspace));
        rvCarOption.setAdapter(new CarOptionAdapter());
        rvCarOption.setNestedScrollingEnabled(false);
        tvActivity = ws.findViewById(R.id.tvActivity);
        tvAmountLMenu = ws.findViewById(R.id.tvAmountLMenu);
        tvRating = ws.findViewById(R.id.tvRating);
        tvDistance = ws.findViewById(R.id.txtDistance);
        tvBalance = ws.findViewById(R.id.tvBalance);
    }


    private void close() {
        mCarClasses.saveToPref("carclasses");
        mWorkspace.hideTodayMenu();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSetup:
//                Intent intent = new Intent(this, WorkMenuActivity.class);
//                startActivity(intent);
                break;
            case R.id.btnClose: {
                close();
                break;
            }
            case R.id.imgProfileLMenu:
                if (UPref.getBoolean("display_landscape")) {
                    close();
                    mWorkspace.showProfile(true);
                } else {
                    Intent intent = new Intent(mWorkspace, ProfileActivity.class);
                    mWorkspace.startActivity(intent);
                }
                break;
            case R.id.btnHistory:
                if (UPref.getBoolean("display_landscape")) {
                    mWorkspace.showHistory();
                } else {
                    Intent historyIntent = new Intent(mWorkspace, OrdersHistoryActivity.class);
                    mWorkspace.startActivity(historyIntent);
                }
                break;
            case R.id.btnCloseApp:
                UDialog.alertDialogWithButtonTitles(mWorkspace, R.string.Empty,
                        mWorkspace.getString(R.string.CloseAppQuestion),
                        mWorkspace.getString(R.string.YES),
                        mWorkspace.getString(R.string.NO), new DialogInterface(){

                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void dismiss() {
                                WebLogout wl = new WebLogout(new WebResponse() {
                                    @Override
                                    public void webResponse(int code, int webResponse, String s) {
                                        Intent i3  = new Intent(mWorkspace, WebSocketHttps.class);
                                        i3.putExtra("cmd", 1);
                                        mWorkspace.startService(i3);

                                        UPref.setBearerKey("");
                                        UPref.setBoolean("finish", true);
                                        mWorkspace.finish();
                                    }
                                });
                                wl.request();
                            }
                        });
                break;
            case R.id.btnGoOffline:
                UDialog.alertDialogWithButtonTitles(mWorkspace, R.string.Empty, mWorkspace.getString(R.string.QuestionGoOffline),
                        mWorkspace.getString(R.string.YES), mWorkspace.getString(R.string.NO),
                        new DialogInterface() {
                            @Override
                            public void cancel() {

                            }

                            @Override
                            public void dismiss() {
                                mWorkspace.createProgressDialog(R.string.Empty, R.string.Wait);
                                WebRequest.create("/api/driver/order_ready", WebRequest.HttpMethod.POST, new WebRequest.HttpResponse() {
                                            @Override
                                            public void httpRespone(int httpReponseCode, String data) {
                                                mWorkspace.hideProgressDialog();
                                                if (httpReponseCode == -1) {
                                                    UDialog.alertError(mWorkspace, R.string.MissingInternet);
                                                } else if (httpReponseCode > 299) {
                                                    UDialog.alertError(mWorkspace, data);
                                                } else {
                                                    mWorkspace.hideTodayMenu();
                                                    mWorkspace.queryState();
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

    public void click(int action) {
        switch (action) {
        case mclProfile:
            mWorkspace.hideTodayMenu();
            if (UPref.getBoolean("display_landscape")) {
                close();
                mWorkspace.showProfile(true);
            } else {
                Intent intent = new Intent(mWorkspace, ProfileActivity.class);
                mWorkspace.startActivity(intent);
            }
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
            mWorkspace.hideProgressDialog();
            if (code > 299) {
                return;
            }
            switch (code) {
                case mResponseDayOrdersInfo:
                    GOrderDayInfo orderDayInfo = GOrderDayInfo.parse(s, GOrderDayInfo.class);
                    tvActivity.setText(UText.valueOf(orderDayInfo.assessment));
                    tvRating.setText(String.valueOf(orderDayInfo.rating));
                    tvAmountLMenu.setText(UText.valueOf(orderDayInfo.days_cost));
                    break;
                case mResponseToggleCarClass:
                    break;
                case mResponseInitialInfo:
                    GInitialInfo info = GInitialInfo.parse(s, GInitialInfo.class);
                    tvDistance.setText(String.format("%.1f", info.distance));
                    tvBalance.setText(String.format("%.0f", info.balance));
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
                GCarClasses.CarClass f = mCarClasses.classes.get(i);
                f.selected = !f.selected;
                notifyDataSetChanged();
                if (!mWorkspace.createProgressDialog(R.string.Empty, R.string.Wait)) {
                    return;
                }
                WebToggleCarClass cc = new WebToggleCarClass(f.class_id,mWebResponse);
                cc.request();
            }

            public void bind(int i) {
                GCarClasses.CarClass f = mCarClasses.classes.get(i);
                tvName.setText(f.name);
                tvPrice.setText(String.format("%.0f", f.min_price));
                tvPrice.setVisibility(View.GONE);
                tvPrice.setVisibility(View.GONE);
                if (f.selected) {
                    ivCheck.setBackground(mWorkspace.getDrawable(R.drawable.ok_active));
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
                    ivCheck.setBackground(mWorkspace.getDrawable(R.drawable.ok_inactive));
                    ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, 140);
                    anim.setDuration(400);
                    vCheck.startAnimation(anim);
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new CarTypeAdapter.VH(mWorkspace.getLayoutInflater().inflate(R.layout.item_car_type, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            CarTypeAdapter.VH vh = (CarTypeAdapter.VH) viewHolder;
            vh.bind(i);
        }

        @Override
        public int getItemCount() {
            return mCarClasses.classes.size();
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
                GCarClasses.CarOption f = mCarClasses.options.get(i);
                f.selected = !f.selected;
                notifyDataSetChanged();
                if (!mWorkspace.createProgressDialog(R.string.Empty, R.string.Wait)) {
                    return;
                }
                WebToggleCarOption cc = new WebToggleCarOption(f.id,mWebResponse);
                cc.request();
            }

            public void bind(int i) {
                GCarClasses.CarOption f = mCarClasses.options.get(i);
                tvName.setText(f.option);
                tvPrice.setText(String.format("%.0f", f.price));
                if (f.selected) {
                    ivCheck.setBackground(mWorkspace.getDrawable(R.drawable.ok_active));
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
                    ivCheck.setBackground(mWorkspace.getDrawable(R.drawable.ok_inactive));
                    ResizeWithAnimation anim = new ResizeWithAnimation(vCheck, 140);
                    anim.setDuration(400);
                    vCheck.startAnimation(anim);
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new CarOptionAdapter.VH(mWorkspace.getLayoutInflater().inflate(R.layout.item_car_type, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            CarOptionAdapter.VH vh = (CarOptionAdapter.VH) viewHolder;
            vh.bind(i);
        }

        @Override
        public int getItemCount() {
            return mCarClasses.options.size();
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
                click(mAction);
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
                                Intent intent = new Intent(mWorkspace, Workspace.class);
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mWorkspace.startActivity(intent);
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
            ma.mTitle = mWorkspace.getString(R.string.Profile);
            ma.mImage = R.drawable.roza;
            mData.add(ma);
            ma = new MenuAdapterOption();
            ma.mAction = mclNavigator;
            ma.mTitle = mWorkspace.getString(R.string.Navigator);
            ma.mImage = R.drawable.navigator;
            mData.add(ma);
            ma = new MenuAdapterOption();
            ma.mAction = mclNavigatorNightMode;
            ma.mTitle = mWorkspace.getString(R.string.NavigatorNightMode);
            ma.mImage = R.drawable.night_mode;
            mData.add(ma);
            ma = new MenuAdapterOption();
            ma.mAction = mclLandscapeOrientation;
            ma.mTitle = mWorkspace.getString(R.string.LangscapeOrientation);
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
            return new MenuAdapter.MenuAdapterHolder(mWorkspace.getLayoutInflater().inflate(R.layout.item_work_menu_navigator, viewGroup, false));
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
