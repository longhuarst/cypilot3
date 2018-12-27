package cyfly.com.dji.cypilot3;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.accessory.AccessoryAggregation;
import dji.sdk.airlink.AirLink;
import dji.sdk.base.DJIDiagnostics;
import dji.sdk.battery.Battery;
import dji.sdk.camera.Camera;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyBaseProduct {




    //DJI产品的抽象类
    //


    //设置诊断列表更新回调函数
    void setDiagnosticsInformationCallback(DJIDiagnostics.DiagnosticsInformationCallback callback){
        DJISDKManager.getInstance().getProduct().setDiagnosticsInformationCallback(callback);
    }


    //获取产品的固件包版本
    String getFirmwarePackageVersion(){
        return DJISDKManager.getInstance().getProduct().getFirmwarePackageVersion();
    }



    //检索产品的model
    void setName(@NonNull String name, @Nullable final CommonCallbacks.CompletionCallback callback){
        DJISDKManager.getInstance().getProduct().setName(name,callback);
    }


    //检索产品的model
    void getName(final CommonCallbacks.CompletionCallbackWith<String> callback){
        DJISDKManager.getInstance().getProduct().getName(callback);
    }


    //检索产品的model
    Model getModel(){
        return DJISDKManager.getInstance().getProduct().getModel();
    }



    //true if the device is connected.
    //如果设备已经链接则返回真
    boolean isConnected(){
        return DJISDKManager.getInstance().getProduct().isConnected();
    }


    //Retrieves an instance of the device's camera.
    //检索一个设备相机的实例
    Camera getCamera(){
        return DJISDKManager.getInstance().getProduct().getCamera();
    }


    //Retrieves an instance of the product's smart battery. When using products with multiple batteries, such as the M600 or Inspire 2, use getBatteries, as this method will only return a single battery.
    //检索一个产品的智能电池的实例
    Battery getBattery(){
        return DJISDKManager.getInstance().getProduct().getBattery();
    }


    //Returns an array of aircraft's batteries. It is used when the aircraft has multiple batteries, e.g. Matrice 600.
    //返回一个飞机电池的列表
    synchronized List<Battery> getBatteries(){
        return DJISDKManager.getInstance().getProduct().getBatteries();
    }



    //检索一个设备的平衡环
    Gimbal getGimbal(){
        return DJISDKManager.getInstance().getProduct().getGimbal();
    }



    //检索一个AirLink的设备实例
    AirLink getAirLink(){
        return DJISDKManager.getInstance().getProduct().getAirLink();
    }



    @Nullable
    AccessoryAggregation getAccessoryAggregation(){
        return DJISDKManager.getInstance().getProduct().getAccessoryAggregation();
    }








}

