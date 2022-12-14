package com.nyt.taxi.Order;

import android.util.Log;

import com.nyt.taxi.Activities.TaxiApp;
import com.nyt.taxi.Model.GDriverStatus;
import com.nyt.taxi.R;
import com.nyt.taxi.Utils.TwoPointDistance;
import com.nyt.taxi.Utils.UPoint;
import com.nyt.taxi.Utils.WebSocketEventReceiver;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.JamSegment;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.MapObjectCollection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TCRoute {

    static public List<OneRoute> mRoutes = new ArrayList<>();
    static public TCRoute mTCRoute = new TCRoute();
    static public String mWayTo;


    public class OneRoute {
        public int mRoadId;
        public double mDuration;
        public double mDistance;
        public List<Point> mPoints;
        public List<JamSegment> mJamSegments;
        public Point lastPoint() {
            if (mPoints.size() == 0) {
                return null;
            }
            return mPoints.get(mPoints.size() - 1);
        }
        public OneRoute() {
            mPoints = new ArrayList<>();
            mJamSegments = new ArrayList<>();
        }
    }

    public Point addOneRoute(JSONObject jo) {
        Point lastPoint = null;
        try {
            OneRoute or = new OneRoute();
            JSONArray j2 = jo.getJSONArray("points");
            or.mRoadId = Integer.valueOf(jo.getString("road_id"));
            or.mDistance = Double.valueOf(jo.getString("distance"));
            or.mDuration = Double.valueOf(jo.getString("duration"));
            for (int j = 0; j < j2.length(); j++) {
                JSONObject jpoint = j2.getJSONObject(j);
                Point p = new Point(jpoint.getDouble("lat"), jpoint.getDouble("lut"));
                or.mPoints.add(p);
            }
            if (or.mPoints.size() > 0) {
                lastPoint = or.mPoints.get(or.mPoints.size() - 1);
            }
            mRoutes.add(or);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lastPoint;
    }

    public Point addOneRoute(DrivingRoute dr) {
        Point lastPoint = null;
        OneRoute or = new OneRoute();
        or.mPoints.addAll(dr.getGeometry().getPoints());
        or.mRoadId = mRoutes.size() + 1;
        or.mDistance = dr.getMetadata().getWeight().getDistance().getValue();
        or.mDuration = dr.getMetadata().getWeight().getTimeWithTraffic().getValue();
        or.mJamSegments = new ArrayList<>(dr.getJamSegments());
        mRoutes.add(or);
        if (or.mPoints.size() > 0) {
            lastPoint = or.mPoints.get(or.mPoints.size() - 1);
        }
        return lastPoint;
    }

    public Point addOneRoute(GDriverStatus.Payload p) {
        Point lastPoint = null;
        if (p == null) {
            return null;
        }
        if (p.routes == null) { //TODO:
            return null;
        }
        if (p.routes.size() == 0) {
            return null;
        }
        OneRoute or = new OneRoute();
        or.mPoints.addAll(p.routes.get(0).toYandexPoints());
        or.mRoadId = p.routes.get(0).road_id;
        or.mDistance = p.routes.get(0).distance;
        or.mDuration = p.routes.get(0).duration;
        mRoutes.add(or);
        if (or.mPoints.size() > 0) {
            lastPoint = or.mPoints.get(or.mPoints.size() - 1);
        }
        return lastPoint;
    }

    public static Point mPoint1 = null;
    public static double mDistanceDelta = 0;
    public static double mNextDrivingSection = 0;
    public static double mDirectionCheck = 0;

    public static void initRoute(MapObjectCollection mapObjectCollection) {
        mPoint1 = null;
        mDistanceDelta = 0;
        mNextDrivingSection = 0;
        mDirectionCheck = 0;
        mapObjectCollection.clear();
    }

    public void drawNewPoint(int index, Point p) {
        if (index < 0) {
            return;
        }
        if (mRoutes.isEmpty()) {
            Log.d("TCRoute.drawNewPaint", "EMPTY ROUTE");
            return;
        }
        OneRoute or = mRoutes.get(index);
        if (or.mPoints.size() > 1) {
            int pi1 = or.mPoints.size() - 1;
            int pi2 = or.mPoints.size() - 2;
            if (mWayTo.equals(TaxiApp.getContext().getString(R.string.WayToDestination))) {
                pi1 = 0;
                pi2 = 1;
            }
            Point p0 = p;
            Point p1 = or.mPoints.get(pi1);
            Point p2 = or.mPoints.get(pi2);

            double currDistanceDelta = TwoPointDistance.getDistance(p0, p2);
            if (mDistanceDelta == 0) {
                mDistanceDelta = currDistanceDelta;
            } else {
                if ((int) currDistanceDelta * 10000 < (int) mDistanceDelta * 10000) {
                    //Go by route
                    mDistanceDelta = currDistanceDelta;
                    if (mDistanceDelta < 3) {
                        or.mPoints.remove(pi1);
                        if (or.mJamSegments.size() > 0) {
                            or.mJamSegments.remove(0);
                        }
                        mDistanceDelta = 0;
                    }
                } else {
                    if (currDistanceDelta - mDistanceDelta > 10) {
                        //Go away from route
                        boolean stop = false;
                        do {
                            or.mPoints.remove(pi1);
                            if (or.mJamSegments.size() > 0) {
                                or.mJamSegments.remove(0);
                            }
                            if (or.mPoints.size() < 2) {
                                //Need new route
//                                String s = "{\"event\":\"change_route_please\"}";
//                                WebSocketEventReceiver.sendMessage(TaxiApp.getContext(), s);
                                break;
                            }
                            if (mWayTo.equals(TaxiApp.getContext().getString(R.string.WayToClient))) {
                                pi1--;
                                pi2--;
                            }
                            p1 = or.mPoints.get(pi1);
                            p2 = or.mPoints.get(pi2);
                            double d1 = TwoPointDistance.getDistance(p1, p2);
                            double d2 = TwoPointDistance.getDistance(p0, p2);
                            if (d1 >= d2) {
                                stop = true;
                                mDistanceDelta = 0;
                            }
                        } while (!stop);
                    }
                }
            }

            Log.d("P0 CLASSIFY", Integer.toString((new UPoint(p0)).classify(new UPoint(p1), new UPoint(p2)).getValue()));
            Log.d("Line1 length", Double.toString(TwoPointDistance.getDistance(p1, p2)));
            Log.d("Line2 length", Double.toString(mDistanceDelta));
        }
    }
}

