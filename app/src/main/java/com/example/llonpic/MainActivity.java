package com.example.llonpic;

import static android.content.ContentValues.TAG;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.print.PrintHelper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

   private  ImageView Iv_camera;
    private   Button Btn_camera;
    private  Bitmap bitmap;

    private  double longitude , latitude;

    FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                1);


        Btn_camera = findViewById(R.id.btn_camera);
        Iv_camera = findViewById(R.id.iv_image);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenCamera();
            }
        });
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                       Location location = task.getResult();
                       if(location != null)
                       {
                           Geocoder geocoder = new Geocoder(MainActivity.this
                           ,Locale.getDefault());
                           try {
                               List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                              Log.e(TAG," addresses "+addresses);
                               longitude = addresses.get(0).getLongitude();
                               latitude = addresses.get(0).getLatitude();
                           } catch (IOException e) {
                               throw new RuntimeException(e);
                           }
                       }
                }
            });
        }
    }

    private void OpenCamera() {
       Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       if(i.resolveActivity(getPackageManager())!= null)
       {
           getLocation();
           startActivityForResult(i,100);
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 ) {
            if (data != null) {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    Bitmap editimage = TextOnImage(bitmap);
                    Iv_camera.setImageBitmap(editimage);
                    SaveImage(editimage);

            }
            else{
                Log.e(TAG,"data is : "+data );
            }
        }
        else{
            Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap TextOnImage( Bitmap bitmap)
    {
        try{
            Date currentTime = Calendar.getInstance().getTime();
        String mCurrentTime = "time "+String.valueOf(currentTime) ;
        String mlatitude = "latitude "+String.valueOf(longitude );
        String mlongitude = "longitude "+String.valueOf(latitude);
        float scale =  this.getResources().getDisplayMetrics().density;

        //here we are creating the bitmap that take value from the given bitmap;
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();

        //here we are checking the given bitmap if null or not if yes then we will create our own bitmap type
        if(bitmapConfig == null)
        {
            //here we are creating Bitmap type which will use in Bitmap maipulation method;
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        //because Bitmap are immutable that's why we need to make bitmap mutable so we use copy method in 1st parameter we pass
        //the type bitmap we want  after manipulation and in 2nd we pass true or false it is kind of permission that allow the
        //bitmap can be manipulated or not .
         bitmap.copy(bitmapConfig,true);

        //Only Canva and Paint class comes in use when we need to manipulate the bitmap
        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255,255, 255));
        // text size in pixels
        paint.setTextSize((int) (2.5 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(mlongitude, 0, mlongitude.length(), bounds);
        canvas.drawText(mlongitude ,3,250 , paint);

        paint.getTextBounds(mlatitude, 0, mlatitude.length(), bounds);
        canvas.drawText(mlatitude ,3,240 , paint);

        paint.getTextBounds(mCurrentTime, 0, mCurrentTime.length(), bounds);
        canvas.drawText(mCurrentTime ,3,220 , paint);
        return bitmap;
    } catch (Exception e)
    {
        Log.e(TAG,"Error in Editing image: "+ e);
        return null;
    }
   }

   private void SaveImage(Bitmap bitmap)
   {
       FileOutputStream outputStream = null;
       File file = Environment.getExternalStorageDirectory();
       File dir = new File(file.getAbsolutePath() + "/LLOnPic/");
       dir.mkdir();

       String filename = String.format("%d.png",System.currentTimeMillis());
       File outFile =  new File(dir , filename);
       try{
           outputStream = new FileOutputStream(outFile);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
       bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
       try {
           outputStream.flush();
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
       try{
           outputStream.close();
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
       MediaScannerConnection.scanFile(this, new String[]{outFile.getPath()}, new String[]{"image/jpeg"}, null);
   }
}
