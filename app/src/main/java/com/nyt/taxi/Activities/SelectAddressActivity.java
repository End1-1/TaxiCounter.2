package com.nyt.taxi.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nyt.taxi.Model.GAirportMetro;
import com.nyt.taxi.Model.GHomeWorkResponse;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.UConfig;
import com.nyt.taxi.Utils.UGeocoderAnswer;
import com.nyt.taxi.Utils.UPref;
import com.nyt.taxi.Web.WQSaveToHomeWork;
import com.nyt.taxi.Web.WebQuery;
import com.nyt.taxi.Web.WebResponse;
import com.nyt.taxi.Web.WebTransportPoints;
import com.nyt.taxi.databinding.ActivitySelectAddressBinding;
import com.nyt.taxi.databinding.ItemDestinationTypeBinding;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Geometry;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.mapkit.search.SuggestSession;
import com.yandex.mapkit.search.SuggestType;
import com.yandex.runtime.Error;

import java.util.ArrayList;
import java.util.List;

public class SelectAddressActivity extends BaseActivity implements SuggestSession.SuggestListener, TextWatcher {

    private class DestinationType {
        GAirportMetro.TP type;
        Drawable dra;
        Drawable dri;
        String name;
        boolean selected;
        public DestinationType() {
            selected = false;
        }
        public DestinationType(GAirportMetro.TP t, String n, Drawable d1, Drawable d2) {
            type = t;
            name = n;
            selected = false;
            dra = d1;
            dri = d2;
        }
    }

    private static final double BOX_SIZE = 0.002;
    private ActivitySelectAddressBinding bind;
    boolean mSuggestAutoPoint = false;
    private boolean mBlockSuggest = false;
    private boolean mSelectAddress = false;
    private ArrayAdapter mSearchResultAdapter;
    private SearchManager mSearchManager;
    private SuggestSession mSuggestSession;
    private Point mAddressPoint;
    private String mTarget;
    private GAirportMetro mTransportPoints;
    private List<GAirportMetro.TransportPoint> mAddresses;
    private List<DestinationType> mDestinationTypes = new ArrayList<>();
    private List<GAirportMetro.TransportPoint> mListOfAddresses = new ArrayList<>();
    private final SuggestOptions SEARCH_OPTIONS =  new SuggestOptions().setSuggestTypes(
            SuggestType.GEO.value |
                    SuggestType.BIZ.value |
                    SuggestType.TRANSIT.value);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivitySelectAddressBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        mAddressPoint = new Point(getIntent().getDoubleExtra("lat", 0.0), getIntent().getDoubleExtra("lon", 0.0));
        SearchFactory.initialize(this);
        mSearchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        mSuggestSession = mSearchManager.createSuggestSession();
        mTarget = getIntent().getStringExtra("target");
        bind.back.setOnClickListener(this);
        bind.clearAddress.setOnClickListener(this);
        bind.save.setOnClickListener(this);
        bind.map.setOnClickListener(this);
        bind.address.addTextChangedListener(this);
        if (getIntent().getBooleanExtra("nosuggest", false)) {
            bind.types.setVisibility(View.GONE);
        }

        mSearchResultAdapter = new ArrayAdapter(TaxiApp.getContext(),
                R.layout.item_address,
                R.id.tvAddress,
                mListOfAddresses) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View vv = super.getView(position, convertView, parent);
                TextView tv = vv.findViewById(R.id.tvAddress);
                TextView tshort = vv.findViewById(R.id.tvShort);
                GAirportMetro.TransportPoint tp = mListOfAddresses.get(position);
                tshort.setText(tp.name);
                tv.setText(tp.address);
                int img;
                switch (tp.getTP()) {
                    case TP_AIRPORT:
                        img = R.drawable.airport;
                        break;
                    case TP_METRO:
                        img = R.drawable.metro;
                        break;
                    case TP_RAILWAY:
                        img = R.drawable.railway;
                        break;
                    default:
                        img = R.drawable.placeholderlight;
                        break;
                }

