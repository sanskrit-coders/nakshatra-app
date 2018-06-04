package in.bhumiputra.nakshatra;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by damodarreddy on 5/21/18.
 */

public class DictionarySelectionFragment extends Fragment {


    private IndexerActivity indexerActivity;
    private TextView textView;
    private ListView listView;
    private LinearLayout actionsBlock;
    private Button proceedButton;
    private Button skipButton;

    private LinearLayout selectAllBlock;
    private CheckBox selectAllCB;
    private TextView selectedCountTV;

    private List<IndexerActivity.DictionaryDetails> details;
    private List<IndexerActivity.DictionaryDetails> selected;
    public List<String> statuses;

    private IndexingProgressAdapter ipAdapter;
    private DictionaryDetailsAdapter ddAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_ind2_select_dictionaries, container, false);
        textView= (TextView) v.findViewById(R.id.fri2_text1);
        listView = (ListView) v.findViewById(R.id.fri2_dictionary_list);
        actionsBlock= (LinearLayout) v.findViewById(R.id.fri2_actions_block);
        proceedButton= (Button) v.findViewById(R.id.fri2_proceed);
        skipButton= (Button) v.findViewById(R.id.fri2_skip);
        indexerActivity= (IndexerActivity) getActivity();
        details= indexerActivity.toBeIndexed;
        selected= indexerActivity.selected;

        selectAllBlock= (LinearLayout) v.findViewById(R.id.fri2_select_all_block);
        selectAllCB= (CheckBox) selectAllBlock.findViewById(R.id.fri2_select_all_cb);
        selectedCountTV= (TextView) selectAllBlock.findViewById(R.id.fri2_selected_count);
        return v;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        selected.clear();
        ddAdapter= new DictionaryDetailsAdapter(indexerActivity, details, selected);
        listView.setAdapter(ddAdapter);

        selectAllCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    selected.clear();
                    selected.addAll(details);
                    ddAdapter.notifyDataSetChanged();
                }
                else {
                    if(selected.size()== details.size()) {
                        selected.clear();
                        ddAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        selectedCountTV.setText(selected.size()+ "/"+ details.size());

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indexerActivity.finish();
                indexerActivity.overridePendingTransition(0, 0);
            }
        });
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DictionarySelectionFragment.this.onProceed();
            }
        });
    }

    private void onProceed() {
        actionsBlock.setVisibility(View.GONE);
        selectAllBlock.setVisibility(View.GONE);
        statuses= new ArrayList<>();
        for(int i= 0; i< selected.size(); i++) {
            statuses.add("Indexing...");
        }
        ((DictionaryDetailsAdapter)listView.getAdapter()).notifyDataSetInvalidated();
        ipAdapter= new IndexingProgressAdapter(indexerActivity, selected, statuses);
        textView.setText(R.string.indexing);
        listView.setAdapter(ipAdapter);
        ipAdapter.notifyDataSetChanged();
        indexerActivity.startIndexingSelectedDictionaries(this);
    }

    public void refreshStatuses() {
        if(ipAdapter!= null) {
            ipAdapter.notifyDataSetChanged();
        }
    }

    public void refreshSelections() {
        if(ddAdapter!= null) {
            ddAdapter.notifyDataSetChanged();
        }
    }

    private void onSelectionChange(IndexerActivity.DictionaryDetails detail, boolean isChecked) {
        if(isChecked) {
            if(!selected.contains(detail)) { //to filter duplicate entries, as it is not set
                selected.add(detail);
            }
        }
        else {
            selected.remove(detail);
        }
        proceedButton.setEnabled(!selected.isEmpty());

        if(selectAllCB.isChecked()) {
            if(selected.size()!= details.size()) {
                selectAllCB.setChecked(false);
            }
        }

        selectedCountTV.setText(selected.size()+ "/"+ details.size());
    }

    private class DictionaryDetailsAdapter extends BaseAdapter {

        private Context tContext;
        private List<IndexerActivity.DictionaryDetails> tDetails;
        private List<IndexerActivity.DictionaryDetails> tSelected;
        private String tag= "dda";

        public DictionaryDetailsAdapter(Context tContext, List<IndexerActivity.DictionaryDetails> tDetails, List<IndexerActivity.DictionaryDetails> tSelected) {
            this.tContext = tContext;
            this.tDetails = tDetails;
            this.tSelected = tSelected;
        }

        @Override
        public int getCount() {
            return tDetails.size();
        }

        @Override
        public Object getItem(int position) {
            return tDetails.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final IndexerActivity.DictionaryDetails detail= tDetails.get(position);
            if(convertView== null) {
                LayoutInflater inflater= (LayoutInflater) this.tContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView= inflater.inflate(R.layout.ind2_dictionary_item, null);
            }
            Ind2ItemHolder holder= (Ind2ItemHolder) convertView.getTag();
            if(holder== null) {
                holder= new Ind2ItemHolder(convertView);
                convertView.setTag(holder);
            }
            holder.nameView.setText(detail.name);
            holder.directoryView.setText("in "+ detail.directory+ "/");
            holder.checkBox.setTag(detail);
            holder.checkBox.setOnCheckedChangeListener(selectionChangeListener);
            holder.checkBox.setChecked(tSelected.contains(detail));
            return convertView;
        }

        CompoundButton.OnCheckedChangeListener selectionChangeListener= new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IndexerActivity.DictionaryDetails detail= (IndexerActivity.DictionaryDetails) buttonView.getTag();
                DictionarySelectionFragment.this.onSelectionChange(detail, isChecked);
            }
        };
    }

    private class Ind2ItemHolder {
        public final CheckBox checkBox;
        public final TextView nameView;
        public final TextView directoryView;

        public Ind2ItemHolder(View v) {
            checkBox= (CheckBox) v.findViewById(R.id.ind2_checkbox);
            nameView= (TextView) v.findViewById(R.id.ind2_dictionary_name);
            directoryView= (TextView) v.findViewById(R.id.ind2_dictionary_directory);
        }
    }




    private class IndexingProgressAdapter extends BaseAdapter {

        private Context tContext;
        private List<IndexerActivity.DictionaryDetails> tSelected;
        private List<String> tStatuses;

        public IndexingProgressAdapter(Context tContext, List<IndexerActivity.DictionaryDetails> tSelected, List<String> tStatuses) {
            this.tContext= tContext;
            this.tSelected= tSelected;
            this.tStatuses= tStatuses;
        }

        @Override
        public int getCount() {
            return tSelected.size();
        }

        @Override
        public Object getItem(int position) {
            return tStatuses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IndexerActivity.DictionaryDetails detail= tSelected.get(position);
            String status= tStatuses.get(position);
            if(convertView== null) {
                LayoutInflater inflater= (LayoutInflater) this.tContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView= inflater.inflate(R.layout.ind3_dictionary_item, null);
            }
            Ind3ItemHolder holder= (Ind3ItemHolder) convertView.getTag();
            if(holder== null) {
                holder= new Ind3ItemHolder(convertView);
                convertView.setTag(holder);
            }
            holder.nameView.setText(detail.name);
            holder.statusView.setText(status);
            if(status.equalsIgnoreCase("Indexing...")) {
                holder.statusView.setTextColor(indexerActivity.getResources().getColor(R.color.red));
            }
            else {
                holder.statusView.setTextColor(indexerActivity.getResources().getColor(R.color.blue));
            }
            return convertView;
        }
    }

    private class Ind3ItemHolder {
        public TextView nameView;
        public TextView statusView;

        public Ind3ItemHolder(View v) {
            nameView= (TextView) v.findViewById(R.id.ind3_dictionary_name);
            statusView= (TextView) v.findViewById(R.id.ind3_indexing_status);
        }
    }
}
