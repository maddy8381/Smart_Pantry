package com.vps.smartpantry.hotel;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vps.smartpantry.Config;
import com.vps.smartpantry.R;
import com.vps.smartpantry.hotel.support.Item;
import com.vps.smartpantry.hotel.support.ItemAdapterForOrderedItems;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderedItemsFragment extends Fragment
{
    private RecyclerView ordred_items_rv;
    private List<Item> items;
    private ItemAdapterForOrderedItems itemAdapterForOrderedItems;
    private RequestQueue volleyRequestQueue;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.ordered_items_fragment, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        ordred_items_rv=(RecyclerView)view.findViewById(R.id.orders_rv);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        ordred_items_rv.setLayoutManager(mLayoutManager);
        ordred_items_rv.setItemAnimator(new DefaultItemAnimator());
        items=new ArrayList<Item>();
        volleyRequestQueue = Volley.newRequestQueue(getContext());
    }

    public void getData()
    {
        final ProgressDialog mDialog = new ProgressDialog(getContext());
        mDialog.setCancelable(false);
        mDialog.setMessage("Getting orders...");
        mDialog.show();
        items.clear();
        itemAdapterForOrderedItems =new ItemAdapterForOrderedItems(items);
        ordred_items_rv.setAdapter(itemAdapterForOrderedItems);
        String url = "http://" + Config.SERVER_ADDRESS + "/android/hotel_ordered_item.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                if(!response.equals("null"))
                {
                    try
                    {
                        mDialog.dismiss();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            Item item = new Item(jsonObject.getString("item_name"), Integer.parseInt(jsonObject.getString("quantity")),jsonObject.getString("seller_name"),Integer.parseInt(jsonObject.getString("status_accepted")));
                            items.add(item);
                        }
                    }
                    catch (Exception e)
                    {
                        mDialog.dismiss();
                        Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                    itemAdapterForOrderedItems.notifyDataSetChanged();
                }
                else
                {
                    Toast.makeText(getContext(),"No items Ordered yet.", Toast.LENGTH_LONG).show();
                    mDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mDialog.dismiss();
                Toast.makeText(getContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", Config.ID);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        volleyRequestQueue.add(request);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getData();
    }
}
