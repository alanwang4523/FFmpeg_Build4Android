package com.alan.ffmpegjni4android.protocols;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Use this class to get Marshmallow Runtime Permissions. Create an instance by calling the
 * {@link #create(Activity, Listener, String...)} factory method in the {@link Activity} onCreate().
 * Handle
 *
 * Created by jeffmcknight on 10/22/15.
 */
public class RuntimePermissionsHelper {
    private static String TAG = RuntimePermissionsHelper.class.getSimpleName();
    private static final int DENY_COUNT_MAX = 3;
    public static final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    public static final int PERMISSIONS_REQUEST_CAMERA = 2;
    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 3;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4;

    /**
     * The {@link Activity} that will receive the runtime permission callback
     */
    private Activity mActivity;
    private Fragment mFragment;
    /**
     * A {@link Map} of permission names mapped to deny counts so we can keep track of the number of
     * times the user has denied each permission
     */
    private final Map<String, Integer> mDenyCountMap;
    /**
     * A set of the names of all the permissions that have been granted.
     */
    private Set<String> mPermissionsGrantedSet;
    /**
     * Use this code in the calling {@link Activity} to match the {@link ActivityCompat.OnRequestPermissionsResultCallback}
     */
    private int mRequestCode;
    /**
     * Use this to notify listeners when runtime permissions are granted or denied.
     */
    private Listener mListener;

    /**
     * Simple constructor. Do not use externally; use the factory method instead so everything gets initialized properly.
     * @param activity the {@link Activity} that will receive the permissions request callback
     * @param requestCode a positive integer that uniquely identifies the {@link Activity} that
     *                    originated the permission(s) request
     * @param listener
     * @param denyCountMap a map of permission(s) requested that lets us count requests/denials
     * @param permissionGrantedSet
     */
    private RuntimePermissionsHelper(@NonNull Activity activity,
                                     int requestCode,
                                     @Nullable Listener listener,
                                     @NonNull Map<String, Integer> denyCountMap,
                                     @NonNull Set<String> permissionGrantedSet) {
        mActivity = activity;
        mRequestCode = requestCode;
        mListener = listener;
        mDenyCountMap = denyCountMap;
        mPermissionsGrantedSet = permissionGrantedSet;
    }

    private RuntimePermissionsHelper(@NonNull Fragment fragment,
                                     int requestCode,
                                     @Nullable Listener listener,
                                     @NonNull Map<String, Integer> denyCountMap,
                                     @NonNull Set<String> permissionGrantedSet) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mRequestCode = requestCode;
        mListener = listener;
        mDenyCountMap = denyCountMap;
        mPermissionsGrantedSet = permissionGrantedSet;
    }

    public void cleanDenyCountMap(){
        if (mDenyCountMap!=null){
            for (String permissionName : mDenyCountMap.keySet()){
                mDenyCountMap.put(permissionName, 0);
            }
        }
    }
    /**
     * Preferred Factory Method. Use this method to instantiate a {@link RuntimePermissionsHelper}.
     * Builds and injects a request code and permissionsMap from the activity and permissionNames,
     * respectively.
     *
     * @param activity
     * @param permissionName the name(s) of the permission(s) we want to request from the user
     * @return
     */
    @NonNull
    public static RuntimePermissionsHelper create(@NonNull Activity activity,
                                                  @Nullable Listener listener,
                                                  @NonNull String... permissionName) {
        int requestCode = generateRequestCode(activity);
        ArrayMap<String, Integer> permissionsMap = new ArrayMap<>();
        Set<String> permissionGrantedSet = new HashSet<>();
        for (int i=0; i< permissionName.length; i++){
            permissionsMap.put(permissionName[i], 0);
            if (isPermissionGranted(permissionName[i], activity)){
                permissionGrantedSet.add(permissionName[i]);
            }
        }
        return new RuntimePermissionsHelper(
                activity,
                requestCode,
                listener,
                permissionsMap,
                permissionGrantedSet);
    }

    @NonNull
    public static RuntimePermissionsHelper create(@NonNull Fragment fragment,
                                                  @Nullable Listener listener,
                                                  @NonNull String... permissionName) {
        int requestCode = generateRequestCode(fragment);
        ArrayMap<String, Integer> permissionsMap = new ArrayMap<>();
        Set<String> permissionGrantedSet = new HashSet<>();
        for (int i = 0; i < permissionName.length; i++) {
            permissionsMap.put(permissionName[i], 0);
            if (isPermissionGranted(permissionName[i], fragment)) {
                permissionGrantedSet.add(permissionName[i]);
            }
        }
        return new RuntimePermissionsHelper(
                fragment,
                requestCode,
                listener,
                permissionsMap,
                permissionGrantedSet);
    }

    /**
     * Generate an 8-bit request code that will be unique to the calling {@link Activity} and >= 0, as
     * specified by Activity.requestPermissions().
     *
     * @param activity
     * @return
     */
    private static int generateRequestCode(Object activity) {
        int byteBitmask = 0b11111111;
        int requestCode = activity.hashCode() & byteBitmask;
        Log.d(TAG, "generateRequestCode()"
                        + "\t -- requestCode: " + requestCode
                        + "\t -- byteBitmask: " + byteBitmask
                        + "\t -- activity.hashCode(): " + activity.hashCode()
        );
        return requestCode;
    }

    /**
     * Make a request of the user to grant permission to do the stuff we need to do
     *
     * @return true if user has already granted permission
     */
    public void makeRequest() {
        Log.i(TAG, "makeRequest()"

        );
        if (!allPermissionsGranted()){
            if (shouldShowRationale()) {
                showRationale();
            } else if (shouldShowSettingsHint()){
                showSettingsHint();
            } else {
                showPermissionsRequest();
            }
        } else {
            notifyPermissionsGranted(mDenyCountMap.keySet());
        }
    }

    /**
     * Check whether we should show the user a hint about how to set the app permissions to enable
     * use of the feature that requires it
     *
     * @return
     */
    private boolean shouldShowSettingsHint() {
        boolean shouldShowHint = false;
        if (isRuntimePermissionRequiredByApiLevel()) {
            for (Integer denyCount : mDenyCountMap.values()){
                if (askedTooManyTimes(denyCount)){
                    shouldShowHint = true;
                }
            }
        }
        Log.i(TAG, "shouldShowSettingsHint()"
                        + "\t -- shouldShowHint: " + shouldShowHint
        );
        return shouldShowHint;
    }

    /**
     * Check whether we should show the user a dialog or something to explain why we are requesting an app permission
     *
     * @return
     */
    public boolean shouldShowRationale() {
        boolean shouldShowRationale = false;
        if (isRuntimePermissionRequiredByApiLevel()) {
            for (String permissionName : mDenyCountMap.keySet()){
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissionName)
                        && !(mDenyCountMap.get(permissionName) > DENY_COUNT_MAX)){
                    shouldShowRationale = true;
                    break;
                }
            }
        }
        Log.i(TAG, "shouldShowRationale()"
                        + "\t -- shouldShowRationale: " + shouldShowRationale
        );
        return shouldShowRationale;
    }

    /**
     * Check whether every requested permission has been granted
     *
     * @return true if every requested permission has been granted
     */
    public boolean allPermissionsGranted() {
        boolean allPermissionsGranted = true;
        if (isRuntimePermissionRequiredByApiLevel()) {
            for (String permissionName : mDenyCountMap.keySet()){
                if (!isPermissionGranted(permissionName)){
                    allPermissionsGranted = false;
                }
            }
        }
        Log.d(TAG, "allPermissionsGranted()"
                        + "\t -- allPermissionsGranted: " + allPermissionsGranted
        );
        return allPermissionsGranted;
    }

    /**
     * Check whether every requested permission has been granted
     *直播中定位的权限可以没有，但是必须查验
     * @return true if every requested permission has been granted
     */
    public boolean allPermissionsGrantedNoParam(String... noCheckPermission) {
        boolean allPermissionsGranted = true;
        if (isRuntimePermissionRequiredByApiLevel()) {
            for (String permissionName : mDenyCountMap.keySet()){
                for (String aNoCheckPermission : noCheckPermission) {
                    if (!isPermissionGranted(permissionName) && !permissionName.equals(aNoCheckPermission)) {
                        allPermissionsGranted = false;
                        break;
                    }
                }

            }
        }
        Log.d(TAG, "allPermissionsGranted()"
                + "\t -- allPermissionsGranted: " + allPermissionsGranted
        );
        return allPermissionsGranted;
    }

    /**
     * Check whether permissions have been granted
     *
     * @param permissionName the permission to check
     * @return
     */
    public boolean isPermissionGranted(String permissionName) {
        if (mFragment != null) {
            return isPermissionGranted(permissionName, mFragment);
        } else {
            return isPermissionGranted(permissionName, mActivity);
        }
    }

    private static boolean isPermissionGranted(String permissionName, Activity activity) {
        return ContextCompat.checkSelfPermission(activity, permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean isPermissionGranted(String permissionName, Fragment fragment) {
        return isPermissionGranted(permissionName, fragment.getActivity());
    }

    /**
     * Tell Android to show the user a dialog asking for the specified permission.  We request the
     * permissions without an explanation because the reason for the request should be obvious to the user.
     */
    public void showPermissionsRequest() {
        Set<String> permissionsSet = new TreeSet<>();
        for (String permissionName : mDenyCountMap.keySet()) {
            if (!isPermissionGranted(permissionName)) {
                if (!askedTooManyTimes(mDenyCountMap.get(permissionName))) {
                    permissionsSet.add(permissionName);
                } else {
                    mListener.onShowTooManyTimes();
                    return;
                }
            }
        }
        String[] permissionsList = permissionsSet.toArray(new String[permissionsSet.size()]);
        Log.d(TAG, "showPermissionsRequest()"
                + "\t -- mRequestCode: " + mRequestCode
                + "\n\t -- permissionsList: " + Arrays.toString(permissionsList)
                + "\n\t -- mActivity: " + mActivity
                + "\n\t -- mFragment: " + mFragment
        );
        if (permissionsList.length > 0) {
            if (mFragment != null) {
                mFragment.requestPermissions(permissionsList, mRequestCode);
            } else {
                ActivityCompat.requestPermissions(mActivity, permissionsList, mRequestCode);
            }
        }
    }

    /**
     * Show a {@link Toast} telling the user how to give us the expected permission after denying it
     * TODO: Extract Strings to xml
     */
    private void showSettingsHint() {
        if (mListener != null){
            mListener.onShowSettingsHint();
        } else {
//            Toast.toastLong("Go to app settings to grant requested permission.");
        }
    }

    /**
     * Show a {@link Dialog} to explain why the user should grant access to the requested permission
     *  Show an explanation to the user *asynchronously* -- don't block this thread waiting for the
     *  user's response! After the user sees the explanation, try again to request the permission.
     */
    public void showRationale() {
        if (mListener != null){
            mListener.onShowRationale();
        }
    }

    /**
     * TODO: Extract Strings to xml
     * @param activity
     * @param message
     * @return
     */
    @NonNull
    public void showRationaleDialog(@NonNull Activity activity, String message) {
        Dialog rationaleDialog = new AlertDialog.Builder(activity)
                .setTitle("Sorry to Bug")
                .setMessage(message)
                .setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showPermissionsRequest();
                    }
                })
                .create();
        rationaleDialog.setCanceledOnTouchOutside(true);
        rationaleDialog.show();
        return;
    }

    /**
     * Did we already ask the user for permission too many times?
     * @return
     * @param denyCount
     */
    private boolean askedTooManyTimes(int denyCount) {
        return denyCount >= DENY_COUNT_MAX;
    }

    /**
     * Determines whether runtime permissions are required, based on the version of Android that
     * the user is running.  API 23 (Marshmallow) or higher requires runtime permissions.
     *
     * @return
     */
    private boolean isRuntimePermissionRequiredByApiLevel() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     *
     * @param permissionName
     */
    private void incrementDenyCount(String permissionName) {
        Integer denyCount = mDenyCountMap.get(permissionName) + 1;
        mDenyCountMap.put(permissionName, denyCount);
        Log.v(TAG, "incrementDenyCount()"
                        + "\t -- mDenyCountMap.get(permissionName): " + mDenyCountMap.get(permissionName)
        );
    }

    /**
     * Build a {@link Map} of request codes.  We use an {@link ArrayMap},
     * as recommended by Google for small maps.
     *
     * @return a {@link Map} that maps permission names to request codes
     */
    @NonNull
    private static Map<String, Integer> buildRequestCodeMap() {
        Map<String, Integer> requestCodeMap = new ArrayMap<>();
        requestCodeMap.put(Manifest.permission.GET_ACCOUNTS, PERMISSIONS_REQUEST_GET_ACCOUNTS);
        requestCodeMap.put(Manifest.permission.CAMERA, PERMISSIONS_REQUEST_CAMERA);
        requestCodeMap.put(Manifest.permission.RECORD_AUDIO, PERMISSIONS_REQUEST_RECORD_AUDIO);
        requestCodeMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        return requestCodeMap;
    }


    /**
     * Updates the {@link java.util.Collection}'s that track whether the user has granted or denied
     * the requested permissions and notifies the {@link Listener}.
     * Call this method from {@code onRequestPermissionsResult()} of the {@link Activity} that
     * originated the permissions request.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 0 && grantResults.length == 0){
            return;
        }
        if (requestCode == mRequestCode) {
            for (int i=0; i<grantResults.length;i++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "handleRequestPermissionsResult()"
                                    + "\t -- permissions[" + i + "]" + permissions[i]
                    );
                    mPermissionsGrantedSet.add(permissions[i]);
                } else {
                    Log.w(TAG, "onRequestPermissionsResult()"
                                    + "\t -- Permission denied"
                    );
                    incrementDenyCount(permissions[i]);
                }
            }
            notifyPermissionsGranted(mPermissionsGrantedSet);
        } else {
            Log.w(TAG, "onRequestPermissionsResult()"
                            + "\t -- Unexpected requestCode: " + requestCode
            );
        }
    }

    /**
     * Use this method to notify listeners that the user has granted all the permissions
     * requested in {@link #mDenyCountMap}
     * @param permissionsGranted
     */
    private void notifyPermissionsGranted(Set<String> permissionsGranted) {
        Log.d(TAG, "notifyPermissionsGranted()"
                        +"\n\t -- permissionsGranted: "+permissionsGranted
                        +"\n\t -- mListener: "+mListener
        );
        if (mListener != null){
            mListener.onPermissionsGranted(permissionsGranted);
        }
    }

    /**
     * Use this interface to notify listeners about user responses to permissions requested in {@link #mDenyCountMap}
     */
    public interface Listener {
        /**
         * Called when the user has either granted or denied a runtime permission request.  The
         * implementing {@link Listener}, can then
         * check whether it has the permissions to do what it needs to do.
         *
         * @param permissionsGranted
         */
        void onPermissionsGranted(Set<String> permissionsGranted);

        /**
         * Called when the {@link Listener} needs
         * to show the user a message to explain why we need the requested permission.
         */
        void onShowRationale();

        /**
         * Called when the {@link Listener} needs
         * to help the user fix a permission problem, typically by suggesting they go to the app
         * permissions setting and grant the requested permission.
         */
        void onShowSettingsHint();

        /**
         * 当显示过多次权限申请弹窗时，回调此函数进行处理
         */
        void onShowTooManyTimes();
    }
}
