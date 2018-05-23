package in.bhumiputra.nakshatra;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by damodarreddy on 3/7/18.
 */

public class ShareDialog extends DialogFragment {

    public Dialog onCreateDialog (Bundle savedInstanceState) {

        String[] options= new String[] {getString(R.string.share_word), getString(R.string.share_all),
        getString(R.string.copy_word), getString(R.string.copy_all)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final NakshatraActivity nakshatra= (NakshatraActivity) getActivity();

        builder.setItems(options, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface samvadam, int edi) {
               switch(edi) {
                   case 0: {
                       ShareDialog.this.dismiss();
                       String padam= nakshatra.descrView.getTitle();
                       nakshatra.share(padam);
                       break;
                   }
                   case 1: {
                       ShareDialog.this.dismiss();
                       nakshatra.descrView.evaluateJavascript("mottamEnchukonu('patra', 2);", null);
                       break;
                   }
                   case 2: {
                       ShareDialog.this.dismiss();
                       String padam= nakshatra.descrView.getTitle();
                       nakshatra.copy(padam);
                       break;
                   }
                   case 3: {
                       ShareDialog.this.dismiss();
                       nakshatra.descrView.evaluateJavascript("mottamEnchukonu('patra', 1);", null);
                       break;
                   }
               }
           }
       });

        return builder.create();
    }
}
