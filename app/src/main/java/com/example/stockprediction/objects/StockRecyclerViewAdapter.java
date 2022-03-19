package com.example.stockprediction.objects;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import co.ankurg.expressview.ExpressView;
import co.ankurg.expressview.OnCheckListener;

public class StockRecyclerViewAdapter <T extends Stock> extends RecyclerView.Adapter<StockRecyclerViewAdapter<T>.ViewHolder> {
    private List<T> stocksData;
    private List<T> likedStocks;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private OnStockLike_Callback onStockLikeCallback;

    public interface OnStockLike_Callback {
        void onStockLike(Stock stock);
        void onStockDislike(Stock stock, int position);
    }


    // data is passed into the constructor
    public StockRecyclerViewAdapter (Context context, List<T> stocksData, List<T> likedStocks, OnStockLike_Callback onStockLikeCallback) {
        this.mInflater = LayoutInflater.from(context);
        this.stocksData = stocksData;
        this.context = context;
        this.likedStocks = likedStocks;
        this.onStockLikeCallback = onStockLikeCallback;
    }

    public List<T> getLikedStocks() {
        return likedStocks;
    }

    public StockRecyclerViewAdapter setLikedStocks(List<T> likedStocks) {
        this.likedStocks = likedStocks;
        return this;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycle_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String ValSign = "";
        T stock = stocksData.get(position);
        setImg(stock.getStockImg(),holder.RVROW_IMG_StockImg);
        setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
        holder.RVROW_LBL_StockName.setText(stock.getName());
        holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());
        holder.RVROW_LBL_StockValue.setText("$" + String.valueOf(stock.getValue()));
        holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock, holder.RVROW_LBL_StockStatusDetails));
        holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock, holder.RVROW_LBL_StockPredictionDetails));
        setTextViewColor(holder.RVROW_LBL_StockStatusDetails);
        setTextViewColor(holder.RVROW_LBL_StockPredictionDetails);
        Log.d("pttt", "onBindViewHolder: "+ stock.getSymbol());
        markLikedStocks(stock,holder); // TODO: fix null pointer exception
        holder.RVROW_EV_likeButton.setOnCheckListener(new OnCheckListener() {
            @Override
            public void onChecked(@Nullable ExpressView expressView) {
                onStockLikeCallback.onStockLike(stock);
            }

            @Override
            public void onUnChecked(@Nullable ExpressView expressView) {
                onStockLikeCallback.onStockDislike(stock,position);
            }
        });
    }

    private void markLikedStocks(T stock, ViewHolder holder) {
        if(likedStocks != null) {
            if(!likedStocks.isEmpty()){
                if (likedStocks.contains(stock)) {
                    Log.d("pttt", "markAsLiked: "+likedStocks);
                    holder.RVROW_EV_likeButton.setChecked(true);
                }
            }
        }
    }

    private void setTextViewColor (TextView textView) {
        char sign =  textView.getText().charAt(0);
        if (sign == '-') {
            textView.setTextColor(context.getColor(R.color.red_200));
        }   else if (sign == '+') {
            textView.setTextColor(context.getColor(R.color.green_200));
        }
    }
    private String getStockChangeDetails(T stock,TextView textView){
        String sign = (stock.getChangeAmount() > 0) ? "+" : (stock.getChangeAmount() < 0) ? "-" : "";
        return sign+stock.getChangeAmount() + "(" + stock.getChangePercent()+ ")";
    }

    @Override
    public int getItemCount() {
        return stocksData.size();
    }

    private void setImg(String imgName,ImageView img) {
        if(!imgName.equalsIgnoreCase("")) {
            Context context = mInflater.getContext();
            int resourceId = context.getResources().getIdentifier(imgName, "drawable",context.getPackageName());//initialize res and context in adapter's contructor
            img.setImageResource(resourceId);
        }
    }

    private void setStockStatusImg(ImageView img, T.StockStatus status, String type) {
        String imgName = "";
        switch (status) {
            case INCREASE:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_increase";
                }
                break;
            case DECREASE:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_decrease";
                }
                break;
            case UNCHANGED:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_unchanged";
                }
                break;
            case NO_DATA:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="";
                }
                break;
        }
        setImg(imgName,img);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        private ImageView RVROW_IMG_StockImg;
        private ImageView RVROW_IMG_currentStatus;
        private ImageView RVROW_IMG_predictionStatus;
        private TextView RVROW_LBL_StockName;
        private TextView RVROW_LBL_StockSymbol;
        private TextView RVROW_LBL_StockValue;
        private TextView RVROW_LBL_StockStatusDetails;
        private TextView RVROW_LBL_StockPredictionDetails;
        private ExpressView RVROW_EV_likeButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            RVROW_IMG_StockImg = itemView.findViewById(R.id.RVROW_IMG_StockImg);
            RVROW_IMG_predictionStatus = itemView.findViewById(R.id.RVROW_IMG_predictionStatus);
            RVROW_LBL_StockName = itemView.findViewById(R.id.RVROW_LBL_StockName);
            RVROW_LBL_StockSymbol = itemView.findViewById(R.id.RVROW_LBL_StockSymbol);
            RVROW_LBL_StockValue = itemView.findViewById(R.id.RVROW_LBL_StockValue);
            RVROW_LBL_StockStatusDetails = itemView.findViewById(R.id.RVROW_LBL_StockStatusDetails);
            RVROW_LBL_StockPredictionDetails = itemView.findViewById(R.id.RVROW_LBL_StockPredictionDetails);
            RVROW_EV_likeButton = itemView.findViewById(R.id.RVROW_EV_likeButton);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getBindingAdapterPosition());
        }
    }
    // convenience method for getting data at click position
    public T getItem(int id) {
        return stocksData.get(id);
    }

    public void removeAt(int position) {
        if(position >= 0) {
            notifyItemRemoved(position);
            notifyItemChanged(position);
        } else {
            Log.d("pttt", "removeAt: no element found");
        }
    }

    public void addItem(int position) {
        notifyItemInserted(stocksData.size()-1);
        notifyItemChanged(stocksData.size()-1);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
