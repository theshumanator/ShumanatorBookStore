package com.example.fatoumeh.shumanatorbookstore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.fatoumeh.shumanatorbookstore.data.InventoryContract.InventoryDB;

/**
 * Created by fatoumeh on 06/03/2018.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parentViewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parentViewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvProductName=view.findViewById(R.id.product_name);
        String productName=cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_PRODUCT_NAME));
        tvProductName.setText(productName);

        TextView tvPrice=view.findViewById(R.id.product_price);
        String price=cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_PRICE));
        tvPrice.setText(price);

        TextView tvQuantity=view.findViewById(R.id.product_quantity);
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB.COLUMN_QUANTITY));
        int quantityInt = Integer.parseInt(quantity);
        tvQuantity.setText(quantity);

        //if the quantity remaining is less than the min, display the item in red
        LinearLayout llEachItem = view.findViewById(R.id.each_list_item);
        int minQty=Integer.valueOf(context.getString(R.string.min_qty));
        if (quantityInt<minQty) {
            llEachItem.setBackgroundColor(context.getResources().getColor(R.color.colorBreach));
        } else {
            //we have to reset the colour in case quantity increases >=5
            llEachItem.setBackgroundColor(context.getResources().getColor(R.color.colorWhite));
        }

        //set the id of the product to the sale and manage button tags
        String productId = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDB._ID));
        Button manageButton = view.findViewById(R.id.manage_button);
        Button saleButton=view.findViewById(R.id.sale_button);
        manageButton.setTag(productId);
        saleButton.setTag(productId);
    }
}
