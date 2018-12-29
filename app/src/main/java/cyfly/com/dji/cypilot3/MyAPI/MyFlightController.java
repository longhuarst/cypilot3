package cyfly.com.dji.cypilot3.MyAPI;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightOrientationMode;
import dji.common.flightcontroller.adsb.AirSenseSystemInformation;
import dji.common.flightcontroller.imu.IMUState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.AccessLocker;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.LandingGear;
import dji.sdk.flightcontroller.RTK;
import dji.sdk.flightcontroller.Simulator;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyFlightController {


    private  MyFlightController(){

    }

    private static class MyFlightControllerHolder{
        private final static MyFlightController instance = new MyFlightController();


    }

    public static MyFlightController getInstance(){
        return MyFlightControllerHolder.instance;
    }



    BaseProduct product = null;
    FlightController controller = null;

    FlightController getFlightController(){
        product = DJISDKManager.getInstance().getProduct();

        controller = ((Aircraft)product).getFlightController();

        if (controller == null)
            return null;

        controller.setStateCallback(FlightControllerStateCallback);

        controller.setIMUStateCallback(IMUStateCallback);

        controller.setASBInformationCallback(AirSenseSystemInformationCallback);


        return controller;
    }







    //飞行控制器 状态 回调函数
    FlightControllerState.Callback FlightControllerStateCallback = new FlightControllerState.Callback(){

        @Override
        public void onUpdate(@NonNull FlightControllerState flightControllerState) {

        }
    };



    //飞行控制器 惯性测量单元 回调函数
    IMUState.Callback IMUStateCallback = new IMUState.Callback(){

        @Override
        public void onUpdate(@NonNull IMUState imuState) {

        }
    };


    //Sets the AirSenseSystemInformation callback, which will receive the AirSenseSystemInformation.
    //飞机传感器系统信息
    AirSenseSystemInformation.Callback AirSenseSystemInformationCallback = new AirSenseSystemInformation.Callback(){

        @Override
        public void onUpdate(@NonNull AirSenseSystemInformation airSenseSystemInformation) {

        }
    };


    //Sets the callback function that updates the data received from an external device (e.g. the onboard device).
    //拓展设备的更新数据
    FlightController.OnboardSDKDeviceDataCallback OnboardSDKDeviceDataCallback = new FlightController.OnboardSDKDeviceDataCallback() {
        @Override
        public void onReceive(byte[] bytes) {

        }
    };



    //=================To Determine==================================
    //=================To Determine==================================

    //Returns the current state of flight controller.
    //返回当前飞行控制器的状态
    FlightControllerState getState(){
        if (controller == null)
            return null;
        return controller.getState();
    }




    //==============Sensors===========================================
    //==============传感器===========================================

    //Compass object.
    //获取电子罗盘
    Compass getCompass(){
        if (controller == null)
            return null;

        return controller.getCompass();
    }


    //Number of Compass modules in the flight controller.
    //获取飞行控制器上的电子罗盘的个数
    int getCompassCount(){
        if (controller == null)
            return 0;

        return controller.getCompassCount();
    }



    //RTK positioning object.
    // RTK 定位对象
    @Nullable
    RTK getRTK(){
        if (controller == null)
            return null;

        return controller.getRTK();
    }


    //Number of IMU modules in the flight controller. Most products have one IMU.
    //返回飞行控制器上的惯性测量单位的个数，大多数的产品有一个惯性测量单元
    int getIMUCount(){
        if (controller == null){
            return 0;
        }

        return controller.getIMUCount();
    }



    //Starts IMU calibration. For aircraft with multiple IMUs, this method will start the calibration of all IMUs. Keep the aircraft stationary and horizontal during calibration, which will take 5 to 10 minutes. The completion block will be called once the calibration is started. Use the onUpdate method to check the execution status of the IMU calibration.
    //开始惯性测量单元校准
    void startIMUCalibration(@Nullable CommonCallbacks.CompletionCallback callback){
        if (controller == null)
            return;

        controller.startIMUCalibration(callback);
    }

    //Starts the calibration for IMU with a specific ID. Keep the aircraft stationary and horizontal during calibration, which will take 5 to 10 minutes. The completion block will be called once the calibration is started. Use the onUpdate method to check the execution status of the IMU calibration.
    //开始校准某个特定ID的惯性测量单元
    void startIMUCalibration(@IntRange(from = 0, to = 2) int index,
                             @Nullable CommonCallbacks.CompletionCallback callback){
        if (controller == null){
            return ;
        }

        controller.startIMUCalibration(index,callback);
    }



    //=========================Sub Components=======================
    //=========================子部件================================


    //Landing Gear object. For products with movable landing gear only.
    //获取起落架对象
    LandingGear getLandingGear(){
        if (controller == null)
            return null;

        return controller.getLandingGear();
    }


    //true if landing gear is supported for the connected aircraft.
    //如果连接的飞机支持起落架则返回真
    boolean isLandingGearMovable(){
        if (controller == null)
            return false;

        return controller.isLandingGearMovable();
    }


    //Simulator object.
    //仿真对象
    Simulator getSimulator(){
        if (controller == null)
            return null;

        return controller.getSimulator();
    }


    //Returns if the Onboard SDK feature is available on the connected product.
    //返回连接的产品是否支持onboardSDK
    boolean isOnboardSDKDeviceAvailable(){
        if (controller == null)
            return false;
        return controller.isOnboardSDKDeviceAvailable();
    }



    //If there is a device connected to the aircraft using the Onboard SDK, this method will send data to that device. The size of the data cannot be greater than 100 bytes, and will be sent in 40 byte increments every 14ms. This method is only supported on products that support the Onboard SDK (Matrice 100, Matrice 600, Matrice 600 Pro, A3, A3 Pro, and N3).
    //发送数据给OnBoardSDK设备
    void sendDataToOnboardSDKDevice(@Size(min = 1, max = 100) byte[] data,
                                    @Nullable CommonCallbacks.CompletionCallback callback){
        if (controller == null)
            return ;
        controller.sendDataToOnboardSDKDevice(data,callback);
    }




    //============================Flight Assistance========================
    //============================Flight Assistance========================


    //Gets the instance of the access locker. It is used to encapsulate the access protection features on the aircraft.
    //获取访问锁定的是实例
    AccessLocker getAccessLocker(){
        if (controller == null)
            return null;

        return controller.getAccessLocker();

    }


    //true if the product supports IntelligentFlightAssistant.
    //如果产品支持IntelligentFlightAssistant 则返回真
    boolean isFlightAssistantSupported(){
        if (controller == null)
            return false;
        return controller.isFlightAssistantSupported();
    }


    //Intelligent flight assistant.
    @Nullable
    FlightAssistant getFlightAssistant(){
        if (controller == null)
            return null;
        return controller.getFlightAssistant();
    }




    //Sets the aircraft flight orientation relative to the Aircraft Heading, Course Lock, or Home Lock. See the Flight Controller User Guide for more information about flight orientation.
    void setFlightOrientationMode(@NonNull FlightOrientationMode type,
                                  @Nullable CommonCallbacks.CompletionCallback callback){
        if (controller == null)
            return;

        controller.setFlightOrientationMode(type,callback);
    }



}
