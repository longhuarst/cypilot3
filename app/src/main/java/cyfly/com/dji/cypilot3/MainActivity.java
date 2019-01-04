package cyfly.com.dji.cypilot3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dji.mapkit.core.maps.DJIMap;
import com.dji.mapkit.core.models.DJILatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.ux.widget.MapWidget;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;
    private static boolean isAppStarted = false;//程序运行标记

    


    //    权限列表
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;



    private MapWidget mapWidget;
    private ViewGroup parentView;
    private View fpvWidget;
    private boolean isMapMini = true;

    private int height;
    private int width;
    private int margin;
    private int deviceWidth;
    private int deviceHeight;


    public static boolean isStarted() {
        return isAppStarted;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        isAppStarted = true;//APP已经启动

        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        //setContentView(R.layout.activity_main);

        //Initialize DJI SDK Manager
        //mHandler = new Handler(Looper.getMainLooper());


        setContentView(R.layout.activity_main); //加载界面


        height = DensityUtil.dip2px(this, 100);
        width = DensityUtil.dip2px(this, 150);
        margin = DensityUtil.dip2px(this, 12);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        mapWidget = (MapWidget) findViewById(R.id.map_widget);
        mapWidget.initAMap(new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap map) {




                map.setOnMapClickListener(new DJIMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(DJILatLng latLng) {
                        onViewClick(mapWidget);
                    }
                });
            }
        });
        mapWidget.onCreate(savedInstanceState);




        parentView = (ViewGroup) findViewById(R.id.root_view);



        fpvWidget = findViewById(R.id.fpv_widget);
        fpvWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewClick(fpvWidget);
            }
        });


        Toast.makeText(this, "SDK Version :"+DJISDKManager.getInstance().getSDKVersion().toString(), Toast.LENGTH_SHORT).show();



        //打印SDK版本号
        Log.e("cypilot","SDK Version :"+DJISDKManager.getInstance().getSDKVersion().toString());


        //请求数据的定时器
        flightControllerInit();



        //initAMap(savedInstanceState);//初始化高德地图



        //断线重连
//        IntentFilter i = new IntentFilter();
//        i.addAction(MApplication.FLAG);
    }

    private void resizeFPVWidget(int width, int height, int margin, int fpvInsertPosition) {
        RelativeLayout.LayoutParams fpvParams = (RelativeLayout.LayoutParams) fpvWidget.getLayoutParams();
        fpvParams.height = height;
        fpvParams.width = width;
        fpvParams.rightMargin = margin;
        fpvParams.bottomMargin = margin;
        fpvWidget.setLayoutParams(fpvParams);

        parentView.removeView(fpvWidget);
        parentView.addView(fpvWidget, fpvInsertPosition);
    }





    private class ResizeAnimation extends Animation {

        private View mView;
        private int mToHeight;
        private int mFromHeight;

        private int mToWidth;
        private int mFromWidth;
        private int mMargin;

        private ResizeAnimation(View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int margin) {
            mToHeight = toHeight;
            mToWidth = toWidth;
            mFromHeight = fromHeight;
            mFromWidth = fromWidth;
            mView = v;
            mMargin = margin;
            setDuration(300);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
            float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = mMargin;
            p.bottomMargin = mMargin;
            mView.requestLayout();
        }
    }



    //点击地图
    // 然后地图放大缩小
    private void onViewClick(View view) {
        if (view == mapWidget && !isMapMini) {
            resizeFPVWidget(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT, 0, 0);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, deviceWidth, deviceHeight, width, height, margin);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = true;
        } else if (view == mapWidget && isMapMini) {
            resizeFPVWidget(width, height, margin, 3);
            ResizeAnimation mapViewAnimation = new ResizeAnimation(mapWidget, width, height, deviceWidth, deviceHeight, 0);
            mapWidget.startAnimation(mapViewAnimation);
            isMapMini = false;
        }
    }









    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mapWidget.onResume();
    }



    @Override
    protected void onPause() {
        mapWidget.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapWidget.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapWidget.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapWidget.onLowMemory();
    }





















    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }

    }



    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }










    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    showToast("registering, pls wait...");
                    //注册DJI APP
                    DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                        @Override
                        public void onRegister(DJIError djiError) {
                            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                                showToast("Register Success");
                                DJISDKManager.getInstance().startConnectionToProduct();
                            } else {
                                showToast("Register sdk fails, please check the bundle id and network connection!");
                            }
                            Log.v(TAG, djiError.getDescription());
                        }

                        @Override
                        public void onProductDisconnect() {
                            Log.d(TAG, "onProductDisconnect");
                            showToast("Product Disconnected");
                            notifyStatusChange();

                        }
                        @Override
                        public void onProductConnect(BaseProduct baseProduct) {
                            Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                            showToast("Product Connected");
                            notifyStatusChange();

                        }
                        @Override
                        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                      BaseComponent newComponent) {

                            if (newComponent != null) {
                                newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                    @Override
                                    public void onConnectivityChange(boolean isConnected) {
                                        Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                        notifyStatusChange();
                                    }
                                });
                            }
                            Log.d(TAG,
                                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                            componentKey,
                                            oldComponent,
                                            newComponent));

                        }
                    });
                }
            });
        }
    }





    private void notifyStatusChange() {
//        有bug
//        mHandler.removeCallbacks(updateRunnable);
//        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });

    }











    //断线重连

