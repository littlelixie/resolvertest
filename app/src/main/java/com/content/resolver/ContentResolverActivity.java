package com.content.resolver;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class ContentResolverActivity extends Activity {
    private static final String TAG = "fruitsMainActivity";
    private TextInputEditText editText;
    private Button addButton;
    private ListView listView;
    private FruitAdapter fruitAdapter;
    private ContentResolver resolver = null;
    private static final Uri CONTENT_URI = Uri.parse("content://com.mytest.fruit.provider/fruits");
    private ArrayList<FruitItem> fruitList = new ArrayList<FruitItem>();
    private Handler handler = new Handler(Looper.getMainLooper());

    private ContentObserver contentObserver = new ContentObserver(handler) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "onChange: =======+>>>>>>>>>>>>selfChange is " + selfChange);
            super.onChange(selfChange);

            refreshContent();
        }
    };

    private void refreshContent() {
        refreshFruits();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (TextInputEditText)findViewById(R.id.inputAdd);
        addButton = (Button)findViewById(R.id.button_add);
        listView = (ListView)findViewById(R.id.list_gruips);
        fruitAdapter = new FruitAdapter(this);
        listView.setAdapter(fruitAdapter);

        resolver = this.getContentResolver();

        refreshFruits();
        fruitAdapter.setData(fruitList);

        initOperator();

        resolver.registerContentObserver(CONTENT_URI, true, contentObserver);
    }

    private void refreshFruits() {
        String value = null;
        FruitItem item;

        fruitList.clear();

        Log.i(TAG, " begin query  fruits");
        try {
            Uri uri = CONTENT_URI;
            Cursor mCursor = resolver.query(uri, null, null, null, null);
            if (mCursor != null) {
                while (mCursor.moveToNext()) { //Move the cursor to the next row.
                    value = mCursor.getString(mCursor.getColumnIndex("fruitName"));
                    if(value != null) {
                        item = new FruitItem(value);
                        fruitList.add(item);
                        Log.i(TAG, "fruitName = " + value);
                    }
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, " end query  fruits");

        fruitAdapter.setData(fruitList);
    }

    private boolean isDuplicate(String text) {
        boolean ret = false;
        for(FruitItem item :fruitList) {
            if(item.getFruitName().equals(text)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private void initOperator() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addContent = editText.getText().toString();
                if(!TextUtils.isEmpty(addContent) && !isDuplicate(addContent)) {
                    ContentValues values = new ContentValues();
                    values.put("fruitName", addContent);
                    resolver.insert(CONTENT_URI, values);
                } else {
                    Toast.makeText(ContentResolverActivity.this, "名字重复", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class FruitAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public FruitAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<FruitItem> list) {
            fruitList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(fruitList != null)
                return fruitList.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            if(fruitList != null)
                return fruitList.get(position);
            else
                return null;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BuyViewHolder holder = null;
            if (convertView == null) {
                holder = new BuyViewHolder();
                convertView = mInflater
                        .inflate(R.layout.fruit_list_item, null);
                holder.setFruitTxv((TextView) convertView
                        .findViewById(R.id.item_tv0));

                holder.setBtnDelete((Button) convertView
                        .findViewById(R.id.button_delete));

                convertView.setTag(holder);
            } else {
                holder = (BuyViewHolder) convertView.getTag();
            }
            if(fruitList != null && fruitList.size() > position)
                holder.setData(fruitList.get(position), position);
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    public class BuyViewHolder {
        private TextView fruitTxv;
        private Button btnDelete;

        public Button getBtnDelete() {
            return btnDelete;
        }

        public void setBtnDelete(Button btnDelete) {
            this.btnDelete = btnDelete;
        }

        public TextView getFruitTxv() {
            return fruitTxv;
        }

        public void setFruitTxv(TextView fruitTxv) {
            this.fruitTxv = fruitTxv;
        }


        public void setData(FruitItem model, int postion) {
            if (model == null) {
                return;
            }
            fruitTxv.setText(model.getFruitName());
            btnDelete.setTag(postion);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int)v.getTag();
                    resolver.delete(CONTENT_URI,"fruitName=?", new String[] {fruitList.get(position).getFruitName()});
                }
            });
        }
    }
}
