package kominfo.go.id.storage.proyek01;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_STORAGE = 100;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Aplikasi Catatan Proyek 1");
        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            //buat intent dan tentukan InsertAndActivity yang akan di running
            Intent intent = new Intent(MainActivity.this, InsertAndViewActivity.class);
            //ambil posisi item yang telah diklik dan objectnya berikan ke data
            Map<String, Object> data = (Map<String, Object>) parent.getAdapter().getItem(position);
            //titipkan data melalui intent dengan key(kunci) "filename"
            intent.putExtra("filename", data.get("name").toString());
            //tampilkan pesan "you clicked " dan data
            Toast.makeText(MainActivity.this, "You clicked " + data.get("name"), Toast.LENGTH_SHORT).show();
               // jalankan activity
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Map<String, Object> data = (Map<String, Object>) parent.getAdapter().getItem(position);
            tampilkanDialogKonfirmasiHapusCatatan(data.get("name").toString());
            return true;
        });

    }

    void tampilkanDialogKonfirmasiHapusCatatan( String filename) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Catatan ini?")
                .setMessage("Apakah Anda yakin ingin menghapus Catatan "+filename+"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", (dialog, whichButton) -> hapusFile())
                .setNegativeButton("No", null)
                .show();
    }

    void hapusFile() {
        //String path = Environment.getExternalStorageDirectory().toString() + "/kominfo.proyek1";
        File parent = new File(getExStorePath());
        if (parent.exists()) {
            parent.delete();
        }
        loadAllFilesFromFolder();
    }
    private void loadAllFilesFromFolder() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "external storage not available", Toast.LENGTH_SHORT).show();
            return;
        }

        File parent = new File(getExStorePath());

        if (parent.exists()) {

            File[] files = parent.listFiles();
            String[] fileNames = new String[files.length];
            String[] dateCreated = new String[files.length];
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss");
            ArrayList<Map<String, Object>> itemList = new ArrayList<>();

            for (int i = 0; i < files.length; i++) {
                fileNames[i] = files[i].getName();
                Date lastModDate = new Date(files[i].lastModified());
                dateCreated[i] = simpleDateFormat.format(lastModDate);
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("name", fileNames[i]);
                itemMap.put("date", dateCreated[i]);
                itemList.add(itemMap);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(this, itemList, android.R.layout.simple_list_item_2,
                    new String[]{"name", "date"}, new int[]{android.R.id.text1, android.R.id.text2});

            listView.setAdapter(simpleAdapter);
            simpleAdapter.notifyDataSetChanged();
        }

    }

    private String getExStorePath() {
        return getExternalFilesDir(null) + "/proyek01.kominfo";
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tambah:
                Intent intent = new Intent(this, InsertAndViewActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            if (periksaIzinPenyimpanan()) {
                loadAllFilesFromFolder();
            }
        } else {
            loadAllFilesFromFolder();
        }
    }

    public boolean periksaIzinPenyimpanan() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                return false;
            }
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadAllFilesFromFolder();
                }
                break;
        }
    }








}
