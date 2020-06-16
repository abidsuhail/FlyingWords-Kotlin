package com.dragontelnet.mychatapp.datasource.remote.firebase.modules;

import android.content.Context;

public class MyStaticLastLocation {

    public void setLocationToDb(Context context) {
        //MutableLiveData<LatLng> locationMutableLiveData = new MutableLiveData<>();
/*        if (CurrentUser.getCurrentUser() != null) {
            final GeoFire geoFire = new GeoFire(MyFirestoreDbRefs.getAllLocsRef());
            FusedLocationProviderClient fusedLocationClient = LocationServices
                    .getFusedLocationProviderClient(context);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {

                            //pushing location in firestore
                            GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                            geoFire.setLocation(CurrentUser.getCurrentUser().getUid(), geoLocation);
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            //locationMutableLiveData.setValue(latLng);
                        }

                    });
        }*/
    }
}
