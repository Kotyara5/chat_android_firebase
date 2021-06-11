package com.bc.chatbc;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterUser extends BaseAdapter {
    Context ctx;
    LayoutInflater lInflater;
    ArrayList<User> objects;

    AdapterUser(Context context, ArrayList<User> Users) {
        ctx = context;
        objects = Users;
        lInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.user_list_item, parent, false);
        }

        User model = ((User) getItem(position));

        // заполняем View в пункте списка
        TextView name_user, email_user;
        name_user = view.findViewById(R.id.textNameUserFriend);
        email_user = view.findViewById(R.id.textEmailUserFriends);
        name_user.setText(model.getName());
        email_user.setText(model.getEmail());

        return view;
    }
}
