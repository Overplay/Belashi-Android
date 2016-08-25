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

    private List<OGBoxObject> mOGBoxObjects; //Make a list of OGBoxObjects

    private Context mContext; //Get our context

    private View.OnClickListener mOnClickListener ; //Get our onclicklistener

    /**
     * Make a new OGObjectAdapter
     * @param context The context of the adapter
     * @param ogBoxObjects The list of OGBoxObjects we need to adapt to
     * @param onClickListener The onclick listener we want it to run
     */
    public OGObjectAdapter(Context context, List<OGBoxObject> ogBoxObjects, View.OnClickListener onClickListener){

        this.mOGBoxObjects = ogBoxObjects;
        this.mContext = context;
        this.mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext(); //use our parent context
        LayoutInflater inflater = LayoutInflater.from(context); //get the inflater from context

        View ogBoxView = inflater.inflate(R.layout.ogbox_layout, parent, false); //Use the inflater and inflate

        ogBoxView.setOnClickListener(mOnClickListener); //set the onclick to the one we made
        return new ViewHolder(ogBoxView); //return the viewholder
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        OGBoxObject ogBox = mOGBoxObjects.get(position); //Get the position in the recycler view that corrisponds to the ogbox

        TextView textView = holder.ogTextView; //get the textview
        textView.setText(ogBox.getName()); //set text to the ogbox name

        holder.itemView.setTag(ogBox); //set the itemview tag to the box

    }

    @Override
    public int getItemCount() {
        return mOGBoxObjects.size(); //get the size of the ogbox list
    }

    private Context getContext() {
        return mContext;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ogImageView;
        public TextView ogTextView;
        public OGBoxObject ogBoxObject;

        public ViewHolder(View itemView){

            super(itemView);

            ogImageView = (ImageView) itemView.findViewById(R.id.ogLogo); //set the itemView image
            ogTextView = (TextView) itemView.findViewById(R.id.ogName); //set the itemView text


        }


    }


}
