package com.example.ahkazzaz.foursquare;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

/**
 * Created by ahkaz on 3/25/2018.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.RecipeViewHolder> {
    //private final ClickListener listener;
    private  int mNumberOfItems;
    private Context context;
    String []name,address,images;
    LinkedList<String> idList = new LinkedList<String>();
    TextView nameTv,addressTv;
    ImageView imageIcon;
    public  class RecipeViewHolder extends RecyclerView.ViewHolder   {

       // private WeakReference<ClickListener> listenerRef;



        public RecipeViewHolder(View itemVIew){
            super(itemVIew);

            nameTv=(TextView)itemVIew.findViewById(R.id.locationName);
            //detail=(TextView)itemVIew.findViewById(R.id.detailtv);
            addressTv=(TextView)itemVIew.findViewById(R.id.locationAddres);
           imageIcon=(ImageView)itemVIew.findViewById(R.id.locationIcon);
           // apply=(Button)itemVIew.findViewById(R.id.applyjob);
         //   add=(Button)itemVIew.findViewById(R.id.addquestion);
          //  title.setOnClickListener(this);

            //apply.setOnClickListener(this);
           // add.setOnClickListener(this);


        }


            public void bind(int listIndex)
        {
          //  image.setLayoutParams(new GridView.LayoutParams(250, 350));
            nameTv.setText(name[listIndex]);
        //    detail.setText(data[listIndex]);
            addressTv.setText(address[listIndex]);
            Picasso.with(context)
                    .load(images[listIndex])
                    .into(imageIcon);
            imageIcon.setScaleType(ImageView.ScaleType.FIT_XY);
           // imageIcon.setColorFilter(R.color.colorAccent);
           // imageIcon.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            //Glide.with(context).load(images[listIndex]).into(imageIcon);

            //   skills.setText(data[listIndex][3]);

        }



    }
    public PlacesAdapter(Context c, String [] name,String [] address,String [] images)
    {context=c;
       // this.listener = listener;
        mNumberOfItems=name.length;
        this.name=name;
        this.address=address;
        this.images=images;



    }


    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context  =parent.getContext();
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.recycler_item,parent,false);
        RecipeViewHolder viewHolder =new RecipeViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {

        holder.bind(position);


    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }
}
