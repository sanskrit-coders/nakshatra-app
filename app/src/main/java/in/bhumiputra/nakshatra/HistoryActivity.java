package in.bhumiputra.nakshatra;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeMap;

import in.bhumiputra.nakshatra.nighantu.anga.LongTuple;

import static in.bhumiputra.nakshatra.PreferancesActivity.handleDisplay;

public class HistoryActivity extends AppCompatActivity {

    ActionBar actionBar;

    LinearLayout orderByBlock;
    RadioGroup orderByOptions;

    LinearLayout bulkRemoveBlock;
    ImageButton removeAllButton;

    Spinner fromSpinner;
    Spinner toSpinner;
    ImageButton removeInRangeButton;

    ExpandableListView listView;
    Cursor history;
    HistoryAdapter adapter;
    HistoryTask task;

    TreeMap<String, LongTuple> indices;
    ArrayList<String> groups;

    HashSet<String> selections;
    SimpleDateFormat dateFormat;

    String orderBy = HistoryTask.time;
    String ascDesc = HistoryTask.descending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleDisplay(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar actionBarToolBar= findViewById(R.id.hst_actionbar);
        setSupportActionBar(actionBarToolBar);
        actionBar= getSupportActionBar();
        initActionBar();

        orderByBlock = (LinearLayout) findViewById(R.id.hst_order_by_box);
        initOrderByBlock();

        bulkRemoveBlock = (LinearLayout) findViewById(R.id.hst_bulk_remove_box);
        initBulkRemoveBlock();

        listView = (ExpandableListView) findViewById(R.id.charitra_jabita);
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        initListView();
    }

