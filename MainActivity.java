package com.example.admin.janitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity {


    static String value[] = new String[10];
    static int cubicals, basins, count = 1;
    String entity = new String("");
    List<String> list=new ArrayList<>();
    HashMap<String, List<String>> contents=new HashMap<>();
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> contentlist[]=new ArrayList[10];
    String branch=new String("");
    Button reviewbtn;
   static int usagestatus[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reviewbtn = findViewById(R.id.review_btn);
        expandableListView = findViewById(R.id.listview1);
        Database.init();
        expandableListAdapter = new ExpandableListAdapter(this, list, contents);
        expandableListView.setAdapter(expandableListAdapter);
        branch = getSharedPreferences(getPackageName() + "branchfile", Context.MODE_PRIVATE).getString("branchfile", "null");


        reviewbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, ReviewsActivity.class);
                startActivity(intent);

            }
        });
        Database.db.collection(branch).document("layout").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    cubicals = Integer.parseInt(documentSnapshot.get("cubicals").toString());
                    basins = Integer.parseInt(documentSnapshot.get("basins").toString());

                    entity = "Cubicle";
                    count = 1;
                    usagestatus = new int[cubicals + 1];

                    for (int i = 1; i <= cubicals + basins; i++) {
                        String str = entity + count;

                        list.add(str);
                        ++count;
                        if (i == cubicals) {
                            entity = "Basin";
                            count = 1;
                        }
                    }

                    list.add("Waterlevel");
                    expandableListAdapter.notifyDataSetChanged();


                }

                    proceed();


                }

            }
        );

    }

public void proceed() {

    Database.db.collection(branch).document("status").addSnapshotListener(new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {


            if (documentSnapshot.exists()) {
                for (int i = 1; i <= cubicals; i++) {
                        usagestatus[i] = Integer.parseInt((documentSnapshot.get("sensor" + i)).toString());
                 }

                expandableListAdapter.notifyDataSetChanged();

            }

        }
    });

        Database.db.collection(branch + "_waterlevel").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        List<String> waterlevellist = new ArrayList<>();
                        QueryDocumentSnapshot dcSnap = dc.getDocument();

                        if (dcSnap.get("value") != null) {
                            double value= Double.parseDouble(dcSnap.get("value").toString());
                            DecimalFormat df = new DecimalFormat("0.00");

                            waterlevellist.add("Waterlevel: " +df.format(value).toString()+" litres");
                            contents.put("Waterlevel", waterlevellist);
                        } else {
                            waterlevellist.add("Waterlevel: -");
                            contents.put("Waterlevel", waterlevellist);

                        }

                        expandableListAdapter.notifyDataSetChanged();

                    }
                }
            });


            Database.db.collection(branch + "_waterflow").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        QueryDocumentSnapshot dcSnap = dc.getDocument();

                        entity = "Cubicle";
                        count = 1;
                        for (int i = 1; i <= (basins + cubicals); i++) {

                            contentlist[i] = new ArrayList<>();
                            if (dcSnap.get("sensor" + i) != null) {
                                contentlist[i].add("Waterflow: " + dcSnap.get("sensor" + i).toString()+" litres");
                            } else {
                                contentlist[i].add("Waterflow: -");
                            }

                            ++count;
                            if (i == cubicals) {
                                entity = "Basin";
                                count = 1;
                            }

                            contents.put(list.get(i-1), contentlist[i]);

                        }


                        expandableListAdapter.notifyDataSetChanged();
                    }
                }

            });

        }


}

class ExpandableListAdapter extends BaseExpandableListAdapter
{

    android.content.Context context;
    List<String> listDataHeader;
    HashMap<String, List<String>> listHashMap;

    public ExpandableListAdapter(android.content.Context context, List<String> listDataHeader, HashMap<String, List<String>> listHashMap) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listHashMap.get(listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHashMap.get(listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null)
        {
            LayoutInflater inflator = (LayoutInflater)context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            convertView= inflator.inflate(R.layout.viewlayout, null);
        }
        TextView txtview = convertView.findViewById( R.id.keytext);
        txtview.setText(((String)getGroup(groupPosition)).toString());



        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null)
        {
            LayoutInflater inflator = (LayoutInflater)context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            convertView= inflator.inflate(R.layout.subitem, null);
        }
        TextView txtview = convertView.findViewById( R.id.subitemview);
        txtview.setText((String)getChild(groupPosition,childPosition));


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}