                ImageView iv = vv.findViewById(R.id.img);
                iv.setImageDrawable(getDrawable(img));
                return vv;
            }
        };
        bind.suggest.setAdapter(mSearchResultAdapter);
        bind.suggest.setOnItemClickListener((parent, view, position, id) -> {
            mBlockSuggest = true;
            mSelectAddress = true;
            GAirportMetro.TransportPoint tp = mListOfAddresses.get(position);
            switch (tp.getTP()) {
                case TP_ADDRESS:
                    mSuggestAutoPoint = false;
                    bind.address.setText(tp.address);
                    break;
                default:
                    mSuggestAutoPoint = true;
                    bind.address.setText(tp.name);
                    mAddressPoint = tp.getPoint();
                    break;
            }
            bind.suggest.setVisibility(View.GONE);
            if (!mSuggestAutoPoint) {
                bind.save.setVisibility(View.GONE);
            }
        });

        mDestinationTypes.add(new DestinationType(GAirportMetro.TP.TP_ADDRESS, getString(R.string.Suggestions), getDrawable(R.drawable.placeholderlighta), getDrawable(R.drawable.placeholderlight)));
        mDestinationTypes.add(new DestinationType(GAirportMetro.TP.TP_METRO, getString(R.string.Metro), getDrawable(R.drawable.metroa), getDrawable(R.drawable.metro)));
        mDestinationTypes.add(new DestinationType(GAirportMetro.TP.TP_RAILWAY, getString(R.string.Railways), getDrawable(R.drawable.railwaya), getDrawable(R.drawable.railway)));
        mDestinationTypes.add(new DestinationType(GAirportMetro.TP.TP_AIRPORT, getString(R.string.Airports), getDrawable(R.drawable.airporta), getDrawable(R.drawable.airport)));

        bind.types.setAdapter(new DestinationTypesAdapter());
        bind.types.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mDestinationTypes.get(0).selected = true;
        setDestinationType(mDestinationTypes.get(0));
        bind.types.getAdapter().notifyDataSetChanged();

        WebTransportPoints webTransportPoints = new WebTransportPoints(mWebResponse);
        webTransportPoints.request();
    }

    private void setDestinationType(DestinationType dt) {
        mSearchResultAdapter.clear();
        switch (dt.type) {
            case TP_NONE:
            case TP_ADDRESS:
                mSuggestAutoPoint = false;
                break;
            case TP_METRO:
                mSuggestAutoPoint = true;
                bind.suggest.setVisibility(View.VISIBLE);
                mSearchResultAdapter.addAll(mTransportPoints.metros);
                break;
            case TP_RAILWAY:
                mSuggestAutoPoint = true;
                bind.suggest.setVisibility(View.VISIBLE);
                mSearchResultAdapter.addAll(mTransportPoints.railways);
                break;
            case TP_AIRPORT:
                mSuggestAutoPoint = true;
                bind.suggest.setVisibility(View.VISIBLE);
                mSearchResultAdapter.addAll(mTransportPoints.airports);
                break;
        }
        mSearchResultAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.map:
                Intent mapIntent = new Intent(this, SelectAddressMapActivity.class);
                mapIntent.putExtra("lat", mAddressPoint.getLatitude());
                mapIntent.putExtra("lon", mAddressPoint.getLongitude());
                startActivityForResult(mapIntent, 1);
                break;
            case R.id.clearAddress:
                bind.suggest.setVisibility(View.GONE);
                bind.address.setText("");
                mBlockSuggest = false;
                mSelectAddress = false;
                bind.save.setVisibility(View.GONE);
                break;
            case R.id.save:
                if (mTarget == null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("lat", mAddressPoint.getLatitude());
                    resultIntent.putExtra("lon", mAddressPoint.getLongitude());
                    resultIntent.putExtra("address", bind.address.getText().toString());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    WQSaveToHomeWork saveToHomeWork = new WQSaveToHomeWork(bind.address.getText().toString(), mTarget, mAddressPoint.getLatitude(), mAddressPoint.getLongitude(), mWebResponse);
                    saveToHomeWork.request();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                mAddressPoint = new Point(data.getDoubleExtra("lat", 0.0), data.getDoubleExtra("lon", 0.0));
                bind.address.removeTextChangedListener(this);
                bind.address.setText(data.getStringExtra("address"));
                bind.address.addTextChangedListener(this);
                bind.save.setVisibility(View.VISIBLE);
                break;
        }
    }

    WebResponse mWebResponse = new WebResponse() {
        @Override
        public void webResponse(int code, int webResponse, String s) {
            hideProgressDialog();
            if (webResponse > 299) {
                mSelectAddress = false;
                return;
            }
            switch (code) {
                case mResponseGeocoder:
                    UGeocoderAnswer g = new UGeocoderAnswer(s);
                    if (g == null || !g.isValid) {
                        return;
                    }
                    mAddressPoint = g.mPoint;
                    bind.save.setVisibility(View.VISIBLE);
                    break;
                case mResponseHomeWork:
                    GHomeWorkResponse homeWorkResponse = GHomeWorkResponse.parse(s, GHomeWorkResponse.class);
                    UPref.setInt(mTarget + "_address_id", homeWorkResponse.address_id);
                    UPref.setString(mTarget + "_address", homeWorkResponse.address);
                    finish();
                    break;
                case mResponseAirportMetro:
                    mTransportPoints = GAirportMetro.parse(s, GAirportMetro.class);
                    if (mTransportPoints.metros == null) {
                        mTransportPoints.metros = new ArrayList();
                    }
                    if (mTransportPoints.airports == null) {
                         mTransportPoints.airports = new ArrayList();
                    }
                    if (mTransportPoints.railways == null) {
                        mTransportPoints.railways  = new ArrayList();
                    }
                    break;
            }
        }
    };


    @Override
    public void onResponse(@NonNull List<SuggestItem> list) {
        mListOfAddresses.clear();
        for (SuggestItem i: list) {
            GAirportMetro.TransportPoint tp = new GAirportMetro.TransportPoint();
            tp.address = i.getDisplayText();
            tp.name = i.getTitle().getText();
            tp.tp = GAirportMetro.TP.TP_ADDRESS;
            mListOfAddresses.add(tp);
        }
        mSearchResultAdapter.notifyDataSetChanged();
        bind.suggest.setVisibility(View.VISIBLE);
        mBlockSuggest = false;
    }

    @Override
    public void onError(@NonNull Error error) {
        System.out.println(error.toString());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mSuggestAutoPoint) {
            bind.save.setVisibility(View.VISIBLE);
            return;
        }
        bind.save.setVisibility(View.GONE);
        if (s.length() < 4) {
            mBlockSuggest = false;
            return;
        }
        if (mSelectAddress) {
            String link = String.format("https://geocode-maps.yandex.ru/1.x?apikey=%s&format=json&geocode=%s", UConfig.mGeocoderApiKey, s.toString());
            WebQuery.create(link, WebQuery.HttpMethod.GET, WebResponse.mResponseGeocoder, mWebResponse).request();
        } else {
            if (mBlockSuggest) {
                return;
            }
            mBlockSuggest = true;
            bind.suggest.setVisibility(View.INVISIBLE);
            Geometry geometry = Geometry.fromBoundingBox(new BoundingBox(
                    new Point(mAddressPoint.getLatitude() - BOX_SIZE, mAddressPoint.getLongitude() - BOX_SIZE),
                    new Point(mAddressPoint.getLatitude() + BOX_SIZE, mAddressPoint.getLongitude() + BOX_SIZE)));
//                    SearchOptions SEARCH_OPTIONS = new SearchOptions()
//                            .setSearchTypes(SearchType.GEO.value | SearchType.BIZ.value | SearchType.TRANSIT.value)
//                            .setGeometry(true);
            mSuggestSession.suggest(s.toString(), geometry.getBoundingBox(), SEARCH_OPTIONS, SelectAddressActivity.this);
            //mSearchManager.submit(s.toString(), geometry, SEARCH_OPTIONS, SelectCoordinateActivity.this);
        }
    }

    private class DestinationTypesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            private ItemDestinationTypeBinding bind;

            public VH(ItemDestinationTypeBinding b) {
                super(b.getRoot());
                bind = b;
                bind.getRoot().setOnClickListener(this);
            }

            public void onBind(int index) {
                DestinationType dt = mDestinationTypes.get(index);
                bind.img.setImageDrawable(dt.selected ? dt.dra : dt.dri);
                bind.name.setText(dt.name);
                //bind.br.setVisibility(dt.selected ? View.VISIBLE : View.GONE);
                bind.br.setVisibility(View.GONE);
            }

            @Override
            public void onClick(View v) {
                int index = getAdapterPosition();
                DestinationType dt = mDestinationTypes.get(index);
                for (DestinationType d: mDestinationTypes) {
                    d.selected = false;
                }
                dt.selected = true;
                setDestinationType(dt);
                notifyDataSetChanged();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDestinationTypeBinding b = ItemDestinationTypeBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((VH) holder).onBind(position);
        }

        @Override
        public int getItemCount() {
            return mDestinationTypes.size();
        }
    }
}