package com.example.user.my_test_proj_2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final Context context = this;
    private Button Btn_add, Btn_glr, Btn_ph, Btn_ld;
    private DataBase Db;
    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    private WebView mWebView;

    private EditText etName;
    private List<String> Listnames = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private ListView lvMain;
    private ImageView imageView;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle("TestProj");

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        lvMain = (ListView) findViewById(R.id.lvMain);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, Listnames);
        lvMain.setAdapter(adapter);

        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.List);
        tabSpec.setIndicator("List", getResources().getDrawable(R.mipmap.ic_launcher));
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.Scaling);
        tabSpec.setIndicator("Scaling");
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("tag3");
        tabSpec.setContent(R.id.Parsing);
        tabSpec.setIndicator("Parsing");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag4");
        tabSpec.setContent(R.id.Map);
        tabSpec.setIndicator("Map");
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(0);


        Btn_add = (Button) findViewById(R.id.Add);
        Btn_glr = (Button) findViewById(R.id.Gallery);
        Btn_ph = (Button) findViewById(R.id.Photo);
        Btn_ld = (Button) findViewById(R.id.ParsingB);

        Btn_add.setOnClickListener(this);
        Btn_glr.setOnClickListener(this);
        Btn_ph.setOnClickListener(this);
        Btn_ld.setOnClickListener(this);


        imageView = (ImageView) findViewById(R.id.images);
        mWebView = (WebView) findViewById(R.id.webView);
        Db = new DataBase(this);
        setUpMapIfNeeded();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Add:
                LayoutInflater li = LayoutInflater.from(context);
                View textedit = li.inflate(R.layout.edittext, null);
                etName = (EditText) findViewById(R.id.etNme);
                //Создаем AlertDialog
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

                //Настраиваем prompt.xml для нашего AlertDialog:
                mDialogBuilder.setView(textedit);

                //Настраиваем сообщение в диалоговом окне:
                mDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        try {
                                            Listnames.clear();
                                            String name = etName.getText().toString();
                                            SQLiteDatabase database = Db.getWritableDatabase();
                                            //database.delete(Db.TABLE, null, null);
                                            ContentValues contentValues = new ContentValues();
                                            Cursor cursor = database.query(Db.TABLE, null, null, null, null, null, null);
                                            int nameIndex;
                                            contentValues.put(Db.KEY_NAME, name);
                                            database.insert(Db.TABLE, null, contentValues);
                                            if (cursor.moveToFirst()) {
                                                nameIndex = cursor.getColumnIndex(Db.KEY_NAME);
                                                do {
                                                    Listnames.add(cursor.getString(nameIndex));
                                                } while (cursor.moveToNext());
                                            }

                                            lvMain.setAdapter(adapter);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                //Создаем AlertDialog:
                AlertDialog alertDialog = mDialogBuilder.create();
                Db.close();
                alertDialog.show();
                break;

            case R.id.ParsingB:
                // включаем поддержку JavaScript
                mWebView.getSettings().setJavaScriptEnabled(true);
                // указываем страницу загрузки
                mWebView.loadUrl("http://quotes.zennex.ru/api/v3/bash/quotes?sort=time");
                break;
            case R.id.Photo:
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                break;
            case R.id.Gallery:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
                ;
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Bitmap bitmap = null;
        switch (requestCode) {
            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageView.setImageBitmap(bitmap);
                    break;
                }
            case CAMERA_PIC_REQUEST:
                bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                ImageView image = (ImageView) findViewById(R.id.images);
                image.setImageBitmap(bitmap);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        try {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    setUpMap();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling

            return;
        }
            mMap.setMyLocationEnabled(true);
            return;

    }
}