    public void initActionBar() {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void initOrderByBlock() {
        if(orderByBlock != null) {
            orderByOptions = (RadioGroup) orderByBlock.findViewById(R.id.hst_order_options);
            if(orderByOptions != null) {
                ((RadioButton) orderByOptions.findViewById(R.id.hst_order_chrono_desc)).toggle();
                orderByOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch(checkedId) {
                            case R.id.hst_order_chrono_desc: {
                                orderBy = HistoryTask.time;
                                ascDesc = HistoryTask.descending;
                                break;
                            }
                            case R.id.hst_order_chrono_asc: {
                                orderBy = HistoryTask.time;
                                ascDesc = HistoryTask.ascending;
                                break;
                            }
                            case R.id.hst_order_ab_desc: {
                                orderBy = HistoryTask.words;
                                ascDesc = HistoryTask.descending;
                                break;
                            }
                            case R.id.hst_order_ab_asc: {
                                orderBy = HistoryTask.words;
                                ascDesc = HistoryTask.ascending;
                                break;
                            }
                        }
                        task = new HistoryTask();
                        task.execute(HistoryTask.show, orderBy, ascDesc);
                        orderByBlock.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    public void initBulkRemoveBlock() {
        if(bulkRemoveBlock != null) {
            removeAllButton = (ImageButton) ((LinearLayout)(bulkRemoveBlock.findViewById(R.id.hst_remove_all_box))).findViewById(R.id.hst_remove_all_button);
            removeAllButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAll();
                    bulkRemoveBlock.setVisibility(View.GONE);
                }
            });

            fromSpinner = (Spinner)(bulkRemoveBlock.findViewById(R.id.hst_range_from_spinner));
            toSpinner = (Spinner) (bulkRemoveBlock.findViewById(R.id.hst_range_to_spinner));
            removeInRangeButton = (ImageButton) (bulkRemoveBlock.findViewById(R.id.hst_remove_in_range_button));
            removeInRangeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView nv= (TextView) fromSpinner.getSelectedView();
                    TextView vv= (TextView) toSpinner.getSelectedView();
                    if((nv!= null) && (vv!= null)) {
                        String ns= nv.getText().toString();
                        String vs= vv.getText().toString();
                        if((ns!= null) && (vs!= null) && (vs.compareToIgnoreCase(ns)>= 0)) {
                            removeInRange(ns, vs);
                            bulkRemoveBlock.setVisibility(View.GONE);
                        }
                        else {
                            Toast.makeText(HistoryActivity.this, "invalid interval.\nPlease select interval again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    public void initListView() {
        adapter = new HistoryAdapter(this, null, null, null);
        listView.setAdapter(adapter);
        if(task != null) {
            task.cancel(true);
        }
        task = new HistoryTask();
        task.execute(HistoryTask.show, orderBy, ascDesc);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(task != null) {
            task.cancel(true);
        }
        task = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.hst_menu_remove: {
                removeSelected();
                return true;
            }
            case R.id.hst_menu_bulk_remove: {
                toggleBulkRemoveBlock();
                return true;
            }
            case R.id.hst_menu_order_by: {
                toggleOrderByBlock();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeSelected() {
        if((selections != null) && (selections.size()> 0)) {
            task = new HistoryTask();
            task.execute(HistoryTask.remove, orderBy, ascDesc);
        }
        else {
            Toast.makeText(this, getString(R.string.no_items_selected), Toast.LENGTH_SHORT).show();
        }
    }

    public void removeAll() {
        task = new HistoryTask();
        task.execute(HistoryTask.remove_all, orderBy, ascDesc);
    }

    public void removeInRange(String ns, String vs) {
        if((ns!= null) && (vs!= null) && (vs.compareToIgnoreCase(ns)>= 0)) {
            long nAvadhi= 0;
            long vAvadhi= 0;
            try {
                nAvadhi = dateFormat.parse(ns).getTime();
                vAvadhi = dateFormat.parse(vs).getTime() + 86400000;
            }
            catch(ParseException pe) {
                //
            }
            task = new HistoryTask();
            task.execute(HistoryTask.remove_in_range, orderBy, ascDesc, ""+ nAvadhi, ""+ vAvadhi);
        }
    }

    public void toggleOrderByBlock() {
        if(orderByBlock != null) {
            if(orderByBlock.getVisibility()== View.GONE) {
                bulkRemoveBlock.setVisibility(View.GONE);
                orderByBlock.setVisibility(View.VISIBLE);
            }
            else if(orderByBlock.getVisibility()== View.VISIBLE) {
                orderByBlock.setVisibility(View.GONE);
            }
        }
    }

    public void toggleBulkRemoveBlock() {
        if(bulkRemoveBlock != null) {
            if(bulkRemoveBlock.getVisibility()== View.GONE) {
                orderByBlock.setVisibility(View.GONE);
                bulkRemoveBlock.setVisibility(View.VISIBLE);
                initRemoveInRangeBlock();
            }
            else if(bulkRemoveBlock.getVisibility()== View.VISIBLE) {
                bulkRemoveBlock.setVisibility(View.GONE);
            }
        }
    }

    public void initRemoveInRangeBlock() {
        if((orderBy.equalsIgnoreCase(HistoryTask.time)) && (groups != null) && (groups.size()> 0)) {
            final ArrayList<String> nJabita= new ArrayList<>(groups);
            Collections.sort(nJabita);

            ArrayAdapter<String> nAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nJabita);
            nAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            fromSpinner.setAdapter(nAdapter);
            fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ArrayList<String> vJabita= new ArrayList<>(nJabita);
                    for(int i= position-1; i>= 0; i--) {
                        vJabita.remove(i);
                    }
                    ArrayAdapter<String> vAdapter= new ArrayAdapter<String>(HistoryActivity.this, android.R.layout.simple_spinner_item, vJabita);
                    vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    toSpinner.setAdapter(vAdapter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    public void restore() { //debug only...
        task = new HistoryTask();
        task.execute(HistoryTask.restore, orderBy, ascDesc);
    }


    class HistoryTask extends AsyncTask<String, Void, Cursor> {

        static final String show = "show";
        static final String remove = "remove";
        static final String remove_all = "anni_dachu";
        static final String remove_in_range = "remove_in_range";
        static final String restore = "restore"; //debug only...


        static final String words = "padam";
        static final String time = "time";

        static final String ascending = "asc";
        static final String descending = "desc";

        @Override
        protected Cursor doInBackground(String... args) {
            Helper.initBHHelper(HistoryActivity.this.getApplicationContext());
            SQLiteDatabase db= Helper.bhDbHelper.getWritableDatabase();
            String charya= args[0];

            if(charya.equalsIgnoreCase(show)) {
                String mida = args[1];
                String kramam = args[2];

                history = db.rawQuery("SELECT id as _id, padam, time FROM history WHERE exist='true' ORDER BY "+ mida+ " "+ kramam+ ";", null);
                return history;
            }

            else if(charya.equalsIgnoreCase(remove)) {
                String by= args[1];
                String krama= args[2];

                if((selections != null) && (selections.size()> 0)) {
                    StringBuilder condition= new StringBuilder("");
                    for(String selection: selections) {
                        condition.append(" padam='"+ selection.replaceAll("'", "''")+ "' or");
                    }
                    condition.delete(condition.length()-3, condition.length());

                    db.execSQL("DELETE FROM history WHERE"+ condition.toString()+ ";");

                    history = db.rawQuery("SELECT id as _id, padam, time FROM history WHERE exist='true' ORDER BY "+ by+ " "+ krama+ ";", null);
                    return history;
                }

            }

            else if(charya.equalsIgnoreCase(remove_all)) {
                String by= args[1];
                String krama= args[2];

                db.execSQL("DELETE FROM history;");

                history = db.rawQuery("SELECT id as _id, padam, time FROM history WHERE exist='true' ORDER BY "+ by+ " "+ krama+ ";", null);
                return history;
            }

            else if(charya.equalsIgnoreCase(remove_in_range)) {
                String by= args[1];
                String krama= args[2];
                long nAvadhi= Long.valueOf(args[3]);
                long vAvadhi= Long.valueOf(args[4]);
                db.execSQL("DELETE FROM history WHERE ((time>="+ nAvadhi+ ") AND (time<"+ vAvadhi+ "));");

                history = db.rawQuery("SELECT id as _id, padam, time FROM history WHERE exist='true' ORDER BY "+ by+ " "+ krama+ ";", null);
                return history;
            }

            else if(charya.equalsIgnoreCase(restore)) { //debug only...
                String by= args[1];
                String krama= args[2];

                db.execSQL("UPDATE history SET exist='true' WHERE padam IS NOT NULL;");

                history = db.rawQuery("SELECT id as _id, padam, time FROM history WHERE exist='true' ORDER BY "+ by+ " "+ krama+ ";", null);
                return history;
            }

            return null;
        }

        protected void onPostExecute(Cursor cursor) {
            if((cursor!= null) && (listView != null)) {
                if(adapter == null) {
                    adapter = new HistoryAdapter(HistoryActivity.this, null, null, null);
                    listView.setAdapter(adapter);
                }
                HistoryActivity.this.computeHistory();
                adapter.swap(cursor, groups, indices);
                for(int i = groups.size()-1; i>= 0; i--) {
                    listView.expandGroup(i, true);
                }
            }
            task = null;
        }

    }

    public void computeHistory() {
        if(history == null) {
            groups = null;
            indices = null;
            return;
        }
        groups = new ArrayList<>();
        indices = new TreeMap<>();
        int lu= history.getCount();

        if((orderBy == null) || orderBy.equalsIgnoreCase(HistoryTask.time)) {
            String prevDate = "";
            int start = 0;
            int len = 0;
            for (int i = 0; i < lu; i++) {
                history.moveToPosition(i);
                long samayam = history.getLong(2);
                String date = dateFormat.format(new Date(samayam));
                if (!(date.equalsIgnoreCase(prevDate))) {
                    if (!(prevDate.equalsIgnoreCase(""))) {
                        groups.add(prevDate);
                        indices.put(prevDate, new LongTuple(start, len));
                    }
                    prevDate = date;
                    len = 1;
                    start = i;
                } else {
                    len++;
                }
            }
            if (!(prevDate.equalsIgnoreCase(""))) {
                groups.add(prevDate);
                indices.put(prevDate, new LongTuple(start, len));
            }
        }

        else if(orderBy.equalsIgnoreCase(HistoryTask.words)) {
            String prevLang= "";
            int start = 0;
            int len = 0;
            for(int i= 0; i< lu; i++) {
                history.moveToPosition(i);
                String padam= history.getString(1);
                //String bhasha= (Bhasha.bhasha(padam).equalsIgnoreCase(Bhasha.DB)) ? getString(R.string.desha) : getString(R.string.anglam);
                String bhasha= "All Words"; //TODO!!!! should add more languages!
                if(!(bhasha.equalsIgnoreCase(prevLang))) {
                    if(!(prevLang.equalsIgnoreCase(""))) {
                        groups.add(prevLang);
                        indices.put(prevLang, new LongTuple(start, len));
                    }
                    prevLang= bhasha;
                    len= 1;
                    start= i;
                }
                else {
                    len++;
                }
            }
            if(!(prevLang.equalsIgnoreCase(""))) {
                groups.add(prevLang);
                indices.put(prevLang, new LongTuple(start, len));
            }
        }
        selections = new HashSet<>();
    }


    public class HistoryAdapter extends BaseExpandableListAdapter {

        TreeMap<String, LongTuple> _indices;
        ArrayList<String> _groups;
        Cursor _cursor;
        Context _context;

        public HistoryAdapter(Context _context, Cursor _cursor, ArrayList<String> _groups, TreeMap<String, LongTuple> _indices) {
            super();
            this._context= _context;
            this._cursor= _cursor;
            this._groups = _groups;
            this._indices = _indices;
        }

        @Override
        public int getGroupCount() {
            return (_groups == null) ? 0 : _groups.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            int sankhya= ((_indices == null)||(_groups == null)) ? 0 : (int) _indices.get(_groups.get(groupPosition)).second;
            return sankhya;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return (_groups == null) ? null : groups.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return new LongTuple(groupPosition, childPosition);
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
            if((_groups == null) || (_indices == null) || (_cursor== null)) {
                return null;
            }
            String group= _groups.get(groupPosition);
            View row= convertView;
            if (row== null) {
                LayoutInflater inflater= (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row= inflater.inflate(R.layout.group_amsham_history, null);
            }

            Holder holder= (Holder) row.getTag();

            if(holder== null) {
                holder= new Holder(row);
                row.setTag(holder);
            }

            holder.group.setText((orderBy.equalsIgnoreCase(HistoryTask.time) ? "On ": "")+ group);

            return row;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if((_groups == null) || (_indices == null) || (_cursor== null)) {
                return null;
            }

            int va= (int) (_indices.get(_groups.get(groupPosition)).first+ childPosition);
            _cursor.moveToPosition(va);
            String padam= _cursor.getString(1);

            View innerRow= convertView;
            if(innerRow== null) {
                LayoutInflater inflater= (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //loVarusa= inflater.inflate(android.R.layout.simple_list_item_multiple_choice, null);
                innerRow= inflater.inflate((isLastChild ? R.layout.amsham_history : R.layout.amsham_history), null);
            }
            InHolder inHolder= (InHolder) innerRow.getTag();

            if(inHolder== null) {
                inHolder= new InHolder(innerRow);
                innerRow.setTag(inHolder);
            }

            inHolder.padamView.setText(padam);
            inHolder.padamView.setOnClickListener(padamClickListener);

            boolean isChecked= selections.contains(padam);
            //inHolder.selectionView.setChecked(isChecked);
            inHolder.selectionView.setOnCheckedChangeListener(selectionChangeListener);
            inHolder.selectionView.setChecked(isChecked);

            return innerRow;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean swap(Cursor _cursor, ArrayList<String> _vargamulu, TreeMap<String, LongTuple> _sankhyalu) {
            this._cursor= _cursor;
            this._groups = _vargamulu;
            this._indices = _sankhyalu;
            this.notifyDataSetChanged();
            return true;
        }

        public View.OnClickListener padamClickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryActivity.this.onPadamClick(v);
            }
        };

        public CompoundButton.OnCheckedChangeListener selectionChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String padam= null;
                TextView padamView= (TextView) ((InHolder)(((LinearLayout)buttonView.getParent()).getTag())).padamView;
                if(padamView!= null) {
                    padam= padamView.getText().toString();
                    HistoryActivity.this.onSelectionChange(padam, isChecked, buttonView);
                }
            }
        };

    }

    public void onPadamClick(View v) {
        TextView padamView= null;
        try {
            padamView = (TextView) v;
        }
        catch (ClassCastException cce) {
            Log.e("History", "unable to perform onClick on padamView.", cce);
        }
        if(padamView!= null) {
            String padam= padamView.getText().toString();
            if(padam!= null) {
                Intent intent= new Intent(Intent.ACTION_SEARCH, Uri.parse(NighantuProvider.AKARADISTR+ "/nighantu/"+ padam),this, NakshatraActivity.class);
                startActivity(intent);
                this.finish();
            }
        }
    }

    public void onSelectionChange(String padam, boolean isChecked, CompoundButton cb) {
        if(isChecked) {
            selections.add(padam);
        }
        else  {
            selections.remove(padam);
        }
    }





    public static class Holder {
        TextView group;

        Holder(View varusa) {
            this.group = (TextView) varusa.findViewById(R.id.vargamu);
        }
    }

    public static class InHolder {
        TextView padamView;
        CheckBox selectionView;
        LinearLayout block;

        InHolder(View varusa) {
            this.block = varusa.findViewById(R.id.charitra_amsham);
            this.padamView= this.block.findViewById(R.id.charitra_amsham_padam);
            this.selectionView = this.block.findViewById(R.id.charitra_amsham_empika);
        }
    }

}