//    protected BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//    };




//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////


    //实时获取飞机数据
    Timer flightControllerTimer = null;//定时器
    BaseProduct product = null;
    FlightController controller = null;



    //更新飞行控制对象
    boolean updateFlightContollerInstance(){
        if (product == null){
            product = DJISDKManager.getInstance().getProduct();
        }
        if (product !=null){
            if (controller == null){
                controller = ((Aircraft)product).getFlightController();
            }
            if (controller != null){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }



    private void flightControllerInit(){
        flightControllerTimer = new Timer();
        Log.e("cypilot3","timer start");
        flightControllerTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                product = DJISDKManager.getInstance().getProduct();

                if (product != null){
                    controller = ((Aircraft)product).getFlightController();

                    if (controller != null){

                        int compassCount = controller.getCompassCount();//获取罗盘个数

                        float heading = controller.getCompass().getHeading();

                        Log.e("cypilot3","compassCounter = "+compassCount);
                        Log.e("cypilot3","heading = "+heading);
                    }
                }


                Log.e("cypilot3","timer...");
                //Toast.makeText(MainActivity,"timer",Toast.LENGTH_LONG).show();

                flightControllerInit();
            }
        },6000);
        Log.e("cypilot3","timer complete");
    }






//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////
//////////////////////////////////////////////////


    //地图相关操作
    //private MapWidget mapWidget = null;
    private int mapProvider;
    public static final String MAP_PROVIDER = "MapProvider";

    void SetHomeBitmap(){

    }



    //初始化高德地图
    void initAMap(@Nullable Bundle savedInstanceState){
        mapWidget = findViewById(R.id.map_widget);

        MapWidget.OnMapReadyListener onMapReadyListener = new MapWidget.OnMapReadyListener() {
            @Override
            public void onMapReady(@NonNull DJIMap djiMap) {
                djiMap.setMapType(DJIMap.MapType.Normal);//设置地图类型为正常
            }
        };
        Intent intent = getIntent();

        mapWidget.initAMap(onMapReadyListener);//直接设定为高德地图
//        mapProvider = intent.getIntExtra(MAP_PROVIDER,0);
//        switch (mapProvider){
//            case 0:
//                mapWidget.initHereMap(onMapReadyListener);
//                break;
//            case 1:
//                mapWidget.initGoogleMap(onMapReadyListener);
//                break;
//            case 2:
//                mapWidget.initAMap(onMapReadyListener);
//                break;
//            default:
//                break;
//        }
        mapWidget.onCreate(savedInstanceState);


    }













    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////
    /////////////////////////////////////////

    //自动飞行


    //起飞
    void takeoff(){
        //if ()

        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例


        //Starts aircraft takeoff. Takeoff is considered complete when the aircraft is hovering 1.2 meters (4 feet) above the ground. Completion block is called when aircraft crosses 0.5 meters (1.6 feet). If the motors are already on, this command cannot be executed.
        //开始起飞
        //当飞机悬停在相对地面 1.2 米处则被认为是起飞完成
        //当穿越0.5米时，Completion block将会被调用
        //如果电机已经在旋转，则这个命令将不会被执行
        controller.startTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                //起飞结果
                Log.e("cypilot3","takeoff() --> result = "+djiError.toString());
            }
        });
    }



    //退出起飞
    void cancelTakeoff(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例


        //Stops aircraft takeoff. If called before startTakeoff is complete, the aircraft will cancel takeoff (startTakeoff completion block will return an error) and hover at the current height.
        //停止起飞,需要在起飞结束之前调用
        //飞机将会退出起飞
        //startTakeoff 的完成回调函数将会返回出错 并且飞机悬停在当前高度
        controller.cancelTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });

    }


    //开始降落
    void startLanding(){
        if (updateFlightContollerInstance() == false)
            return; //没有获取到飞行控制器实例

        //Starts auto-landing of the aircraft. Returns completion block once aircraft begins to descend for auto-land.
        //开始 自动降落
        //回调函数将会在开始自动降落时调用
        controller.startLanding(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {

            }
        });
    }








}
