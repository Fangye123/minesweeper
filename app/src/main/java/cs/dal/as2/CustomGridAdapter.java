package cs.dal.as2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomGridAdapter extends BaseAdapter{

    private Context context;
    private Integer[] items;
    private Boolean[] visible;
    private Boolean[] flag;
    LayoutInflater inflater;

    public CustomGridAdapter(Context context, Integer[]items, Boolean[] visible, Boolean[] flag){
        this.context=context;
        this.items=items;
        this.visible=visible;
        this.flag=flag;
        inflater=(LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null){
            view = inflater.inflate(R.layout.cell,null);
        }
       // TextView tv=(TextView) view.findViewById(R.id.textview);
       // tv.setText(items[i]);
       /* if(!visible[i]){
            tv.setVisibility(View.INVISIBLE);
        }
        else{
            tv.setVisibility(View.VISIBLE);
        }*/

        ImageView im = (ImageView) view.findViewById(R.id.imageview);
        if(flag[i]){
            im.setImageLevel(21);
        }
        else if(visible[i]) {
            switch (items[i]) {
                case 0:im.setImageLevel(0);break;
                case 1:im.setImageLevel(1);break;
                case 2:im.setImageLevel(2);break;
                case 3:im.setImageLevel(3);break;
                case 4:im.setImageLevel(4);break;
                case 5:im.setImageLevel(5);break;
                case 6:im.setImageLevel(6);break;
                case 7:im.setImageLevel(7);break;
                case 8:im.setImageLevel(8);break;
                case 9:im.setImageLevel(9);break;

                case 20:im.setImageLevel(20);break;

                case 30:im.setImageLevel(30);break;
                case 31:im.setImageLevel(31);break;
            }
        }
        else{
            im.setImageLevel(10);
        }

        return view;
    }
}
