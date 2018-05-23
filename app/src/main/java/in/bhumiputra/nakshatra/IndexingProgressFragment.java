package in.bhumiputra.nakshatra;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by damodarreddy on 5/21/18.
 */

public class IndexingProgressFragment extends Fragment {

    private IndexerActivity indexerActivity;
    private List<IndexerActivity.DictionaryDetails> selected;
    private List<String> statuses;

    private ListView listView;
    private IndexingProgressAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_ind3_indexing, container, false);
        listView= (ListView) view.findViewById(R.id.fri3_dictionary_list);
        indexerActivity= (IndexerActivity) getActivity();
        selected= indexerActivity.selected;
        statuses= indexerActivity.indexStatuses;

        return view;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        adapter= new IndexingProgressAdapter(indexerActivity, selected, statuses);
        listView.setAdapter(adapter);
        //indexerActivity.onIndexingProgressFragmentViewCreated(this);
    }

    public void refresh() {
        if(adapter!= null) {
            adapter.notifyDataSetChanged();
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
