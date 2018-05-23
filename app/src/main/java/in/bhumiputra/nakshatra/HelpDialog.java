package in.bhumiputra.nakshatra;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by damodarreddy on 3/3/18.
 */

public class HelpDialog extends DialogFragment {

    TextView helpView;
    Button closeButton;
    Button nextButton;

    int nth = 0;
    int upperLimit = 6;
    int minimum = 3;

    int[] helpDescriptions = new int[] {R.string.help0, R.string.help1, R.string.help2, R.string.help3,
    R.string.help4, R.string.help5, R.string.help6};

    int[] helpTitles = new int[] {R.string.help_title0, R.string.help_title1, R.string.help_title2, R.string.help_title3,
    R.string.help_title4, R.string.help_title5, R.string.help_title6};


    public Dialog onCreateDialog (Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view= inflater.inflate(R.layout.fragment_help_dialog, null);
        helpView = (TextView) view.findViewById(R.id.fhd_help_content);
        closeButton = (Button) view.findViewById(R.id.ffhd_close);
        nextButton = (Button) view.findViewById(R.id.fhd_next);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog.this.dismiss();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HelpDialog.this.next();
            }
        });

        helpView.setText(getString(helpDescriptions[0]));
        nth = 0;

        builder.setView(view).setTitle(helpTitles[0]);

        return builder.create();
    }

    void next() {
        nth++;
        if(nth > upperLimit) {
            this.dismiss();
            return;
        }
        if(nth == upperLimit) {
            nextButton.setVisibility(View.GONE);
            closeButton.setVisibility(View.VISIBLE);
        }
        if(nth > minimum) {
            closeButton.setVisibility(View.VISIBLE);
        }
        else {
            closeButton.setVisibility(View.GONE);
        }

        String sahayam= getString(helpDescriptions[nth]);
        String shirshika= getString(helpTitles[nth]);

        helpView.setText(sahayam);
        getDialog().setTitle(shirshika);
    }
}
