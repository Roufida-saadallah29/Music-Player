package dz.esi.tdm.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class favorisList extends AppCompatActivity {
    private ListView listView;
    private ListViewAdapter adapter;
    private List<favorisSongClass> favorisList;
    private ArrayList<String> favorisListPath;
    private ArrayList<favorisSongClass> favorisListPathBD;
    private MaBaseManager mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favoris);
        favorisListPathBD = new ArrayList<>();
        // Create a list of Favoris objects
        mDatabase = new MaBaseManager(this);
        Cursor cursor = mDatabase.getAllFav();
        if (cursor.moveToFirst()) {
            do {
                String sonnng=cursor.getString(1);
                int lastSlashIndex =sonnng.lastIndexOf('/');
                String extractedString = sonnng.substring(lastSlashIndex + 1, sonnng.length() - 4);
                favorisListPathBD.add(new favorisSongClass(extractedString,cursor.getInt(0)));
            } while (cursor.moveToNext());
        }

        // Add more items as needed

        // Initialize the ListView and adapter
        listView = findViewById(R.id.listView);
        adapter = new ListViewAdapter(this, favorisListPathBD);

        // Set the adapter on the ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the clicked item
                favorisSongClass clickedItem = adapter.getItem(position);
                // Launch the new activity or perform the desired action
                Intent intent = new Intent(dz.esi.tdm.myapplication.favorisList.this, favoriteItemMain.class);
                intent.putExtra("position", position);
                intent.putExtra("id", clickedItem.getId());
                intent.putStringArrayListExtra("musicFileFav",favorisListPath);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                favorisSongClass clickedItem = adapter.getItem(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(dz.esi.tdm.myapplication.favorisList.this);
                builder.setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Perform the deletion action here
                               favorisListPathBD.remove(position);
                               adapter.notifyDataSetChanged();
                               mDatabase.deleteFav(clickedItem.getId());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true; // Return true to consume the long click event
            }
        });
    }

    public void onstopClick(View view) {
        Intent intent = new Intent("STOP_MUSIC_SERVICE");
        sendBroadcast(intent);
    }
    @Override
    public void onDestroy ()
    {
        super.onDestroy();
        mDatabase.close();
    }
}

