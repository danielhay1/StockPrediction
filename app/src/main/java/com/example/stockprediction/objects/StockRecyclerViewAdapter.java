package com.example.stockprediction.objects;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockprediction.R;
import com.example.stockprediction.objects.stock.Stock;
import com.example.stockprediction.utils.MyPreference;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.ankurg.expressview.ExpressView;
import co.ankurg.expressview.OnCheckListener;

public class StockRecyclerViewAdapter <T extends Stock> extends RecyclerView.Adapter<StockRecyclerViewAdapter<T>.ViewHolder> implements Filterable {
    private List<T> stocksData;
    private List<T> filteredStockData;
    private List<T> likedStocks;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private OnStockLike_Callback onStockLikeCallback;
    private  JSONObject jsonStockData;

    public interface OnStockLike_Callback {
        void onStockLike(Stock stock);
        void onStockDislike(Stock stock, int position);
    }


    // data is passed into the constructor
    public StockRecyclerViewAdapter (Context context, List<T> filteredStockData, List<T> likedStocks, OnStockLike_Callback onStockLikeCallback) {
        this.mInflater = LayoutInflater.from(context);
        this.filteredStockData = filteredStockData;
        this.context = context;
        this.likedStocks = likedStocks;
        this.onStockLikeCallback = onStockLikeCallback;
        this.stocksData = new ArrayList<T>(filteredStockData);
        jsonStockData = MyPreference.getInstance(context).getStocksData(MyPreference.StockCacheManager.CACHE_KEYS.STOCKS_DATA_JSON);
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
        T stock = filteredStockData.get(position);
        if(jsonStockData != null) {
            setDataFromCache(stock,holder,position);
        } else {
            initStubData(stock,holder);
        }
        Log.d("stock_recycler", "onBindViewHolder: "+ stock.getSymbol());
        setTextViewColor(holder.RVROW_LBL_StockStatusDetails);
        setTextViewColor(holder.RVROW_LBL_StockPredictionDetails);
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

    private void setDataFromCache(T stock, ViewHolder holder, int position){
        JSONObject cacheStock;
        try {
            cacheStock = jsonStockData.getJSONObject("stocks").getJSONObject(stock.getSymbol());
            stock.setChangeAmount(Double.parseDouble(cacheStock.getString("regularMarketChange")));
            stock.setChangePercent(Double.parseDouble(cacheStock.getString("regularMarketChangePercent")));
            holder.RVROW_LBL_StockValue.setText("$" + Double.parseDouble(cacheStock.getString("regularMarketPrice")));
            holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock, holder.RVROW_LBL_StockStatusDetails));
            holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock, holder.RVROW_LBL_StockPredictionDetails));
            setImg(stock.getStockImg(),holder.RVROW_IMG_StockImg);
            setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
            holder.RVROW_LBL_StockName.setText(stock.getName());
            holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());
            if(stock.getPredictionStatus() == Stock.StockStatus.NO_DATA) {
                //holder.
            }

        } catch (JSONException e) {
            Log.e("stock_recycler", "parseQuotesResponse: jsonException = "+e);
            initStubData(stock,holder);
        }
    }

    private void initStubData(T stock, ViewHolder holder) {
        setImg(stock.getStockImg(),holder.RVROW_IMG_StockImg);
        setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
        holder.RVROW_LBL_StockName.setText(stock.getName());
        holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());
        holder.RVROW_LBL_StockValue.setText("$" + String.valueOf(stock.getValue()));
        holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock, holder.RVROW_LBL_StockStatusDetails));
        holder.RVROW_LBL_StockStatusDetails.setText(getStockChangeDetails(stock, holder.RVROW_LBL_StockPredictionDetails));
    }

    @Override
    public Filter getFilter() {
        return stockFilter;
    }

    private Filter stockFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<T> filteredList = new ArrayList<T>();
            if(constraint == null || constraint.length() == 0) {
                filteredList.addAll(stocksData);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (T item: stocksData) {
                    if(isMatchPrefix(item,filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredStockData.clear();
            filteredStockData.addAll((List<T>) results.values);
            notifyDataSetChanged();
        }
    };

    private boolean isMatchPrefix(T stock, String prefix) {
        return stock.getName().toLowerCase().startsWith(prefix) || stock.getSymbol().toLowerCase().startsWith(prefix);
    }

    private void markLikedStocks(T stock, ViewHolder holder) {
        if(likedStocks != null) {
            if(!likedStocks.isEmpty()){
                if (likedStocks.contains(stock)) {
                    Log.d("stock_recycler", "markAsLiked: "+likedStocks);
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
        //String sign = (stock.getChangeAmount() > 0) ? "+" : (stock.getChangeAmount() < 0) ? "-" : "";
        String sign = (stock.getChangeAmount() > 0) ? "+" : "";
        return sign+String.format("%.2f", stock.getChangeAmount()) + "(" + String.format("%.2f", stock.getChangePercent())+ "%)";
    }

    @Override
    public int getItemCount() {
        return filteredStockData.size();
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
            case NO_DATA:
            case UNCHANGED:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_unchanged";
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
        return filteredStockData.get(id);
    }

    public void removeAt(int position) {
        if(position >= 0) {
            notifyItemRemoved(position);
            notifyItemChanged(position);
        } else {
            Log.d("stock_recycler", "removeAt: no element found");
        }
    }

    public void addItem(int position) {
        notifyItemInserted(filteredStockData.size()-1);
        notifyItemChanged(filteredStockData.size()-1);
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
