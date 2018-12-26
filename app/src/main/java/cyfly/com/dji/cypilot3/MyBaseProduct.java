package cyfly.com.dji.cypilot3;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.DJIDiagnostics;
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
}
