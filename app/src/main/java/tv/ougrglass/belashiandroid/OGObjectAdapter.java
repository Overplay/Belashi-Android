package tv.ougrglass.belashiandroid;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * BelashiAndroid Created by logansaso on 8/2/16.
 */
public class OGObjectAdapter extends RecyclerView.Adapter<OGObjectAdapter.ViewHolder> {

    private List<OGObject> mOGObjects;

    private Context mContext;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View ogBoxView = inflater.inflate(R.layout.ogbox_layout, parent, false);

        return new ViewHolder(ogBoxView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        OGObject ogBox = mOGObjects.get(position);

        TextView textView = holder.ogTextView;
        textView.setText(ogBox.getName());

        ImageView imageView = holder.ogImageView;
    }

    @Override
    public int getItemCount() {
        return mOGObjects.size();
    }


    public OGObjectAdapter(Context mContext, List<OGObject> mOGObjects){
        this.mOGObjects = mOGObjects;
        this.mContext = mContext;
    }

    private Context getContext() {
        return mContext;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ogImageView;
        public TextView ogTextView;

        public ViewHolder(View itemView){

            super(itemView);

                ogImageView = (ImageView) itemView.findViewById(R.id.ogLogo);
                ogTextView = (TextView) itemView.findViewById(R.id.ogName);

        }

    }
}
