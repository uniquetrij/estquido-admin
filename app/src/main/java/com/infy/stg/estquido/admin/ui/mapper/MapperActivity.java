package com.infy.stg.estquido.admin.ui.mapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableArray;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.app.services.CBLService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MapperActivity extends AppCompatActivity {


    private static final String TAG = MapperActivity.class.getName();

    private ArFragment mArFragment;
    private Vector3 mCamPosition;

    private Set<WayPoint> mWayPoints = Collections.synchronizedSet(new LinkedHashSet<>());
    private WayPoint selectedWayPoint;

    private int wayPointCounter = -1;
    private MutableDocument buildingDoc;
    private MutableDocument spotsDoc;
    private Quaternion mCamRotation;
    private Anchor anchor = null;
    private Vector3 prevCamPosition = null;
    private Quaternion prevCamRotation = null;
    private String wayPointName = "wayPoint_";

    private Location location;
    private Replicator mReplicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapper);

        Intent intent = getIntent();
        location = intent.getParcelableExtra("location");

        if (This.CBL_DATABASE.get() == null) {
            This.CBL_DATABASE.set(new CBLService(This.Static.COUCHBASE_DATABASE_URL, This.Static.COUCHBASE_DATABASE, This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS));
        }
        Log.d(TAG, "CBL building_" + This.CENTER.get() + "_" + This.BUILDING.get());
        CBLService cblDatabase = This.CBL_DATABASE.get();
        mReplicator = cblDatabase.async(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, new CBLService.Callback() {
            @Override
            public void onError(ReplicatorChange change) {

            }

            @Override
            public void onUpdate(ReplicatorChange change) {
                Log.d(TAG, "CBL " + change.getStatus().getProgress());
                try {
                    Log.d(TAG, "CBL " + cblDatabase.getDatabase().getDocument("building_" + This.CENTER.get() + "_" + This.BUILDING.get()).toMutable().toMap());
                    Log.d(TAG, "CBL " + cblDatabase.getDatabase().getDocument("spots_" + This.CENTER.get()).toMutable().toMap());
                    if (buildingDoc == null) {
                        initCBL();
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "CBL " + ex.toString());
                    spotsDoc = new MutableDocument("spots_" + This.CENTER.get());
                    buildingDoc = new MutableDocument("building_" + This.CENTER.get() + "_" + This.BUILDING.get());
                    buildingDoc.setValue("WayPoints", new ArrayList<Map<String, Object>>());
                    buildingDoc.setValue("WayPointIDs", new ArrayList<Integer>(Arrays.asList(0)));
                    buildingDoc.setValue("CheckPoints", new ArrayList<Map<String, Integer>>());
                    try {
                        This.CBL_DATABASE.get().getDatabase().save(buildingDoc);
                        This.CBL_DATABASE.get().getDatabase().save(spotsDoc);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, "building_" + This.CENTER.get() + "_" + This.BUILDING.get(), "spots_" + This.CENTER.get());
        Log.d(TAG, "REPLICATOR " + "START");

        mArFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.checkpoints_fragment);

        mArFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            Camera mARCamera = mArFragment.getArSceneView().getScene().getCamera();
            mCamPosition = mARCamera.getLocalPosition();
            mCamRotation = mARCamera.getLocalRotation();


            if (prevCamPosition == null & prevCamRotation == null) {
                prevCamPosition = mCamPosition;
                prevCamRotation = mCamRotation;
            }

            if (anchor == null & !prevCamRotation.equals(mCamRotation) && !prevCamPosition.equals(mCamPosition)) {
                createAnchor(mCamPosition, mCamRotation);
            }
            if (anchor == null) {
                prevCamPosition = mCamPosition;
                prevCamRotation = mCamRotation;
            }
        });
    }


    private void initCBL() {
        try {

            Document doc = This.CBL_DATABASE.get().getDatabase().getDocument("building_" + This.CENTER.get() + "_" + This.BUILDING.get());
            if (doc == null) {
                Log.d("document_ created", "");
                spotsDoc = new MutableDocument("spots_" + This.CENTER.get());
                buildingDoc = new MutableDocument("building_" + This.CENTER.get() + "_" + This.BUILDING.get());
                buildingDoc.setValue("WayPoints", new ArrayList<Map<String, Object>>());
                buildingDoc.setValue("WayPointIDs", new ArrayList<Integer>(Arrays.asList(0)));
                buildingDoc.setValue("CheckPoints", new ArrayList<Map<String, Integer>>());
                This.CBL_DATABASE.get().getDatabase().save(buildingDoc);
                This.CBL_DATABASE.get().getDatabase().save(spotsDoc);
            } else {
                spotsDoc = This.CBL_DATABASE.get().getDatabase().getDocument("spots_" + This.CENTER.get()).toMutable();
                buildingDoc = doc.toMutable();
                Log.d("document_ loaded", "");
                Map<String, WayPoint> newWayPoints = Collections.synchronizedMap(new LinkedHashMap<>());
                ((MutableArray) buildingDoc.getValue("WayPoints")).toList().stream().forEachOrdered(m -> {
                    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) m;
                    Map<String, Object> wpMap = map.values().stream().findFirst().get();
                    WayPoint wayPoint = new WayPoint(((Long) wpMap.get("id")).intValue(), new Vector3(((Number) wpMap.get("x")).floatValue(), ((Number) wpMap.get("y")).floatValue(), ((Number) wpMap.get("z")).floatValue()), (String) wpMap.get("wayPointName"), (boolean) wpMap.get("isCheckpoint"));
                    wayPoint.setCheckpointsPath((Map<String, List<String>>) wpMap.get("routes"));
                    mWayPoints.add(wayPoint);
                    newWayPoints.put(wayPoint.getWayPointName(), wayPoint);
                });
                wayPointCounter = ((MutableArray) buildingDoc.getValue("WayPointIDs")).toList().stream().mapToInt(value -> ((Long) value).intValue()).max().getAsInt();

                ((MutableArray) buildingDoc.getValue("WayPoints")).toList().stream().forEachOrdered(m -> {
                    Log.d("m", m.toString());
                    Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) m;
                    Map<String, Object> wpMap = map.values().stream().findFirst().get();
                    List<String> connections = (List<String>) wpMap.get("connections");
                    connections.stream().forEachOrdered(wayPointName -> {
                        newWayPoints.get((wpMap.get("wayPointName"))).getConnections().add(newWayPoints.get((wayPointName)));
                        newWayPoints.get((wayPointName)).getConnections().add(newWayPoints.get((wpMap.get("wayPointName"))));
                    });

                });
                Log.d("document_ loaded  size = ", mWayPoints.size() + "");
            }
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public void placeWayPoint(View view) {
        int id = ++wayPointCounter;

        Log.d("checkpoints_ waypoint counter", wayPointCounter + " ");
        mWayPoints.add(addWayPoint(id, mCamPosition, wayPointName + id, false));
        Log.d("anchor waypoints ", mWayPoints.toString());
    }

    private WayPoint addWayPoint(Integer id, Vector3 position, String wayPointName, boolean isCheckpoint) {

        WayPoint wayPoint = new WayPoint(id, position, wayPointName, isCheckpoint);
        Log.d("db1 waypoint name ", wayPointName);
        MaterialFactory.makeOpaqueWithColor(this, new com.google.ar.sceneform.rendering.Color(Color.parseColor("#FFBF00")))
                .thenAccept(material -> {
                    ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.1f, new Vector3(position.x, position.y, position.z), material);
                    AnchorNode anchorNode = new AnchorNode();
                    anchorNode.setAnchor(anchor);
                    anchorNode.setParent(mArFragment.getArSceneView().getScene());

                    wayPoint.getNode().setParent(anchorNode);
                    wayPoint.getNode().setRenderable(modelRenderable);
                    wayPoint.getNode().setOnTapListener(new Node.OnTapListener() {
                        @SuppressLint("RestrictedApi")
                        @Override
                        public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                            if (!wayPoint.isSelected()) {
                                if (selectedWayPoint == null) {
                                    wayPoint.setSelected(true);
                                    wayPoint.getNode().getRenderable().getMaterial().setFloat3(MaterialFactory.MATERIAL_COLOR, new com.google.ar.sceneform.rendering.Color(Color.BLUE));
                                    selectedWayPoint = wayPoint;

                                    ((FloatingActionButton) findViewById(R.id.floatingActionButton1)).setVisibility(View.VISIBLE);

                                    ((FloatingActionButton) findViewById(R.id.floatingActionButtonWaypointName)).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String waypointName = ((EditText) findViewById(R.id.editTextWayPointDetails)).getText().toString();
                                            Log.d(TAG, waypointName);

                                            if (waypointName != null) {
                                                wayPoint.setWayPointName(waypointName);
                                                wayPoint.setIsCheckpoint(true);
                                                Log.d(TAG, waypointName);
                                            }
                                            findViewById(R.id.constraintLayoutCheckpointDetails).setVisibility(View.GONE);
                                        }
                                    });


                                } else {
                                    selectedWayPoint.setSelected(false);
                                    connectWayPoints(selectedWayPoint, wayPoint);
                                    selectedWayPoint.getNode().getRenderable().getMaterial().setFloat3(MaterialFactory.MATERIAL_COLOR, new com.google.ar.sceneform.rendering.Color(Color.parseColor("#FFBF00")));
                                    selectedWayPoint = null;

                                    runOnUiThread(new Runnable() {
                                        @SuppressLint("RestrictedApi")
                                        @Override
                                        public void run() {

                                            findViewById(R.id.floatingActionButton1).setVisibility(View.GONE);
                                            findViewById(R.id.constraintLayoutCheckpointDetails).setVisibility(View.GONE);
                                        }
                                    });

                                }
                            } else {
                                wayPoint.setSelected(false);
                                wayPoint.getNode().getRenderable().getMaterial().setFloat3(MaterialFactory.MATERIAL_COLOR, new com.google.ar.sceneform.rendering.Color(Color.parseColor("#FFBF00")));
                                selectedWayPoint = null;

                                findViewById(R.id.floatingActionButton1).setVisibility(View.GONE);
                                findViewById(R.id.constraintLayoutCheckpointDetails).setVisibility(View.GONE);
                            }
                        }
                    });
                });

        return wayPoint;
    }


    private void connectWayPoints(WayPoint from, WayPoint to) {
        if (from.getConnections().contains(to) && to.getConnections().contains(from))
            return;

        from.getConnections().add(to);
        to.getConnections().add(from);

        AnchorNode node1 = (AnchorNode) from.getNode().getParent();
        AnchorNode node2 = (AnchorNode) to.getNode().getParent();

        Vector3 point1, point2;
        point1 = from.getPosition();
        point2 = to.getPosition();


        final Vector3 difference = Vector3.subtract(point1, point2);
        final Vector3 directionFromTopToBottom = difference.normalized();
        final Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new com.google.ar.sceneform.rendering.Color(Color.RED))
                .thenAccept(material -> {
                            ModelRenderable model = ShapeFactory.makeCube(new Vector3(0.025f, 0.025f, difference.length()), Vector3.zero(), material);
                            Node node = new Node();
                            node.setParent(node1);
                            node.setRenderable(model);
                            node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                            node.setWorldRotation(rotationFromAToB);

                            node.setOnTapListener(new Node.OnTapListener() {
                                @Override
                                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                                    node.setParent(null);
                                    from.getConnections().remove(to);
                                    to.getConnections().remove(from);
                                }
                            });
                        }
                );

    }

    public void createAnchor(Vector3 position, Quaternion rotation) {
        anchor = mArFragment.getArSceneView().getSession().createAnchor(new Pose(new float[]{0, 0, 0}, new float[]{0, 0, 0, -rotation.w}));
    }


    public void syncPositions(View view) {
        view.setEnabled(false);

        Log.d("document_ anchor", mWayPoints.size() + "");
        syncPositions();

        view.setEnabled(true);
    }

    public void syncPositions() {

        Set<WayPoint> oldWP = Collections.synchronizedSet(new LinkedHashSet<>(mWayPoints));
        Map<Integer, WayPoint> newWayPoints = Collections.synchronizedMap(new LinkedHashMap<>());
        mWayPoints.clear();

        oldWP.stream().forEachOrdered(wayPoint -> {
            WayPoint wpoint = addWayPoint(wayPoint.getId(), wayPoint.getPosition(), wayPoint.getWayPointName(), wayPoint.getIsCheckpoint());
            wpoint.setCheckpointsPath(wayPoint.getCheckpointsPath());
            newWayPoints.put(wayPoint.getId(), wpoint);

        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Set<WayPoint> visited = new HashSet<>();
                        oldWP.stream().forEachOrdered(wayPoint -> {
                            visited.add(wayPoint);
                            wayPoint.getConnections().stream().forEachOrdered(wayPoint1 -> {
                                if (!visited.contains(wayPoint1)) {
                                    connectWayPoints(newWayPoints.get(wayPoint.getId()), newWayPoints.get(wayPoint1.getId()));
                                }
                            });
                        });
                    }
                });
            }
        }, 10);

        mWayPoints.addAll(newWayPoints.values());
    }

    public void persistWayPoints(View view) {
        if (mWayPoints.isEmpty())
            return;
        List<Map<String, Object>> wpArray = new ArrayList<>();
        List<Integer> idArray = new ArrayList<>();
        Map<String, Integer> checkpointArray = new HashMap<>();


        mWayPoints.stream().forEachOrdered(wayPoint -> {
            if (wayPoint.getIsCheckpoint()) {
                Map<String, String> spots = new HashMap<>();
                checkpointArray.put(wayPoint.getWayPointName(), wayPoint.getId());
                spots.put("building", This.BUILDING.get());
                spotsDoc.setValue(wayPoint.getWayPointName(), spots);
            }
        });

        mWayPoints.stream().forEachOrdered(wayPoint -> {

            Map<String, Object> node = new LinkedHashMap<>();
            Map<String, Object> map = wayPoint.toMap();

            node.put(wayPoint.getWayPointName(), map);
            wpArray.add(node);
            idArray.add(wayPoint.getId());

        });

        buildingDoc.setValue("location", This.BUILDING_LOCATION.get());
        buildingDoc.setValue("WayPoints", wpArray);
        buildingDoc.setValue("WayPointIDs", idArray);
        buildingDoc.setValue("CheckPoints", checkpointArray);


        Log.d("db1 WayPoints", wpArray.toString());
        Log.d("db1 WayPointIDs", idArray.toString());
        Log.d("db1 checkpoints", checkpointArray.toString());
        try {
            This.CBL_DATABASE.get().getDatabase().save(buildingDoc);
            This.CBL_DATABASE.get().getDatabase().save(spotsDoc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void reset(View view) {
        mArFragment.getArSceneView().getSession().getAllAnchors().forEach(anchor -> anchor.detach());
        mWayPoints.clear();
        buildingDoc.setValue("WayPoints", new ArrayList<Map<String, Object>>());
        buildingDoc.setValue("WayPointIDs", new ArrayList<>(Arrays.asList(0)));
        try {
            This.CBL_DATABASE.get().getDatabase().save(buildingDoc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void calib(View view) {
//        calibPosition = mCamPosition;

    }

    public void setCheckPoint(View view) {
        ((ConstraintLayout) findViewById(R.id.constraintLayoutCheckpointDetails)).setVisibility(View.VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mReplicator.start();
        Log.d(TAG, "REPLICATOR " + "START");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReplicator.stop();
        Log.d(TAG, "REPLICATOR " + "STOP");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mReplicator.stop();
        Log.d(TAG, "REPLICATOR " + "STOP");
    }
}
