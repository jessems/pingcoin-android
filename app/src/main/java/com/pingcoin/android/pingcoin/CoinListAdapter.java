package com.pingcoin.android.pingcoin;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class CoinListAdapter extends RecyclerView.Adapter<CoinListAdapter.CoinViewHolder> {

    class InstructionViewHolder extends RecyclerView.ViewHolder {
        private final TextView instructionTextView;

        private InstructionViewHolder(View itemView) {
            // TODO: Understand what this super does?
            super(itemView);
            instructionTextView = itemView.findViewById(R.id.instruction_text);
        }
    }

    class CoinViewHolder extends RecyclerView.ViewHolder {
        private final TextView coinItemNameView;
        private final TextView coinItemCountryView;
        private final TextView coinItemClassWeightView;
        private final ImageView coinItemIcon;
        private final TextView instructionTextView;
        public final RelativeLayout coinItemRowLayout;



        private CoinViewHolder(View itemView) {
            super(itemView);
            coinItemNameView = itemView.findViewById(R.id.coin_name);
            coinItemCountryView = itemView.findViewById(R.id.coin_country);
            coinItemClassWeightView = itemView.findViewById(R.id.coin_class_weight);
            coinItemIcon = itemView.findViewById(R.id.verdict_icon);
            coinItemRowLayout = itemView.findViewById(R.id.coin_row);

            instructionTextView = itemView.findViewById(R.id.instruction_text);

        }



    }

    private final LayoutInflater mInflater;
    private List<Coin> mCoins; // Cached copy of coins

    CoinListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public CoinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View itemView = mInflater.inflate(R.layout.item_instruction, parent, false);
            return new CoinViewHolder(itemView);
        } else {
            View itemView = mInflater.inflate(R.layout.item_coin, parent, false);
            return new CoinViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(CoinViewHolder holder, final int position) {

        if (mCoins != null) {
            Resources res = holder.itemView.getContext().getResources();
            Coin current = mCoins.get(position);
            String currentMaterialClass = current.getMaterialClass();
            holder.coinItemNameView.setText(current.getPopularName());
            holder.coinItemCountryView.setText(current.getCountry());
            holder.coinItemClassWeightView.setText(current.getMaterialClass() + " " + "(" + Float.toString(current.getWeightInOz()) + " oz" + ")");

            // Set the icon resource name to the name prefixed with "ic_"
            String currentCoinIconStringName = "ic_" + current.getId() + "_listview";
//            String currentCoinIconStringName = "ic_1oz_silver_canadian_maple_leaf_listview";

            // check if the resource exists
            try{
                Integer currentCoinId = res.getIdentifier(currentCoinIconStringName, "drawable", holder.itemView.getContext().getPackageName());

                Log.d("WHAT", "currentcoinicon: " + currentCoinIconStringName + " id: " + currentCoinId);
                if (currentCoinId != 0) {
                    holder.coinItemIcon.setImageResource(currentCoinId);
                } else {
                    // Change icon to respective material class color e.g. gold-colored, silver-colored
                    if (currentMaterialClass.equals("Gold")) {
                        Log.d("test", "Did we reach here Gold?");
                        holder.coinItemIcon.setImageDrawable(res.getDrawable(R.drawable.ic_coin_gold));
                    } else if (currentMaterialClass.equals("Silver")) {
                        holder.coinItemIcon.setImageDrawable(res.getDrawable(R.drawable.ic_coin_silver));
                    }
                }

//                holder.coinItemIcon.setImageResource(R.drawable.ic_1oz_gold_american_eagle_listview);

            } catch(Exception e) {
                Log.d("EEP","Could not find the icon resource for the coin " + current.getPopularName());

            }



            // if it exists assign it to the current icon
            // if it doesn't exist fall back on default depending on the metal

//            holder.coinItemIcon.setImageResource(res.getIdentifier(currentCoinIcon, "drawable", null));





            holder.coinItemRowLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO how do I prevent the need to call Coin current = ... twice??
                    Coin current = mCoins.get(position);
                    Context context = v.getContext();

                    Intent intent = new Intent(context, TestCoin.class);
                    intent.putExtra("PopularName", current.getPopularName());
                    intent.putExtra("id", current.getId());
                    intent.putExtra("WeightInOz", current.getWeightInOz());
                    intent.putExtra("MaterialClass", current.getMaterialClass());
                    intent.putExtra("C0D2", current.getC0D2());
                    intent.putExtra("C0D3", current.getC0D3());
                    intent.putExtra("C0D4", current.getC0D4());
                    intent.putExtra("C0D2Error", current.getC0D2Error());
                    intent.putExtra("C0D3Error", current.getC0D3Error());
                    intent.putExtra("C0D4Error", current.getC0D4Error());

                    context.startActivity(intent);
                }
            });

        } else {
            // Covers the case of data not being ready yet.
            holder.coinItemNameView.setText("No Coin");
        }
    }

    void setCoins(List<Coin> coins){
        mCoins = coins;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mCoins has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mCoins != null)
            return mCoins.size();
        else return 0;
    }

//    @Override
//    public int getItemViewType(int position) {
//        if (position == 0) return 1;
//        else return 2;
//    }
}
