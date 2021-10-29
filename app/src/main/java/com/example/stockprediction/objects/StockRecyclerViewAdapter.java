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

import java.util.List;
public class StockRecyclerViewAdapter extends RecyclerView.Adapter<StockRecyclerViewAdapter.ViewHolder> {
    private List<Stock> stocksData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public StockRecyclerViewAdapter(Context context, List<Stock> stocksData) {
        this.mInflater = LayoutInflater.from(context);
        this.stocksData = stocksData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stocksData.get(position);
        setImg(stock.getStockImg(),holder.RVROW_IMG_StockImg);
        setStockStatusImg(holder.RVROW_IMG_currentStatus,stock.getCurrentStatus(),"current_status");
        setStockStatusImg(holder.RVROW_IMG_predictionStatus,stock.getPredictionStatus(),"prediction_status");
        holder.RVROW_LBL_StockName.setText(stock.getName());
        holder.RVROW_LBL_StockSymbol.setText(stock.getSymbol());
        holder.RVROW_LBL_StockValue.setText(String.valueOf(stock.getValue()));
        holder.RVROW_LBL_StockStatusDetails.setText(stock.getStockStatusDetails());
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

    private void setStockStatusImg(ImageView img, Stock.StockStatus status, String type) {
        String imgName = "";
        switch (status) {
            case INCREASE:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_increase";
                }
                else if(type.equalsIgnoreCase("current_status")) {
                    imgName="current_status_increase";
                }
                break;
            case DECREASE:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_decrease";
                }
                else if(type.equalsIgnoreCase("current_status")) {
                    imgName="current_status_decrease";
                }
                break;
            case UNCHANGED:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="prediction_status_unchanged";
                }
                else if(type.equalsIgnoreCase("current_status")) {
                    imgName="current_status_unchanged";
                }
                break;
            case NO_DATA:
                if(type.equalsIgnoreCase("prediction_status")) {
                    imgName="";
                }
                else if(type.equalsIgnoreCase("current_status")) {
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            RVROW_IMG_StockImg = itemView.findViewById(R.id.RVROW_IMG_StockImg);
            RVROW_IMG_currentStatus = itemView.findViewById(R.id.RVROW_IMG_currentStatus);
            RVROW_IMG_predictionStatus = itemView.findViewById(R.id.RVROW_IMG_predictionStatus);
            RVROW_LBL_StockName = itemView.findViewById(R.id.RVROW_LBL_StockName);
            RVROW_LBL_StockSymbol = itemView.findViewById(R.id.RVROW_LBL_StockSymbol);
            RVROW_LBL_StockValue = itemView.findViewById(R.id.RVROW_LBL_StockValue);
            RVROW_LBL_StockStatusDetails = itemView.findViewById(R.id.RVROW_LBL_StockStatusDetails);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getBindingAdapterPosition());
        }
    }
    // convenience method for getting data at click position
    Stock getItem(int id) {
        return stocksData.get(id);
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
