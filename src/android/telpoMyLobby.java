package cordova-plugin-telpomylobby;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.thermalimage.CalibrationCallBack;
import com.common.thermalimage.HotImageCallback;
import com.common.thermalimage.TemperatureBitmapData;
import com.common.thermalimage.TemperatureData;
import com.common.thermalimage.ThermalImageUtil;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


/**
 * This class echoes a string called from JavaScript.
 */
public class telpoMyLobby extends CordovaPlugin {
    ThermalImageUtil temperatureUtil;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }
        if (action.equals("getTempData")) {
            this.getTempData(callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        temperatureUtil = new ThermalImageUtil(this);
        if(!isInstalled(MainActivity.this,"com.telpo.temperatureservice")){
            // showTip("not install TempatureServices.apk");
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Telpo TempatureServices App not found");
            callbackctx.sendPluginResult(result);
            return;
        }
    }

    private void getTempData(CallbackContext callbackContext) {
        float distance=50;
        final float distances = distance;
        Void img;
        new Thread(new Runnable() {
           @Override
           public void run() {
               TemperatureData temperatureData = temperatureUtil.getDataAndBitmap(distances,true, new HotImageCallback.Stub() {
                   @Override
                   public void onTemperatureFail(String e) {
                       // Log.i("getDataAndBitmap", "onTemperatureFail " + e);
                       // showTip("Failed to get temperature:  " + e);
                        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Fail to get temperature.");
                        callbackctx.sendPluginResult(result);
                        return;
                   }

                   @Override
                   public void getTemperatureBimapData(final TemperatureBitmapData data) {
                       this.img = data.getBitmap();
                   }

               });
               if (temperatureData != null) {
                  JSONObject jsonObj1 = new JSONObject().put("temp", temperatureData.getTemperature()).put("img", this.img) ;

                  PluginResult result = new PluginResult(PluginResult.Status.OK, jsonObj1);
                  callbackctx.sendPluginResult(result);
                  return;
               } else {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error while capturing temperature.");
                    callbackctx.sendPluginResult(result);
                    return;
               }
           }
       }).start();
    }

    private boolean isInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    public class AppInstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            PackageManager manager = context.getPackageManager();
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                //检测到安装成功是temperatureservice.apk
                if(packageName.equals("com.telpo.temperatureservice")){
                    //重新创建ThermalImageUtil
                    temperatureUtil = new ThermalImageUtil(MainActivity.this);
                }
            }
        }

    }
}
