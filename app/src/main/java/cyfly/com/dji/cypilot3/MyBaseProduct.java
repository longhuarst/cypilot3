package cyfly.com.dji.cypilot3;

import dji.sdk.base.DJIDiagnostics;
import dji.sdk.sdkmanager.DJISDKManager;

public class MyBaseProduct {




    //DJI产品的抽象类
    //


    //设置诊断列表更新回调函数
    void setDiagnosticsInformationCallback(DJIDiagnostics.DiagnosticsInformationCallback callback){
        DJISDKManager.getInstance().getProduct().setDiagnosticsInformationCallback(callback);
    }









}
