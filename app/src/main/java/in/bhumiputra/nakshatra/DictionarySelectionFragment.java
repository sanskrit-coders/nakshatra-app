package in.bhumiputra.nakshatra;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by damodarreddy on 5/21/18.
 */

public class DictionarySelectionFragment extends Fragment {


    private IndexerActivity indexerActivity;
    private ListView listView;
    private Button proceedButton;
    private Button skipButton;

    private List<IndexerActivity.DictionaryDetails> details;
    private List<IndexerActivity.DictionaryDetails> selected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_ind2_select_dictionaries, container, false);
        listView = (ListView) v.findViewById(R.id.fri2_dictionary_list);
        proceedButton= (Button) v.findViewById(R.id.fri2_proceed);
        skipButton= (Button) v.findViewById(R.id.fri2_skip);
        indexerActivity= (IndexerActivity) getActivity();
        details= indexerActivity.toBeIndexed;
        selected= indexerActivity.selected;
        return v;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        listView.setAdapter(new DictionaryDetailsAdapter(indexerActivity, details, selected));
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
                skipButton.setEnabled(false);
                indexerActivity.startIndexingSelectedDictionaries();
            }
        });
    }


    private class DictionaryDetailsAdapter extends BaseAdapter {

        private Context tContext;
        private List<IndexerActivity.DictionaryDetails> tDetails;
        private List<IndexerActivity.DictionaryDetails> tSelected;

        public DictionaryDetailsAdapter(Context tContext, List<IndexerActivity.DictionaryDetails> tDetails, List<IndexerActivity.DictionaryDetails> tSelected) {
            this.tContext = tContext;
            this.tDetails = tDetails;
            this.tSelected = tSelected;
            this.tSelected.clear();
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
        public View getView(int position, View convertView, ViewGroup parent) {
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
            holder.checkBox.setChecked(tSelected.contains(detail));
            holder.nameView.setText(detail.name);
            holder.directoryView.setText("in "+ detail.directory+ "/");
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        tSelected.add(detail);
                    }
                    else {
                        tSelected.remove(detail);
                    }
                    proceedButton.setEnabled(tSelected.size()> 0);
                }
            });
            return convertView;
        }
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
}
